FROM vegardit/graalvm-maven:latest-java17 as builder

WORKDIR /source

ADD . /source

# RUN mvn verify clean --fail-never

RUN --mount=type=cache,target=/root/.m2 mvn -e clean -DskipTests package

FROM alpine:3.20.3

RUN apk update && apk add --no-cache gcompat libstdc++

WORKDIR /app

COPY --from=builder /source/jdph-volume/target/jdph-volume jdph-volume

EXPOSE 8080

ENTRYPOINT /app/jdph-volume
