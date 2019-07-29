#!/bin/bash

while true ; do
    wget -q -O /dev/null http://wally133.cit.tu-berlin.de/10gb.bin || { sleep 30; }
    sleep 10;
done

