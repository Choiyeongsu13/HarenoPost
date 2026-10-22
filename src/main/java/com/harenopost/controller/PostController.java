package com.harenopost.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.harenopost.dto.CustomUserDetails;
import com.harenopost.dto.EnvelopeResponseDTO;
import com.harenopost.dto.PostRequestDTO;
import com.harenopost.dto.PostResponseDTO;
import com.harenopost.dto.Theme;
import com.harenopost.dto.UserResponseDTO;
import com.harenopost.exception.BusinessException;
import com.harenopost.service.CardService;
import com.harenopost.service.DesignService;
import com.harenopost.service.PostService;
import com.harenopost.service.UserService;
import com.harenopost.util.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("Post")
public class PostController {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(PostController.class);

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

	private final PostService postService;
	private final UserService userService;
	private final CardService cardService;
	private final DesignService designService;

	//등록 폼
	@GetMapping("post_write")
	public String postWrite(@AuthenticationPrincipal CustomUserDetails login, Model model) {
		log.info("Post Call : post_write");
		UserResponseDTO user = userService.userView(login.getUserIdx());
		PostRequestDTO postRequestDTO = new PostRequestDTO();
		postRequestDTO.setTitle(user.getNickname() + "のポスト");
		model.addAttribute("postRequestDTO", postRequestDTO);
		model.addAttribute("nickname", user.getNickname());
		model.addAttribute("themeDefaults", themeDefaults(user));
		return "Post/post_write";
	}

	//등록 처리
	@PostMapping("post_write")
	public String postWritePro(@AuthenticationPrincipal CustomUserDetails login, @Valid PostRequestDTO postRequestDTO,
			BindingResult result, Model model) {
		log.info("Post Call : post_write_pro");
		UserResponseDTO user = userService.userView(login.getUserIdx());
		if (!result.hasErrors()) {
			try {
				String code = postService.postWrite(user, postRequestDTO);
				return "redirect:/Post/post_created?code=" + code;
			} catch (BusinessException e) {
				result.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		model.addAttribute("nickname", user.getNickname());
		model.addAttribute("themeDefaults", themeDefaults(user));
		return "Post/post_write";
	}

	//완료 화면 (공유 링크)
	@GetMapping("post_created")
	public String postCreated(@RequestParam("code") String code, @AuthenticationPrincipal CustomUserDetails login, Model model) {
		log.info("Post Call : post_created");
		model.addAttribute("post", postService.postOwnerView(code, login.getUserIdx()));
		model.addAttribute("shareUrl", shareUrl(code));
		return "Post/post_created";
	}

	//포스트 화면 (누구나). 카드 본문은 모델에 넣지 않는다 (봉투 = 보낸 이름 + 디자인)
	@GetMapping("post_view")
	public String postView(@RequestParam("code") String code, Model model) {
		log.info("Post Call : post_view");
		Long loginIdx = SecurityUtil.currentUserIdx();
		PostResponseDTO post = postService.postView(code, loginIdx);
		//카드 모달: 이 테마 전용 + 공통 디자인, 로그인 회원이면 닉네임을 미리 채운다
		model.addAttribute("dList", designService.designList(post.getTheme()));
		model.addAttribute("loginNickname", loginIdx == null ? "" : userService.userView(loginIdx).getNickname());
		List<EnvelopeResponseDTO> eList = cardService.envelopeList(post.getPostIdx());
		model.addAttribute("post", post);
		model.addAttribute("eList", eList);
		//화면 JS 용 (보낸 이름, 디자인 이미지만)
		model.addAttribute("envelopeJs", eList.stream().map(env -> {
			Map<String, Object> m = new HashMap<>();
			m.put("senderName", env.getSenderName());
			m.put("imageUrl", env.getImageUrl());
			return m;
		}).toList());
		model.addAttribute("shareUrl", shareUrl(code));
		return "Post/post_view";
	}

	//꾸미기 폼 (주인, 개봉 전)
	@GetMapping("post_decorate")
	public String postDecorate(@RequestParam("code") String code, @AuthenticationPrincipal CustomUserDetails login, Model model) {
		log.info("Post Call : post_decorate");
		model.addAttribute("post", postService.postModify(code, login.getUserIdx()));
		return "Post/post_decorate";
	}

	//꾸미기 처리 (ajax)
	@ResponseBody
	@PostMapping(value = "post_decorate", produces = "text/plain;charset=UTF-8")
	public String postDecoratePro(@RequestParam("code") String code,
			@RequestParam(value = "decos", required = false) List<String> decos,
			@RequestParam(value = "nameTag", required = false) String nameTag,
			@AuthenticationPrincipal CustomUserDetails login) {
		log.info("Post Call : post_decorate_pro");
		postService.postDecorate(code, login.getUserIdx(), decos, nameTag);
		return "保存しました";
	}

	//수정 폼 (주인, 개봉 전)
	@GetMapping("post_modify")
	public String postModify(@RequestParam("code") String code, @AuthenticationPrincipal CustomUserDetails login, Model model) {
		log.info("Post Call : post_modify");
		PostResponseDTO post = postService.postModify(code, login.getUserIdx());
		model.addAttribute("post", post);
		model.addAttribute("postRequestDTO", new PostRequestDTO(post));
		model.addAttribute("themeDefaults", themeDefaults(userService.userView(login.getUserIdx())));
		return "Post/post_modify";
	}

	//수정 처리
	@PostMapping("post_modify")
	public String postModifyPro(@RequestParam("code") String code, @AuthenticationPrincipal CustomUserDetails login,
			@Valid PostRequestDTO postRequestDTO, BindingResult result, Model model, RedirectAttributes ra) {
		log.info("Post Call : post_modify_pro");
		PostResponseDTO post = postService.postModify(code, login.getUserIdx());
		if (!result.hasErrors()) {
			try {
				postService.postModifyPro(code, login.getUserIdx(), postRequestDTO);
				ra.addFlashAttribute("toast", "保存しました");
				return "redirect:/post/" + code;
			} catch (BusinessException e) {
				result.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		model.addAttribute("post", post);
		model.addAttribute("themeDefaults", themeDefaults(userService.userView(login.getUserIdx())));
		return "Post/post_modify";
	}

	//삭제 처리
	@PostMapping("post_delete")
	public String postDeletePro(@RequestParam("code") String code, @AuthenticationPrincipal CustomUserDetails login,
			RedirectAttributes ra) {
		log.info("Post Call : post_delete_pro");
		postService.postDelete(code, login.getUserIdx());
		ra.addFlashAttribute("toast", "ポストを削除しました");
		return "redirect:/User/user_mypage";
	}

	//테마 키 → {date, time} 개봉일 기본값 (화면 JS 용). 誕生日 은 생일이 없으면 빈 값
	private Map<String, Map<String, String>> themeDefaults(UserResponseDTO user) {
		Map<String, Map<String, String>> map = new LinkedHashMap<>();
		for (Theme theme : Theme.values()) {
			LocalDateTime d = postService.defaultOpenDate(theme, user.getBirthday());
			map.put(theme.getKey(), Map.of(
					"date", d == null ? "" : d.format(DATE),
					"time", d == null ? "00:00" : d.format(TIME)));
		}
		return map;
	}

	//공유 링크 (http://호스트/post/코드)
	private String shareUrl(String code) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path("/post/{code}").buildAndExpand(code).toUriString();
	}
}
