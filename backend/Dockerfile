FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_DATASOURCE_URL="jdbc:mariadb://mariadb-service:3306/stock101"
ENV SPRING_DATASOURCE_USERNAME="stock101"
ENV SPRING_DATASOURCE_PASSWORD="password"
ENV SPRING_DATA_REDIS_HOST="redis-service"
ENV SPRING_DATA_REDIS_PORT="6379"
ENV SPRING_DATA_REDIS_PASSWORD=""
ENV JWT_ISSUER="https://accounts.beyond.com"
ENV JWT_SECRET="change-me"
ENV APIKEY_STOCK_KEY="change-me"
ENV APIKEY_KIS_KEY="change-me"
ENV APIKEY_KIS_SECRET="change-me"

ENTRYPOINT ["java", "-jar", "app.jar"]
