package com.ssafy.fourcut.domain.image.repository;

import com.ssafy.fourcut.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Image, Integer> {
}