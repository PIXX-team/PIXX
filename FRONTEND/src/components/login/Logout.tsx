"use client";

import { useRouter } from "next/navigation";
import styles from "./logout.module.css";

export default function Logout() {
  const router = useRouter();

  const handleLogout = async () => {
    try {
      const response = await fetch("/api/v1/auth/logout", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        credentials: "include",
      });

      if (response.ok) {
        // 브라우저의 모든 쿠키 삭제
        document.cookie.split(";").forEach((c) => {
          document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });
        router.push("/welcome");
      } else {
        const data = await response.json();
        throw new Error(data.message || "로그아웃 실패");
      }
    } catch (error) {
      console.error("로그아웃 요청 실패:", error);
      alert("로그아웃 처리 중 오류가 발생했습니다.");
    }
  };

  return (
    <button className={styles.logoutButton} onClick={handleLogout}>
      로그아웃
    </button>
  );
}
