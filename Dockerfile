FROM openjdk:21-rc-oracle
ARG JAR_FILE=target/clothing-store-backend.jar
COPY ${JAR_FILE} clothing-store-backend.jar

ENTRYPOINT ["java", "-jar", "clothing-store-backend.jar"]

EXPOSE 8080