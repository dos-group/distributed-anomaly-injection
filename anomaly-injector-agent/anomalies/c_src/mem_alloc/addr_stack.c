#include "memory.h"

int init_size(addr_stack *self, unsigned long size){
	if(size == 0){
		fprintf(stderr, "Container size must be > 0\n");
		return -1;
	}
	if(self->elements != NULL){
		free(self->elements);
	}

	self->elements = (int**) malloc(size * sizeof(int*));
	if(!self->elements){
		fprintf(stderr, "Address container initialization failed\n");
		return -1;
	}

	self->index = 0;
	self->max_size = size;

	return 0;
}

int push(addr_stack *self, int* element){
	if(self->index > self->max_size){
		return -1;
	} else {
		self->elements[self->index] = element;
		self->index++;
	}
	return 0;
}

int* pop(addr_stack *self){
	if(self->index == 0){
		return NULL;
	} else {
		self->index--;
		return self->elements[self->index];
	}
}

addr_stack get_addr_container_instance(){
	addr_stack container;
	container.elements = NULL;
	container.index = 0;
	container.max_size = 0;
	container.init_size = init_size;
	container.push = push;
	container.pop = pop;
	return container;
}
