#include "fork_flooding.h"

#define FORK_CHUNK_SIZE 3000

#define OP_MODE_FORK 1
#define OP_MODE_KILL


void run_fork_flooding_fluct(parameters *params){
	int fork_counter = 0, got_memory = 0;
	int max_childs_reached = 0;
	pid_t pid = 1, pid_root;

	pid_root = getpid();

	while(1){
		if(getpid() == pid_root){
			if(max_childs_reached == 0){
				fork_counter++;
				pid = fork();
			}
		}
		if(pid == -1){
			pid = -2;
			fork_counter = params->fork_number;
			if(params->safety == 1)
				max_childs_reached = 1;
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
			break;
		}else{
			if(fork_counter % params->fork_number == 0 || max_childs_reached == 1){
				nanosleep(&params->sleep_between_forks, NULL);
				fork_counter = 0;
				max_childs_reached = 0;
				while (waitpid(0,NULL,WNOHANG)!=-1);
				busy_loop();
				nanosleep(&params->sleep_between_forks, NULL);
			}
		}
	}
}
