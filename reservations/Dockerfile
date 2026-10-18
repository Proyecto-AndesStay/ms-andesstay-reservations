# Etapa de ejecución liviana con Java (Eclipse Temurin)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia el JAR generado por Maven
COPY target/*.jar app.jar

# Expone el puerto por defecto (se mapea en compose.yml)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]