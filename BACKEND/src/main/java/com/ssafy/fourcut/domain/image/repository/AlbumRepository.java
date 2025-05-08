package com.ssafy.fourcut.domain.image.repository;

import com.ssafy.fourcut.domain.image.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Integer> {
    // 사용자별 앨범 전체 조회
    Page<Album> findByUserUserId(Integer userId, Pageable pageable);

    // userId와 defaultAlbum이 true인 경우 조회
    Optional<Album> findByUser_UserIdAndDefaultAlbumTrue(int userId);

    Optional<Album> findByUserUserIdAndDefaultAlbumTrue(Integer userId);
    Optional<Album> findByUserUserIdAndFavoriteAlbumTrue(Integer userId);
}
