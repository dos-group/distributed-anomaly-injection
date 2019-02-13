#!/bin/bash
home=`dirname $(readlink -e $0)`

#all_hosts=sprout-2.ims4,sprout-0.ims4,sprout-1.ims4,bono-2.ims4,homestead-0.ims4,bono-0.ims4,bono-1.ims4,ellis-0.ims4,homer-0.ims4,wally141,wally142,wally147,wally131,wally136,wally134
#all_hosts=sprout-0.ims4,sprout-1.ims4,homestead-0.ims4,bono-0.ims4,bono-1.ims4,ellis-0.ims4,wally141,wally142,wally147,wally131,wally136,wally134
#all_hosts=wally136,wally134
#all_hosts=bono-1.ims4,sprout-0.ims4
all_hosts=wally136,bono-1.ims4,bono@bono-0.ims4,sprout-0.ims4,sprout@sprout-0.ims4,wally142,bono-0.ims4,bono@bono-0.ims4

python "$home/injector_agent.py" \
    -cooldown 30 \
    -loop-scenario -host ignored \
    -all-hosts "$all_hosts" \
    -scenario "$home/all_anomalies.csv" \
    -start "2000-01-01 00:00:00" \
    -simulate
