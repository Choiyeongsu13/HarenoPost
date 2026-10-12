package com.harenopost.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/** /api/** 요청이면 JSON, 화면 요청이면 에러 화면으로 바꾼다 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public Object handleBusiness(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
		ErrorCode code = e.getErrorCode();
		if (isApi(request)) {
			return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code));
		}
		response.setStatus(code.getStatus().value());
		String view = switch (code.getStatus()) {
			case NOT_FOUND -> "error/404";
			case FORBIDDEN -> "error/403";
			default -> "error/error";
		};
		ModelAndView mav = new ModelAndView(view);
		mav.addObject("message", code.getMessage());
		return mav;
	}

	/** @Valid 실패(ajax JSON 본문) → 첫 번째 메시지를 돌려준다 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValid(MethodArgumentNotValidException e) {
		FieldError fe = e.getBindingResult().getFieldError();
		String message = fe != null ? fe.getDefaultMessage() : ErrorCode.INVALID_INPUT.getMessage();
		return ResponseEntity.badRequest().body(ApiResponse.fail(ErrorCode.INVALID_INPUT, message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(ErrorCode.INVALID_INPUT));
	}

	private boolean isApi(HttpServletRequest request) {
		return request.getRequestURI().startsWith(request.getContextPath() + "/api/");
	}
}
