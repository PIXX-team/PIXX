// components/feed/FeedMainContent.tsx

"use client";

import React, { useRef } from "react";
import Image from "next/image";
import styles from "./feed-main-content.module.css";

type FeedMainContentProps = {
  file: {
    imageType: "IMAGE" | "VIDEO" | "GIF";
    imageUrl: string;
  };
  onSwipeLeft: () => void;
  onSwipeRight: () => void;
};

export default function FeedMainMedia({ file, onSwipeLeft, onSwipeRight }: FeedMainContentProps) {
  const touchStartX = useRef(0);
  const touchEndX = useRef(0);

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
      onSwipeLeft();
    } else if (distance < -50) {
      onSwipeRight();
    }
  };

  return (
    <div className={styles["fixed-aspect-ratio-container"]} onTouchStart={handleTouchStart} onTouchEnd={handleTouchEnd}>
      {file.imageType === "VIDEO" ? (
        <video
          src={file.imageUrl}
          className={styles["fixed-aspect-ratio-video"]}
          autoPlay // 자동재생
          muted // 자동재생
          playsInline
          controls // 조절
        />
      ) : (
        <Image
          src={file.imageUrl}
          alt="피드 미디어"
          className={styles["fixed-aspect-ratio-image"]}
          fill
          sizes="100vw"
        />
      )}
    </div>
  );
}
