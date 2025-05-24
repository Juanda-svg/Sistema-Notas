FROM openjdk:17

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

COPY target/Sistema-Notas-1.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
