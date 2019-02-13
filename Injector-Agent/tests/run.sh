#!/bin/bash

docker build -t alexanderacker/anomaly_injector ../
docker start $(docker create --net="host" --pid="host" --privileged alexanderacker/anomaly_injector -host=wally166 -api-port 7999)

