#include "disk.h"

void run_disk_pollution(parameters *parameters){
	long wsize = parameters->write_size_mb * MB;
	long size_counter = 0;
	FILE* filep = NULL;

	while (1) {
		if(parameters->max_size_mb > 0)
			wsize = update_size_counter(&size_counter, parameters->write_size_mb, parameters->max_size_mb);
		if(wsize > 0){
			write_to_disk(&filep, wsize, parameters->path_to_dir);
			if(filep != NULL && parameters->newfile == 1){
				if(parameters->free_fp == 1)
					fclose(filep);
				filep = NULL;
			}
		}else{
			sleep(IDLE_SLEEP_SEC);
		}
		sleep(parameters->sleep_sec);
	}
}
