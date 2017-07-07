#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

void p (int *a) {printf ("%d\t%d\n", a, *a);}
void q (int a) {p ((int*) a);}

int main(int argc, char **argv)
{
	int a = 1;
	
	p (&a);
	p ((&a) -1);
	
	printf ("%d\n", argv);
	printf ("%d\n", &argc);
	
	*((int*)4096) = 4;
	*((int*)15356) = 4;
//	*((int*)-1) = 4;	// -> address error
//	*((int*)-4) = 4;	// -> page fault
//	*((int*)15360) = 4;	// -> page fault
//	*((int*)15359) = 4;	// -> address error
//	*((int*)15361) = 4;	// -> address error
//	*((int*)0) = 4;		// -> read-only
//	*((int*)4092) = 4;	// -> read-only
//	*((int*)4097) = 4;	// -> address error
	
	return 0;
}
