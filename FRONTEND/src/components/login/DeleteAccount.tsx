"use client";

import { useRouter } from "next/navigation";
import styles from "./delete-account.module.css";

export default function DeleteAccount() {
  const router = useRouter();

  const handleDeleteAccount = async () => {
    if (window.confirm("정말로 회원탈퇴 하시겠습니까?")) {
      try {
        const response = await fetch("/api/v1/auth/withdraw", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
          },
          credentials: "include", // 쿠키 전송을 위해 필수
        });

        if (response.ok) {
          // 회원탈퇴 성공 시 쿠키 삭제
          document.cookie.split(";").forEach((c) => {
            document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
          });
          router.push("/welcome");
        } else {
          const data = await response.json();
          throw new Error(data.message || "회원탈퇴 실패");
        }
      } catch (error) {
        console.error("회원탈퇴 요청 실패:", error);
        alert("회원탈퇴 처리 중 오류가 발생했습니다.");
      }
    }
  };

  return (
    <button className={styles.deleteButton} onClick={handleDeleteAccount}>
      회원탈퇴
    </button>
  );
}
