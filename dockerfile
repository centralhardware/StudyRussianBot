FROM openjdk:12
ARG CACHE_DATE=not_a_date COPY ./target/StudyRussian-jar-with-dependencies.jar /
ENTRYPOINT ["java","-jar","StudyRussian-jar-with-dependencies.jar"]
