"use client";

import { useRouter } from "next/navigation";

export default function Logout() {
  const router = useRouter();

  const handleLogout = () => {
    localStorage.removeItem("token");
    router.push("/welcome");
  };

  return (
    <button
      className="w-full py-2 px-4 bg-gray-100 text-gray-800 rounded-lg hover:bg-gray-200 transition-colors"
      onClick={handleLogout}
    >
      로그아웃
    </button>
  );
}
