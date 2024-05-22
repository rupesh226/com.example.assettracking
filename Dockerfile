# Use a multi-stage build to optimize the image size
# First stage: build the Spring Boot application
## export JAVA_HOME=`/usr/libexec/java_home -v 17`
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY src/main/resources/application.properties /etc/assettracking/config/application.properties
COPY target/assettracking-1.0.1.jar assettracking-1.0.1.jar
ENTRYPOINT ["java", "-jar", "-Dspring.config.location=/etc/assettracking/config/application.properties", "assettracking-1.0.1.jar"]

# Expose the application port
EXPOSE 8080


