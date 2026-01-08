FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/*.jar app.jar
# Criamos a pasta onde o Hollow salvará os snapshots
RUN mkdir /hollow-data
ENTRYPOINT ["java", "-jar", "app.jar"]