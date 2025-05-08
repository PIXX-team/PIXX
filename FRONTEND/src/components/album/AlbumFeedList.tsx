"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import { getAlbumDetail } from "@/app/lib/api/albumApi";
import { AlbumDetail, AlbumFeed } from "@/app/types/album";
import { useParams } from "next/navigation";

const AlbumFeedList = () => {
  const params = useParams();
  const albumId = parseInt(params.id as string);

  const [albumDetail, setAlbumDetail] = useState<AlbumDetail | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchAlbumDetail = async () => {
      try {
        setLoading(true);
        const response = await getAlbumDetail(albumId, {
          type: 0,
          page: 0,
          size: 10,
        });

        if (response.status === "200") {
          setAlbumDetail(response.data);
        } else {
          setError(response.message || "앨범 정보를 불러오는 데 실패했습니다.");
        }
      } catch (err) {
        setError("앨범 정보를 불러오는 데 실패했습니다.");
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    if (albumId) {
      fetchAlbumDetail();
    }
  }, [albumId]);

  if (loading) {
    return <div className="flex justify-center items-center h-40">로딩 중...</div>;
  }

  if (error) {
    return <div className="text-red-500 text-center p-4">{error}</div>;
  }

  if (!albumDetail) {
    return <div className="text-center p-4">앨범 정보가 없습니다.</div>;
  }

  return (
    <div className="container mx-auto px-4 py-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">{albumDetail.albumName}</h1>
        <p className="text-gray-600">{new Date(albumDetail.albumDate).toLocaleDateString()}</p>
        {albumDetail.albumMemo && <p className="mt-2 text-gray-700">{albumDetail.albumMemo}</p>}
      </div>

      {albumDetail.albumFeedList.length === 0 ? (
        <div className="text-center p-8 bg-gray-100 rounded-lg">이 앨범에는 피드가 없습니다.</div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {albumDetail.albumFeedList.map((feed) => (
            <FeedItem key={feed.feedId} feed={feed} />
          ))}
        </div>
      )}
    </div>
  );
};

interface FeedItemProps {
  feed: AlbumFeed;
}

const FeedItem = ({ feed }: FeedItemProps) => {
  return (
    <div className="relative aspect-square overflow-hidden rounded-lg group">
      <div className="w-full h-full relative">
        <Image
          src={feed.feedThumbnailImgUrl}
          alt={`피드 ${feed.feedId}`}
          fill
          sizes="(max-width: 768px) 50vw, (max-width: 1200px) 33vw, 25vw"
          className="object-cover transition-transform duration-300 group-hover:scale-105"
        />
      </div>
      <div className="absolute bottom-2 right-2">
        {feed.feed_favorite && (
          <span className="text-red-500">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-6 h-6">
              <path d="M11.645 20.91l-.007-.003-.022-.012a15.247 15.247 0 01-.383-.218 25.18 25.18 0 01-4.244-3.17C4.688 15.36 2.25 12.174 2.25 8.25 2.25 5.322 4.714 3 7.688 3A5.5 5.5 0 0112 5.052 5.5 5.5 0 0116.313 3c2.973 0 5.437 2.322 5.437 5.25 0 3.925-2.438 7.111-4.739 9.256a25.175 25.175 0 01-4.244 3.17 15.247 15.247 0 01-.383.219l-.022.012-.007.004-.003.001a.752.752 0 01-.704 0l-.003-.001z" />
            </svg>
          </span>
        )}
      </div>
    </div>
  );
};

export default AlbumFeedList;
