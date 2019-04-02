#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

#define MB (1024*1024)
#define MB_INTS (MB/sizeof(int))

typedef struct parameters{
	unsigned int alloc_mb;
	int max_mb;
	unsigned int sleep_sec;
	int fluct;
} parameters;

typedef struct addr_stack addr_stack;
struct addr_stack {
	int** elements;
	unsigned int index;
	unsigned long max_size;
	int (*init_size)(addr_stack *self, unsigned long max_size);
	int (*push)(addr_stack *self, int* element);
	int* (*pop)(addr_stack *self);
};

int parse_arguments(parameters *parameters, int argc, char **argv);

void run_memory_leak(parameters *parameters);
void run_memory_fluct(parameters *parameters);

addr_stack get_addr_container_instance();

long update_memory_counter(long* memory_counter, int alloc_mb, int max_memory);
int* allocate_memory(long malloc_size);
int check_value(int value, int min, int max);
