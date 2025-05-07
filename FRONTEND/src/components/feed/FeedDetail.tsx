"use client";

import { useParams, useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { useRef, useState } from "react";
import { getFeedDetail } from "../../../app/lib/api/feedApi";
import { FeedDetailResponse } from "../../../app/types/feed";
import FeedInfoModal from "./FeedInfoModal";
import Image from "next/image";
import styles from "./feed-detail.module.css";

export default function FeedDetail() {
  const router = useRouter();
  const { feedId } = useParams<{ feedId: string }>();
  const [isInfoModalOpen, setIsInfoModalOpen] = useState(false);

  const { data, isLoading, isError } = useQuery<FeedDetailResponse>({
    queryKey: ["feedDetail", feedId],
    queryFn: () => getFeedDetail(feedId),
  });

  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFavorite, setIsFavorite] = useState(false);
  const touchStartX = useRef(0);
  const touchEndX = useRef(0);
  const thumbnailListRef = useRef<HTMLDivElement>(null);

  const handleTouchStart = (e: React.TouchEvent) => {
    touchStartX.current = e.changedTouches[0].clientX;
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    touchEndX.current = e.changedTouches[0].clientX;
    handleSwipe();
  };

  const handleSwipe = () => {
    const distance = touchStartX.current - touchEndX.current;
    if (distance > 50) {
      if (data && currentIndex < data.feedList.length - 1) {
        setCurrentIndex((prev) => prev + 1);
      }
    } else if (distance < -50) {
      if (data && currentIndex > 0) {
        setCurrentIndex((prev) => prev - 1);
      }
    }
  };

  // 하단 미리보기 좌우 스크롤
  const scrollThumbnailLeft = () => {
    if (thumbnailListRef.current) {
      thumbnailListRef.current.scrollBy({ left: -100, behavior: "smooth" });
    }
  };

  const scrollThumbnailRight = () => {
    if (thumbnailListRef.current) {
      thumbnailListRef.current.scrollBy({ left: 100, behavior: "smooth" });
    }
  };

  if (isLoading) return <div>로딩 중...</div>;
  if (isError || !data) return <div>피드를 불러오는데 실패했습니다.</div>;

  const currentFile = data.feedList[currentIndex];

  return (
    <div>
      <div className={styles.topBar}>
        <button onClick={() => router.back()}>
          <Image src="/icons/icon-back" alt="뒤로가기" />
        </button>
        <div className={styles.iconButtons}>
          <button onClick={() => setIsInfoModalOpen(true)}>
            <Image src="/icons/icon-info.png" alt="상세정보" />
          </button>
          <button onClick={() => setIsFavorite(!isFavorite)}>
            <Image src={isFavorite ? "/icons/icon-like.png" : "/icons/icon-unlike.png"} alt="즐겨찾기" />
          </button>
          <button>
            <Image src="/icons/icon-download.png" alt="다운로드" />
          </button>
          <button>
            <Image src="/icons/icon-send.png" alt="공유" />
          </button>
        </div>
      </div>

      <div className={styles.mainMedia} onTouchStart={handleTouchStart} onTouchEnd={handleTouchEnd}>
        {currentFile.imageType === "VIDEO" ? (
          <video src={currentFile.imageUrl} controls className={styles.mainMediaContent} />
        ) : (
          <Image src={currentFile.imageUrl} alt="피드 미디어" className={styles.mainMediaContent} />
        )}
      </div>

      {/* 하단 미리보기 */}
      <div className={styles.thumbnailWrapper}>
        <button className={styles.arrowButton} onClick={scrollThumbnailLeft}>
          <Image src="/icons/icon-back-gray.png" alt="왼쪽으로" />
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
                <Image src={file.imageUrl} alt="이미지" className={styles.thumbnailContent} />
              )}
            </div>
          ))}
        </div>
        <button className={styles.arrowButton} onClick={scrollThumbnailRight}>
          <Image src="/icons/icon-next-gray.png" alt="오른쪽으로" />
        </button>
      </div>

      {/* FeedInfoModal 연결 */}
      <FeedInfoModal isOpen={isInfoModalOpen} onClose={() => setIsInfoModalOpen(false)} feedDetail={data} />
    </div>
  );
}
