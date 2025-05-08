
# 1단계: 빌드용 이미지 (Gradle로 JAR 빌드)
FROM openjdk:17-jdk-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# gradlew에 실행 권한 부여
RUN chmod +x gradlew

# Gradle로 .jar 파일 빌드
RUN ./gradlew clean bootJar

# 2단계: 실행용 이미지 (Spring Boot 실행 환경)
FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사 (builder 스테이지에서 가져옴)
COPY --from=builder /app/build/libs/*.jar app.jar

# SpringBoot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
