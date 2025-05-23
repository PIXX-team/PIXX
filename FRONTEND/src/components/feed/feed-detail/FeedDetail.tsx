"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import { getFeedDetail, toggleFavorite } from "@/app/lib/api/feedApi";
import { FeedDetailResponse } from "@/app/types/feed";

import FeedMainMedia from "./FeedMainMedia";
import FeedInfoModal from "./FeedInfoModal";
import Image from "next/image";
import styles from "./feed-detail.module.css";

type FeedDetailProps = {
  feedId: number;
};

export default function FeedDetail({ feedId }: FeedDetailProps) {
  const router = useRouter();
  const queryClient = useQueryClient();

  const [isInfoModalOpen, setIsInfoModalOpen] = useState(false);

  // API 호출
  const { data, isLoading, isError } = useQuery<FeedDetailResponse>({
    queryKey: ["feedDetail", feedId],
    queryFn: () => getFeedDetail(feedId),
  });

  // 현재 이미지 인덱스 관리
  const [currentIndex, setCurrentIndex] = useState(0);
  const currentFile = data?.feedList[currentIndex];

  const [isFavorite, setIsFavorite] = useState(false);
  const thumbnailListRef = useRef<HTMLDivElement>(null);

  // 하단 미리보기 좌우 < > 아이콘
  const scrollThumbnailLeft = () => {
    if (currentIndex > 0) {
      setCurrentIndex((prev) => prev - 1);
    }
  };

  const scrollThumbnailRight = () => {
    if (data && currentIndex < data.feedList.length - 1) {
      setCurrentIndex((prev) => prev + 1);
    }
  };

  // 피드 즐겨찾기
  const favoriteMutation = useMutation({
    mutationFn: () => toggleFavorite(feedId),
    onSuccess: () => {
      // 캐시 무효화
      queryClient.invalidateQueries({ queryKey: ["feedDetail", feedId] }); // 피드 상세
      queryClient.invalidateQueries({ queryKey: ["albumFeeds", 2, "recent"] }); // 즐겨찾기 앨범
      queryClient.invalidateQueries({
        predicate: (query) => query.queryKey[0] === "feeds", // 피드 목록 전체 무효화
      });
    },
  });

  const handleFavorite = () => {
    favoriteMutation.mutate(); // async/await 대신 react-query 방식
  };

  useEffect(() => {
    if (data) {
      setIsFavorite(data.feedFavorite);
    }
  }, [data]);

  if (isLoading) return <div>로딩 중...</div>;
  if (isError || !data)
    return (
      <div>
        피드를 불러오는데 <br /> 실패했습니다.
      </div>
    );

  return (
    <div>
      <div className={styles.topBar}>
        <button onClick={() => router.back()}>
          <Image src="/icons/icon-back.png" alt="뒤로가기" width={28} height={28} />
        </button>
        <div className={styles.iconButtons}>
          <button onClick={() => setIsInfoModalOpen(true)}>
            <Image src="/icons/icon-info.png" alt="상세정보" width={26} height={26} />
          </button>
          {/* <button onClick={() => setIsFavorite(!isFavorite)} > */}
          <button onClick={handleFavorite}>
            <Image
              src={isFavorite ? "/icons/icon-star-fill-yellow.png" : "/icons/icon-star-empty-white.png"}
              alt="즐겨찾기"
              width={28}
              height={28}
            />
          </button>
          {/* <button onClick={() => currentFile && handleDownload(currentFile.imageId)}> */}
          {/* <button>
            <Image src="/icons/icon-download.png" alt="다운로드" width={22} height={22} />
          </button>
          <button onClick={handleUnfinished}>
            <Image src="/icons/icon-send.png" alt="공유" width={22} height={20} />
          </button> */}
        </div>
      </div>

      {currentFile && (
        <FeedMainMedia
          file={currentFile}
          onSwipeLeft={() => {
            if (currentIndex < (data?.feedList.length || 0) - 1) {
              setCurrentIndex((prev) => prev + 1);
            }
          }}
          onSwipeRight={() => {
            if (currentIndex > 0) {
              setCurrentIndex((prev) => prev - 1);
            }
          }}
        />
      )}

      {/* 하단 미리보기 */}
      <div className={styles.thumbnailWrapper}>
        <button className={styles.arrowButton} onClick={scrollThumbnailLeft}>
          <Image src="/icons/icon-back-gray.png" alt="왼쪽으로" width={20} height={20} />
        </button>
        <div className={styles.thumbnailList} ref={thumbnailListRef}>
          {data.feedList.map((file, idx) => (
            <div
              key={file.imageId}
              className={`${styles.thumbnailItem} ${currentIndex === idx ? styles.thumbnailSelected : ""}`}
              onClick={() => setCurrentIndex(idx)}
            >
              {file.imageType === "VIDEO" ? (
                <video src={file.imageUrl} className={styles.thumbnailContent} />
              ) : (
                // eslint-disable-next-line @next/next/no-img-element
                <Image src={file.imageUrl} alt="이미지" className={styles.thumbnailContent} fill />
              )}
            </div>
          ))}
        </div>
        <button className={styles.arrowButton} onClick={scrollThumbnailRight}>
          <Image src="/icons/icon-next-gray.png" alt="오른쪽으로" width={20} height={20} />
        </button>
      </div>

      {/* FeedInfoModal 연결 */}
      <FeedInfoModal
        isOpen={isInfoModalOpen}
        onClose={() => setIsInfoModalOpen(false)}
        feedDetail={data}
        feedId={feedId}
      />
    </div>
  );
}
