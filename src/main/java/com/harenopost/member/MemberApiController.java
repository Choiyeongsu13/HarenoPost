package com.harenopost.member;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harenopost.common.ApiResponse;
import com.harenopost.common.BusinessException;
import com.harenopost.common.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberApiController {

	private static final String USERID_PATTERN = "^[a-z][a-z0-9]{3,19}$";

	private final MemberService memberService;

	/** 아이디 중복 확인 */
	@GetMapping("/check-id")
	public ApiResponse<Map<String, Boolean>> checkId(@RequestParam(name = "userid", defaultValue = "") String userid) {
		if (!userid.matches(USERID_PATTERN)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		boolean available = memberService.isUseridAvailable(userid);
		String message = available ? "使用できるIDです" : ErrorCode.DUPLICATE_USERID.getMessage();
		return ApiResponse.ok(message, Map.of("available", available));
	}
}
