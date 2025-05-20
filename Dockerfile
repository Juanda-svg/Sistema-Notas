FROM openjdk:17

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR generado
COPY "./target/Sistema-Notas-1.jar" "app.jar"

# Exponer el puerto que usará la aplicación
EXPOSE 8086

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]