package com.harenopost.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harenopost.entity.Design;

public interface DesignRepository extends JpaRepository<Design, Long> {
}
