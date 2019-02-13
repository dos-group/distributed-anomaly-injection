#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters. Port number as parameter required."
	return;
fi

PORT=$1

while read anomaly; do
	echo "############## Injecting $anomaly ##############"
	curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{}' "0.0.0.0:$PORT/api/anomalies/$anomaly/"
	sleep 1;
	curl -X DELETE --header 'Accept: application/json' "0.0.0.0:$PORT/api/anomalies/"
	sleep 1;
done <./anomalies.txt
