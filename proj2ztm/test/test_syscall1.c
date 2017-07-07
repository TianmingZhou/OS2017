#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

void j (int pid) {
	int status = -19960305, ret;
	ret = join (pid, &status);
	printf ("join: pid = %d\tret = %d\tstatus = %d\n", pid, ret, status);
}

void j2 (int pid, int _status) {
	int ret = join (pid, (int*) _status);
	printf("join: pid = %d\tret = %d\tstatus = %d\n", pid, ret);
}

int main(int argc, char **argv)
{
	printf ("I love biology begin\n");
	
	int pid, i;
	char my_argv0[ ] = "Sept";
	char my_argv1[ ] = "13";
	char my_argv2[ ] = "1996";
	char my_argv3[ ] = "DeiDuiDiuDuang";
	char my_argv4[241];
	char *my_argv[5] = {my_argv0, my_argv1, my_argv2, my_argv3, my_argv4};
	for (i = 0; i < 240; i++) my_argv4[i] = (i % 26) + 'a'; my_argv4[240] = 0;
	for (i = 0; i < 5; i++) printf ("%s\n", my_argv[i]);
	
	j (0);	// My PID is this number
	j (-1);
	j (1);
	
	pid = exec ("test_syscall1_1.coff", 0, 0);
	printf ("embryo");
	j (2);
	j (pid);
	
	j (exec ("echo.coff", 5, my_argv));
	j (exec ("echo.coff", 4, my_argv));
//	j (exec ("echo.coff", 4, my_argv+1));
	j (exec ("echo.coff", 6, my_argv));
	
	pid = exec ("echo.coff", 4, my_argv);
	printf ("pid of child process is %d\n", pid);
	printf ("Endonuclease\n");
	j (pid);
	
	printf ("BiuBiuBiu\n");
	
	j (exec ("47.coff", 1, my_argv));
	j (exec ("echo.c", 1, my_argv));
	
	j (exec ("echo.coff", 0, 0));
	
	printf ("Xenopus laevis\n");
	
	j2 (pid = exec ("echo.coff", 5, my_argv), -1); j (pid);
	j2 (pid = exec ("echo.coff", 5, my_argv), 15355); j (pid);
	printf ("%d\t", (int) *((char*)15355));
	printf ("%d\t", (int) *((char*)15356));
	printf ("%d\t", (int) *((char*)15357));
	printf ("%d\n", (int) *((char*)15355));
	j2 (pid = exec ("echo.coff", 5, my_argv), 15358); j (pid);
	j2 (pid = exec ("echo.coff", 5, my_argv), 15360); j (pid);
	
	printf ("zebrafish\n");
	
	j (exec ("echo.coff", -1, my_argv));
	j (exec ("echo.coff", 5, (char **) -1));
	
	printf ("Drosophila melanogaster\n");
	char longstring[258]; char *my_argvl[1]; my_argvl[0] = longstring;
	for (i = 0; i < 252; i++) longstring[i] = 'a';
	longstring[i++] = '.';
	longstring[i++] = 'c';
	longstring[i++] = 'o';
	longstring[i++] = 'f';
	longstring[i++] = 'f';
	longstring[i++] = 0;
	j (exec (longstring, 0, 0));
	j (exec ("echo.coff", 1, my_argvl));
	
	printf ("test_syscall1 end\n");
	return 0;
}
