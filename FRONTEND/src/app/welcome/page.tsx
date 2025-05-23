"use client";

import { useState } from "react";
import Image from "next/image";
import Logo from "@/../public/icons/main-logo.png";
import KakaoLogin from "@/components/login/KakaoLogin";
import Tutorial from "@/components/main/Tutorial";
import styles from "./page.module.css";

export default function Page() {
  const [showTutorial, setShowTutorial] = useState(false);

  return (
    <>
      <button onClick={() => setShowTutorial(true)} className={styles.tutorialButton} aria-label="튜토리얼 보기">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10-4.477 10-10 10zm0-2a8 8 0 1 0 0-16 8 8 0 0 0 0 16zm-1-5h2v2h-2v-2zm2-1.645V14h-2v-1.5a1 1 0 0 1 1-1 1.5 1.5 0 1 0-1.471-1.794l-1.962-.393A3.5 3.5 0 1 1 13 13.355z"
            fill="white"
          />
        </svg>
      </button>
      <Image src={Logo} alt="PIXX Logo" className={styles.logo} />
      <KakaoLogin />
      {showTutorial && <Tutorial onClose={() => setShowTutorial(false)} type="welcome" />}
    </>
  );
}
