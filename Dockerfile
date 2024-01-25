FROM --platform=linux/amd64/v8 openjdk:17-jdk
ARG JAR_FILE=build/libs/DrugEscape-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]