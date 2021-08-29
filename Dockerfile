FROM openjdk:14
EXPOSE 8080
ARG JAR_FILE=build/libs/testhz.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar", "1", "{\"cassandra\":{\"keyspace\":\"bank\",\"port\":9042,\"host\":\"host.docker.internal\",\"datacenter\":\"datacenter1\"}}" ]
