#include "syscall.h"

#include "stdlib.h"
#include "stdio.h"


#define Dim 41

int A[Dim][Dim];
int B[Dim][Dim];
int C[Dim][Dim];

int
main(int argc, char **argv)
{
	char id = argv[0][0]; int n = atoi (argv[1]);
	printf ("#%c#", id);
	printf ("#%d#", n);
	
	int i, j, k;
	
	for (i = 0; i < Dim; i++)		/* first initialize the matrices */
	for (j = 0; j < Dim; j++) {
		A[i][j] = i;
		B[i][j] = j;
		C[i][j] = 0;
	}
	
	for (i = 0; i < Dim; i++)		/* then multiply them together */
	for (j = 0; j < Dim; j++)
//	for (k = 0; k < Dim; k++)
//	C[i][j] += A[i][k] * B[k][j];
	C[i][j] = A[i][j] + B[i][j];
	
	//	printf("C[%d][%d] = %d\n", Dim-1, Dim-1, C[Dim-1][Dim-1]);
	
	while (n-- > 0) printf ("%c", id);
	
	return (C[Dim-1][Dim-1]);		/* and then we're done */
}
