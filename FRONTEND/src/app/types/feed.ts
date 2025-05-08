// Feed 타입 정의
export interface Feed {
  feedId: number;
  feedThumbnailImgUrl: string;
  feedFavorite: boolean;
}

export interface FeedFile {
  imageId: number;
  imageUrl: string;
  imageType: "IMAGE" | "VIDEO" | "GIF";
}

export interface FeedDetailResponse {
  feedList: FeedFile[];
  feedTitle: string;
  feedHashtags: string[];
  feedMemo: string;
  brandName: string;
  feedLocation: string;
  feedDate: string;
  feedFavorite: boolean;
}
