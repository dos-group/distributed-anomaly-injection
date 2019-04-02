#include "memory.h"

void print_params(parameters* params){
	printf("Memory: %dMB\nMax memory: %dMB\nTime between allocs: %ds\nFluctuating: %d\n",
		params->alloc_mb, params->max_mb, params->sleep_sec, params->fluct);
}

int main(int argc, char **argv) {
	parameters params;
	if(parse_arguments(&params, argc, argv) != 0)
		return -1;

	if(params.fluct == 0){
		run_memory_leak(&params);
	}else if(params.fluct == 1){
		run_memory_fluct(&params);
	}

	return 0;
}



