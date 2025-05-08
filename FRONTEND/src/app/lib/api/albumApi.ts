// app/lib/api/albumApi.ts

import axiosInstance from "./axiosInstance";
import { AlbumResponse } from "@/app/types/album";
import { AlbumDetailResponse } from "@/app/types/album";

export async function getAlbums(params: { type: number; page: number; size: number }): Promise<AlbumResponse> {
  const response = await axiosInstance.post("/api/v1/feed/album", params);
  return response.data;
}

export async function getAlbumDetail(
  albumId: number,
  params: { type: number; page: number; size: number }
): Promise<AlbumDetailResponse> {
  const response = await axiosInstance.post(`/api/v1/feed/album/${albumId}`, params);
  return response.data;
}
