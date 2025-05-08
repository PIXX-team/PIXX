"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import styles from "./menubar.module.css";

export default function Menubar() {
  // url의 현재 위치 파악
  const pathname = usePathname();

  // 메뉴 목록
  const menu = [
    { name: "feed", href: "/feed" },
    { name: "album", href: "/album" },
    { name: "people", href: "/people" },
  ];

  return (
    <nav className={styles.nav}>
      {menu.map((item) => (
        <Link
          key={item.href}
          href={item.href}
          className={`${styles.link} ${pathname === item.href ? styles.active : ""}`} // 경로의 메뉴명 active 스타일 적용
        >
          {item.name}
        </Link>
      ))}
    </nav>
  );
}
