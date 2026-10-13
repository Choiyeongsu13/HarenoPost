package com.harenopost.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.dto.PasswordForm;
import com.harenopost.dto.ProfileForm;
import com.harenopost.dto.SignupForm;
import com.harenopost.entity.User;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailAuthService mailAuthService;

	public User get(Long userIdx) {
		return userRepository.findById(userIdx)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	public boolean isUseridAvailable(String userid) {
		return !userRepository.existsByUserid(userid);
	}

	/** 회원가입. 화면에서 확인했더라도 서버에서 다시 검사한다 */
	@Transactional
	public void signup(SignupForm form, HttpSession session) {
		if (userRepository.existsByUserid(form.getUserid())) {
			throw new BusinessException(ErrorCode.DUPLICATE_USERID);
		}
		if (userRepository.existsByEmail(form.getEmail())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		if (!mailAuthService.isVerified(session, form.getEmail())) {
			throw new BusinessException(ErrorCode.MAIL_NOT_VERIFIED);
		}
		User user = User.create(form.getUserid(), passwordEncoder.encode(form.getPasswd()),
				form.getNickname().strip(), form.getEmail(), form.getBirthday());
		userRepository.save(user);
		mailAuthService.clear(session);
	}

	/** 닉네임·생일·이메일 수정. 이메일을 바꾸면 인증을 다시 거쳐야 한다 */
	@Transactional
	public void updateProfile(Long userIdx, ProfileForm form, HttpSession session) {
		User user = get(userIdx);
		boolean emailChanged = !user.getEmail().equals(form.getEmail());
		if (emailChanged) {
			if (userRepository.existsByEmail(form.getEmail())) {
				throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
			}
			if (!mailAuthService.isVerified(session, form.getEmail())) {
				throw new BusinessException(ErrorCode.MAIL_NOT_VERIFIED);
			}
		}
		user.updateProfile(form.getNickname().strip(), form.getEmail(), form.getBirthday());
		if (emailChanged) {
			mailAuthService.clear(session);
		}
	}

	@Transactional
	public void changePassword(Long userIdx, PasswordForm form) {
		User user = get(userIdx);
		if (!passwordEncoder.matches(form.getCurrentPasswd(), user.getPasswd())) {
			throw new BusinessException(ErrorCode.PASSWORD_WRONG);
		}
		user.changePassword(passwordEncoder.encode(form.getNewPasswd()));
	}

	/** 탈퇴. 포스트·카드·신고는 DB 의 ON DELETE CASCADE 로 함께 지워진다 */
	@Transactional
	public void withdraw(Long userIdx, String passwd) {
		User user = get(userIdx);
		if (!passwordEncoder.matches(passwd, user.getPasswd())) {
			throw new BusinessException(ErrorCode.PASSWORD_WRONG);
		}
		userRepository.delete(user);
	}

	/** 로그인 성공 시 last_login 갱신 */
	@Transactional
	public void loggedIn(String userid) {
		userRepository.findByUserid(userid).ifPresent(User::loggedIn);
	}
}
