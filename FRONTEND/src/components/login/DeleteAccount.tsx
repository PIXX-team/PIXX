"use client";

import { useRouter } from "next/navigation";

export default function DeleteAccount() {
  const router = useRouter();

  const handleDeleteAccount = () => {
    if (window.confirm("정말로 회원탈퇴 하시겠습니까?")) {
      // 회원탈퇴 API 호출
      console.log("회원탈퇴 처리");
      localStorage.removeItem("token");
      router.push("/welcome");
    }
  };

  return (
    <button
      className="w-full py-2 px-4 bg-red-100 text-red-800 rounded-lg hover:bg-red-200 transition-colors"
      onClick={handleDeleteAccount}
    >
      회원탈퇴
    </button>
  );
}
