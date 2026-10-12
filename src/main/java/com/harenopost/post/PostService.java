package com.harenopost.post;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harenopost.card.CardService;
import com.harenopost.common.BusinessException;
import com.harenopost.common.ErrorCode;
import com.harenopost.member.Member;
import com.harenopost.post.dto.DecorateRequest;
import com.harenopost.post.dto.MyPostDto;
import com.harenopost.post.dto.PostForm;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	/** 受付中 포스트 최대 개수 */
	private static final int MAX_ACTIVE_POSTS = 5;
	private static final int NAME_TAG_MAX = 8;

	private final PostRepository postRepository;
	private final CardService cardService;

	// ---------- 조회 ----------

	public Post getByCode(String code) {
		return postRepository.findByCode(code).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
	}

	/** 주인만 */
	public Post getOwned(String code, Long loginIdx) {
		Post post = getByCode(code);
		if (!post.isOwner(loginIdx)) {
			throw new BusinessException(ErrorCode.NOT_OWNER);
		}
		return post;
	}

	/** 주인만, 개봉 전만 (꾸미기·설정) */
	public Post getOwnedBeforeOpen(String code, Long loginIdx) {
		Post post = getOwned(code, loginIdx);
		if (post.isOpened()) {
			throw new BusinessException(ErrorCode.ALREADY_OPENED);
		}
		return post;
	}

	public List<MyPostDto> getMyPosts(Long memberIdx) {
		List<Post> posts = postRepository.findByMemberIdxOrderByRegdateDesc(memberIdx);
		Map<Long, Long> counts = cardService.countByPosts(posts.stream().map(Post::getPostIdx).toList());
		List<MyPostDto> list = new ArrayList<>();
		for (Post p : posts) {
			list.add(new MyPostDto(p.getCode(), p.getTitle(), p.getTheme(), p.getOpenDate(),
					counts.getOrDefault(p.getPostIdx(), 0L), p.isOpened()));
		}
		return list;
	}

	/** 만들기 화면의 테마별 개봉일 기본값 (誕生日 은 생일이 없으면 null) */
	public LocalDateTime defaultOpenDate(Theme theme, LocalDate birthday) {
		return theme.nextOpenDate(LocalDateTime.now(), birthday);
	}

	// ---------- 만들기 ----------

	@Transactional
	public Post create(Member member, PostForm form) {
		LocalDateTime now = LocalDateTime.now();
		if (postRepository.countByMemberIdxAndOpenDateAfter(member.getMemberIdx(), now) >= MAX_ACTIVE_POSTS) {
			throw new BusinessException(ErrorCode.POST_LIMIT);
		}
		LocalDateTime openDate = resolveOpenDate(form, now);
		Post post = Post.create(newCode(), member.getMemberIdx(), form.getTitle().strip(), form.getTheme(), openDate,
				cut(member.getNickname(), NAME_TAG_MAX));
		return postRepository.save(post);
	}

	// ---------- 꾸미기 ----------

	@Transactional
	public Post decorate(String code, Long loginIdx, DecorateRequest req) {
		Post post = getOwnedBeforeOpen(code, loginIdx);
		List<Deco> decos = req.decos().stream().map(Deco::fromCode).toList();
		String nameTag = req.nameTag() == null || req.nameTag().isBlank() ? null : req.nameTag().strip();
		post.decorate(decos, nameTag); // 테마·중복 검사는 엔티티에서
		return post;
	}

	// ---------- 설정 · 삭제 ----------

	@Transactional
	public Post edit(String code, Long loginIdx, PostForm form) {
		Post post = getOwnedBeforeOpen(code, loginIdx);
		post.changeSettings(form.getTitle().strip(), form.getTheme(), resolveOpenDate(form, LocalDateTime.now()));
		return post;
	}

	/** 삭제. 카드·신고는 DB 의 ON DELETE CASCADE 로 함께 지워진다 */
	@Transactional
	public void delete(String code, Long loginIdx) {
		Post post = getOwned(code, loginIdx);
		postRepository.delete(post);
	}

	// ---------- 내부 ----------

	/**
	 * 개봉일 결정. お正月·クリスマス 는 입력값을 무시하고 서버가 계산한다.
	 * 誕生日 은 현재 < 개봉일 ≤ 현재 + 1년.
	 */
	LocalDateTime resolveOpenDate(PostForm form, LocalDateTime now) {
		Theme theme = form.getTheme();
		if (theme.isFixed()) {
			return theme.nextOpenDate(now, null);
		}
		if (form.getOpenDate() == null) {
			throw new BusinessException(ErrorCode.OPEN_DATE_REQUIRED);
		}
		LocalTime time = form.getOpenTime() == null ? LocalTime.MIDNIGHT : form.getOpenTime();
		LocalDateTime openDate = LocalDateTime.of(form.getOpenDate(), time);
		if (!openDate.isAfter(now)) {
			throw new BusinessException(ErrorCode.OPEN_DATE_PAST);
		}
		if (openDate.isAfter(now.plusYears(1))) {
			throw new BusinessException(ErrorCode.OPEN_DATE_TOO_FAR);
		}
		return openDate;
	}

	/** 공유용 8자 코드. 중복이면 다시 만든다 */
	private String newCode() {
		String code;
		do {
			code = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		} while (postRepository.existsByCode(code));
		return code;
	}

	/** 앞에서 max 글자 (이모지 같은 서로게이트 문자도 한 글자로) */
	private static String cut(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.codePoints().limit(max).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
	}
}
