# FROM maven:3.9-eclipse-temurin-17 AS build
# WORKDIR /app
# COPY pom.xml .
# RUN mvn dependency:go-offline
# COPY src src
# RUN mvn package
# ENTRYPOINT ["java", "-jar", "target/*.jar"]

# Stage 1: Build ứng dụng
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

# # Stage 1: Build app bằng Maven
# FROM maven:3.9.10-amazoncorretto-17 AS build
#
# WORKDIR /app
# COPY pom.xml ./
# COPY src ./src
#
# ## Build project, bỏ qua test cho nhanh
# #RUN mvn package -DskipTests
# #
# ## Stage 2: Chạy app bằng Amazon Corretto JDK
# #FROM amazoncorretto:17.0.15
# #
# #WORKDIR /app
# #
# ## Copy file jar từ stage build
# #COPY --from=build /app/target/*.jar app.jar
# #
# ## Render sẽ cấp biến PORT, Spring Boot dùng biến này để set server.port
# #ENV PORT=8082
# #EXPOSE 8082
# #
# ## Chạy app
# #ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT}"]
