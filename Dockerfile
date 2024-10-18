FROM maven:3.9.6-amazoncorretto-17 as builder

WORKDIR /source

ADD pom.xml /source

RUN ["/usr/local/bin/mvn-entrypoint.sh", "mvn", "verify", "clean", "--fail-never"]

ADD . /source

RUN mvn -DskipTests package

# https://stackoverflow.com/questions/42208442/maven-docker-cache-dependencies

FROM amazoncorretto:17-alpine3.17

WORKDIR /app

COPY --from=builder /source/target/java-docker-plugin-helpers-0.1.0.jar server.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dsun.net.httpserver.nodelay=true", "-jar", "/app/server.jar"]
