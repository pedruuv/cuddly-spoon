FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/*.jar tracking.jar
ENTRYPOINT ["java", "-jar", "tracking.jar"]