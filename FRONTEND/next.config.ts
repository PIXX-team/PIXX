import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone", // 도커 배포용 (독립실행형)파일 생성 코드
  images: {
    domains: [
      "d2w650bgmlbl7n.cloudfront.net", // ✨ CloudFront 도메인 추가
    ],
  },
};

export default nextConfig;
