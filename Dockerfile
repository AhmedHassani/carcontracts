FROM openjdk:17-jdk-slim

WORKDIR /app

COPY carcontracts.jar /app/your-app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/your-app.jar"]
