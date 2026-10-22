package com.harenopost.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.dto.UserModifyRequestDTO;
import com.harenopost.dto.UserRequestDTO;
import com.harenopost.dto.UserResponseDTO;
import com.harenopost.entity.UserEntity;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
	private static final String PASSWD_PATTERN = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,20}$";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailAuthService mailAuthService;

	//id 중복검사 (true = 이미 사용 중)
	@Transactional(readOnly = true)
	public boolean userIdCheck(String userid) {
		return userRepository.existsByUserid(userid);
	}

	//본인인증(이메일): 인증 코드 발송
	@Transactional(readOnly = true)
	public void userMailSend(HttpSession session, String email) {
		if (email == null || !email.matches(EMAIL_PATTERN)) {
			throw new BusinessException(ErrorCode.INVALID_EMAIL);
		}
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		mailAuthService.mailSend(session, email);
	}

	//본인인증(이메일): 인증 코드 확인
	public void userMailCheck(HttpSession session, String email, String code) {
		if (code == null || !code.matches("^[0-9]{6}$")) {
			throw new BusinessException(ErrorCode.MAIL_CODE_WRONG);
		}
		mailAuthService.mailCheck(session, email, code);
	}

	//회원가입 처리. 화면에서 확인했더라도 서버에서 다시 검사한다
	@Transactional
	public void userInsert(UserRequestDTO userRequestDTO, HttpSession session) {
		if (userRepository.existsByUserid(userRequestDTO.getUserid())) {
			throw new BusinessException(ErrorCode.DUPLICATE_USERID);
		}
		if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
			throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
		}
		if (!mailAuthService.isVerified(session, userRequestDTO.getEmail())) {
			throw new BusinessException(ErrorCode.MAIL_NOT_VERIFIED);
		}
		//비번 암호화(BCrypt) 후 저장
		userRepository.save(userRequestDTO.toEntity(passwordEncoder.encode(userRequestDTO.getPasswd())));
		mailAuthService.clear(session);
	}

	//회원 정보 (마이페이지, 수정 폼)
	@Transactional(readOnly = true)
	public UserResponseDTO userView(Long userIdx) {
		return new UserResponseDTO(userFind(userIdx));
	}

	//프로필 수정 처리. 이메일을 바꾸면 인증을 다시 거쳐야 한다
	@Transactional
	public void userModify(Long userIdx, UserModifyRequestDTO dto, HttpSession session) {
		UserEntity userEntity = userFind(userIdx);
		boolean emailChanged = !userEntity.getEmail().equals(dto.getEmail());
		if (emailChanged) {
			if (userRepository.existsByEmail(dto.getEmail())) {
				throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
			}
			if (!mailAuthService.isVerified(session, dto.getEmail())) {
				throw new BusinessException(ErrorCode.MAIL_NOT_VERIFIED);
			}
		}
		userEntity.userModify(dto.getNickname().strip(), dto.getEmail(), dto.getBirthday());
		if (emailChanged) {
			mailAuthService.clear(session);
		}
	}

	//비밀번호 변경 처리
	@Transactional
	public void userPasswd(Long userIdx, String currentPasswd, String newPasswd, String newPasswdConfirm) {
		UserEntity userEntity = userFind(userIdx);
		if (currentPasswd == null || !passwordEncoder.matches(currentPasswd, userEntity.getPasswd())) {
			throw new BusinessException(ErrorCode.PASSWORD_WRONG);
		}
		if (newPasswd == null || !newPasswd.matches(PASSWD_PATTERN)) {
			throw new BusinessException(ErrorCode.PASSWORD_FORMAT);
		}
		if (!newPasswd.equals(newPasswdConfirm)) {
			throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
		}
		userEntity.userPasswd(passwordEncoder.encode(newPasswd));
	}

	//탈퇴 처리. 포스트·카드·신고는 DB 의 ON DELETE CASCADE 로 함께 지워진다
	@Transactional
	public void userDelete(Long userIdx, String passwd) {
		UserEntity userEntity = userFind(userIdx);
		if (passwd == null || !passwordEncoder.matches(passwd, userEntity.getPasswd())) {
			throw new BusinessException(ErrorCode.PASSWORD_WRONG);
		}
		userRepository.delete(userEntity);
	}

	//로그인 성공 시 last_login 갱신
	@Transactional
	public void userLoginTime(String userid) {
		userRepository.findByUserid(userid).ifPresent(UserEntity::userLoginTime);
	}

	//user_idx 로 회원 찾기 (없으면 예외)
	private UserEntity userFind(Long userIdx) {
		return userRepository.findById(userIdx)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
