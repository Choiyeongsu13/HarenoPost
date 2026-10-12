package com.harenopost.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByUserid(String userid);

	boolean existsByUserid(String userid);

	boolean existsByEmail(String email);
}
