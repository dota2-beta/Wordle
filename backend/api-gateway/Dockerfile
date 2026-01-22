FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN apk add --no-cache dos2unix
RUN dos2unix ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew :api-gateway:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/api-gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]