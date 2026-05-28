# ─────────────────────────────
# 1. BUILD STAGE (Maven build)
# ─────────────────────────────
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests


# ─────────────────────────────
# 2. RUNTIME STAGE (JDK 25 only)
# ─────────────────────────────
FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]