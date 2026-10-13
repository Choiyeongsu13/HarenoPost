package com.harenopost.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harenopost.dto.ApiResponse;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

	private static final String USERID_PATTERN = "^[a-z][a-z0-9]{3,19}$";

	private final UserService userService;

	/** 아이디 중복 확인 */
	@GetMapping("/check-id")
	public ApiResponse<Map<String, Boolean>> checkId(@RequestParam(name = "userid", defaultValue = "") String userid) {
		if (!userid.matches(USERID_PATTERN)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		boolean available = userService.isUseridAvailable(userid);
		String message = available ? "使用できるIDです" : ErrorCode.DUPLICATE_USERID.getMessage();
		return ApiResponse.ok(message, Map.of("available", available));
	}
}
