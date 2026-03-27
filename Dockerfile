# Etapa de construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecución (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el JAR del Front (asegúrate que el nombre coincida con tu pom.xml)
COPY --from=build /app/target/seguridadcalidad-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# No necesita Wallet, solo ejecutar el JAR
ENTRYPOINT ["java", "-jar", "app.jar"]