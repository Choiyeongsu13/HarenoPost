package com.harenopost.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// LoginSuccessHandler 는 생성자가 아닌 메서드 인자로 받는다 (PasswordEncoder 와 순환 참조 방지)
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, LoginSuccessHandler loginSuccessHandler) throws Exception {
		http.authorizeHttpRequests(a -> a
				.requestMatchers("/post/new").authenticated() // "/post/*" 보다 먼저
				.requestMatchers("/", "/login", "/signup", "/post/*", "/css/**", "/js/**", "/img/**", "/upload/**",
						"/error", "/error/**", "/favicon.ico").permitAll()
				.requestMatchers("/api/users/check-id", "/api/mail/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/posts/*/designs").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/posts/*/cards").permitAll()
				.requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.formLogin(f -> f
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.usernameParameter("userid")
				.passwordParameter("passwd")
				.successHandler(loginSuccessHandler)
				.failureUrl("/login?error")
				.permitAll())
			.logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/"))
	
			.exceptionHandling(e -> e
				.defaultAuthenticationEntryPointFor(
					new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
					PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
				.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"),
					AnyRequestMatcher.INSTANCE));
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
