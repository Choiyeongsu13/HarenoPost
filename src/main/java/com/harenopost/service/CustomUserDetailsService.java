package com.harenopost.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.dto.CustomUserDetails;
import com.harenopost.entity.UserEntity;
import com.harenopost.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	//로그인: id 를 이용한 사용자 정보 조회 (비밀번호 비교는 시큐리티가 BCrypt 로 한다)
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByUserid(userid)
				.orElseThrow(() -> new UsernameNotFoundException(userid + " が存在しません"));
		return new CustomUserDetails(userEntity);
	}
}
