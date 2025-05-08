// components/feed/FeedInfoModal.tsx

"use client";

import { FeedDetailResponse } from "@/app/types/feed";
import Image from "next/image";
import styles from "./feed-info-modal.module.css";

type FeedInfoModalProps = {
  isOpen: boolean;
  onClose: () => void;
  feedDetail?: FeedDetailResponse;
};

export default function FeedInfoModal({ isOpen, onClose, feedDetail }: FeedInfoModalProps) {
  if (!isOpen) return null;

  return (
    <div className={styles.backdrop} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <button className={styles.closeButton} onClick={onClose}>
          <Image src="/icons/icon-close.png" alt="닫기" />
        </button>

        {/* 상세 정보 표시 */}
        <div className={styles.content}>
          {/* 제목 */}
          <h2 className={styles.title}>{feedDetail?.feedTitle}</h2>

          {/* 해시태그 */}
          <div className={styles.hashtags}>{feedDetail?.feedHashtags?.map((tag) => <span key={tag}>#{tag}</span>)}</div>

          {/* 날짜/장소/브랜드 */}
          <div className={styles.infoRow}>
            <Image src="/icons/icon-date.png" alt="촬영일" />
            {feedDetail?.feedDate}
          </div>
          <div className={styles.infoRow}>
            <Image src="/icons/icon-location.png" alt="위치" /> {feedDetail?.feedLocation}
          </div>
          <div className={styles.infoRow}>
            <Image src="/icons/icon-brand.png" alt="브랜드" /> {feedDetail?.brandName}
          </div>

          {/* 메모 */}
          <div className={styles.memo}>
            <h3>Memo</h3>
            <p>{feedDetail?.feedMemo}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
