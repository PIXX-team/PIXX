"use client";

export default function Inquiry() {
  return (
    <button
      className="w-full py-2 px-4 bg-gray-100 text-gray-800 rounded-lg hover:bg-gray-200 transition-colors"
      onClick={() => {
        // 1:1 문의 처리 로직
        console.log("1:1 문의 클릭");
      }}
    >
      1:1 문의
    </button>
  );
}
