package com.harenopost.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.harenopost.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/*
  예외 처리
  - /api 요청: 화면에서 toast 로 표시
  - 화면 요청: 에러 팝업 → 「OK」 로 이전 화면
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public Object handleBusiness(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
		return respond(e.getErrorCode().getStatus(), e.getErrorCode(), e.getErrorCode().getMessage(), request, response);
	}

	// 그 밖의 예외. 내부 메시지는 화면에 내보내지 않고 로그에만 남긴다 
	@ExceptionHandler(Exception.class)
	public Object handleAll(Exception e, HttpServletRequest request, HttpServletResponse response) {
		if (e instanceof ErrorResponse er) {
			HttpStatusCode status = er.getStatusCode();
			String message = status.value() == 404 ? "ページが見つかりません" : ErrorCode.INVALID_INPUT.getMessage();
			return respond(status, ErrorCode.INVALID_INPUT, message, request, response);
		}
		log.error("처리하지 못한 예외: {} {}", request.getMethod(), request.getRequestURI(), e);
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage(), request, response);
	}

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

	// API 면 JSON, 화면이면 상태 코드에 맞는 에러 팝업 화면
	private Object respond(HttpStatusCode status, ErrorCode code, String message, HttpServletRequest request, HttpServletResponse response) {
		if (isApi(request)) {
			return ResponseEntity.status(status).body(ApiResponse.fail(code, message));
		}
		response.setStatus(status.value());
		String view = switch (status.value()) {
			case 404 -> "error/404";
			case 403 -> "error/403";
			default -> "error/error";
		};
		ModelAndView mav = new ModelAndView(view);
		mav.addObject("message", message);
		return mav;
	}

	private boolean isApi(HttpServletRequest request) {
		return request.getRequestURI().startsWith(request.getContextPath() + "/api/");
	}
}
