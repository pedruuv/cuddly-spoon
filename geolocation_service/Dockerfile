FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/*.jar geolocation.jar
ENTRYPOINT ["java", "-jar", "geolocation.jar"]