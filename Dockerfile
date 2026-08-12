# Stage 1: Build file .jar với Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Khởi chạy ứng dụng Java 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/aka-system.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
