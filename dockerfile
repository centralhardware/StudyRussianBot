FROM openjdk:12
COPY ./target/StudyRussian-jar-with-dependencies.jar /
ENTRYPOINT ["java","-jar","StudyRussian-jar-with-dependencies.jar"]
