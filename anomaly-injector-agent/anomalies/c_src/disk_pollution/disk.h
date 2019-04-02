#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <signal.h>
#include <sys/types.h>
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>



#if defined(WIN32) || defined(_WIN32)
#define PATH_SEPARATOR "\\"
#define POLLUTION_SUBDIR ".\\p"
#else
#define PATH_SEPARATOR "/"
#define POLLUTION_SUBDIR "/tmp/p"
#endif

#define MB (1024*1024)
#define MB_INTS (MB/sizeof(int))

#define IDLE_SLEEP_SEC 60

typedef struct parameters{
	char* path_to_dir;
	unsigned int write_size_mb;
	int max_size_mb;
	unsigned int sleep_sec;
	int temp;
	int newfile;
	int clean;
	int free_fp;
} parameters;

int parse_arguments(parameters *parameters, int argc, char **argv);

void run_disk_pollution(parameters *parameters);
void run_disk_pollution_temp(parameters *parameters);

long update_size_counter(long* memory_counter, int alloc_mb, int max_memory);
int write_to_disk(FILE** filep, long wsize, const char* path);
int check_value(int value, int min, int max);
int create_pollution_dir(const char* path);
int remove_directory(const char* path);

int write_to_disk(FILE** filep, long wsize, const char* path);
