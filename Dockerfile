FROM maven:3.9.1-amazoncorretto-20 as maven

COPY ./pom.xml ./pom.xml

RUN mvn dependency:go-offline -B

COPY ./src ./src

RUN mvn package


FROM bitnami/java:20

RUN apk update && \
    apk add --no-cache tzdata

ENV TZ Asia/Novosibirsk

COPY --from=maven target/StudyRussian-jar-with-dependencies.jar .

CMD ["java", "-jar", "StudyRussian-jar-with-dependencies.jar" ]