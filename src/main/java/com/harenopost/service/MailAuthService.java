package com.harenopost.service;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 이메일 인증 코드를 세션에 보관하고 비교한다.
 * 코드는 응답에 넣지 않고 서버에서만 비교한다.
 */
@Service
@RequiredArgsConstructor
public class MailAuthService {

	private static final String SESSION_AUTH = "mailAuth";
	private static final String SESSION_VERIFIED = "verifiedEmail";
	private static final int EXPIRE_MINUTES = 5;
	private static final int RESEND_SECONDS = 30;

	private final SecureRandom random = new SecureRandom();
	private final EmailService emailService;

	/** 세션에 저장하는 인증 정보 */
	record MailAuth(String email, String code, LocalDateTime expiresAt, LocalDateTime sentAt) implements Serializable {
	}

	public void sendCode(HttpSession session, String email) {
		MailAuth before = (MailAuth) session.getAttribute(SESSION_AUTH);
		LocalDateTime now = LocalDateTime.now();
		if (before != null && before.sentAt().plusSeconds(RESEND_SECONDS).isAfter(now)) {
			throw new BusinessException(ErrorCode.MAIL_TOO_FAST);
		}
		String code = String.format("%06d", random.nextInt(1_000_000));
		emailService.sendAuthCode(email, code);
		session.setAttribute(SESSION_AUTH, new MailAuth(email, code, now.plusMinutes(EXPIRE_MINUTES), now));
		session.removeAttribute(SESSION_VERIFIED);
	}

	public void verify(HttpSession session, String email, String code) {
		MailAuth auth = (MailAuth) session.getAttribute(SESSION_AUTH);
		if (auth == null || !auth.email().equals(email)) {
			throw new BusinessException(ErrorCode.MAIL_CODE_WRONG);
		}
		if (LocalDateTime.now().isAfter(auth.expiresAt())) {
			session.removeAttribute(SESSION_AUTH);
			throw new BusinessException(ErrorCode.MAIL_CODE_EXPIRED);
		}
		if (!auth.code().equals(code)) {
			throw new BusinessException(ErrorCode.MAIL_CODE_WRONG);
		}
		session.removeAttribute(SESSION_AUTH);
		session.setAttribute(SESSION_VERIFIED, email);
	}

	/** 이 이메일로 인증을 마쳤는지 */
	public boolean isVerified(HttpSession session, String email) {
		return email != null && email.equals(session.getAttribute(SESSION_VERIFIED));
	}

	/** 가입·변경이 끝나면 인증 정보를 지운다 */
	public void clear(HttpSession session) {
		session.removeAttribute(SESSION_AUTH);
		session.removeAttribute(SESSION_VERIFIED);
	}
}
