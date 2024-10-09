# Use an official OpenJDK image
FROM openjdk:23-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the jar file
COPY target/consumer-app-v1.jar /app/consumer-app-v1.jar

# Expose the port on which the consumer runs
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app/consumer-app-v1.jar", "--spring.profiles.active=dev"]