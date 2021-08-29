#!/bin/sh
for c in {1..1000};
do
   curl -s POST -H "Content-Type: application/json" -d {\"key$c\":\"val\"} http://localhost:8081/add &
done
curl --request GET http://localhost:8082/all
curl --request GET http://localhost:8083/stats-test
