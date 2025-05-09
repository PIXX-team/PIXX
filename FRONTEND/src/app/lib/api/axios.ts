import axios from "axios";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

// axios 인스턴스 생성
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // 쿠키 송수신을 위해 필수
});

// 클라이언트 사이드에서만 실행되는 코드
if (typeof window !== "undefined") {
  // 토큰 갱신 중인지 확인하는 플래그
  let isRefreshing = false;
  // 토큰 갱신 대기 중인 요청들을 저장하는 배열
  let refreshSubscribers: (() => void)[] = [];

  // 토큰 갱신 후 대기 중인 요청들을 처리하는 함수
  const onRefreshed = () => {
    refreshSubscribers.forEach((callback) => callback());
    refreshSubscribers = [];
  };

  // 토큰 갱신을 기다리는 함수
  const addRefreshSubscriber = (callback: () => void) => {
    refreshSubscribers.push(callback);
  };

  // 토큰 갱신 함수
  const refreshToken = async () => {
    try {
      const response = await api.post(
        "/api/auth/refresh",
        {},
        {
          withCredentials: true,
        }
      );
      if (response.data.status === 200) {
        return true;
      }
      throw new Error("토큰 갱신 실패");
    } catch (error) {
      throw error;
    }
  };

  // 응답 인터셉터
  api.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (error.response?.status === 401 && !originalRequest._retry) {
        if (isRefreshing) {
          // 토큰 갱신 중이면 새로운 토큰을 기다림
          try {
            await new Promise<void>((resolve) => {
              addRefreshSubscriber(() => {
                resolve();
              });
            });
            return api(originalRequest);
          } catch (err) {
            return Promise.reject(err);
          }
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          await refreshToken();
          onRefreshed();
          isRefreshing = false;
          return api(originalRequest);
        } catch (refreshError) {
          isRefreshing = false;
          console.error("토큰 갱신 실패:", refreshError);
          // 토큰 갱신 실패 시 로그아웃 처리
          window.location.href = "/welcome";
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(error);
    }
  );
}

export default api;
