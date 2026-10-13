package com.harenopost.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harenopost.dto.ApiResponse;
import com.harenopost.dto.DecorateRequest;
import com.harenopost.dto.DecorateResponse;
import com.harenopost.entity.Post;
import com.harenopost.service.PostService;
import com.harenopost.util.SecurityUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostApiController {

	private final PostService postService;

	/** 꾸미기 저장 (주인, 개봉 전) */
	@PutMapping("/{code}/decorate")
	public ApiResponse<DecorateResponse> decorate(@PathVariable("code") String code, @Valid @RequestBody DecorateRequest req) {
		Post post = postService.decorate(code, SecurityUtil.requireUserIdx(), req);
		return ApiResponse.ok("保存しました", new DecorateResponse(post.getDecoCodes(), post.getNameTag()));
	}
}
