"use client";

import { useRouter } from "next/navigation";

export default function KakaoLogin() {
  const router = useRouter();

  const handleKakaoLogin = () => {
    const KAKAO_CLIENT_ID = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID;
    const REDIRECT_URI = process.env.NEXT_PUBLIC_KAKAO_REDIRECT_URI;
    const kakaoURL = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code`;

    router.push(kakaoURL);
  };

  return (
    <button
      onClick={handleKakaoLogin}
      className="w-full py-2 px-4 bg-[#FEE500] text-black rounded-lg hover:bg-[#FDD835] transition-colors"
    >
      카카오로 시작하기
    </button>
  );
}
