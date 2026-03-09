FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY . .
RUN mvn -pl payments-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/payments-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]