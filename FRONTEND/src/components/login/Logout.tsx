"use client";

import { useRouter } from "next/navigation";
import styles from "./logout.module.css";
import api from "@/app/lib/api/axios";

export default function Logout() {
  const router = useRouter();

  const handleLogout = async () => {
    try {
      const response = await api.post("/api/v1/auth/logout");

      if (response.data.status === 200) {
        document.cookie.split(";").forEach((c) => {
          document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });
        router.push("/welcome");
      } else {
        throw new Error(response.data.message || "로그아웃 실패");
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
