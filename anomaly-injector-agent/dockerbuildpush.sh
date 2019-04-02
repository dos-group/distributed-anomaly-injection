#!/bin/bash

REPO=teambitflow/anomaly-injector-agent

docker build -t "$REPO" ./
docker push "$REPO"

#docker start $(docker create --net="host" --pid="host" --privileged "$REPO" -host=wally166 -api-port 7999)

