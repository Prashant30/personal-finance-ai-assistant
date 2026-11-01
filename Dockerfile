FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN mkdir -p /app/runtime
COPY --from=build /workspace/target/personal-finance-ai-assistant-*.jar app.jar
EXPOSE 8080
ENV FINANCE_DB_PATH=/app/runtime/finance.db
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

