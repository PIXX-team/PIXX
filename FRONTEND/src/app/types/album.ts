// app/types/album.ts

export interface Album {
  albumId: number;
  albumName: string;
  albumDate: string; // ISO 문자열
  albumMemo?: string;
}

export interface AlbumFeed {
  feedId: number;
  feedThumbnailImgUrl: string;
  feed_favorite: boolean;
}

export interface AlbumDetail {
  albumId: number;
  albumName: string;
  albumDate: string;
  albumMemo: string;
  albumFeedList: AlbumFeed[];
}

export interface AlbumResponse {
  status: string;
  message: string;
  data: {
    albumList: Album[];
  };
}

export interface AlbumDetailResponse {
  status: string;
  message: string;
  data: AlbumDetail;
}
