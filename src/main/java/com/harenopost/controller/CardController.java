package com.harenopost.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harenopost.dto.CardRequestDTO;
import com.harenopost.dto.CardResponseDTO;
import com.harenopost.dto.CustomUserDetails;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.service.CardService;
import com.harenopost.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("Card")
public class CardController {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(CardController.class);

	private final CardService cardService;
	private final PostService postService;

	//카드 보내기 (ajax, 누구나). 응답에 카드 본문은 넣지 않는다
	@ResponseBody
	@PostMapping(value = "card_write", produces = "text/plain;charset=UTF-8")
	public String cardWrite(@ModelAttribute CardRequestDTO cardRequestDTO, HttpServletRequest request) {
		log.info("Card Call : card_write");
		cardService.cardWrite(cardRequestDTO, clientIp(request));
		return "カードを届けました！";
	}

	//카드 목록 (주인, 개봉 후). 개봉 전이면 포스트 화면으로 돌려보낸다
	@GetMapping("card_list")
	public String cardList(@RequestParam("code") String code, @AuthenticationPrincipal CustomUserDetails login,
			Model model, RedirectAttributes ra) {
		log.info("Card Call : card_list");
		List<CardResponseDTO> cList;
		try {
			cList = cardService.cardList(code, login.getUserIdx());
		} catch (BusinessException e) {
			if (e.getErrorCode() != ErrorCode.NOT_OPENED_YET) {
				throw e;
			}
			ra.addFlashAttribute("toast", e.getMessage());
			return "redirect:/post/" + code;
		}
		model.addAttribute("post", postService.postView(code, login.getUserIdx()));
		model.addAttribute("cList", cList);
		return "Card/card_list";
	}

	//읽음 처리 (ajax, 주인, 개봉 후)
	@ResponseBody
	@PostMapping(value = "card_read", produces = "text/plain;charset=UTF-8")
	public String cardRead(@RequestParam("cardIdx") Long cardIdx, @AuthenticationPrincipal CustomUserDetails login) {
		log.info("Card Call : card_read");
		cardService.cardRead(cardIdx, login.getUserIdx());
		return "OK";
	}

	//카드 신고 (ajax, 주인, 개봉 후)
	@ResponseBody
	@PostMapping(value = "card_report", produces = "text/plain;charset=UTF-8")
	public String cardReport(@RequestParam("cardIdx") Long cardIdx, @RequestParam(value = "reason", required = false) String reason,
			@AuthenticationPrincipal CustomUserDetails login) {
		log.info("Card Call : card_report");
		cardService.cardReport(cardIdx, login.getUserIdx(), reason);
		return "通報しました。管理者が確認します";
	}

	//보낸 사람 IP. 같은 서버의 프록시(nginx 등)를 거쳐 온 경우에만 X-Forwarded-For 첫 값을 믿는다
	//(그렇지 않으면 누구나 헤더를 꾸며서 1분 제한을 피할 수 있다)
	private String clientIp(HttpServletRequest request) {
		String remote = request.getRemoteAddr();
		boolean fromLocalProxy = "127.0.0.1".equals(remote) || "0:0:0:0:0:0:0:1".equals(remote) || "::1".equals(remote);
		String forwarded = request.getHeader("X-Forwarded-For");
		if (fromLocalProxy && forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].strip();
		}
		return remote;
	}
}
