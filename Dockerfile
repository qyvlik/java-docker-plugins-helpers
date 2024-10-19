FROM vegardit/graalvm-maven:latest-java17 as builder

WORKDIR /source

ADD . /source

RUN mvn clean -DskipTests package

# https://stackoverflow.com/questions/42208442/maven-docker-cache-dependencies

FROM ghcr.io/graalvm/jdk:java17

WORKDIR /app

COPY --from=builder /source/target/java-docker-plugin-helpers-0.1.0.jar server.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dsun.net.httpserver.nodelay=true", "-jar", "/app/server.jar"]
