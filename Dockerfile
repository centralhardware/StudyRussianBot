FROM maven:3.9.4-amazoncorretto-21 as maven

COPY ./pom.xml ./pom.xml

RUN mvn dependency:go-offline -B

COPY ./src ./src

RUN mvn package

FROM openjdk:21-slim

RUN apt update && \
    apt install tzdata

COPY --from=maven target/StudyRussian-jar-with-dependencies.jar .

CMD ["java", "-jar", "StudyRussian-jar-with-dependencies.jar" ]