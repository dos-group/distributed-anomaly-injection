#include "disk.h"

parameters params;

//Set target dir to pollute
int set_pollution_dir(parameters* params){
	params->path_to_dir = (char*) malloc(sizeof(POLLUTION_SUBDIR) / sizeof(char));
	strcpy(params->path_to_dir, POLLUTION_SUBDIR);

	return 0;
}

void print_params(parameters* params){
	printf("Path to pollution directory: %s\nMB per write operation: %dMB\n"\
			"Max write size: %dMB\nSleep time: %ds\nTemporal file: %d\n"\
			"New file flag: %d\n", params->path_to_dir, params->write_size_mb,
			params->max_size_mb, params->sleep_sec, params->temp, params->newfile);
}

void sig_handler(int signo){
	if(params.clean)
		remove_directory(params.path_to_dir);
	exit(0);
}

int main(int argc, char **argv) {
	signal(SIGINT, sig_handler);
	signal(SIGTERM, sig_handler);

	if(parse_arguments(&params, argc, argv) != 0)
		return -1;

	if(set_pollution_dir(&params) != 0 || create_pollution_dir(params.path_to_dir) != 0){
		fprintf(stderr, "Setting up pollution directory failed.\n");
		return -1;
	}

	if(params.temp == 0){
		run_disk_pollution(&params);
	}else if(params.temp == 1){
		run_disk_pollution_temp(&params);
	}

	return 0;
}
