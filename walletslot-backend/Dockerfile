# --- 1. Build Stage ---
FROM gradle:8-jdk21 AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew build -x test --no-daemon

# --- 2. Run Stage ---
# run stage이므로 jre만
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# builder 스테이지에서 빌드된 .jar 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]