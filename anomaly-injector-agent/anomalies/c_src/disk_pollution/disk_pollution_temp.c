#include "disk.h"

void run_disk_pollution_temp(parameters *parameters){
	long wsize = parameters->write_size_mb * MB;
	long size_counter = 0;
	FILE* filep = tmpfile();

	if(filep != NULL){
		while (1) {
			if(parameters->max_size_mb > 0)
				wsize = update_size_counter(&size_counter, parameters->write_size_mb, parameters->max_size_mb);
			if(wsize > 0){
				write_to_disk(&filep, wsize, parameters->path_to_dir);
				if(parameters->newfile == 1){
					filep = tmpfile();
				}
			}else{
				sleep(IDLE_SLEEP_SEC);
			}
			sleep(parameters->sleep_sec);
		}
	}
}
