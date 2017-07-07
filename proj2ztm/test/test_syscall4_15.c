#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

int main(int argc, char **argv)
{
	char id = argv[0][0]; int n = atoi (argv[1]);
	printf ("#%c#", id);
	printf ("#%d#", n);
	while (n-- > 0) printf ("%c", id);
	
	return 0;
}
