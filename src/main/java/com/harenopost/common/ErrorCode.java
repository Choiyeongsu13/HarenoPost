package com.harenopost.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 에러 코드와 화면에 보여 줄 메시지 (docs/06_api.md) */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	INVALID_INPUT(HttpStatus.BAD_REQUEST, "入力内容を確認してください"),
	DUPLICATE_USERID(HttpStatus.CONFLICT, "このIDはすでに使われています"),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "このメールアドレスはすでに登録されています"),
	USERID_NOT_CHECKED(HttpStatus.BAD_REQUEST, "IDの重複確認をしてください"),
	MAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "メール認証を完了してください"),
	MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "メールを送信できませんでした"),
	MAIL_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS, "しばらくしてからもう一度お試しください"),
	MAIL_CODE_WRONG(HttpStatus.BAD_REQUEST, "認証コードが正しくありません"),
	MAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "認証コードの有効期限が切れました。もう一度送信してください"),
	PASSWORD_WRONG(HttpStatus.BAD_REQUEST, "パスワードが正しくありません"),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "会員が見つかりません"),
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "ポストが見つかりません"),
	POST_LIMIT(HttpStatus.BAD_REQUEST, "同時につくれるポストは5つまでです"),
	OPEN_DATE_REQUIRED(HttpStatus.BAD_REQUEST, "開封日時を選んでください"),
	OPEN_DATE_PAST(HttpStatus.BAD_REQUEST, "開封日は今より後の日時を選んでください"),
	OPEN_DATE_TOO_FAR(HttpStatus.BAD_REQUEST, "開封日は1年以内で選んでください"),
	NOT_OWNER(HttpStatus.FORBIDDEN, "アクセス権がありません"),
	ALREADY_OPENED(HttpStatus.BAD_REQUEST, "開封後は変更できません"),
	NOT_OPENED_YET(HttpStatus.BAD_REQUEST, "まだ開封日ではありません"),
	RECEIVE_CLOSED(HttpStatus.BAD_REQUEST, "このポストはもう受付を終了しました"),
	CARD_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS, "少し時間をおいてから送ってください（1分に1通まで）"),
	INVALID_DECO(HttpStatus.BAD_REQUEST, "選べない飾りが含まれています"),
	ALREADY_REPORTED(HttpStatus.CONFLICT, "すでに通報済みです"),
	LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "ログインしてください");

	private final HttpStatus status;
	private final String message;
}
