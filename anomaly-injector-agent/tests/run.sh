#!/bin/bash

REPO=teambitflow/anomaly-injector-agent

#docker build -t "$REPO" ../
docker pull $REPO
docker start $(docker create --net="host" --pid="host" --privileged "$REPO" -host=wally166 -api-port 7999)

