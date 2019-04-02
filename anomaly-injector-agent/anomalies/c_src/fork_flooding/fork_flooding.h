#define _GNU_SOURCE
#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <argp.h>
#include <time.h>

#define KB 1024
#define KB_INTS (KB/sizeof(int))

typedef struct {
	unsigned int alloc_kb;
	int fluct;
	int safety;
	unsigned int fork_number;
	struct timespec sleep_between_forks;
} parameters;

typedef struct{
	int current_count;
	int current_max_count;
	int growing_factor;
} fork_growing_setting;

int parse_arguments(parameters *parameters, int argc, char **argv);

void run_fork_flooding_leak(parameters *params);
void run_fork_flooding_fluct(parameters *params);

int* allocate_memory(long malloc_size);
void convert_to_timespec(struct timespec *time, long value);
int check_value(int value, int min, int max);
int busy_loop();
