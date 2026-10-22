package com.harenopost.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriUtils;

@Controller
public class IndexController {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(IndexController.class);

	//메인 화면
	@GetMapping("/")
	public String index() {
		log.info("Index Call : index");
		return "index";
	}

	//공유 링크(/post/코드)는 짧게 유지하고 포스트 화면으로 넘긴다
	@GetMapping("/post/{code}")
	public String postLink(@PathVariable("code") String code) {
		log.info("Index Call : post_link");
		return "forward:/Post/post_view?code=" + UriUtils.encodeQueryParam(code, "UTF-8");
	}
}
