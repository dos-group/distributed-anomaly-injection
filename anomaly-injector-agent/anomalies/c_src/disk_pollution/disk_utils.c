#include "disk.h"

#define FILE_NAME_MASK "fileXXXXXX"

#define CHUNK_WSIZE_CHAR (102400 / sizeof(char)) //around 100KB per write operation

char* _create_file_path(const char* path);
int _create_file(FILE** filep, char* path);
char* _create_write_data(long size);

long update_size_counter(long* size_counter, int wsize_mb, int max_wsize_mb){
	long wsize = (long) wsize_mb * (long) MB;
	long max_wsize = (long) max_wsize_mb * (long) MB;
	if((*size_counter + wsize) > max_wsize){
		wsize = max_wsize - *size_counter;
		*size_counter = *size_counter + wsize;
	}else{
		*size_counter = *size_counter + wsize;
	}

	return wsize;
}

int write_to_disk(FILE** filep, long wsize, const char* path){
	FILE* f = NULL;
	char* file_path = NULL;
	if(*filep == NULL){
		file_path = _create_file_path(path);
		if(_create_file(&f, file_path) != 0)
			return 1;
	}else{
		f = *filep;
	}

	char* data = NULL;
	if((data = _create_write_data(CHUNK_WSIZE_CHAR)) == NULL)
		return -1;

	long size = 0;
	for(size = 0; size < wsize; size = size + CHUNK_WSIZE_CHAR){
		if((size + CHUNK_WSIZE_CHAR) > wsize){
			int offset = (size + CHUNK_WSIZE_CHAR) - wsize;
			fprintf(f, "%s", data + offset);
			break;
		}else{
			fprintf(f, "%s", data);
		}
	}
	fflush(f);
	free(data);
	*filep = f;
	return 0;
}

char* _create_file_path(const char* path){
	int size_of_path = sizeof(path) + sizeof(PATH_SEPARATOR) + sizeof(FILE_NAME_MASK);
	char* path_to_file = (char*) malloc(size_of_path);
	strcpy(path_to_file, path);
	strcat(path_to_file, PATH_SEPARATOR);
	strcat(path_to_file, FILE_NAME_MASK);
	mkstemp(path_to_file);
	return path_to_file;
}

int _create_file(FILE** filep, char* path){
	mkstemp(path);
	FILE* fp = NULL;
	fp = fopen(path, "w");
	if(!fp)
		return -1;
	else
		*filep = fp;
	return 0;
}

char* _create_write_data(long size){
	char* data = (char*) malloc(size + 1);
	if(!data)
		return NULL;

	for(int i = 0; i <= size; i++){
		if(i == size){
			data[i] = '\0';
		}else if(i == size - 1){
			data[i] = '\n';
		}else{
			data[i] = 'a';
		}
	}
	return data;
}

int check_value(const int value, const int min, const int max){
	if(min == -1 && max == -1){
	}else if(min == -1){
		if(value > max){
			return 1;
		}
	}else if(max == -1){
		if(value < max){
			return 1;
		}
	}else{
		if(value < min || value > max){
			return 1;
		}
	}
	return 0;
}

int create_pollution_dir(const char* path){
	int err = mkdir(path, 0777);
	if(err != 0 && errno == EEXIST){
		return 0;
	}
	return err;
}

int remove_directory(const char *path){
	DIR *d = opendir(path);
	size_t path_len = strlen(path);
	int r = -1;

	if(d){
		struct dirent *p;
		r = 0;
		while(!r && (p=readdir(d))){
			int r2 = -1;
			char *buf;
			size_t len;
			/* Skip the names "." and ".." as we don't want to recurse on them. */
			if(!strcmp(p->d_name, ".") || !strcmp(p->d_name, "..")){
				continue;
			}
			len = path_len + strlen(p->d_name) + 2;
			buf = malloc(len);
			if(buf){
				struct stat statbuf;
				snprintf(buf, len, "%s%s%s", path, PATH_SEPARATOR, p->d_name);
				if(!stat(buf, &statbuf)){
					if (S_ISDIR(statbuf.st_mode)){
						r2 = remove_directory(buf); //recursive call
					}else{
						r2 = unlink(buf);
					}
				}
				free(buf);
			}
			r = r2;
		}
		closedir(d);
	}

	if(!r){
		r = rmdir(path);
	}
	return r;
}


