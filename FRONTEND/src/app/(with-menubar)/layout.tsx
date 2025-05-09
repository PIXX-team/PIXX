// app/(with-menubar)/layout.tsx
"use client";

import { ReactNode } from "react";
import { usePathname } from "next/navigation";
import Menubar from "@/components/Menubar";

export default function Layout({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const isFeedDetailPage = /^\/feed\/[^/]+$/.test(pathname); // '/feed/무엇' 형태인지 검사

  return (
    <div>
      {!isFeedDetailPage && <Menubar />}
      {children}
    </div>
  );
}
