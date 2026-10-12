package com.harenopost.post.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** PUT /api/posts/{code}/decorate 요청 */
public record DecorateRequest(
		@NotNull(message = "飾りを選んでください") @Size(max = 3, message = "選べない飾りが含まれています") List<String> decos,
		@Size(max = 8, message = "名札は8文字までです") String nameTag) {
}
