#include <argp.h>
#include "memory.h"

#define FLUCT_ARG 'f'
#define MEM_ARG 222
#define SLEEP_ARG 333
#define MAX_MEM_ARG 444


#define MIN_SLEEP 0
#define DEFAULT_SLEEP 5
#define MIN_MEM_MB 1
#define MIN_MAX_MEM_MB 500


static int parse_opt(int key, char *arg, struct argp_state *state) {
	parameters *params = state->input;
	int value;
  	switch (key) {
		case FLUCT_ARG: {
			if(arg != NULL)
				fprintf(stderr, "Does not expect value for -\"%c\" argument. Value will be ignored.\n", FLUCT_ARG);

 			params->fluct = 1;
			break;
		}
		case MEM_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a memory value in MB for memory argument.");
				return -1;
			}

			value = atoi(arg);
			if(check_value(value, MIN_MEM_MB, -1) != 0){
				fprintf(stderr, "Invalid memory size. %d will be used instead.\n", MIN_MEM_MB);
				value = MIN_MEM_MB;
			}
			params->alloc_mb = value;
			break;
		}
		case MAX_MEM_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a maximal memory value in MB for max_memory argument.");
				return -1;
			}

			value = atoi(arg);
			if(check_value(value, MIN_MEM_MB, -1) != 0){
				fprintf(stderr, "Invalid max_memory size. %d will be used instead.\n", MIN_MAX_MEM_MB);
				value = MIN_MAX_MEM_MB;
			}
			params->max_mb = value;
			break;
		}
  		case SLEEP_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a sleep value in seconds for sleep argument.");
				return -1;
			}
			value = atoi(arg);
			if(check_value(value, MIN_SLEEP, -1) != 0){
				fprintf(stderr, "Invalid sleep value. %d will be used instead.\n", MIN_SLEEP);
				value = MIN_SLEEP;
			}
			params->sleep_sec = value;
			break;
		}
		case ARGP_KEY_INIT: {
			params->alloc_mb = MIN_MEM_MB;
			params->max_mb = 0;
			params->fluct = 0;
			params->sleep_sec = DEFAULT_SLEEP;
			break;
		}
		case ARGP_KEY_ARG: {
			argp_error(state, "Unexpected argument.\n");
			break;
		}
		case ARGP_KEY_END: {
			if(params->fluct == 1 && params->max_mb == 0){
				argp_error(state, "If the fluctuation flag \'-%c\' is set a maximal memory value must be defined.",
						FLUCT_ARG);
				return -1;
			}
			break;
		}
	}
  	return 0;
}

int parse_arguments(parameters *parameters, int argc, char **argv){
	struct argp_option options[] = {
		{ 0, FLUCT_ARG, 0, OPTION_ARG_OPTIONAL, \
				"Enable the fluctuating memory usage. If this flag is set, a maximal memory must be defined with"\
				" --max_memory=NUM_MB." },
		{ "memory", MEM_ARG, "NUM_MB", 0, \
				"Amount of memory which should be allocated per allocation step (in MB)." },
		{ "max_memory", MAX_MEM_ARG, "NUM_MB", 0, \
				"Maximum amount of memory which should be allocated (in MB)." },
		{ "sleep", SLEEP_ARG, "NUM_S", 0, \
				"Time which should be waited after each allocation/free step (in Seconds)." },
		{ 0 }
	};
	struct argp argp = { options, parse_opt, ""};
	return argp_parse(&argp, argc, argv, 0, 0, parameters);
}
