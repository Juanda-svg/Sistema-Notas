FROM openjdk:17

# Establecer directorio de trabajo
WORKDIR /app

# Copiar los archivos del proyecto al contenedor
COPY . .

# Ejecutar Maven para compilar y crear el .jar sin tests
RUN ./mvnw clean package -DskipTests

# Exponer el puerto que usará la aplicación
EXPOSE 8086

# Ejecutar el .jar generado
ENTRYPOINT ["java", "-jar", "target/Sistema-Notas-1.jar"]
