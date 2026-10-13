package com.harenopost.dto;

import com.harenopost.exception.ErrorCode;

public record ApiResponse<T>(boolean success, String code, String message, T data) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, null, null, data);
	}

	public static <T> ApiResponse<T> ok(String message, T data) {
		return new ApiResponse<>(true, null, message, data);
	}

	public static ApiResponse<Void> fail(ErrorCode e) {
		return new ApiResponse<>(false, e.name(), e.getMessage(), null);
	}

	public static ApiResponse<Void> fail(ErrorCode e, String message) {
		return new ApiResponse<>(false, e.name(), message, null);
	}

}
