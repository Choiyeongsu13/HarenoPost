package com.harenopost.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.harenopost.dto.DesignResponseDTO;
import com.harenopost.dto.Theme;
import com.harenopost.entity.DesignEntity;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.repository.DesignRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DesignService {

	//로그 출력용
	private static final Logger log = LoggerFactory.getLogger(DesignService.class);

	private static final Set<String> EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
	private static final long MAX_SIZE = 2L * 1024 * 1024; //2MB
	private static final int NAME_MAX = 50;

	private final DesignRepository designRepository;

	@Value("${app.upload-dir}")
	private String uploadDir;

	//카드 모달용 디자인 목록 (사용 중, 이 테마 전용 + 공통, 순서대로)
	@Transactional(readOnly = true)
	public List<DesignResponseDTO> designList(Theme theme) {
		return designRepository.designList(theme).stream().map(DesignResponseDTO::new).toList();
	}

	//관리자 목록
	@Transactional(readOnly = true)
	public List<DesignResponseDTO> designAdminList() {
		return designRepository.findAllByOrderBySortOrderAscDesignIdxAsc().stream().map(DesignResponseDTO::new).toList();
	}

	//등록 처리: 외부 경로(app.upload-dir)에 UUID 파일명으로 저장
	@Transactional
	public void designWrite(String name, Theme theme, MultipartFile file) {
		if (name == null || name.isBlank() || name.strip().length() > NAME_MAX) {
			throw new BusinessException(ErrorCode.DESIGN_NAME_INVALID);
		}
		if (file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.FILE_REQUIRED);
		}
		if (file.getSize() > MAX_SIZE) {
			throw new BusinessException(ErrorCode.FILE_TOO_BIG);
		}
		String ext = extension(file.getOriginalFilename());
		String contentType = file.getContentType() == null ? "" : file.getContentType();
		if (!EXTENSIONS.contains(ext) || !contentType.startsWith("image/")) {
			throw new BusinessException(ErrorCode.FILE_TYPE);
		}
		String saveName = UUID.randomUUID() + "." + ext;
		try {
			Path dir = Path.of(uploadDir).toAbsolutePath();
			Files.createDirectories(dir);
			file.transferTo(dir.resolve(saveName));
		} catch (IOException e) {
			log.error("디자인 이미지 저장 실패", e);
			throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
		}
		designRepository.save(DesignEntity.builder()
				.name(name.strip())
				.theme(theme)
				.originName(file.getOriginalFilename())
				.saveName(saveName)
				.build());
	}

	//사용 여부·표시 순서 수정
	@Transactional
	public void designModify(Long designIdx, String useYn, Integer sortOrder) {
		if (!"Y".equals(useYn) && !"N".equals(useYn)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		designFind(designIdx).designModify(useYn, sortOrder == null ? 0 : sortOrder);
	}

	//삭제 처리. 이 디자인을 쓴 카드는 DB 에서 design_idx = NULL (기본 카드 모양)
	@Transactional
	public void designDelete(Long designIdx) {
		DesignEntity designEntity = designFind(designIdx);
		designRepository.delete(designEntity);
		designRepository.flush();
		if (designEntity.getSaveName() != null) {
			try {
				Files.deleteIfExists(Path.of(uploadDir).toAbsolutePath().resolve(designEntity.getSaveName()));
			} catch (IOException e) {
				log.warn("디자인 이미지 파일 삭제 실패: {}", designEntity.getSaveName(), e);
			}
		}
	}

	private DesignEntity designFind(Long designIdx) {
		return designRepository.findById(designIdx)
				.orElseThrow(() -> new BusinessException(ErrorCode.DESIGN_NOT_FOUND));
	}

	//파일 확장자 (소문자)
	private static String extension(String fileName) {
		if (fileName == null || fileName.lastIndexOf('.') < 0) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
	}
}
