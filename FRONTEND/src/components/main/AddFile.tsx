"use client";
import { useState } from "react";
import axios from "axios";
import styles from "./add-file.module.css";

export default function AddFile() {
  const [files, setFiles] = useState<FileList | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      setFiles(event.target.files);
    }
  };

  const handleUpload = async () => {
    if (!files) return;

    setIsUploading(true);
    const formData = new FormData();

    Array.from(files).forEach((file) => {
      formData.append("files", file);
    });

    try {
      const response = await axios.post("/api/v1/photos/upload/file", formData, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "Content-Type": "multipart/form-data",
        },
      });

      if (response.status === 200) {
        alert("파일이 성공적으로 업로드되었습니다.");
        setFiles(null);
        // 파일 입력 초기화
        const fileInput = document.getElementById("fileInput") as HTMLInputElement;
        if (fileInput) fileInput.value = "";
      }
    } catch (error) {
      alert("파일 업로드에 실패했습니다.");
      console.error("Error:", error);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className={styles.uploadContainer}>
      <label htmlFor="fileInput" className={styles.uploadButton}>
        사진 업로드
      </label>
      <input
        type="file"
        id="fileInput"
        multiple
        accept="image/*, video/mp4"
        onChange={handleFileChange}
        className={styles.fileInput}
      />
      {files && files.length > 0 && (
        <div className={styles.fileList}>
          <p>선택된 파일 ({files.length}개):</p>
          {Array.from(files).map((file, index) => (
            <p key={index} className={styles.fileName}>
              {file.name}
            </p>
          ))}
          <button onClick={handleUpload} disabled={isUploading} className={styles.submitButton}>
            {isUploading ? "업로드 중..." : "업로드하기"}
          </button>
        </div>
      )}
    </div>
  );
}
