#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

void j (int pid) {
	int status = -19960305, ret;
	ret = join (pid, &status);
	printf("join: pid = %d\tret = %d\tstatus = %d\n", pid, ret, status);
}

int main(int argc, char **argv)
{
	j (exec ("test_syscall1_0.coff", 0, 0));
	
	return 111;
}
