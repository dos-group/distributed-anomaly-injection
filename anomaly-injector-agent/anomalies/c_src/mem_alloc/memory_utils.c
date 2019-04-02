#include "memory.h"

long update_memory_counter(long* memory_counter, int alloc_mb, int max_memory){
	long malloc_size = (long) alloc_mb * (long) MB;
	long max_malloc_size = (long) max_memory * (long) MB;
	if((*memory_counter + malloc_size) > max_malloc_size){
		malloc_size = max_malloc_size - *memory_counter;
		*memory_counter = *memory_counter + malloc_size;
	}else{
		*memory_counter = *memory_counter + malloc_size;
	}

	return malloc_size;
}

int* allocate_memory(long malloc_size){
	int *addr = (int*) malloc(malloc_size);
	if (!addr) {
	    fprintf(stderr, "Memory allocation failed\n");
	} else {
	    // Touch the data to make the OS actually allocate the pages
	    for (long i = 0; i < (malloc_size / sizeof(int)); i++) {
	    	addr[i] = i;
	    }
	}

	return addr;
}

int check_value(int value, int min, int max){
	if(min == -1 && max == -1){
	}else if(min == -1){
		if(value > max){
			return 1;
		}
	}else if(max == -1){
		if(value < max){
			return 1;
		}
	}else{
		if(value < min || value > max){
			return 1;
		}
	}
	return 0;
}
