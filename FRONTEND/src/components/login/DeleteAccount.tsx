"use client";

import { useRouter } from "next/navigation";
import styles from "./delete-account.module.css";

export default function DeleteAccount() {
  const router = useRouter();

  const handleDeleteAccount = () => {
    if (window.confirm("정말로 회원탈퇴 하시겠습니까?")) {
      console.log("회원탈퇴 처리");
      localStorage.removeItem("token");
      router.push("/welcome");
    }
  };

  return (
    <button className={styles.deleteButton} onClick={handleDeleteAccount}>
      회원탈퇴
    </button>
  );
}
