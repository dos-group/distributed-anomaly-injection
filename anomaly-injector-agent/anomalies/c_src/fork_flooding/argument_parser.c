#include "fork_flooding.h"

#define FLUCT_FLAG 'f'
#define SAFETY_FLAG 's'
#define MEM_ARG 222
#define SLEEP_ARG 333
#define MAX_PROC_ARG 444

#define MIN_SLEEP 1
#define DEFAULT_SLEEP 3000

#define MIN_MAX_PROCS 1
#define MAX_MAX_PROCS 60000
#define DEFAULT_MAX_PROCS 60000

#define MIN_MEM_KB 1
#define DEFAULT_MEM_KB 1


static int parse_opt(int key, char *arg, struct argp_state *state) {
	parameters *params = state->input;
	int value;
	switch (key) {
		case FLUCT_FLAG: {
			if(arg != NULL)
				fprintf(stderr, "Does not expect value for -\"%c\" argument. Value will be ignored.\n", FLUCT_FLAG);

			params->fluct = 1;
			break;
		}
		case SAFETY_FLAG: {
			if(arg != NULL)
				fprintf(stderr, "Does not expect value for -\"%c\" argument. Value will be ignored.\n", SAFETY_FLAG);

			params->safety = 1;
			break;
		}
		case MEM_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a memory value in MB for memory argument.");
				return -1;
			}

			value = atoi(arg);
			if(check_value(value, MIN_MEM_KB, -1) != 0){
				fprintf(stderr, "Invalid memory size. %d will be used instead.\n", DEFAULT_MEM_KB);
				value = DEFAULT_MEM_KB;
			}
			params->alloc_kb = value;
			break;
		}
		case MAX_PROC_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a maximal amount of processes to be forked for the max_proc_fork argument.");
				return -1;
			}

			value = atoi(arg);
			if(check_value(value, MIN_MAX_PROCS, MAX_MAX_PROCS) != 0){
				fprintf(stderr, "Invalid max_memory size. %d will be used instead.\n", DEFAULT_MAX_PROCS);
				value = DEFAULT_MAX_PROCS;
			}
			params->fork_number = value;
			break;
		}
		case SLEEP_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a sleep value in milliseconds for sleep argument.");
				return -1;
			}
			value = atoi(arg);
			if(check_value(value, MIN_SLEEP, -1) != 0){
				fprintf(stderr, "Invalid sleep value. %d will be used instead.\n", DEFAULT_SLEEP);
				value = DEFAULT_SLEEP;
			}
			convert_to_timespec(&params->sleep_between_forks, value);
			break;
		}
		case ARGP_KEY_INIT: {
			params->alloc_kb = DEFAULT_MEM_KB;
			params->fluct = 0;
			params->safety = 0;
			params->fork_number = DEFAULT_MAX_PROCS;
			convert_to_timespec(&params->sleep_between_forks, DEFAULT_SLEEP);
			break;
		}
		case ARGP_KEY_ARG: {
			argp_error(state, "Unexpected argument.\n");
			break;
		}
		case ARGP_KEY_END: {
			break;
		}
	}
	return 0;
}

int parse_arguments(parameters *parameters, int argc, char **argv){
	char *max_proc_msg;
	asprintf(&max_proc_msg, "Amount of forks which should be executed. "\
				"If the safety flag is enabled, the amount of forks is limited by the "\
				"system's boundary.");
	struct argp_option options[] = {
		{ 0, FLUCT_FLAG, 0, 0, "Enable the fluctuating of the amount of child processes." },
		{ 0, SAFETY_FLAG, 0, 0, "Safety flag. Prevents total system destruction if running for longer time." },
		{ "memory", MEM_ARG, "NUM_KB", 0, "Amount of memory which should be allocated"\
				" for each forked child process (in kB)." },
		{ "num_forks", MAX_PROC_ARG, "NUM", 0, max_proc_msg},
		{ "sleep", SLEEP_ARG, "NUM_MS", 0, "Time which should be waited between a certain amount of forks "\
				"(in milliseconds)." },
		{ 0 }
	};
	struct argp argp = { options, parse_opt, ""};
	return argp_parse(&argp, argc, argv, 0, 0, parameters);
}
