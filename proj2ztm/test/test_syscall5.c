#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

int main(int argc, char **argv)
{
	close (stdin);
	
	printf ("%d\t%d\n", stdin, open (stdin));
	
	return 0;
}
