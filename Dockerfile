# 1. Base Image Java 17 JRE Alpine siêu nhẹ
FROM eclipse-temurin:17-jre-alpine

# 2. Tạo thư mục làm việc trong container
WORKDIR /app

# 3. Copy file .jar đã đóng gói vào container
COPY target/aka-system.jar app.jar

# 4. Mở cổng 8081 cho ứng dụng Web Spring Boot
EXPOSE 8081

# 5. Khởi chạy ứng dụng Java
ENTRYPOINT ["java", "-jar", "app.jar"]
