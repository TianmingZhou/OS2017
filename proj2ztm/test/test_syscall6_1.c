#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"

void c (char * filename) {
	int fid = creat (filename);
	printf ("c%d\n", fid);
	close (fid);
}

void o (char * filename) {
	int fid = open (filename);
	printf ("o%d\n", fid);
	close (fid);
}

void w (int pid, char * buff) {
	printf ("w%d\n", write (pid, buff, strlen (buff)));
}

void u (char * filename) {
	printf ("u%d\n", unlink (filename));
}

int main(int argc, char **argv)
{
/*	int fid, fid2;  char buffer[1025];
	printf ("%d\n", fid = open ("phosphoribosyl_pyrophosphate"));
	printf ("%d\n", fid2 = open ("phosphoribosyl_pyrophosphate"));
	w (fid, "ribonucleotide reductase\n");
	w (fid, "nucleoside diphosphate kinase\n");
	w (fid, "deaminase\n");
	w (fid, "dUTPase\n");
	w (fid, "thymidylate synthase\n");
	close (fid);
	u ("phosphoribosyl_pyrophosphate");
	
	printf ("Guanylate\n");
	printf ("%d\n%s", read (fid2, buffer, 1024), buffer); memset (buffer, 0, sizeof(buffer));
	close (fid2);	*/
	
	int fid;
	fid = creat ("thylakoid");
	close (fid);
	u ("thylakoid");
	printf ("%d\n", fid);
	
	return 0;
}
