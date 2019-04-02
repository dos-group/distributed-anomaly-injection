#include "fork_flooding.h"

void print_fork_params(parameters* params){
	printf( "Time between forks: %ld.%lds\n"\
			"Memory allocation size: %dkB\n"\
			"Max. amount of processes: %d\n"\
			"Fluctuating: %d\n"\
			"Safety: %d\n",
			params->sleep_between_forks.tv_sec, params->sleep_between_forks.tv_nsec,
			params->alloc_kb, params->fork_number, params->fluct, params->safety);
}

void signal_callback_handler(int signum){
	while (waitpid(0, NULL, WNOHANG) !=- 1);
	exit(0);
}

int main(int argc, char **argv) {
	signal(SIGINT, signal_callback_handler);
	signal(SIGTERM, signal_callback_handler);

	parameters params;
	parse_arguments(&params, argc, argv);

	if(params.fluct == 1)
		run_fork_flooding_fluct(&params);
	else
		run_fork_flooding_leak(&params);

	return 0;
}


