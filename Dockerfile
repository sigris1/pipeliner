FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .
COPY Model/pom.xml Model/
COPY DAO/pom.xml DAO/
COPY Core/pom.xml Core/
COPY Service/pom.xml Service/
COPY Controller/pom.xml Controller/
COPY App/pom.xml App/

RUN mvn dependency:go-offline -B

COPY . .

RUN mvn clean install -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /build/App/target/App-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]