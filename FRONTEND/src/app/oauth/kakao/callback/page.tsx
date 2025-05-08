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
        body: JSON.stringify({ code }),
      })
        .then((response) => {
          if (response.ok) {
            return response.json(); // 응답 데이터 파싱 추가
          } else {
            return response.json().then((data) => {
              throw new Error(data.message || "카카오 로그인 실패");
            });
          }
        })
        .then((data) => {
          // 토큰 저장
          localStorage.setItem("token", data.data.accessToken);
          // 메인 페이지로 이동
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
