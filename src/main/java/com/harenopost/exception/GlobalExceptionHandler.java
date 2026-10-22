package com.harenopost.exception;

import org.apache.tomcat.util.http.InvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//예외를 처리하는 곳
// - ajax 요청: 상태 코드 + 메시지 문자열 (화면에서 toast 나 메시지로 표시)
// - 화면 요청: 에러 팝업(error/*.html) → 「OK」 로 이전 화면
@ControllerAdvice
public class GlobalExceptionHandler {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	//BusinessException 처리
	@ExceptionHandler(BusinessException.class)
	public Object handleBusinessException(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
		return respond(e.getErrorCode().getStatus(), e.getErrorCode().getMessage(), request, response);
	}

	//업로드 파일이 spring.servlet.multipart 크기 제한을 넘었을 때
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public Object handleMaxUpload(MaxUploadSizeExceededException e, HttpServletRequest request, HttpServletResponse response) {
		return respond(ErrorCode.FILE_TOO_BIG.getStatus(), ErrorCode.FILE_TOO_BIG.getMessage(), request, response);
	}

	//깨진 문자 인코딩 등 잘못된 요청 파라미터 → 400
	@ExceptionHandler(InvalidParameterException.class)
	public Object handleInvalidParameter(InvalidParameterException e, HttpServletRequest request, HttpServletResponse response) {
		log.warn("잘못된 요청 파라미터: {} {}", request.getMethod(), request.getRequestURI());
		return respond(ErrorCode.INVALID_INPUT.getStatus(), ErrorCode.INVALID_INPUT.getMessage(), request, response);
	}

	//기타 예외 처리. 내부 메시지는 화면에 내보내지 않고 로그에만 남긴다
	@ExceptionHandler(Exception.class)
	public Object handleAllException(Exception e, HttpServletRequest request, HttpServletResponse response) {
		//Spring MVC 예외(없는 URL 404, 파라미터 누락 400 등)는 원래 상태 코드를 쓴다
		if (e instanceof ErrorResponse er) {
			HttpStatusCode status = er.getStatusCode();
			String message = status.value() == 404 ? "ページが見つかりません" : ErrorCode.INVALID_INPUT.getMessage();
			return respond(status, message, request, response);
		}
		log.error("처리하지 못한 예외: {} {}", request.getMethod(), request.getRequestURI(), e);
		return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.getMessage(), request, response);
	}

	//ajax 면 메시지 문자열, 화면이면 상태 코드에 맞는 에러 팝업 화면
	private Object respond(HttpStatusCode status, String message, HttpServletRequest request, HttpServletResponse response) {
		if (isAjax(request)) {
			return ResponseEntity.status(status)
					.contentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
					.body(message);
		}
		response.setStatus(status.value());
		String view;
		switch (status.value()) {
		case 404:
			view = "error/404";
			break;
		case 403:
			view = "error/403";
			break;
		default:
			view = "error/error";
		}
		ModelAndView mav = new ModelAndView(view);
		mav.addObject("message", message);
		return mav;
	}

	//jQuery ajax 는 X-Requested-With: XMLHttpRequest 헤더를 보낸다
	public static boolean isAjax(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
}
