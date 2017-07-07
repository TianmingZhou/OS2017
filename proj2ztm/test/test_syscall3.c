#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

int main(int argc, char **argv)
{
	exec ("delay_exit.coff", 0, 0);
	exit (0);
	return 0;
}
