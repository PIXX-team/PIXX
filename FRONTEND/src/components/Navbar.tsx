"use client";
import Link from "next/link";
import styles from "./navbar.module.css";

export default function Navbar() {
  return (
    <nav className={styles.navbar}>
      <Link href="/feed" className={styles.navItem}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className={styles.icon}
        >
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <circle cx="8.5" cy="8.5" r="1.5" />
          <path d="M20.4 14.5L16 10 4 20" />
        </svg>
        <span>Photo</span>
      </Link>

      <Link href="/" className={styles.navItem}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.1"
          strokeLinecap="round"
          strokeLinejoin="round"
          className={styles.plusIcon}
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="16" />
          <line x1="8" y1="12" x2="16" y2="12" />
        </svg>
      </Link>

      <Link href="/profile" className={styles.navItem}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className={styles.icon}
        >
          <circle cx="12" cy="12" r="10" />
          <path d="M5.52 19.346C7.2 16.63 9.47 15 12 15c2.53 0 4.8 1.63 6.48 4.346" />
          <circle cx="12" cy="8" r="3" />
        </svg>
        <span>Profile</span>
      </Link>
    </nav>
  );
}
