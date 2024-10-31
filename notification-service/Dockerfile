FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/*.jar notification.jar
ENTRYPOINT ["java", "-jar", "notification.jar"]