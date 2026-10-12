package com.harenopost.member;

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

import com.harenopost.common.BusinessException;
import com.harenopost.mail.MailAuthService;
import com.harenopost.member.dto.PasswordForm;
import com.harenopost.member.dto.ProfileForm;
import com.harenopost.member.dto.SignupForm;
import com.harenopost.member.dto.WithdrawForm;
import com.harenopost.post.PostService;
import com.harenopost.post.dto.MyPostDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;
	private final PostService postService;
	private final MailAuthService mailAuthService;

	@GetMapping("/login")
	public String login() {
		return "member/login";
	}

	// ---------- 회원가입 ----------

	@GetMapping("/signup")
	public String signupForm(Model model) {
		model.addAttribute("form", new SignupForm());
		return "member/signup";
	}

	@PostMapping("/signup")
	public String signup(@Valid @ModelAttribute("form") SignupForm form, BindingResult br, HttpSession session, Model model) {
		if (form.getPasswd() != null && !form.getPasswd().equals(form.getPasswdConfirm())) {
			br.rejectValue("passwdConfirm", "mismatch", "パスワードが一致しません");
		}
		if (!br.hasErrors()) {
			try {
				memberService.signup(form, session);
				return "redirect:/login?joined";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		form.setPasswd(null);
		form.setPasswdConfirm(null);
		// 이미 인증한 메일이면 다시 인증하지 않아도 되게 화면에 알려 준다
		model.addAttribute("mailVerified", mailAuthService.isVerified(session, form.getEmail()));
		return "member/signup";
	}

	// ---------- 마이페이지 ----------

	@GetMapping("/mypage")
	public String mypage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
		Member member = memberService.get(user.getMemberIdx());
		List<MyPostDto> posts = postService.getMyPosts(member.getMemberIdx());
		model.addAttribute("member", member);
		model.addAttribute("posts", posts);
		model.addAttribute("postCount", posts.size());
		model.addAttribute("cardCount", posts.stream().mapToLong(MyPostDto::cardCount).sum());
		return "member/mypage";
	}

	@GetMapping("/mypage/edit")
	public String editForm(@AuthenticationPrincipal CustomUserDetails user, Model model) {
		return editPage(memberService.get(user.getMemberIdx()), model);
	}

	@PostMapping("/mypage/edit")
	public String edit(@AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute("profileForm") ProfileForm form,
			BindingResult br, HttpSession session, Model model, RedirectAttributes ra) {
		Member member = memberService.get(user.getMemberIdx());
		if (!br.hasErrors()) {
			try {
				memberService.updateProfile(member.getMemberIdx(), form, session);
				ra.addFlashAttribute("toast", "プロフィールを保存しました");
				return "redirect:/mypage";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		return editPage(member, model);
	}

	@PostMapping("/mypage/password")
	public String changePassword(@AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute("passwordForm") PasswordForm form,
			BindingResult br, Model model, RedirectAttributes ra) {
		Member member = memberService.get(user.getMemberIdx());
		if (form.getNewPasswd() != null && !form.getNewPasswd().equals(form.getNewPasswdConfirm())) {
			br.rejectValue("newPasswdConfirm", "mismatch", "パスワードが一致しません");
		}
		if (!br.hasErrors()) {
			try {
				memberService.changePassword(member.getMemberIdx(), form);
				ra.addFlashAttribute("toast", "パスワードを変更しました");
				return "redirect:/mypage";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		model.addAttribute("profileForm", profileOf(member));
		return editPage(member, model);
	}

	@PostMapping("/mypage/withdraw")
	public String withdraw(@AuthenticationPrincipal CustomUserDetails user, @Valid @ModelAttribute("withdrawForm") WithdrawForm form,
			BindingResult br, Model model, Authentication auth, HttpServletRequest request, HttpServletResponse response) {
		Member member = memberService.get(user.getMemberIdx());
		if (!br.hasErrors()) {
			try {
				memberService.withdraw(member.getMemberIdx(), form.getPasswd());
				new SecurityContextLogoutHandler().logout(request, response, auth);
				return "redirect:/";
			} catch (BusinessException e) {
				br.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		model.addAttribute("profileForm", profileOf(member));
		return editPage(member, model);
	}

	private String editPage(Member member, Model model) {
		model.addAttribute("member", member);
		if (!model.containsAttribute("profileForm")) {
			model.addAttribute("profileForm", profileOf(member));
		}
		if (!model.containsAttribute("passwordForm")) {
			model.addAttribute("passwordForm", new PasswordForm());
		}
		if (!model.containsAttribute("withdrawForm")) {
			model.addAttribute("withdrawForm", new WithdrawForm());
		}
		return "member/edit";
	}

	private ProfileForm profileOf(Member member) {
		ProfileForm form = new ProfileForm();
		form.setNickname(member.getNickname());
		form.setEmail(member.getEmail());
		form.setBirthday(member.getBirthday());
		return form;
	}
}
