# --- Стадия 1: Сборка приложения ---
    FROM gradle:8.5-jdk17-alpine AS builder

    # Устанавливаем рабочую директорию
    WORKDIR /app
    
    # Копируем весь проект из контекста (из папки backend)
    COPY . .
    
    # Собираем JAR-файл ТОЛЬКО для user-service (и его зависимостей)
    RUN ./gradlew :user-service:bootJar --no-daemon
    
    # --- Стадия 2: Создание финального, легковесного образа ---
    FROM openjdk:17-alpine
    
    WORKDIR /app
    
    # Копируем только собранный JAR-файл из стадии сборки
    COPY --from=builder /app/user-service/build/libs/*.jar app.jar
    
    # Указываем порт, который использует сервис (согласно docker-compose.yml)
    EXPOSE 8082
    
    # Запускаем приложение
    ENTRYPOINT ["java", "-jar", "app.jar"]