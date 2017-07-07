#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

int main(int argc, char **argv)
{
	exec ("halt.coff", 0, 0);
	printf ("apolipoprotein");
	halt ();
	return 0;
}
