FROM maven:3.6-jdk-12 as maven

COPY ./pom.xml ./pom.xml

RUN mvn dependency:go-offline -B

COPY ./src ./src

RUN mvn package -DskipTests


FROM openjdk:12


COPY --from=maven target/StudyRussian-jar-with-dependencies.jar .


CMD ["java", "-jar", "StudyRussian-jar-with-dependencies.jar" ]