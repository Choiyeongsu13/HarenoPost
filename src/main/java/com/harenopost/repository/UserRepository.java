package com.harenopost.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harenopost.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUserid(String userid);

	boolean existsByUserid(String userid);

	boolean existsByEmail(String email);
}
