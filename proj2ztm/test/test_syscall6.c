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
	printf ("w%d\n", w2 (pid, buff));
}

int w2 (int pid, char * buff) {
	return write (pid, buff, strlen (buff));
}

void u (char * filename) {
	printf ("u%d\n", u2 (filename));
}

int u2 (char * filename) {
	return unlink (filename);
}

int main(int argc, char **argv)
{
	int i, fid, pid, status; char buffer[1025];
	int wres[10], ures[10], iw, iu;
	int afid[10], iaf;
	
	c ((char*) -1);
	o ((char*) -1);
	c ((char*) (15*1024));
	o ((char*) (15*1024));
	c ("");
	o ("");
	
	
	printf ("nitrate\n");
	
	
	w (-1, "entropy");
	w (0, "enthalpy");
	w (1, "Gibbs free energy\n");
	w (5, "temperature");
	w (16, "internal energy");
	
	
	printf ("nitrite\n");
	
	
	printf ("%d\n", creat ("glycine"));
	printf ("%d\n", creat ("alanine"));
	printf ("%d\n", creat ("proline"));
	printf ("%d\n", creat ("valine"));
	printf ("%d\n", creat ("leucine"));
	printf ("%d\n", creat ("isoleucine"));
	printf ("%d\n", creat ("methionine"));
	printf ("%d\n", creat ("phenylalanine"));
	printf ("%d\n", creat ("tyrosine"));
	printf ("%d\n", creat ("tryptophan"));
	printf ("%d\n", creat ("serine"));
	printf ("%d\n", creat ("threonine"));
	printf ("%d\n", creat ("cysteine"));
	printf ("%d\n", creat ("asparagine"));
	printf ("%d\n", creat ("glutamine"));
	printf ("%d\n", creat ("lysine"));
	printf ("%d\n", creat ("histidine"));
	printf ("%d\n", creat ("arginine"));
	printf ("%d\n", creat ("aspartate"));
	printf ("%d\n", creat ("glutamate"));
	
	for (i = 2; i < 16; i++) close (i);
	for (i = 2; i < 16; i++) close (i);
	
	unlink ("chemiosmotic");
	
	fid = creat ("chemiosmotic");
	w (fid, "     carnitine\n");
	w (fid, "malonyl\n");
	close (fid);
	fid = open ("chemiosmotic");
	printf ("%d\n%s", read (fid, buffer, 1024), buffer); memset (buffer, 0, sizeof(buffer));
	close (fid);
	
	fid = creat ("chemiosmotic");
	w (fid, "acyl-");
	printf ("%d\n%s", read (fid, buffer, strlen ("carnitine\n")), buffer);
	w (fid, "malonyl-CoA\n");
	close (fid);
	fid = open ("chemiosmotic");
	printf ("%d\n%s", read (fid, buffer, 1024), buffer); memset (buffer, 0, sizeof(buffer));
	close (fid);
	
	
	fid = open ("chemiosmotic");
	const char *a = "cholesterol";
	printf ("%d\n", a);
	printf ("%d\n%s\n", read (fid, (char*)a, 5), a);
	printf ("%d\n%s\n", read (fid, buffer, 5), buffer); memset (buffer, 0, sizeof(buffer));
	printf ("%d\n", read (fid, (char*) 1023, 5));
	close (fid);
	printf ("%d\n", read (-1, buffer, 1024));
	printf ("%d\n", read (0, buffer, 1024));
	printf ("%d\n", read (fid, buffer, 1024));
	printf ("%d\n", read (16, buffer, 1024));
	
	
	printf ("nitrogen\n");
	
	
	o ("sea_urchin");
	c ("sea_urchin");
	o ("sea_urchin");
	close (0);
	c ("allosteric");
	
	c ("opsonization");
	o ("opsonization");
	
	
	printf ("ammonia\n");
	
	
	u ("rubisco");
	c ("rubisco");
	u ("rubisco");
	u ("rubisco");
	
	
	printf ("PRPP\n");
	
	
	iaf = 0;
	u ("phosphoribosyl_pyrophosphate");
	printf ("cc%d\n", afid[iaf++] = creat ("phosphoribosyl_pyrophosphate"));
	printf ("oo%d\n", afid[iaf++] = open ("phosphoribosyl_pyrophosphate"));
	u ("phosphoribosyl_pyrophosphate");
	u ("phosphoribosyl_pyrophosphate");
	printf ("cc%d\n", afid[iaf++] = creat ("phosphoribosyl_pyrophosphate"));
	printf ("oo%d\n", afid[iaf++] = open ("phosphoribosyl_pyrophosphate"));
	u ("phosphoribosyl_pyrophosphate");
	u ("phosphoribosyl_pyrophosphate");
	printf ("cl%d\n", close (afid[1]));
	u ("phosphoribosyl_pyrophosphate");
	u ("phosphoribosyl_pyrophosphate");
	printf ("cl%d\n", close (afid[0]));
	u ("phosphoribosyl_pyrophosphate");
	u ("phosphoribosyl_pyrophosphate");
	c ("phosphoribosyl_pyrophosphate");
	
	
	printf ("stroma\n");
	
	
	fid = creat ("thylakoid");
	join (exec ("test_syscall6_1.coff", 0, 0), &status);
	close (fid);
	u ("thylakoid");
	printf ("%d\n", fid);
	
	
	char longstring[258]; char *my_argvl[1]; my_argvl[0] = longstring;
	for (i = 0; i < 257; i++) longstring[i] = 'a'; longstring[257] = 0;
	c (longstring);
	u (longstring);
	
	close (1);
	printf ("bovine");
	
	
/*u ("phosphoribosyl_pyrophosphate");
	iw = iu = 0;
	fid = creat ("phosphoribosyl_pyrophosphate");
	pid = exec ("test_syscall6_1.coff", 0, 0);
	wres[iw++] = w2 (fid, "Inosinate\n");
	wres[iw++] = w2 (fid, "Adenylosuccinate\n");
	wres[iw++] = w2 (fid, "Xanthylate\n");
	ures[iu++] = u2 ("phosphoribosyl_pyrophosphate");
	close (fid);
	ures[iu++] = u2 ("phosphoribosyl_pyrophosphate");
	ures[iu++] = u2 ("phosphoribosyl_pyrophosphate");
	join (pid, &status);
	printf ("Adenylate\n");
	printf ("%d\n", fid);
	for (i = 0; i < iw; i++) printf ("%d\t", wres[i]); printf ("\n");
	for (i = 0; i < iu; i++) printf ("%d\t", ures[i]); printf ("\n");
	u ("phosphoribosyl_pyrophosphate");	*/

	
	return 0;
}
