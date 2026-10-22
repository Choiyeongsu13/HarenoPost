package com.harenopost.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

//메일 발송 (Gmail SMTP, spring.mail.*)
@Service
public class EmailService {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	// spring.mail.host 가 없으면 빈이 없으므로 required=false
	@Autowired(required = false)
	private JavaMailSender emailSender;

	@Value("${spring.mail.username:}")
	private String mailFrom;

	@Value("${app.mail.dev-log:false}")
	private boolean devLog;

	//회원가입·메일 변경 인증 코드 발송
	public void mailSend(String toEmail, String code) {
		if (emailSender == null || mailFrom == null || mailFrom.isBlank()) {
			if (devLog) {
				// 로컬에서 메일 계정 없이 테스트할 때만. 응답에는 절대 넣지 않는다
				log.info("[개발용] 메일 계정이 없어 발송하지 않음. to={}, 인증 코드={}", toEmail, code);
				return;
			}
			throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
		}
		String subject = "【ハレの日ポスト】認証コードのお知らせ";
		String text = "ハレの日ポストをご利用いただきありがとうございます。\n\n"
				+ "認証コード： " + code + "\n\n"
				+ "このコードの有効期限は5分です。\n"
				+ "お心当たりのない場合は、このメールを破棄してください。";
		try {
			MimeMessage mimeMessage = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
			helper.setTo(toEmail);
			helper.setFrom(mailFrom);
			helper.setSubject(subject);
			helper.setText(text, false);
			emailSender.send(mimeMessage);
		} catch (MessagingException | MailException e) {
			log.warn("메일 발송 실패 to={}", toEmail, e);
			throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
		}
	}
}
