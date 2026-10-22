package com.harenopost.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.dto.Deco;
import com.harenopost.dto.PostRequestDTO;
import com.harenopost.dto.PostResponseDTO;
import com.harenopost.dto.Theme;
import com.harenopost.dto.UserResponseDTO;
import com.harenopost.entity.PostEntity;
import com.harenopost.exception.BusinessException;
import com.harenopost.exception.ErrorCode;
import com.harenopost.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private static final int MAX_ACTIVE_POSTS = 5; //受付中 포스트 최대 개수
	private static final int NAME_TAG_MAX = 8; //이름표 글자 수

	private final PostRepository postRepository;
	private final CardService cardService;

	//등록 처리. 등록된 공유 코드를 반환
	@Transactional
	public String postWrite(UserResponseDTO user, PostRequestDTO postRequestDTO) {
		LocalDateTime now = LocalDateTime.now();
		if (postRepository.countByUserIdxAndOpenDateAfter(user.getUserIdx(), now) >= MAX_ACTIVE_POSTS) {
			throw new BusinessException(ErrorCode.POST_LIMIT);
		}
		LocalDateTime openDate = resolveOpenDate(postRequestDTO, now);
		PostEntity postEntity = postRequestDTO.toEntity(newCode(), user.getUserIdx(), openDate,
				cut(user.getNickname(), NAME_TAG_MAX));
		return postRepository.save(postEntity).getCode();
	}

	//내 포스트 목록 (마이페이지, 최신순, 카드 수 포함)
	@Transactional(readOnly = true)
	public List<PostResponseDTO> postList(Long userIdx) {
		List<PostEntity> posts = postRepository.findByUserIdxOrderByRegdateDesc(userIdx);
		Map<Long, Long> counts = cardService.cardCountList(posts.stream().map(PostEntity::getPostIdx).toList());
		return posts.stream()
				.map(post -> new PostResponseDTO(post, userIdx, counts.getOrDefault(post.getPostIdx(), 0L)))
				.toList();
	}

	//상세보기 (포스트 화면, 누구나)
	@Transactional(readOnly = true)
	public PostResponseDTO postView(String code, Long loginIdx) {
		PostEntity postEntity = postFind(code);
		return new PostResponseDTO(postEntity, loginIdx, cardService.cardCount(postEntity.getPostIdx()));
	}

	//주인만 볼 수 있는 화면 (완료 화면)
	@Transactional(readOnly = true)
	public PostResponseDTO postOwnerView(String code, Long loginIdx) {
		return new PostResponseDTO(postOwnerFind(code, loginIdx), loginIdx, 0);
	}

	//꾸미기 폼, 수정 폼 (주인, 개봉 전)
	@Transactional(readOnly = true)
	public PostResponseDTO postModify(String code, Long loginIdx) {
		return new PostResponseDTO(postBeforeOpenFind(code, loginIdx), loginIdx, 0);
	}

	//꾸미기 처리 (테마·중복 검사는 엔티티에서)
	@Transactional
	public PostResponseDTO postDecorate(String code, Long loginIdx, List<String> decos, String nameTag) {
		PostEntity postEntity = postBeforeOpenFind(code, loginIdx);
		String tag = (nameTag == null || nameTag.isBlank()) ? null : nameTag.strip();
		if (tag != null && tag.codePointCount(0, tag.length()) > NAME_TAG_MAX) {
			throw new BusinessException(ErrorCode.NAME_TAG_TOO_LONG);
		}
		List<Deco> decoList = (decos == null) ? List.of() : decos.stream().map(Deco::fromCode).toList();
		postEntity.postDecorate(decoList, tag);
		return new PostResponseDTO(postEntity, loginIdx, 0);
	}

	//수정 처리 (이름·기념일·개봉일)
	@Transactional
	public void postModifyPro(String code, Long loginIdx, PostRequestDTO postRequestDTO) {
		PostEntity postEntity = postBeforeOpenFind(code, loginIdx);
		postEntity.postModify(postRequestDTO.getTitle().strip(), postRequestDTO.getTheme(),
				resolveOpenDate(postRequestDTO, LocalDateTime.now()));
	}

	//삭제 처리. 카드·신고는 DB 의 ON DELETE CASCADE 로 함께 지워진다
	@Transactional
	public void postDelete(String code, Long loginIdx) {
		postRepository.delete(postOwnerFind(code, loginIdx));
	}

	//만들기 화면의 테마별 개봉일 기본값 (誕生日 은 생일이 없으면 null)
	public LocalDateTime defaultOpenDate(Theme theme, LocalDate birthday) {
		return theme.nextOpenDate(LocalDateTime.now(), birthday);
	}

	//개봉일 결정. お正月·クリスマス 는 입력값을 무시하고 서버가 계산한다.
	//誕生日 은 현재 < 개봉일 ≤ 현재 + 1년
	LocalDateTime resolveOpenDate(PostRequestDTO postRequestDTO, LocalDateTime now) {
		Theme theme = postRequestDTO.getTheme();
		if (theme.isFixed()) {
			return theme.nextOpenDate(now, null);
		}
		if (postRequestDTO.getOpenDate() == null) {
			throw new BusinessException(ErrorCode.OPEN_DATE_REQUIRED);
		}
		LocalTime time = postRequestDTO.getOpenTime() == null ? LocalTime.MIDNIGHT : postRequestDTO.getOpenTime();
		LocalDateTime openDate = LocalDateTime.of(postRequestDTO.getOpenDate(), time);
		if (!openDate.isAfter(now)) {
			throw new BusinessException(ErrorCode.OPEN_DATE_PAST);
		}
		if (openDate.isAfter(now.plusYears(1))) {
			throw new BusinessException(ErrorCode.OPEN_DATE_TOO_FAR);
		}
		return openDate;
	}

	//공유 코드로 찾기 (없으면 404)
	private PostEntity postFind(String code) {
		return postRepository.findByCode(code)
				.orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}

	//주인만 (아니면 403)
	private PostEntity postOwnerFind(String code, Long loginIdx) {
		PostEntity postEntity = postFind(code);
		if (!postEntity.isOwner(loginIdx)) {
			throw new BusinessException(ErrorCode.NOT_OWNER);
		}
		return postEntity;
	}

	//주인만, 개봉 전만 (꾸미기·설정)
	private PostEntity postBeforeOpenFind(String code, Long loginIdx) {
		PostEntity postEntity = postOwnerFind(code, loginIdx);
		if (postEntity.isOpened()) {
			throw new BusinessException(ErrorCode.ALREADY_OPENED);
		}
		return postEntity;
	}

	//공유용 8자 코드. 중복이면 다시 만든다
	private String newCode() {
		String code;
		do {
			code = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		} while (postRepository.existsByCode(code));
		return code;
	}

	//앞에서 max 글자 (이모지 같은 서로게이트 문자도 한 글자로)
	private static String cut(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.codePoints().limit(max).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
	}
}
