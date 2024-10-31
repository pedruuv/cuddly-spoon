FROM openjdk:17-jdk-alpine
COPY target/*.jar delivery.jar
ENTRYPOINT ["java", "-jar", "delivery.jar"]