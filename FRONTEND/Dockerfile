# 1. Base image
FROM node:22-alpine AS base



# 2. Install dependencies
FROM base AS deps
# (필요한 추가 패키지 설치 - 알파인용. git은 빌드시 필요할 수도 있음)
RUN apk add --no-cache libc6-compat git



# 공통 환경변수 설정
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# 작업 디렉토리
WORKDIR /app

# 패키지 설치를 위해 필요한 파일만 먼저 복사
COPY package.json yarn.lock ./

# 패키지 설치
RUN yarn install --frozen-lockfile


# 3. Build app
FROM base AS builder

# 작업 디렉토리
WORKDIR /app

# deps 단계에서 설치한 node_modules 복사
COPY --from=deps /app/node_modules ./node_modules
# 소스코드 전체 복사
COPY . .

# After COPY . .
RUN ls -R /app/src/components/
RUN find src -type f -name "*.tsx" | sort
RUN cat src/app/\(with-menubar\)/layout.tsx || echo "File not found"

# Next.js 빌드
RUN yarn build

# 4. Production image
FROM base AS runner

# 작업 디렉토리
WORKDIR /app

# 보안을 위해 non-root user와 그룹 생성
RUN addgroup nodejs
RUN adduser -SDH -G nodejs nextjs
RUN mkdir .next
RUN chown nextjs:nodejs .next

# 빌드 결과물만 복사 (standalone)
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static
COPY --from=builder --chown=nextjs:nodejs /app/public ./public

# 사용자 변경
USER nextjs

# 포트 열기
# EXPOSE 3000
# ENV PORT 3000
# ENV HOSTNAME "0.0.0.0"

# Healthcheck (optional)
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/ || exit 1

# 실행
CMD ["node", "server.js"]
