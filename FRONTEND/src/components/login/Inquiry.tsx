"use client";

import styles from "./inquiry.module.css";

export default function Inquiry() {
  const handleInquiry = () => {
    window.open("https://forms.gle/t2ZB2NkXGcTBGd7u8", "_blank", "noopener,noreferrer");
  };

  return (
    <button className={styles.inquiryButton} onClick={handleInquiry}>
      1:1 문의
    </button>
  );
}
