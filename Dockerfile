FROM maven:3.9.2-amazoncorretto-17 as builder

WORKDIR /source

ADD . /source

# RUN mvn verify clean --fail-never

RUN --mount=type=cache,target=/root/.m2 mvn -e clean -DskipTests package

FROM amazoncorretto:17-alpine3.17

WORKDIR /app

COPY --from=builder /source/jdph-volume/target/jdph-volume-0.1.0-jar-with-dependencies.jar jdph-volume.jar

EXPOSE 8080

ENTRYPOINT java -jar /app/jdph-volume.jar
