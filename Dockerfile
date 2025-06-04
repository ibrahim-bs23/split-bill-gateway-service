# Use a minimal base image with Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR file into the image
COPY target/gateway-service.jar app.jar

# Expose the port used by the Discovery Service (Eureka)
EXPOSE 7030

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
