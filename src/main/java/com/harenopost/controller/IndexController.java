package com.harenopost.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// 메인 화면
@Controller
public class IndexController {

	@GetMapping("/")
	public String index() {
		return "index";
	}
}
