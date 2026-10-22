package com.harenopost.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	//LoginSuccessHandler 는 생성자가 아닌 메서드 인자로 받는다 (PasswordEncoder 와 순환 참조 방지)
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, LoginSuccessHandler loginSuccessHandler) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				//누구나: 메인, 로그인·가입(ID 중복검사, 메일 인증), 포스트 화면(공유 링크), 정적 파일, 에러
				.requestMatchers("/", "/User/user_login", "/User/user_insert", "/User/user_idCheck",
						"/User/user_mailSend", "/User/user_mailCheck").permitAll()
				.requestMatchers("/post/*", "/Post/post_view", "/Card/card_write").permitAll() //카드는 비로그인도 보낸다
				.requestMatchers("/css/**", "/js/**", "/img/**", "/upload/**", "/error", "/error/**", "/favicon.ico").permitAll()
				.requestMatchers("/Admin/**").hasRole("ADMIN") //관리자 폴더는 ADMIN 만
				.anyRequest().authenticated()) //그 밖은 로그인 회원
			.formLogin(login -> login
				.loginPage("/User/user_login")
				.loginProcessingUrl("/User/user_login") //로그인 form 의 action 주소와 일치
				.usernameParameter("userid")
				.passwordParameter("passwd") //로그인 form 의 비밀번호 input name 과 일치
				.successHandler(loginSuccessHandler)
				.failureUrl("/User/user_login?error")
				.permitAll())
			.logout(logout -> logout.logoutUrl("/User/user_logout").logoutSuccessUrl("/"))
			//ajax 는 로그인 화면으로 보내지 않고 401, 그 밖의 화면은 로그인 화면으로
			.exceptionHandling(e -> e
				.defaultAuthenticationEntryPointFor(
					new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
					new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"))
				.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/User/user_login"),
					AnyRequestMatcher.INSTANCE));
		return http.build();
	}

	//비밀번호 암호화
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
