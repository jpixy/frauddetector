FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/frauddetector-0.0.1-SNAPSHOT.jar /app/frauddetector.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "frauddetector.jar"]