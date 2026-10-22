package com.harenopost.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harenopost.dto.Theme;
import com.harenopost.entity.ReportEntity;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.service.CardService;
import com.harenopost.service.DesignService;
import com.harenopost.service.ReportService;

import lombok.RequiredArgsConstructor;

//관리자 (ROLE_ADMIN 만, SecurityConfig 에서 /Admin/** 제한)
@Controller
@RequiredArgsConstructor
@RequestMapping("Admin")
public class AdminController {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(AdminController.class);

	private final ReportService reportService;
	private final CardService cardService;
	private final DesignService designService;

	//관리자 첫 화면 → 신고 목록
	@GetMapping("")
	public String admin() {
		return "redirect:/Admin/admin_reports";
	}

	//신고 목록 (WAIT / DONE)
	@GetMapping("admin_reports")
	public String adminReports(@RequestParam(value = "status", defaultValue = ReportEntity.WAIT) String status, Model model) {
		log.info("Admin Call : admin_reports");
		String st = ReportEntity.DONE.equals(status) ? ReportEntity.DONE : ReportEntity.WAIT;
		model.addAttribute("rList", reportService.reportList(st));
		model.addAttribute("status", st);
		return "Admin/admin_reports";
	}

	//신고 처리 완료 (ajax)
	@ResponseBody
	@PostMapping(value = "admin_reportDone", produces = "text/plain;charset=UTF-8")
	public String adminReportDone(@RequestParam("reportIdx") Long reportIdx) {
		log.info("Admin Call : admin_reportDone");
		reportService.reportDone(reportIdx);
		return "処理済みにしました";
	}

	//신고된 카드 삭제 (ajax). 신고도 함께 지워진다
	@ResponseBody
	@PostMapping(value = "admin_cardDelete", produces = "text/plain;charset=UTF-8")
	public String adminCardDelete(@RequestParam("cardIdx") Long cardIdx) {
		log.info("Admin Call : admin_cardDelete");
		cardService.cardDelete(cardIdx);
		return "カードを削除しました";
	}

	//카드 디자인 목록 + 등록 폼
	@GetMapping("admin_designs")
	public String adminDesigns(Model model) {
		log.info("Admin Call : admin_designs");
		model.addAttribute("dList", designService.designAdminList());
		model.addAttribute("themes", Theme.values());
		return "Admin/admin_designs";
	}

	//카드 디자인 등록 처리 (multipart)
	@PostMapping("admin_designs")
	public String adminDesignsPro(@RequestParam("name") String name,
			@RequestParam(value = "theme", required = false) String theme,
			@RequestParam(value = "file", required = false) MultipartFile file, RedirectAttributes ra) {
		log.info("Admin Call : admin_designs_pro");
		try {
			designService.designWrite(name, toTheme(theme), file);
			ra.addFlashAttribute("toast", "デザインを登録しました");
		} catch (BusinessException e) {
			ra.addFlashAttribute("toast", e.getMessage());
		}
		return "redirect:/Admin/admin_designs";
	}

	//사용 여부·순서 수정 (ajax)
	@ResponseBody
	@PostMapping(value = "admin_designModify", produces = "text/plain;charset=UTF-8")
	public String adminDesignModify(@RequestParam("designIdx") Long designIdx, @RequestParam("useYn") String useYn,
			@RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
		log.info("Admin Call : admin_designModify");
		designService.designModify(designIdx, useYn, sortOrder);
		return "保存しました";
	}

	//디자인 삭제 (ajax)
	@ResponseBody
	@PostMapping(value = "admin_designDelete", produces = "text/plain;charset=UTF-8")
	public String adminDesignDelete(@RequestParam("designIdx") Long designIdx) {
		log.info("Admin Call : admin_designDelete");
		designService.designDelete(designIdx);
		return "削除しました";
	}

	//"" 는 공통(null), 그 밖은 테마 코드
	private Theme toTheme(String theme) {
		if (theme == null || theme.isBlank()) {
			return null;
		}
		try {
			return Theme.valueOf(theme);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}
}
