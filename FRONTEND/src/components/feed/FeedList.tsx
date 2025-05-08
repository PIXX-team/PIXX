// components/feed/FeedList.tsx

"use client";

import { useInfiniteQuery } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { getFeeds } from "@/app/lib/api/feedApi";
import styles from "./feed-list.module.css";
import Image from "next/image";
import { Feed } from "@/app/types/feed";
import FloatingButton from "../common/FloatingButton";

export default function FeedList() {
  const router = useRouter();

  // 앨범 생성
  const [mode, setMode] = useState<"default" | "select">("default"); // 앨범 생성 플로팅버튼 상태 관리
  const longPressTimer = useRef<NodeJS.Timeout | null>(null); // 썸네일 오래 누르는거 상태 관리
  const [selectedFeedIds, setSelectedFeedIds] = useState<number[]>([]); // 선택된 피드 관리

  // 피드 썸네일 로딩 및 에러 상태 관리
  const [imageLoaded, setImageLoaded] = useState<{ [key: number]: boolean }>({});
  const [imageErrors, setImageErrors] = useState<{ [key: number]: boolean }>({});
  const [retryCount, setRetryCount] = useState<{ [key: number]: number }>({});
  const MAX_RETRY_ATTEMPTS = 3;

  // 리액트쿼리 API + 무한스크롤
  const { data, isLoading, isError, fetchNextPage, hasNextPage, isFetchingNextPage } = useInfiniteQuery({
    queryKey: ["feeds"],
    queryFn: ({ pageParam = 0 }) => getFeeds({ type: 0, page: pageParam, size: 20 }),
    getNextPageParam: (lastPage, allPages) => {
      // lastPage.length가 20개보다 적으면 더 이상 가져올게 없다고 판단
      if (lastPage.length < 20) return undefined;
      return allPages.length; // 다음 pageParam으로 사용
    },
    initialPageParam: 0,
    refetchOnWindowFocus: false, // 창 포커스 변경 시 리페치 방지
    staleTime: 1000 * 60 * 5, // 5분 동안 데이터를 신선한 상태로 유지 -> 나중에 사진 업로드 시 피드 변경 코드 추가 해야함!!
  });

  const observerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!observerRef.current) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    });

    observer.observe(observerRef.current);

    return () => observer.disconnect();
  }, [fetchNextPage, hasNextPage, isFetchingNextPage]);

  // 이미지 로드 성공 핸들러
  const handleImageLoad = (feedId: number) => {
    setImageLoaded((prev) => ({ ...prev, [feedId]: true }));
    setImageErrors((prev) => ({ ...prev, [feedId]: false }));
  };

  // 이미지 로드 실패 핸들러
  const handleImageError = (feedId: number) => {
    // 현재 재시도 횟수 가져오기
    const currentRetryCount = retryCount[feedId] || 0;

    if (currentRetryCount < MAX_RETRY_ATTEMPTS) {
      // 재시도 횟수 증가
      setRetryCount((prev) => ({ ...prev, [feedId]: currentRetryCount + 1 }));

      // 이미지 캐시 무효화를 위한 랜덤 쿼리 파라미터 생성
      const feedWithRetry = data?.pages.flat().find((feed: Feed) => feed.feedId === feedId);

      if (feedWithRetry) {
        // 실패한 이미지 다시 로드 시도 (setTimeout으로 약간의 지연 추가)
        setTimeout(() => {
          // 이미지 상태 초기화하여 다시 로드되도록 함
          setImageLoaded((prev) => ({ ...prev, [feedId]: false }));
          setImageErrors((prev) => ({ ...prev, [feedId]: false }));
        }, 1000); // 1초 후 재시도
      }
    } else {
      // 최대 재시도 횟수 초과 - 최종 에러 상태로 설정
      setImageErrors((prev) => ({ ...prev, [feedId]: true }));
      setImageLoaded((prev) => ({ ...prev, [feedId]: true })); // 로딩 인디케이터 숨기기
    }
  };

  // 이미지 재시도 요청 핸들러
  const handleRetryRequest = (feedId: number, e: React.MouseEvent) => {
    e.stopPropagation(); // 이벤트 버블링 방지 (피드 클릭 이벤트 방지)

    // 재시도 횟수 초기화
    setRetryCount((prev) => ({ ...prev, [feedId]: 0 }));
    // 이미지 상태 초기화
    setImageLoaded((prev) => ({ ...prev, [feedId]: false }));
    setImageErrors((prev) => ({ ...prev, [feedId]: false }));
  };

  if (isLoading) return <div className={styles["loading-message"]}>소중한 피드를 불러오고 있어요</div>;
  if (isError) return <div className={styles["error-message"]}>피드를 불러오는데 실패했습니다</div>;
  // longPress
  const handleTouchStart = () => {
    longPressTimer.current = setTimeout(() => {
      setMode("select");
    }, 300); // 300ms 이상 누르면 선택 모드로
  };

  // longPress 타이머 취소
  const handleTouchEnd = () => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
      longPressTimer.current = null;
    }
  };

  // 피드 클릭 핸들러
  const handleFeedClick = (feedId: number) => {
    if (mode === "select") {
      // 선택 모드인 경우, 선택 추가/삭제
      if (selectedFeedIds.includes(feedId)) {
        setSelectedFeedIds(selectedFeedIds.filter((id) => id !== feedId)); // 선택 해제
      } else {
        setSelectedFeedIds([...selectedFeedIds, feedId]); // 선택 추가
      }
    } else {
      // 기본 모드면 상세 페이지로 이동
      router.push(`/feed/${feedId}`);
    }
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles["feed-grid-wrapper"]}>
        <div className={styles["feed-grid"]}>
          {data?.pages.map((page, pageIndex) =>
            page.map((feed: Feed, feedIndex: number) => (
              <div
                key={feed.feedId}
                className={styles["feed-item"]}
                onClick={() => handleFeedClick(feed.feedId)}
                onTouchStart={handleTouchStart} // longPress
                onTouchEnd={handleTouchEnd} // longPress
              >
                <Image
                  src={"/dummy-feed-thumbnail.png"}
                  // src={feed.feedThumbnailImgUrl || "/dummy-feed-thumbnail.png"}
                  alt={`Feed ${feed.feedId}`}
                  fill
                  className={styles.feedImage}
                  priority={pageIndex === 0 && feedIndex < 6} // 처음 6개는 priority
                  onLoad={() => handleImageLoad(feed.feedId)}
                  onError={() => handleImageError(feed.feedId)}
                />

                {/* 로딩 표시 */}
                {!imageLoaded[feed.feedId] && !imageErrors[feed.feedId] && (
                  <div className={styles.imageLoading}>로딩중...</div>
                )}

                {/* 에러 표시 및 재시도 버튼 */}
                {imageErrors[feed.feedId] && (
                  <div className={styles.imageError}>
                    <p>이미지 로드 실패</p>
                    <button className={styles.retryButton} onClick={(e) => handleRetryRequest(feed.feedId, e)}>
                      다시 시도
                    </button>
                  </div>
                )}

                {/* 선택 모드일 때만 체크 아이콘 렌더링 */}
                {mode === "select" && (
                  <div className={styles.checkIcon}>
                    <Image
                      src={
                        selectedFeedIds.includes(feed.feedId) ? "/icons/icon-checked.png" : "/icons/icon-unchecked.png"
                      }
                      alt="선택 여부"
                    />
                  </div>
                )}
              </div>
            ))
          )}
        </div>

        {/* 감시용 div: 마지막에 스크롤 도달하면 다음 페이지 불러오기 */}
        <div ref={observerRef} style={{ height: "1px" }} />

        {/* 추가 로딩 중이면 표시 */}
        {isFetchingNextPage && <div>추가 로딩 중...</div>}
      </div>
      {/* 플로팅 버튼 추가 */}
      <FloatingButton
        mode={mode}
        onClick={() => {
          if (mode === "default") {
            setMode("select"); // Create 누르면 select 모드로
          } else {
            setMode("default"); // Cancel 누르면 다시 default 모드로
            setSelectedFeedIds([]); // 선택한 피드 초기화
          }
        }}
      />
    </div>
  );
}
