FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY baro-cloud/opa-bundle/build/libs/opa-bundle-*.jar app.jar
COPY baro-cloud/opa/policy /app/policy
EXPOSE 8095
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8095/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
