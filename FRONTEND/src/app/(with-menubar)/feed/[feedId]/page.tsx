// app/(with-menubar)/feed/[feedId]/page.tsx

import FeedDetail from "@/components/feed/feed-detail/FeedDetail";

// 피드 상세 페이지
export default async function Page({ params }: { params: Promise<{ feedId: string }> }) {
  const { feedId } = await params;
  return (
    <main>
      <FeedDetail feedId={feedId} />
    </main>
  );
}
