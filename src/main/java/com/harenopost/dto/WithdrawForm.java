package com.harenopost.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawForm {

	@NotBlank(message = "パスワードを入力してください")
	private String passwd;
}
