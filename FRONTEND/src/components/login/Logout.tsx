"use client";

import { useRouter } from "next/navigation";
import styles from "./logout.module.css";

export default function Logout() {
  const router = useRouter();

  const handleLogout = () => {
    localStorage.removeItem("token");
    router.push("/welcome");
  };

  return (
    <button className={styles.logoutButton} onClick={handleLogout}>
      로그아웃
    </button>
  );
}
