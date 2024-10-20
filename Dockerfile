FROM vegardit/graalvm-maven:latest-java17 as builder

WORKDIR /source

ADD . /source

RUN --mount=type=cache,target=/root/.m2 mvn -e clean -DskipTests package

# https://stackoverflow.com/questions/42208442/maven-docker-cache-dependencies

FROM ghcr.io/graalvm/jdk:java17

WORKDIR /app

COPY --from=builder /source/target/jdph jdph

EXPOSE 8080

ENTRYPOINT /app/jdph
