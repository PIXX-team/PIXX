"use client";

import { useEffect, useState } from "react";
import Image from "next/image";

interface FileType {
  imageUrl: string;
  imageType: "IMAGE" | "VIDEO";
}

interface MainMediaProps {
  file: FileType;
}

export default function MainMedia({ file }: MainMediaProps) {
  const [show, setShow] = useState(false);

  useEffect(() => {
    setShow(false); // 먼저 투명하게
    const timeout = setTimeout(() => {
      setShow(true); // 그리고 자연스럽게 나타남
    }, 50); // 짧은 딜레이로 자연스러운 전환
    return () => clearTimeout(timeout);
  }, [file.imageUrl]); // 파일이 바뀔 때마다 다시 페이드 인

  return (
    <div
      style={{
        width: "96%",
        margin: "0 auto",
        minHeight: "300px",
        position: "relative",
        textAlign: "center",
        transition: "opacity 0.5s ease-in-out",
        opacity: show ? 1 : 0,
      }}
    >
      {file.imageType === "VIDEO" ? (
        <video
          src={file.imageUrl}
          controls
          style={{
            width: "100%",
            height: "auto",
            backgroundColor: "#000",
          }}
        />
      ) : (
        <Image
          src={file.imageUrl}
          alt="피드 미디어"
          width={0}
          height={0}
          sizes="100vw"
          style={{ width: "100%", height: "auto", objectFit: "contain" }}
        />
      )}
    </div>
  );
}
