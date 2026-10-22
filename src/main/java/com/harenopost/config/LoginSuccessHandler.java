package com.harenopost.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.harenopost.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//로그인 성공: last_login 갱신 후 원래 가려던 화면 또는 마이페이지
@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private final UserService userService;

	public LoginSuccessHandler(UserService userService) {
		this.userService = userService;
		setDefaultTargetUrl("/User/user_mypage");
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		userService.userLoginTime(authentication.getName());
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
