FROM eclipse-temurin:17-jdk-alpine
#creazione cartella
WORKDIR /app
COPY target/*.war app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]d