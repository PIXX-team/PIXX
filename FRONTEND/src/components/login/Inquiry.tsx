"use client";

import styles from "./inquiry.module.css";

export default function Inquiry() {
  return (
    <button
      className={styles.inquiryButton}
      onClick={() => {
        console.log("1:1 문의 클릭");
      }}
    >
      1:1 문의
    </button>
  );
}
