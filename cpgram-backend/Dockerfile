# Use a base image
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy application files
COPY target/backend-application.jar backend-application.jar

# Expose the application port
EXPOSE 8087

# Command to run the application
CMD ["java", "-jar", "backend-application.jar"]
