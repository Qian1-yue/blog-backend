FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S blog && adduser -S blog -G blog

WORKDIR /app

COPY --from=builder /workspace/target/blog-backend-*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER blog

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -q -O - http://localhost:8080/actuator/health/readiness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
