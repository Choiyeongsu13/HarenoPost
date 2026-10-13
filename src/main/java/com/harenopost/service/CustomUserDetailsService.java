package com.harenopost.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.dto.CustomUserDetails;
import com.harenopost.entity.User;
import com.harenopost.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		User user = userRepository.findByUserid(userid)
				.orElseThrow(() -> new UsernameNotFoundException(userid + " が存在しません"));
		return new CustomUserDetails(user);
	}
}
