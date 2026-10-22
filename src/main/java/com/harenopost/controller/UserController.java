package com.harenopost.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harenopost.dto.CustomUserDetails;
import com.harenopost.dto.PostResponseDTO;
import com.harenopost.dto.UserModifyRequestDTO;
import com.harenopost.dto.UserRequestDTO;
import com.harenopost.dto.UserResponseDTO;
import com.harenopost.exception.BusinessException;
import com.harenopost.service.MailAuthService;
import com.harenopost.service.PostService;
import com.harenopost.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("User")
public class UserController {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	private final UserService userService;
	private final PostService postService;
	private final MailAuthService mailAuthService;

	//로그인 폼 (로그인 처리는 시큐리티가 한다)
	@GetMapping("user_login")
	public String userLogin() {
		log.info("User Call : user_login");
		return "User/user_login";
	}

	//회원가입 폼
	@GetMapping("user_insert")
	public String userInsert(Model model) {
		log.info("User Call : user_insert");
		model.addAttribute("userRequestDTO", new UserRequestDTO());
		return "User/user_insert";
	}

	//ID 중복검사 ("true" = 이미 사용 중)
	@ResponseBody
	@PostMapping(value = "user_idCheck", produces = "text/plain;charset=UTF-8")
	public String userIdCheck(@RequestParam("userid") String userid) {
		log.info("User Call : user_idCheck");
		boolean bool = userService.userIdCheck(userid);
		return String.valueOf(bool);
	}

	//본인인증(이메일): 인증 코드 발송
	@ResponseBody
	@PostMapping(value = "user_mailSend", produces = "text/plain;charset=UTF-8")
	public String userMailSend(@RequestParam("email") String email, HttpSession session) {
		log.info("User Call : user_mailSend");
		userService.userMailSend(session, email.strip());
		return "認証コードを送信しました（5分間有効）";
	}

	//본인인증(이메일): 인증 코드 확인
	@ResponseBody
	@PostMapping(value = "user_mailCheck", produces = "text/plain;charset=UTF-8")
	public String userMailCheck(@RequestParam("email") String email, @RequestParam("code") String code, HttpSession session) {
		log.info("User Call : user_mailCheck");
		userService.userMailCheck(session, email.strip(), code.strip());
		return "メール認証が完了しました";
	}

	//회원가입 처리
	@PostMapping("user_insert")
	public String userInsertPro(@Valid UserRequestDTO userRequestDTO, BindingResult result, HttpSession session, Model model) {
		log.info("User Call : user_insert_pro");
		if (!result.hasErrors()) {
			try {
				userService.userInsert(userRequestDTO, session);
				return "redirect:/User/user_login?joined";
			} catch (BusinessException e) {
				result.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		userRequestDTO.setPasswd(null);
		userRequestDTO.setPasswdConfirm(null);
		//이미 확인한 ID·인증한 메일이면 다시 하지 않아도 되게 화면에 알려 준다
		model.addAttribute("idChecked", !result.hasFieldErrors("userid")
				&& !userService.userIdCheck(userRequestDTO.getUserid()));
		model.addAttribute("mailVerified", mailAuthService.isVerified(session, userRequestDTO.getEmail()));
		return "User/user_insert";
	}

	//마이페이지
	@GetMapping("user_mypage")
	public String userMypage(@AuthenticationPrincipal CustomUserDetails login, Model model) {
		log.info("User Call : user_mypage");
		UserResponseDTO user = userService.userView(login.getUserIdx());
		List<PostResponseDTO> pList = postService.postList(user.getUserIdx());
		model.addAttribute("user", user);
		model.addAttribute("pList", pList);
		model.addAttribute("postCount", pList.size());
		model.addAttribute("cardCount", pList.stream().mapToLong(PostResponseDTO::getCardCount).sum());
		return "User/user_mypage";
	}

	//프로필 수정 폼
	@GetMapping("user_modify")
	public String userModify(@AuthenticationPrincipal CustomUserDetails login, Model model) {
		log.info("User Call : user_modify");
		UserResponseDTO user = userService.userView(login.getUserIdx());
		model.addAttribute("user", user);
		model.addAttribute("userModifyRequestDTO", new UserModifyRequestDTO(user));
		return "User/user_modify";
	}

	//프로필 수정 처리
	@PostMapping("user_modify")
	public String userModifyPro(@AuthenticationPrincipal CustomUserDetails login, @Valid UserModifyRequestDTO userModifyRequestDTO,
			BindingResult result, HttpSession session, Model model, RedirectAttributes ra) {
		log.info("User Call : user_modify_pro");
		if (!result.hasErrors()) {
			try {
				userService.userModify(login.getUserIdx(), userModifyRequestDTO, session);
				ra.addFlashAttribute("toast", "プロフィールを保存しました");
				return "redirect:/User/user_mypage";
			} catch (BusinessException e) {
				result.reject(e.getErrorCode().name(), e.getMessage());
			}
		}
		model.addAttribute("user", userService.userView(login.getUserIdx()));
		return "User/user_modify";
	}

	//비밀번호 변경 처리
	@PostMapping("user_passwd")
	public String userPasswdPro(@AuthenticationPrincipal CustomUserDetails login,
			@RequestParam("currentPasswd") String currentPasswd, @RequestParam("newPasswd") String newPasswd,
			@RequestParam("newPasswdConfirm") String newPasswdConfirm, Model model, RedirectAttributes ra) {
		log.info("User Call : user_passwd_pro");
		try {
			userService.userPasswd(login.getUserIdx(), currentPasswd, newPasswd, newPasswdConfirm);
			ra.addFlashAttribute("toast", "パスワードを変更しました");
			return "redirect:/User/user_mypage";
		} catch (BusinessException e) {
			model.addAttribute("passwdError", e.getMessage());
		}
		return modifyPage(login, model);
	}

	//탈퇴 처리
	@PostMapping("user_delete")
	public String userDeletePro(@AuthenticationPrincipal CustomUserDetails login, @RequestParam("passwd") String passwd,
			Model model, Authentication auth, HttpServletRequest request, HttpServletResponse response) {
		log.info("User Call : user_delete_pro");
		try {
			userService.userDelete(login.getUserIdx(), passwd);
			new SecurityContextLogoutHandler().logout(request, response, auth); //로그아웃
			return "redirect:/";
		} catch (BusinessException e) {
			model.addAttribute("deleteError", e.getMessage());
		}
		return modifyPage(login, model);
	}

	//비밀번호 변경·탈퇴 실패 시 수정 폼을 다시 보여 준다
	private String modifyPage(CustomUserDetails login, Model model) {
		UserResponseDTO user = userService.userView(login.getUserIdx());
		model.addAttribute("user", user);
		model.addAttribute("userModifyRequestDTO", new UserModifyRequestDTO(user));
		return "User/user_modify";
	}
}
