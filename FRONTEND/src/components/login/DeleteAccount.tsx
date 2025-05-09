"use client";

import { useRouter } from "next/navigation";
import styles from "./delete-account.module.css";
import api from "@/app/lib/api/axios";

export default function DeleteAccount() {
  const router = useRouter();

  const handleDeleteAccount = async () => {
    if (window.confirm("정말로 회원탈퇴 하시겠습니까?")) {
      try {
        const response = await api.post(
          "/api/v1/auth/withdraw",
          {},
          {
            headers: {
              "Content-Type": "application/json",
              Accept: "application/json",
            },
          }
        );

        if (response.data.status === 200) {
          router.push("/welcome");
        } else {
          throw new Error(response.data.message || "회원탈퇴 실패");
        }
      } catch (error: any) {
        console.error("회원탈퇴 요청 실패:", error);
        const errorMessage = error.response?.data?.message || "회원탈퇴 처리 중 오류가 발생했습니다.";
        alert(errorMessage);
      }
    }
  };

  return (
    <button className={styles.deleteButton} onClick={handleDeleteAccount}>
      회원탈퇴
    </button>
  );
}
