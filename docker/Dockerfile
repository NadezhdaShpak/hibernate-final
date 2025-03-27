#build project
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#run project
FROM openjdk:21-jdk-oracle
RUN mkdir -p /app/logs && chmod 777 /app/logs
WORKDIR /app
COPY --from=builder /app/target/hibernate-final*.jar hibernate-final.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "hibernate-final.jar"]