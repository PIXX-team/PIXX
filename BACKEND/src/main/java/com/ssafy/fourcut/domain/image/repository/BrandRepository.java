package com.ssafy.fourcut.domain.image.repository;

import com.ssafy.fourcut.domain.image.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByBrandName(String brandName);
}
