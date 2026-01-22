FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN apk add --no-cache dos2unix
RUN dos2unix ./gradlew
RUN chmod +x ./gradlew
RUN ./gradlew :user-service:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/user-service/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]