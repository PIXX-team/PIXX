// components/feed/FeedInfoModal.tsx

"use client";

import { FeedDetailResponse } from "@/app/types/feed";
import { useState } from "react";
import dayjs from "dayjs";
import Image from "next/image";
import styles from "./feed-info-modal.module.css";
import FeedInfoEditModal from "./FeedInfoEditModal";

type FeedInfoModalProps = {
  isOpen: boolean;
  onClose: () => void;
  feedDetail?: FeedDetailResponse;
  feedId: number;
};

export default function FeedInfoModal({ isOpen, onClose, feedDetail, feedId }: FeedInfoModalProps) {
  if (!isOpen) return null;

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);

  return (
    <>
      {/*  피드 상세 정보 모달 */}
      <div className={styles.backdrop} onClick={onClose}>
        <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
          <div className={styles.modalActions}>
            <button className={styles.editButton} onClick={() => setIsEditModalOpen(true)}>
              <Image src="/icons/icon-pencil.png" alt="수정" width={28} height={28} />
            </button>
            <button className={styles.closeButton} onClick={onClose}>
              <Image src="/icons/icon-close.png" alt="닫기" width={28} height={28} />
            </button>
          </div>

          {/* 상세 정보 표시 */}
          <div className={styles.content}>
            {/* 제목 */}
            <h2 className={styles.title}>{feedDetail?.feedTitle}</h2>

            {/* 구분선 */}
            <div className={styles.divider} />

            {/* 해시태그 */}
            <div className={styles.hashtags}>
              {feedDetail?.feedHashtags?.map((tag) => <span key={tag}>#{tag}</span>)}
            </div>

            {/* 날짜/장소/브랜드 */}
            <div className={styles.infoRow}>
              <Image src="/icons/icon-date.png" alt="촬영일" width={20} height={20} />
              {feedDetail?.feedDate ? dayjs(feedDetail.feedDate).format("YYYY.MM.DD") : ""}
            </div>
            <div className={styles.infoRow}>
              <Image src="/icons/icon-location.png" alt="위치" width={20} height={20} /> {feedDetail?.feedLocation}
            </div>
            <div className={styles.infoRow}>
              <Image src="/icons/icon-brand.png" alt="브랜드" width={20} height={20} /> {feedDetail?.brandName}
            </div>

            {/* 메모 */}
            <div className={styles.memo}>
              <h3>Memo</h3>
              <p>{feedDetail?.feedMemo}</p>
            </div>
          </div>
        </div>
      </div>
      {/* 피드 상세 정보 수정 모달 */}
      {isEditModalOpen && (
        <FeedInfoEditModal
          isOpen={isEditModalOpen}
          onClose={() => setIsEditModalOpen(false)}
          feedDetail={feedDetail}
          onSuccess={() => {
            setIsEditModalOpen(false);
            onClose(); // 정보 갱신을 위해 모달 전체 닫고 다시 열도록
          }}
          feedId={feedId}
        />
      )}
    </>
  );
}
