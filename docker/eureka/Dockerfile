FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY baro-cloud/eureka/build/libs/eureka-*.jar app.jar
EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8761/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

