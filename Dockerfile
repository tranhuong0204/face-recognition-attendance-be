

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

COPY certs/isrgrootx1.pem /app/certs/isrgrootx1.pem
RUN keytool -importcert \
    -noprompt \
    -trustcacerts \
    -alias tidb-ca \
    -file /app/certs/isrgrootx1.pem \
    -keystore /app/certs/tidb-truststore.p12 \
    -storetype PKCS12 \
    -storepass changeit

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]