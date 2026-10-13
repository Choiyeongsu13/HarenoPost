package com.harenopost.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harenopost.dto.ApiResponse;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.repository.UserRepository;
import com.harenopost.service.MailAuthService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailApiController {

	private final MailAuthService mailAuthService;
	private final UserRepository userRepository;

	public record SendRequest(
			@NotBlank(message = "メールアドレスを入力してください")
			@Email(message = "メールアドレスの形式を確認してください") String email) {
	}

	public record VerifyRequest(
			@NotBlank(message = "メールアドレスを入力してください") String email,
			@NotBlank(message = "認証コードを入力してください")
			@Pattern(regexp = "^[0-9]{6}$", message = "認証コードは6桁の数字です") String code) {
	}

	@PostMapping("/send")
	public ApiResponse<Void> send(@Valid @RequestBody SendRequest req, HttpSession session) {
		if (userRepository.existsByEmail(req.email())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		mailAuthService.sendCode(session, req.email());
		return ApiResponse.ok("認証コードを送信しました（5分間有効）", null);
	}

	@PostMapping("/verify")
	public ApiResponse<Void> verify(@Valid @RequestBody VerifyRequest req, HttpSession session) {
		mailAuthService.verify(session, req.email(), req.code());
		return ApiResponse.ok("メール認証が完了しました", null);
	}
}
