FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY baro-buyer/build/libs/baro-buyer-*.jar app.jar
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

