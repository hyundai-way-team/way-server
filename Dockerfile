FROM ubuntu:latest
LABEL authors="jaewonlee"

ENTRYPOINT ["top", "-b"]

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/way-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
