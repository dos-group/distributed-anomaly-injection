#!/bin/bash

MY_PATH="`( cd \"$MY_PATH\" && pwd )`"  # absolutized and normalized

#TODO dynamischer machen
PATH_TO_STRESS_NG=$"$MY_PATH/anomalies/binaries/vnf_stress-ng"
PATH_TO_CPULIMIT=$"$MY_PATH/anomalies/binaries/cpulimit"

OP_MODE_CPU_UTIL_GROW=0
OP_MODE_CPU_UTIL_SHRINK=1
OP_MODE_CPU_UTIL_IDLE=2

usage=$"$(basename $0) [--help|-h] [--fluct|f] [--cpu|-c=NUM] --[init-load|-i=NUM] [--load-growth|-g=NUM] [--sleep|-s=NUM] [--load-limit|-l=NUM] -- Programm generates a growing CPU utilization.

Where:
    -h/--help        Shows this message.
    -f/--fluct       Fluctuation flag. After the maximal 
                     CPU utilization is reached, it will be
                     inversly decreased. 
    -c/--cpu         Defines amount of worker processes for 
                     CPU utilization. Stressing of several 
                     cores in multicore systems (default 1).
    -i/--init-load   Initial CPU utilization percentage 
                     (default 0).
    -g/--load-growth Percantage growth of CPU utilization. 
                     Linear growing by this value after defined 
                     time (default 1).
    -s/--sleep       Sleep time between CPU utilization 
                     percantage growing in seconds (default 1).
    -l/--load-limit  Maximal percentage of CPU utilization. Should
                     be a value between 0 and 100. (default -1 
                     --> limit is maximal CPU utilization)."

ARG_FLUCT=false
ARG_CPU=1
ARG_INIT=0
ARG_GROWTH=1
ARG_SLEEP=1
ARG_LIMIT=-1

function check_number_for_param() {
	local re='^[0-9]+$'
	if ! [[ $2 =~ $re ]] ; then
   		>&2 echo "Error: Invalid parameter $2 for $1 argument. Numeric value > 0 expected.\n" 
		exit 1
	fi 
}

for i in "$@"
do
	case $i in
		-h|--help)
		echo "$usage"
		exit 0
		shift
		;;
		-c=*|--cpu=*)
		ARG_CPU="${i#*=}"
		check_number_for_param "cpu" $ARG_CPU
		shift
		;;
		-i=*|--init-load=*)
		ARG_INIT="${i#*=}"
		check_number_for_param "init percentage" $ARG_INIT
		shift
		;;
		-g=*|--load-growth=*)
		ARG_GROWTH="${i#*=}"
		check_number_for_param "percentage grow" $ARG_GROWTH
		shift
		;;
		-s=*|--sleep=*)
		ARG_SLEEP="${i#*=}"
		check_number_for_param "sleep" $ARG_SLEEP
		shift
		;;
		-l=*|--load-limit=*)
		ARG_LIMIT="${i#*=}"
		check_number_for_param "limit" $ARG_LIMIT
		shift
		;;
		-f|--fluct)
		ARG_FLUCT=true
		shift
		;;
		*)
		>&2 echo "Argument $i unknown. Will be ignored."
		;;
	esac
done

growth_value=$(( $ARG_GROWTH * $ARG_CPU ))
op_mode=$OP_MODE_CPU_UTIL_GROW
current_limit=$(( $ARG_INIT * $ARG_CPU ))
max_cpu=$ARG_LIMIT
if [ $ARG_LIMIT -eq -1 ]
then
	max_cpu=$(( $ARG_CPU * 100 ))
fi

function adjust_current_limit() {
	if [ $op_mode -eq $OP_MODE_CPU_UTIL_GROW ]
	then
		current_limit=$(( $current_limit + $growth_value ))
		if [ $current_limit -gt  $max_cpu ]
		then
			current_limit=$max_cpu
		elif [ $current_limit -eq  $max_cpu ]
		then
			adjust_op_mode $OP_MODE_CPU_UTIL_SHRINK		
		fi
	elif [ $op_mode -eq $OP_MODE_CPU_UTIL_SHRINK ]
	then
		current_limit=$(( $current_limit - $growth_value ))
		if [ $current_limit -lt  $ARG_INIT ]
		then
			current_limit=$ARG_INIT
		elif [ $current_limit -eq  $ARG_INIT ]
		then
			adjust_op_mode $OP_MODE_CPU_UTIL_GROW	
		fi
	fi
}

function adjust_op_mode(){
	if $ARG_FLUCT
	then
		op_mode=$1
	else
		op_mode=$OP_MODE_CPU_UTIL_IDLE		
	fi
}

#Start cpu stressing
$PATH_TO_STRESS_NG --cpu=$ARG_CPU -q &
stressor_pid=$!
limitor_pid=0

while true
do
	if [ $op_mode -eq $OP_MODE_CPU_UTIL_GROW ] || [ $op_mode -eq $OP_MODE_CPU_UTIL_SHRINK ]
	then
		#Clear previous limit process
		if [ $limitor_pid -ne 0 ]
		then
			kill $limitor_pid 2> /dev/null
		fi
		#Start new limit process with updated values
		$PATH_TO_CPULIMIT --limit=$current_limit -i --pid=$stressor_pid >/dev/null &
		limitor_pid=$!
		sleep "$ARG_SLEEP"s
		#Check limit
		adjust_current_limit
	else
		sleep 60s
	fi	
done


