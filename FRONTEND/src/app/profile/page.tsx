"use client";

import Inquiry from "@/components/login/Inquiry";
import Logout from "@/components/login/Logout";
import DeleteAccount from "@/components/login/DeleteAccount";
import styles from "./page.module.css";

export default function Page() {
  return (
    <div className={styles.container}>
      <div className={styles.buttonContainer}>
        <Inquiry />
        <Logout />
        <DeleteAccount />
      </div>
    </div>
  );
}
