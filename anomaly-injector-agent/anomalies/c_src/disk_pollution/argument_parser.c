#include <argp.h>

#include "disk.h"

#define TEMP_ARG 't'
#define NEWFILE_ARG 'n'
#define CLEAN_ARG 'c'
#define FREE_FILE_POINTER_ARG 'f'
#define WSIZE_ARG 222
#define MAX_WSIZE_ARG 333
#define SLEEP_ARG 444

#define MIN_SLEEP 0
#define DEFAULT_SLEEP 1

#define MIN_WSIZE_MB 1

#define MIN_MAX_WSIZE_MB 1
#define DEFAULT_MAX_WSIZE_MB 1000


static int parse_opt(int key, char *arg, struct argp_state *state) {
	parameters *params = state->input;
	int value;
  	switch (key) {
		case TEMP_ARG: {
			if(arg != NULL)
				fprintf(stderr, "Does not expect value for -\"%c\" argument. Value will be ignored.\n", TEMP_ARG);
 			params->temp = 1;
			break;
		}
		case NEWFILE_ARG: {
			if(arg != NULL)
				fprintf(stderr, "Does not expect value for -\"%c\" argument. Value will be ignored.\n", NEWFILE_ARG);
			params->newfile = 1;
			break;
		}
		case CLEAN_ARG: {
			if(arg != NULL)
				fprintf(stderr, "Does not expect value for -\"%c\" argument. Value will be ignored.\n", CLEAN_ARG);
			params->clean = 1;
			break;
		}
		case FREE_FILE_POINTER_ARG: {
			if(arg != NULL)
				fprintf(stderr, "Does not expect value for -\"%c\" argument. Value will be ignored.\n", FREE_FILE_POINTER_ARG);
			params->free_fp = 1;
			break;
		}
		case WSIZE_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a size value in MB for write size argument.");
				return -1;
			}

			value = atoi(arg);
			if(check_value(value, MIN_WSIZE_MB, -1) != 0){
				fprintf(stderr, "Invalid size value. %d will be used instead.\n", MIN_WSIZE_MB);
				value = MIN_WSIZE_MB;
			}
			params->write_size_mb = value;
			break;
		}
		case MAX_WSIZE_ARG: {
			if(arg == NULL){
				argp_error(state, "Expect a maximal size value in MB for max_wsize argument.");
				return -1;
			}

			value = atoi(arg);
			if(check_value(value, MIN_MAX_WSIZE_MB, -1) != 0){
				fprintf(stderr, "Invalid max. write size value. %d will be used instead.\n", DEFAULT_MAX_WSIZE_MB);
				value = DEFAULT_MAX_WSIZE_MB;
			}
			params->max_size_mb = value;
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
			params->write_size_mb = MIN_WSIZE_MB;
			params->max_size_mb = -1;
			params->temp = 0;
			params->newfile = 0;
			params->clean = 1;
			params->free_fp = 1;
			params->sleep_sec = DEFAULT_SLEEP;
			break;
		}
		case ARGP_KEY_ARG: {
			argp_error(state, "Unexpected argument.\n");
			break;
		}
		case ARGP_KEY_END: {
			if(params->temp == 1){
				if(params->clean == 1){
					fprintf(stderr, "Argument \"%c\" does not have any effect when the temporal "\
							"file flag \"%c\" is set. Will be ignored.\n", CLEAN_ARG, TEMP_ARG);
				}
				if(params->free_fp == 1){
					fprintf(stderr, "Argument \"%c\" does not have any effect when the temporal "\
							"file flag \"%c\" is set. Will be ignored.\n", FREE_FILE_POINTER_ARG, TEMP_ARG);
				}
			}
			break;
		}
	}
  	return 0;
}

int parse_arguments(parameters *parameters, int argc, char **argv){
	printf("%s\n", argv[0]);
	struct argp_option options[] = {
		{ "temp", TEMP_ARG, 0, 0, \
				"Temporal file flag. If set, a temporal file will be created and written into. "\
				"If the process is terminated regularly, the file will be automatically deleted." },
		{ "newfile", NEWFILE_ARG, 0, 0, \
				"After each sleep period the next write operation will be done into a new file. "\
				"Therefore, a new file is created." },
		{ "clean", CLEAN_ARG, 0, 0, \
				"The process will clean its pollution directory before termination by SIGINT or SIGTERM signals. "\
				"This flag is ignored if the temporal file flag is set." },
		{ "free_fp", FREE_FILE_POINTER_ARG, 0, 0, \
				"If this flag is set, the process will accordingly close the file pointers. "\
				"If not, it will keep holding the file pointers. This flag is ignored if the temporal file flag is set." },
		{ "write-size", WSIZE_ARG, "NUM_MB", 0, \
				"Amount of MB which are written into the file after each sleep period." },
		{ "max-wsize", MAX_WSIZE_ARG, "NUM_MB", 0, \
				"Maximum amount of MB which should be written to files. The process will step into "\
				"an idle mode if this size is reached." },
		{ "sleep", SLEEP_ARG, "NUM_S", 0, \
				"Time which should be waited after each write operation (in Seconds)." },
		{ 0 }
	};
	struct argp argp = { options, parse_opt, ""};
	return argp_parse(&argp, argc, argv, 0, 0, parameters);
}
