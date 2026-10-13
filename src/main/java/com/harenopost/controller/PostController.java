package com.harenopost.controller;

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

import com.harenopost.dto.CustomUserDetails;
import com.harenopost.dto.EnvelopeDto;
import com.harenopost.dto.PostForm;
import com.harenopost.dto.PostViewDto;
import com.harenopost.entity.User;
import com.harenopost.entity.Post;
import com.harenopost.entity.Theme;
import com.harenopost.exception.BusinessException;
import com.harenopost.service.CardService;
import com.harenopost.service.UserService;
import com.harenopost.service.PostService;
import com.harenopost.util.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PostController {

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	private final PostService postService;
	private final UserService userService;
	private final CardService cardService;

	// ---------- 만들기 ----------

	@GetMapping("/post/new")
	public String createForm(@AuthenticationPrincipal CustomUserDetails login, Model model) {
		User user = userService.get(login.getUserIdx());
		PostForm form = new PostForm();
		form.setTitle(user.getNickname() + "のポスト");
		model.addAttribute("form", form);
		return createPage(user, model);
	}

	@PostMapping("/post/new")
	public String create(@AuthenticationPrincipal CustomUserDetails login, @Valid @ModelAttribute("form") PostForm form,
			BindingResult br, Model model) {
		User user = userService.get(login.getUserIdx());
		if (!br.hasErrors()) {
			try {
				Post post = postService.create(user, form);
				return "redirect:/post/" + post.getCode() + "/created";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		return createPage(user, model);
	}

	@GetMapping("/post/{code}/created")
	public String created(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails login, Model model) {
		Post post = postService.getOwned(code, login.getUserIdx());
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
		model.addAttribute("post", PostViewDto.of(post, SecurityUtil.currentUserIdx()));
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
	public String decorate(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails login, Model model) {
		Post post = postService.getOwnedBeforeOpen(code, login.getUserIdx());
		model.addAttribute("post", PostViewDto.of(post, login.getUserIdx()));
		return "post/decorate";
	}

	// ---------- 설정 · 삭제 (주인, 개봉 전) ----------

	@GetMapping("/post/{code}/edit")
	public String editForm(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails login, Model model) {
		Post post = postService.getOwnedBeforeOpen(code, login.getUserIdx());
		PostForm form = new PostForm();
		form.setTheme(post.getTheme());
		form.setTitle(post.getTitle());
		form.setOpenDate(post.getOpenDate().toLocalDate());
		form.setOpenTime(post.getOpenDate().toLocalTime());
		model.addAttribute("form", form);
		return editPage(post, model);
	}

	@PostMapping("/post/{code}/edit")
	public String edit(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails login,
			@Valid @ModelAttribute("form") PostForm form, BindingResult br, Model model, RedirectAttributes ra) {
		Post post = postService.getOwnedBeforeOpen(code, login.getUserIdx());
		if (!br.hasErrors()) {
			try {
				postService.edit(code, login.getUserIdx(), form);
				ra.addFlashAttribute("toast", "保存しました");
				return "redirect:/post/" + code;
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		return editPage(post, model);
	}

	@PostMapping("/post/{code}/delete")
	public String delete(@PathVariable("code") String code, @AuthenticationPrincipal CustomUserDetails login, RedirectAttributes ra) {
		postService.delete(code, login.getUserIdx());
		ra.addFlashAttribute("toast", "ポストを削除しました");
		return "redirect:/mypage";
	}

	// ---------- 내부 ----------

	private String createPage(User user, Model model) {
		model.addAttribute("nickname", user.getNickname());
		model.addAttribute("themeDefaults", themeDefaults(user));
		return "post/create";
	}

	private String editPage(Post post, Model model) {
		User user = userService.get(post.getUserIdx());
		model.addAttribute("post", post);
		model.addAttribute("themeDefaults", themeDefaults(user));
		return "post/edit";
	}

	/** 테마 키 → {date, time} 개봉일 기본값 (화면 JS 용). 誕生日 은 생일이 없으면 빈 값 */
	private Map<String, Map<String, String>> themeDefaults(User user) {
		Map<String, Map<String, String>> map = new LinkedHashMap<>();
		for (Theme theme : Theme.values()) {
			LocalDateTime d = postService.defaultOpenDate(theme, user.getBirthday());
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
