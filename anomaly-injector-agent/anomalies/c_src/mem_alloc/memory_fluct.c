#include "memory.h"

#define OP_ALLOC 1
#define OP_FREE 2

int calc_container_size(int alloc_mb, int max_mb);

void run_memory_fluct(parameters *parameters){
	long malloc_size = 0l;
	long memory_counter = 0;
	int *addr = NULL;
	int operation_mode = OP_ALLOC;

	addr_stack adr_container = get_addr_container_instance();
	int container_size = parameters->max_mb / parameters->alloc_mb;
	if(parameters->max_mb % parameters->alloc_mb != 0)
		container_size = container_size + 1;
	if(adr_container.init_size(&adr_container, container_size) != 0){
		fprintf(stderr, "Address container initiation failed.\n");
		return;
	}

	while (1) {
		if(operation_mode == OP_ALLOC){
			malloc_size = update_memory_counter(&memory_counter, parameters->alloc_mb, parameters->max_mb);
			if(malloc_size > 0){
				addr = allocate_memory(malloc_size);
				if(addr){
					adr_container.push(&adr_container, addr);
				}
			}else{
				operation_mode = OP_FREE;
			}
		} else if(operation_mode == OP_FREE){
			addr = adr_container.pop(&adr_container);
			if(!addr){
				memory_counter = 0;
				operation_mode = OP_ALLOC;
			}else{
				free(addr);
			}
		}
		sleep(parameters->sleep_sec);
	}
}

int calc_container_size(int alloc_mb, int max_mb){
	int container_size = max_mb / alloc_mb;

	return container_size;
}
