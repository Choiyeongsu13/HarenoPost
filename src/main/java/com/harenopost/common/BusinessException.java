package com.harenopost.common;

import lombok.Getter;

/** 업무 규칙 위반. GlobalExceptionHandler 가 JSON 또는 에러 화면으로 바꾼다 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
