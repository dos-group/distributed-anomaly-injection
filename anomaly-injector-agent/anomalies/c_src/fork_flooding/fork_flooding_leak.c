/**
Massive forking of sub processes.
*/
#include "fork_flooding.h"

#define OP_MODE_FORK 1
#define OP_MODE_KILL 2

#define PID_RING_SIZE 150


int handle_fork_growing(fork_growing_setting* settings);
void kill_porcs(pid_t* pid_ring);

void run_fork_flooding_leak(parameters *params){
	int child_counter = 0, got_memory = 0;
	int max_childs_reached = 0;
	pid_t pid = 1, pid_root;
	fork_growing_setting growing_settings = { 0, 1, 2 };

	pid_t pid_ring[PID_RING_SIZE];

	pid_root = getpid();

	while(1){
		if(getpid() == pid_root){
			if(child_counter >= params->fork_number)
				max_childs_reached = 1;
			if(max_childs_reached == 0){
				child_counter++;
				pid = fork();
			}
		}
		if(pid == -1){
			pid = -2;
			if(params->safety){
				kill_porcs(pid_ring);
				max_childs_reached = 1;
			}
			busy_loop();
			nanosleep(&params->sleep_between_forks, NULL);
		}else if(pid == 0){
			if(params->alloc_kb != 0 && got_memory == 0){
				size_t malloc_size = params->alloc_kb * KB;
				allocate_memory(malloc_size);
				got_memory = 1;
			}
			busy_loop();
			nanosleep(&params->sleep_between_forks, NULL);
		}else{
			if(max_childs_reached == 1){
				busy_loop();
				nanosleep(&params->sleep_between_forks, NULL);
			}else{
				pid_ring[child_counter % PID_RING_SIZE] = pid;
				if(handle_fork_growing(&growing_settings) == 0){
					busy_loop();
					nanosleep(&params->sleep_between_forks, NULL);
				}
			}
		}
	}
}

int handle_fork_growing(fork_growing_setting* settings){
	if(settings->current_count >= settings->current_max_count){
		settings->current_count = 0;
		settings->current_max_count = settings->current_max_count * settings->growing_factor;
		return 0;
	}else{
		settings->current_count++;
		return settings->current_max_count - settings->current_count;
	}
}

void kill_porcs(pid_t* pid_ring){
	for(int i = 0; i < PID_RING_SIZE; i++){
		if(pid_ring[i] > 0){
			kill(pid_ring[i], SIGTERM);
			while (waitpid(pid_ring[i], NULL, WNOHANG) !=- 1);
		}
	}
}
