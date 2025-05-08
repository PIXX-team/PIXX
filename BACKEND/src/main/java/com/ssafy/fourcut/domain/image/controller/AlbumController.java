// src/main/java/com/ssafy/fourcut/domain/image/controller/AlbumController.java
package com.ssafy.fourcut.domain.image.controller;

import com.ssafy.fourcut.domain.image.dto.*;
import com.ssafy.fourcut.domain.image.service.AlbumService;
import com.ssafy.fourcut.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/album")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    /**
     * POST /api/v1/album
     * body: { "albumTitle":"...", "imageList":[1,3,4] }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateAlbumResponse>> createAlbum(
            Principal principal,
            @RequestBody CreateAlbumRequest req
    ) {
        Integer userId = Integer.valueOf(principal.getName());
        CreateAlbumResponse data = albumService.createAlbum(
                userId,
                req.getAlbumTitle(),
                req.getImageList()
        );
        ApiResponse<CreateAlbumResponse> resp = ApiResponse.<CreateAlbumResponse>builder()
                .status(Integer.parseInt("200"))
                .message("앨범이 성공적으로 생성되었습니다")
                .data(data)
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * PUT /api/v1/album
     * body: { "albumId":…, "albumName":…, "albumMemo":… }
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateAlbum(
            Principal principal,
            @RequestBody UpdateAlbumRequest req
    ) {
        Integer userId = Integer.valueOf(principal.getName());
        albumService.updateAlbum(userId, req);
        ApiResponse<Void> resp = ApiResponse.<Void>builder()
                .status(Integer.parseInt("200"))
                .message("앨범 정보가 성공적으로 수정되었습니다")
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * DELETE /api/v1/album
     * body: { "albumId": 1 }
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(
            Principal principal,
            @RequestBody DeleteAlbumRequest req
    ) {
        Integer userId = Integer.valueOf(principal.getName());
        albumService.deleteAlbum(userId, req.getAlbumId());
        ApiResponse<Void> resp = ApiResponse.<Void>builder()
                .status(Integer.parseInt("200"))
                .message("앨범이 성공적으로 삭제되었습니다")
                .build();
        return ResponseEntity.ok(resp);
    }

    /**
     * POST /api/v1/album/photo
     * body: { "albumId":1, "imageList":[1,3,4] }
     */
    @PostMapping("/photo")
    public ResponseEntity<ApiResponse<Void>> addPhotos(
            Principal principal,
            @RequestBody AddPhotoRequest req
    ) {
        Integer userId = Integer.valueOf(principal.getName());
        albumService.addPhotosToAlbum(userId, req);

        ApiResponse<Void> resp = ApiResponse.<Void>builder()
                .status(Integer.parseInt("200"))
                .message("앨범 사진 추가")
                .build();  // data 필드가 없어도 OK. 내부에서 null 처리됩니다.

        return ResponseEntity.ok(resp);
    }


    /**
     * DELETE /api/v1/album/photo
     * body: { "albumId":1, "imageList":[1,3,4] }
     */
    @DeleteMapping("/photo")
    public ResponseEntity<ApiResponse<Void>> deletePhotos(
            Principal principal,
            @RequestBody DeletePhotoRequest req
    ) {
        Integer userId = Integer.valueOf(principal.getName());
        albumService.deletePhotosFromAlbum(userId, req);
        ApiResponse<Void> resp = ApiResponse.<Void>builder()
                .status(Integer.parseInt("200"))
                .message("앨범 사진 삭제")
                .build();
        return ResponseEntity.ok(resp);
    }
}
