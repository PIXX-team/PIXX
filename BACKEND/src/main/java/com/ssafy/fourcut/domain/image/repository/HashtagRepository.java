package com.ssafy.fourcut.domain.image.repository;

import com.ssafy.fourcut.domain.image.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Integer> {
    Optional<Hashtag> findByHashtagContent(String tag);
}
