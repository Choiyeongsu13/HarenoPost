package com.harenopost.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harenopost.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
	//findById() //user_idx 로 검색
	//save(entity) //회원가입, 수정
	//delete(entity) //탈퇴

	//1. userid 이용한 사용자 검색 (로그인)
	Optional<UserEntity> findByUserid(String userid);

	//2. id 중복검사
	boolean existsByUserid(String userid);

	//3. 이메일 중복검사
	boolean existsByEmail(String email);
}
