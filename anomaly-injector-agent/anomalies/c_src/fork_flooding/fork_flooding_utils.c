#include "fork_flooding.h"

#define LOOP_COUNT 10000

void convert_to_timespec(struct timespec *time, long value){
	time->tv_sec = (time_t) (value / 1000);
	time->tv_nsec = (long) ((value % 1000) * 1000);
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

int busy_loop(){
	int i = 0;
	for(int i = 0; i < LOOP_COUNT; i++)
		i = (getpid() * 1000) / 23;
	return i * 5 + 5 / 7;
}
