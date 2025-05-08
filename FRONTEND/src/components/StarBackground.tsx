"use client";

import { useEffect, useState } from "react";
import styles from "./star-background.module.css";

export default function StarBackground() {
  const [stars, setStars] = useState<{ id: number; size: number; top: number; left: number; delay: number }[]>([]);

  // 별 생성 함수
  useEffect(() => {
    const createStars = () => {
      const newStars = [];
      const starCount = 30; // 별의 개수 (적은 수로 유지)

      for (let i = 0; i < starCount; i++) {
        newStars.push({
          id: i,
          size: Math.random() * 3 + 1, // 1-3px 크기의 작은 별
          top: Math.random() * 100, // 화면 전체에 랜덤하게 배치
          left: Math.random() * 100,
          delay: Math.random() * 3, // 애니메이션 딜레이
        });
      }

      setStars(newStars);
    };

    createStars();
  }, []);

  return (
    <div className={styles.starsBackground}>
      {/* 별 배경 추가 */}
      {stars.map((star) => (
        <div
          key={star.id}
          className={styles.star}
          style={{
            width: `${star.size}px`,
            height: `${star.size}px`,
            top: `${star.top}%`,
            left: `${star.left}%`,
            animationDelay: `${star.delay}s`,
          }}
        />
      ))}
    </div>
  );
}
