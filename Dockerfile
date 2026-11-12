# Multi-stage: build con Maven (JDK 21) y runtime con JRE 21.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Dependencias primero: esta capa se reutiliza mientras pom.xml no cambie.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q -DskipTests package && cp target/*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# /data: archivo de la H2 (volátil salvo que se monte un volumen de Fly; ver fly.toml).
RUN mkdir -p /data

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE 8080

# Requeridas en runtime: JWT_SECRET (si falta, la app no arranca). Ver README.
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"

# `exec` hace que la JVM sea el PID 1 y reciba SIGTERM (shutdown graceful).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
