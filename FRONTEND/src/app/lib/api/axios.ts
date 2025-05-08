import axios from "axios";

// axios 인스턴스 생성
const api = axios.create({
  baseURL: "https://film-moa.com",
  headers: {
    "Content-Type": "application/json",
    ...(process.env.NODE_ENV && { Authorization: "Bearer test" }),
  },
  withCredentials: true, // refresh_token을 쿠키로 받기 위해 필요
});

// 클라이언트 사이드에서만 실행되는 코드
if (typeof window !== "undefined") {
  // 토큰 갱신 중인지 확인하는 플래그
  let isRefreshing = false;
  // 토큰 갱신 대기 중인 요청들을 저장하는 배열
  let refreshSubscribers: ((token: string) => void)[] = [];

  // 토큰 갱신 후 대기 중인 요청들을 처리하는 함수
  const onRefreshed = (token: string) => {
    refreshSubscribers.forEach((callback) => callback(token));
    refreshSubscribers = [];
  };

  // 토큰 갱신을 기다리는 함수
  const addRefreshSubscriber = (callback: (token: string) => void) => {
    refreshSubscribers.push(callback);
  };

  // 토큰 갱신 함수
  const refreshToken = async () => {
    try {
      const response = await api.post("/api/auth/refresh");
      if (response.data.success) {
        const { accessToken } = response.data.data;
        localStorage.setItem("token", accessToken);
        return accessToken;
      }
      throw new Error("토큰 갱신 실패");
    } catch (error) {
      throw error;
    }
  };

  // Request interceptor to add Authorization header
  api.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem("token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // 응답 인터셉터
  api.interceptors.response.use(
    (response) => {
      // 응답이 성공적이고 데이터가 있는 경우
      if (response.data.success && response.data.data) {
        // accessToken이 응답에 포함되어 있으면 저장
        if (response.data.data.accessToken) {
          localStorage.setItem("token", response.data.data.accessToken);
        }
      }
      return response;
    },
    async (error) => {
      const originalRequest = error.config;

      if (error.response?.status === 401 && !originalRequest._retry) {
        if (isRefreshing) {
          // 토큰 갱신 중이면 새로운 토큰을 기다림
          try {
            const token = await new Promise<string>((resolve) => {
              addRefreshSubscriber((token) => {
                resolve(token);
              });
            });
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          } catch (err) {
            return Promise.reject(err);
          }
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          const token = await refreshToken();
          originalRequest.headers.Authorization = `Bearer ${token}`;
          onRefreshed(token);
          isRefreshing = false;
          return api(originalRequest);
        } catch (refreshError) {
          isRefreshing = false;
          console.error("토큰 갱신 실패:", refreshError);
          // 토큰 갱신 실패 시 로그아웃 처리
          localStorage.removeItem("token");
          window.location.href = "/welcome";
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(error);
    }
  );
}

export default api;
