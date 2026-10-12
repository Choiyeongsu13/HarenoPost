package com.harenopost.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.harenopost.member.MemberService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 로그인 성공: last_login 갱신 후 원래 가려던 화면 또는 /mypage */
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private final MemberService memberService;

	public LoginSuccessHandler(MemberService memberService) {
		this.memberService = memberService;
		setDefaultTargetUrl("/mypage");
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		memberService.loggedIn(authentication.getName());
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
