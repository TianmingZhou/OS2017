#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

// -m

int r (char *filename, char id, char *n) {
	unsigned char my_argv0[2] = {id, 0};
	char *my_argv[2] = {my_argv0, n};
	return exec (filename, 2, my_argv);
}

void jj (int pid, int *status, int *ret) {
	*status = -19960305;
	*ret = join (pid, status);
//	printf ("join: pid = %d\tret = %d\tstatus = %d\n", pid, ret, status);
}

int main(int argc, char **argv)
{
#define N 6
#define j(x) jj (pid[x], status+(x), ret+(x))
	int pid[N]; int status[N]; int ret[N];
	int i = 0; int id = 'a';
	
	pid[i++] = r ("test_syscall4_15.coff", id++, "200");
	pid[i++] = r ("test_syscall4_15.coff", id++, "510");
	pid[i++] = r ("test_syscall4_20.coff", id++, "500");
	pid[i++] = r ("test_syscall4_19.coff", id++, "200");
	j (0); j(3);
	pid[i++] = r ("test_syscall4_35.coff", id++, "50");
	pid[i++] = r ("test_syscall4_34.coff", id++, "50");
	
	j (1); j (2); j (4); j (5);
	for (i = 0; i < N; i++) printf ("pid = %d\tret = %d\tstatus = %d\n", pid[i], ret[i], status[i]);
	
	return 0;
}
