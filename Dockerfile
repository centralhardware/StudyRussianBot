FROM maven:3.6-jdk-14 as maven

COPY ./pom.xml ./pom.xml

RUN mvn dependency:go-offline -B

COPY ./src ./src

RUN mvn package


FROM openjdk:15-alpine

RUN apk update && \
    apk add --no-cache tzdata

ENV TZ Asia/Novosibirsk

COPY --from=maven target/StudyRussian-jar-with-dependencies.jar .

CMD ["java", "-jar", "StudyRussian-jar-with-dependencies.jar" ]