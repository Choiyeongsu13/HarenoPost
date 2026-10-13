package com.harenopost.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harenopost.dto.CustomUserDetails;
import com.harenopost.dto.MyPostDto;
import com.harenopost.dto.PasswordForm;
import com.harenopost.dto.ProfileForm;
import com.harenopost.dto.SignupForm;
import com.harenopost.dto.WithdrawForm;
import com.harenopost.entity.User;
import com.harenopost.exception.BusinessException;
import com.harenopost.service.MailAuthService;
import com.harenopost.service.UserService;
import com.harenopost.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final PostService postService;
	private final MailAuthService mailAuthService;

	@GetMapping("/login")
	public String login() {
		return "user/login";
	}

	// ---------- 회원가입 ----------

	@GetMapping("/signup")
	public String signupForm(Model model) {
		model.addAttribute("form", new SignupForm());
		return "user/signup";
	}

	@PostMapping("/signup")
	public String signup(@Valid @ModelAttribute("form") SignupForm form, BindingResult br, HttpSession session, Model model) {
		if (form.getPasswd() != null && !form.getPasswd().equals(form.getPasswdConfirm())) {
			br.rejectValue("passwdConfirm", "mismatch", "パスワードが一致しません");
		}
		if (!br.hasErrors()) {
			try {
				userService.signup(form, session);
				return "redirect:/login?joined";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		form.setPasswd(null);
		form.setPasswdConfirm(null);
		// 이미 인증한 메일이면 다시 인증하지 않아도 되게 화면에 알려 준다
		model.addAttribute("mailVerified", mailAuthService.isVerified(session, form.getEmail()));
		return "user/signup";
	}

	// ---------- 마이페이지 ----------

	@GetMapping("/mypage")
	public String mypage(@AuthenticationPrincipal CustomUserDetails login, Model model) {
		User user = userService.get(login.getUserIdx());
		List<MyPostDto> posts = postService.getMyPosts(user.getUserIdx());
		model.addAttribute("user", user);
		model.addAttribute("posts", posts);
		model.addAttribute("postCount", posts.size());
		model.addAttribute("cardCount", posts.stream().mapToLong(MyPostDto::cardCount).sum());
		return "user/mypage";
	}

	@GetMapping("/mypage/edit")
	public String editForm(@AuthenticationPrincipal CustomUserDetails login, Model model) {
		return editPage(userService.get(login.getUserIdx()), model);
	}

	@PostMapping("/mypage/edit")
	public String edit(@AuthenticationPrincipal CustomUserDetails login, @Valid @ModelAttribute("profileForm") ProfileForm form,
			BindingResult br, HttpSession session, Model model, RedirectAttributes ra) {
		User user = userService.get(login.getUserIdx());
		if (!br.hasErrors()) {
			try {
				userService.updateProfile(user.getUserIdx(), form, session);
				ra.addFlashAttribute("toast", "プロフィールを保存しました");
				return "redirect:/mypage";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		return editPage(user, model);
	}

	@PostMapping("/mypage/password")
	public String changePassword(@AuthenticationPrincipal CustomUserDetails login, @Valid @ModelAttribute("passwordForm") PasswordForm form,
			BindingResult br, Model model, RedirectAttributes ra) {
		User user = userService.get(login.getUserIdx());
		if (form.getNewPasswd() != null && !form.getNewPasswd().equals(form.getNewPasswdConfirm())) {
			br.rejectValue("newPasswdConfirm", "mismatch", "パスワードが一致しません");
		}
		if (!br.hasErrors()) {
			try {
				userService.changePassword(user.getUserIdx(), form);
				ra.addFlashAttribute("toast", "パスワードを変更しました");
				return "redirect:/mypage";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		model.addAttribute("profileForm", profileOf(user));
		return editPage(user, model);
	}

	@PostMapping("/mypage/withdraw")
	public String withdraw(@AuthenticationPrincipal CustomUserDetails login, @Valid @ModelAttribute("withdrawForm") WithdrawForm form,
			BindingResult br, Model model, Authentication auth, HttpServletRequest request, HttpServletResponse response) {
		User user = userService.get(login.getUserIdx());
		if (!br.hasErrors()) {
			try {
				userService.withdraw(user.getUserIdx(), form.getPasswd());
				new SecurityContextLogoutHandler().logout(request, response, auth);
				return "redirect:/";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		model.addAttribute("profileForm", profileOf(user));
		return editPage(user, model);
	}

	private String editPage(User user, Model model) {
		model.addAttribute("user", user);
		if (!model.containsAttribute("profileForm")) {
			model.addAttribute("profileForm", profileOf(user));
		}
		if (!model.containsAttribute("passwordForm")) {
			model.addAttribute("passwordForm", new PasswordForm());
		}
		if (!model.containsAttribute("withdrawForm")) {
			model.addAttribute("withdrawForm", new WithdrawForm());
		}
		return "user/edit";
	}

	private ProfileForm profileOf(User user) {
		ProfileForm form = new ProfileForm();
		form.setNickname(user.getNickname());
		form.setEmail(user.getEmail());
		form.setBirthday(user.getBirthday());
		return form;
	}
}
