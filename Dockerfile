# Multi-stage build: Maven (Java 21) → JRE runtime for Fly.io
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests package \
    && cp target/*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# /data is the H2 file directory (ephemeral unless a Fly volume is mounted).
RUN mkdir -p /data

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    H2_FILE_PATH=/data/challenge \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
