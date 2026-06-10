FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /app

COPY pom.xml .
COPY bbs-common bbs-common
COPY $SERVICE $SERVICE

RUN mvn clean package -pl $SERVICE -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
ARG SERVICE
WORKDIR /app
COPY --from=build /app/$SERVICE/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
