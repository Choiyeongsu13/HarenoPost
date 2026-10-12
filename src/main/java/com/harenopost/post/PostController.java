package com.harenopost.post;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.harenopost.card.CardService;
import com.harenopost.card.dto.EnvelopeDto;
import com.harenopost.common.BusinessException;
import com.harenopost.common.SecurityUtil;
import com.harenopost.member.CustomUserDetails;
import com.harenopost.member.Member;
import com.harenopost.member.MemberService;
import com.harenopost.post.dto.PostForm;
import com.harenopost.post.dto.PostViewDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PostController {

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	private final PostService postService;
	private final MemberService memberService;
	private final CardService cardService;

	// ---------- 만들기 ----------

	@GetMapping("/post/new")
	public String createForm(@AuthenticationPrincipal CustomUserDetails user, Model model) {
		Member member = memberService.get(user.getMemberIdx());
		PostForm form = new PostForm();
		form.setTitle(member.getNickname() + "のポスト");
		model.addAttribute("form", form);
		return createPage(member, model);
	}

	@PostMapping("/post/new")
	public String create(@AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute("form") PostForm form,
			BindingResult br, Model model) {
		Member member = memberService.get(user.getMemberIdx());
		if (!br.hasErrors()) {
			try {
				Post post = postService.create(member, form);
				return "redirect:/post/" + post.getCode() + "/created";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		return createPage(member, model);
	}

	@GetMapping("/post/{code}/created")
	public String created(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails user, Model model) {
		Post post = postService.getOwned(code, user.getMemberIdx());
		model.addAttribute("post", post);
		model.addAttribute("shareUrl", shareUrl(code));
		return "post/created";
	}

	// ---------- 포스트 화면 (누구나) ----------

	@GetMapping("/post/{code}")
	public String view(@PathVariable("code") String code, Model model) {
		Post post = postService.getByCode(code);
		// 카드 본문은 모델에 넣지 않는다 (봉투 = 보낸 이름 + 디자인)
		List<EnvelopeDto> envelopes = cardService.getEnvelopes(post.getPostIdx());
		model.addAttribute("post", PostViewDto.of(post, SecurityUtil.currentMemberIdx()));
		model.addAttribute("envelopes", envelopes);
		// 화면 JS 용 (Thymeleaf 직렬화가 record 를 확실히 다루도록 Map 으로)
		model.addAttribute("envelopeJs", envelopes.stream().map(e -> {
			Map<String, Object> m = new HashMap<>();
			m.put("senderName", e.senderName());
			m.put("imageUrl", e.imageUrl());
			return m;
		}).toList());
		model.addAttribute("shareUrl", shareUrl(code));
		return "post/view";
	}

	// ---------- 꾸미기 (주인, 개봉 전) ----------

	@GetMapping("/post/{code}/decorate")
	public String decorate(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails user, Model model) {
		Post post = postService.getOwnedBeforeOpen(code, user.getMemberIdx());
		model.addAttribute("post", PostViewDto.of(post, user.getMemberIdx()));
		return "post/decorate";
	}

	// ---------- 설정 · 삭제 (주인, 개봉 전) ----------

	@GetMapping("/post/{code}/edit")
	public String editForm(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails user, Model model) {
		Post post = postService.getOwnedBeforeOpen(code, user.getMemberIdx());
		PostForm form = new PostForm();
		form.setTheme(post.getTheme());
		form.setTitle(post.getTitle());
		form.setOpenDate(post.getOpenDate().toLocalDate());
		form.setOpenTime(post.getOpenDate().toLocalTime());
		model.addAttribute("form", form);
		return editPage(post, model);
	}

	@PostMapping("/post/{code}/edit")
	public String edit(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails user,
			@Valid @ModelAttribute("form") PostForm form, BindingResult br, Model model, RedirectAttributes ra) {
		Post post = postService.getOwnedBeforeOpen(code, user.getMemberIdx());
		if (!br.hasErrors()) {
			try {
				postService.edit(code, user.getMemberIdx(), form);
				ra.addFlashAttribute("toast", "保存しました");
				return "redirect:/post/" + code;
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		return editPage(post, model);
	}

	@PostMapping("/post/{code}/delete")
	public String delete(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
		postService.delete(code, user.getMemberIdx());
		ra.addFlashAttribute("toast", "ポストを削除しました");
		return "redirect:/mypage";
	}

	// ---------- 내부 ----------

	private String createPage(Member member, Model model) {
		model.addAttribute("nickname", member.getNickname());
		model.addAttribute("themeDefaults", themeDefaults(member));
		return "post/create";
	}

	private String editPage(Post post, Model model) {
		Member member = memberService.get(post.getMemberIdx());
		model.addAttribute("post", post);
		model.addAttribute("themeDefaults", themeDefaults(member));
		return "post/edit";
	}

	/** 테마 키 → {date, time} 개봉일 기본값 (화면 JS 용). 誕生日 은 생일이 없으면 빈 값 */
	private Map<String, Map<String, String>> themeDefaults(Member member) {
		Map<String, Map<String, String>> map = new LinkedHashMap<>();
		for (Theme theme : Theme.values()) {
			LocalDateTime d = postService.defaultOpenDate(theme, member.getBirthday());
			map.put(theme.getKey(), Map.of(
					"date", d == null ? "" : d.format(DATE),
					"time", d == null ? "00:00" : d.format(TIME)));
		}
		return map;
	}

	private String shareUrl(String code) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path("/post/{code}").buildAndExpand(code).toUriString();
	}
}
