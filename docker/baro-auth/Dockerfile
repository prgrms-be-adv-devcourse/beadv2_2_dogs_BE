# Multi-stage build
FROM eclipse-temurin:21-jre-alpine AS runtime

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY baro-auth/build/libs/baro-auth-*.jar app.jar

# 애플리케이션 포트
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# 실행
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

