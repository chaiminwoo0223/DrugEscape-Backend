FROM --platform=linux/amd64 openjdk:17-jdk-slim
ARG JAR_FILE=build/libs/DrugEscape-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]