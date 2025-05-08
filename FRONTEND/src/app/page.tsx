"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function Page() {
  const router = useRouter();
  const token = typeof window !== "undefined" ? localStorage.getItem("token") : null;

  useEffect(() => {
    if (token) {
      router.push("/main");
    } else {
      router.push("/welcome");
    }
  }, [router, token]);

  return null; // 리다이렉트 되는 동안 아무것도 표시하지 않음
}
