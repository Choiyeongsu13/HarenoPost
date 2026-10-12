package com.harenopost.member;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		Member member = memberRepository.findByUserid(userid)
				.orElseThrow(() -> new UsernameNotFoundException(userid + " が存在しません"));
		return new CustomUserDetails(member);
	}
}
