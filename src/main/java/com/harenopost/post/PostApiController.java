package com.harenopost.post;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harenopost.common.ApiResponse;
import com.harenopost.common.SecurityUtil;
import com.harenopost.post.dto.DecorateRequest;
import com.harenopost.post.dto.DecorateResponse;

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
		Post post = postService.decorate(code, SecurityUtil.requireMemberIdx(), req);
		return ApiResponse.ok("保存しました", new DecorateResponse(post.getDecoCodes(), post.getNameTag()));
	}
}
