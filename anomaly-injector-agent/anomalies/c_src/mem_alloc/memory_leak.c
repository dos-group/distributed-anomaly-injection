#include "memory.h"

void run_memory_leak(parameters *parameters){
	int malloc_size = parameters->alloc_mb * MB;
	long memory_counter = 0;

	while (1) {
		if(parameters->max_mb > 0)
			malloc_size = update_memory_counter(&memory_counter, parameters->alloc_mb, parameters->max_mb);
		if(malloc_size > 0){
			allocate_memory(malloc_size);
		}
		sleep(parameters->sleep_sec);
	}
}
