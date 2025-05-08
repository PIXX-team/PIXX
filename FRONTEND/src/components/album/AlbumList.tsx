// components/album/AlbumList.tsx

"use client";

import { getAlbums } from "@/app/lib/api/albumApi";
import styles from "./album-list.module.css";
import { useEffect, useRef, useCallback } from "react";
import { useInfiniteQuery } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import Image from "next/image";
import dayjs from "dayjs";

// 실패 시 사용할 Mock 데이터
const mockAlbums = {
  status: "200",
  message: "앨범 불러오기 성공",
  data: {
    albumList: [
      {
        albumId: 1,
        albumName: "Mock 앨범1",
        albumDate: "2025-05-06T10:00:00",
      },
      {
        albumId: 2,
        albumName: "Mock 앨범2",
        albumDate: "2025-05-05T10:00:00",
      },
    ],
  },
};

export default function AlbumList() {
  const router = useRouter();
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadMoreRef = useRef<HTMLDivElement>(null);

  // React Query의 useInfiniteQuery 훅을 사용하여 무한 스크롤 구현
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, status } = useInfiniteQuery({
    queryKey: ["albums"],
    queryFn: async ({ pageParam = 0 }) => {
      try {
        const response = await getAlbums({ type: 0, page: pageParam, size: 20 });
        return response;
      } catch (error) {
        console.error("앨범 불러오기 실패:", error);
        return mockAlbums; // 실패하면 Mock 데이터로 대체
      }
    },
    getNextPageParam: (lastPage, allPages) => {
      // API 응답 형식에 맞게 처리
      // 만약 마지막 페이지의 결과가 20개 미만이면 더 이상 페이지가 없음
      const albumList = lastPage.data?.albumList || [];
      return albumList.length < 20 ? undefined : allPages.length;
    },
    initialPageParam: 0,
  });

  // 무한 스크롤을 위한 IntersectionObserver 설정
  const handleObserver = useCallback(
    (entries: IntersectionObserverEntry[]) => {
      const [target] = entries;
      if (target.isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    [fetchNextPage, hasNextPage, isFetchingNextPage]
  );

  // IntersectionObserver 설정 및 해제
  useEffect(() => {
    const element = loadMoreRef.current;
    if (!element) return;

    observerRef.current = new IntersectionObserver(handleObserver, {
      root: null,
      rootMargin: "0px",
      threshold: 0.1,
    });

    observerRef.current.observe(element);

    return () => {
      if (observerRef.current && element) {
        observerRef.current.unobserve(element);
      }
    };
  }, [handleObserver]);

  // 날짜 형식 변환
  const formatDate = (dateString: string) => {
    return dayjs(dateString).format("YYYY. MM. DD ~");
  };

  if (status === "pending") {
    return <div className={styles["loading-message"]}>앨범을 불러오고 있습니다</div>;
  }

  if (status === "error") {
    return <div className={styles["error-message"]}>오류가 발생했습니다 잠시후 다시 시도해주세요</div>;
  }

  // 모든 페이지의 앨범 데이터를 하나의 배열로 합치기
  const albums = data?.pages.flatMap((page) => page.data?.albumList || []) || [];

  return (
    <div className={styles.albumListWrapper}>
      {albums.map((album, index) => (
        <div key={album.albumId} className={styles.albumItem} onClick={() => router.push(`/album/${album.albumId}`)}>
          <div className={`${styles.albumContent} ${index % 2 === 0 ? styles.leftImage : styles.rightImage}`}>
            {/* 별자리 그림은 나중에 매칭 */}
            <Image
              src="/constellations/aries_1.png"
              alt="별자리"
              className={styles.constellationImage}
              width={100}
              height={100}
            />
            <div className={`${styles.albumInfo} ${index % 2 === 0 ? styles.alignLeft : styles.alignRight}`}>
              <div className={styles.albumName}>{album.albumName}</div>
              <div className={styles.separator} />
              <div className={styles.albumDate}>{formatDate(album.albumDate)}</div>
            </div>
          </div>
        </div>
      ))}

      {/* 무한 스크롤을 위한 로딩 표시기 */}
      <div ref={loadMoreRef} className={styles.loadMoreTrigger}>
        {isFetchingNextPage && <p>앨범을 더 불러오는 중...</p>}
      </div>
    </div>
  );
}
