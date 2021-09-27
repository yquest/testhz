FROM openjdk:8-jre-alpine3.9

COPY target/testhz-0.0.1-SNAPSHOT.jar /app.jar

CMD ["java","-Xmx500m","-jar","/app.jar", "{\"cassandra\":{\"port\":9042,\"host\":\"cassandra\",\"datacenter\":\"datacenter1\"},\"http\":{\"port\":8080},\"accountMDAO\":\"LOCK\"}" ]