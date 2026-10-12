package com.harenopost.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.common.BusinessException;
import com.harenopost.common.ErrorCode;
import com.harenopost.mail.MailAuthService;
import com.harenopost.member.dto.PasswordForm;
import com.harenopost.member.dto.ProfileForm;
import com.harenopost.member.dto.SignupForm;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailAuthService mailAuthService;

	public Member get(Long memberIdx) {
		return memberRepository.findById(memberIdx)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
	}

	public boolean isUseridAvailable(String userid) {
		return !memberRepository.existsByUserid(userid);
	}

	/** 회원가입. 화면에서 확인했더라도 서버에서 다시 검사한다 */
	@Transactional
	public void signup(SignupForm form, HttpSession session) {
		if (memberRepository.existsByUserid(form.getUserid())) {
			throw new BusinessException(ErrorCode.DUPLICATE_USERID);
		}
		if (memberRepository.existsByEmail(form.getEmail())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		if (!mailAuthService.isVerified(session, form.getEmail())) {
			throw new BusinessException(ErrorCode.MAIL_NOT_VERIFIED);
		}
		Member member = Member.create(form.getUserid(), passwordEncoder.encode(form.getPasswd()),
				form.getNickname().strip(), form.getEmail(), form.getBirthday());
		memberRepository.save(member);
		mailAuthService.clear(session);
	}

	/** 닉네임·생일·이메일 수정. 이메일을 바꾸면 인증을 다시 거쳐야 한다 */
	@Transactional
	public void updateProfile(Long memberIdx, ProfileForm form, HttpSession session) {
		Member member = get(memberIdx);
		boolean emailChanged = !member.getEmail().equals(form.getEmail());
		if (emailChanged) {
			if (memberRepository.existsByEmail(form.getEmail())) {
				throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
			}
			if (!mailAuthService.isVerified(session, form.getEmail())) {
				throw new BusinessException(ErrorCode.MAIL_NOT_VERIFIED);
			}
		}
		member.updateProfile(form.getNickname().strip(), form.getEmail(), form.getBirthday());
		if (emailChanged) {
			mailAuthService.clear(session);
		}
	}

	@Transactional
	public void changePassword(Long memberIdx, PasswordForm form) {
		Member member = get(memberIdx);
		if (!passwordEncoder.matches(form.getCurrentPasswd(), member.getPasswd())) {
			throw new BusinessException(ErrorCode.PASSWORD_WRONG);
		}
		member.changePassword(passwordEncoder.encode(form.getNewPasswd()));
	}

	/** 탈퇴. 포스트·카드·신고는 DB 의 ON DELETE CASCADE 로 함께 지워진다 */
	@Transactional
	public void withdraw(Long memberIdx, String passwd) {
		Member member = get(memberIdx);
		if (!passwordEncoder.matches(passwd, member.getPasswd())) {
			throw new BusinessException(ErrorCode.PASSWORD_WRONG);
		}
		memberRepository.delete(member);
	}

	/** 로그인 성공 시 last_login 갱신 */
	@Transactional
	public void loggedIn(String userid) {
		memberRepository.findByUserid(userid).ifPresent(Member::loggedIn);
	}
}
