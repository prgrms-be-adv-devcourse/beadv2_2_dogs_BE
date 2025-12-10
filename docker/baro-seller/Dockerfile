FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY baro-seller/build/libs/baro-seller-*.jar app.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

