# Stage 1: Build với Maven + JDK 21
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy toàn bộ code vào container
COPY . .

# Cấp quyền thực thi cho mvnw rồi build
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Stage 2: Chạy ứng dụng với JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy file jar từ stage build sang
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
