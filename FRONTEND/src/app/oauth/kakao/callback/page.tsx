"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function KakaoCallbackPage() {
  const router = useRouter();

  useEffect(() => {
    const code = new URL(window.location.href).searchParams.get("code");

    if (code) {
      fetch("/api/v1/auth/kakao/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        credentials: "include", // 쿠키 수신을 위해 필수
        body: JSON.stringify({ code }),
      })
        .then((response) => {
          if (response.ok) {
            return response.json();
          } else {
            return response.json().then((data) => {
              throw new Error(data.message || "카카오 로그인 실패");
            });
          }
        })
        .then(() => {
          // 토큰은 쿠키에 자동으로 저장되므로 별도 처리 필요 없음
          router.push("/main");
        })
        .catch((error) => {
          console.error("카카오 로그인 요청 실패:", error);
          router.push("/welcome");
        });
    } else {
      router.push("/welcome");
    }
  }, [router]);

  return <div>카카오 로그인 처리 중...</div>;
}
