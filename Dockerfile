# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 🔥 Copiar pom primero (mejor cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# 🔥 Copiar el resto
COPY src ./src

# 🔥 Build real
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]