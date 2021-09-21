FROM openjdk:14
EXPOSE 8080
ARG JAR_FILE=target/testhz-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar", "{\"cassandra\":{\"port\":9042,\"host\":\"host.docker.internal\",\"datacenter\":\"datacenter1\"},\"http\":{\"port\":8080},\"accountMDAO\":\"LOCK\"}" ]