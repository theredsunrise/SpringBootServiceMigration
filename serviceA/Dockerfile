FROM eclipse-temurin:21-jre
ARG PORT=8080
WORKDIR /app
COPY target/serviceA-*.jar app.jar
EXPOSE ${PORT}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
