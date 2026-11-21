package prouesse;

import java.util.BitSet;

// Class PhaseB2_Super - encapsulates (static) variables used by its subclasses.
//
// Role: gather the working variables of Modules 2, 3 and 4 (Module_2A/2B/2B_V1, Module_3/3_V1 and Module_4),
// which inherit from this class and therefore share the same arrays. This class countains no logic: the variables
// are only declared here and are allocated by the subclass PhaseB2_E (and by the constructors of the modules).
// Code names x notation of the book (Sections 4.1.2, 4.3, 4.4 and 4.5) are in the comments below.
public class PhaseB2_Super extends Probl {

	// --- Search variables ---
	// i      = index of the current unit;  x0 = candidate successor of x[i] determined by the maximum* methods;
	// i1, p, j, a, k, aux* = indices and timerary variables;  n0 = number of lessons of the unit; nLessons = lessons in the second group of days.
	// case_, Ap, Aa, j_, A, x_, maxX0, iMaxI_: redeclare the unit arrays (see Probl); in this hierarchy they are the ones used
	// by Modules 2 and 4. Modules 1, 3 and 5 use the homonymous arrays of Probl (a separate set).
	//
	// --- Linking and control variables (Sections 4.3 to 4.5) ---
	// wp0[p], wp1[p] = w_0p, w_1p: working days of p in (d0,d1) and in (d2,d3);   Wp1[p] = W_1p: maximum number of days in the 4 days of the phase.
	// Np0[p], Np1[p] = N_0p, N_1p: maximum number of lessons of p in (d0,d1) and in (d2,d3);
	// Np2[p], Np3[p] = N_2p, N_3p: maximum number of lessons of p on the 1st and on the 2nd day of the pair (cases 3, 35 and Modules 3 and 4);
	// Np2_0[p], Np3_0[p] = the same for the pair (d2,d3) (case 30/305).  Np2_, Np3_, Sa02_, Sa03_: reserved.
	// Dp[p] = D_p (minimum number of working days); complex_p0[p], complex_p1[p] = complex_0p, complex_1p;
	// x_case_1[p], x_case_10[p] = x_case1, x_case10 (value of the unit of cases 1 and 10 of Teacher p).
	// q_0[p][j] = lessons of Teacher p in the j-th class on the days handled by the module; q2_[p][j] and q2_0[p][j] = lessons of that class
	// in the first and in the second group of days (result of case 2).
	// Sp0_, Sp1_, Sp2_, Sp3_, Sa_0, Sa_1, Sa_4, Sa_5 = control variables S_p0_, S_p1_, S_p2_, S_p3_, S_a0, S_a1, S_a4, S_a5.
	// The Sa_* arrays hold, for each class, a block of 16 positions (auxA[a] = next free position of the block of class a);
	// pointerP, pointerP2, pointerA, pointerA2 = pointers that tell, for each unit, at which position of the control arrays it acts.
	// d_0..d_3 (and days[]) = days handled by the module;  nDays_ = {0,1,1,2,3}: number of working days associated with each value of x[k]
	// when complex = true.  possibP[exec][k][p] = k-th possibility of working days (true if p works on the 1st day of the pair);
	// nPossibP[exec] = number of possibilities;  exec = 0 for the pair (d0,d1) and 1 for the pair (d2,d3).
	// g2_ = instance of Module 3 called by Module 2;  success, test*, case_Trivial = timerary boolean flags.
	static protected int i, x0, i1, p, j, a, k, nLessons, Sp0, Sa0, Sp2, Sa2, aux0, n0, aux1, aux2, wp0[], wp1[], Np0[],
			Np1[], Np2[], Np3[], Np2_0[], Np3_0[], Np2_[], Np3_[], x1, days[], d_0_, d_1_, d_0, d_1, d_2, Wp1[],
			Sa02_[], Sa03_[], imax, case_[], Ap[], Aa[], j_[], A[], x_[], maxX0[], n2, q2_[][], q2_0[][], q_0[][],
			pointerP[], pointerP2[], pointerA[], pointerA2[], aux, Sa_0[], Sa_1[], Sa_4[], Sa_5[], Sp0_[], Sp1_[], Sp2_[],
			Sp3_[], count, iMaxI_[], auxA[];

	static protected boolean test1, test2, case_Trivial;
	static protected int Dp[], arranjo[], nDays_[], x_case_1[], x_case_10[], d_3, aux4, aux5, aux41, aux51, sumA_0[],
			sumA_1[], nPossibP[], i1_, possib, exec;
	static protected boolean complex_p0[], complex_p1[], success, test, possibP[][][];
	static protected long maxIterations2, nIterations;
	static protected BitSet bitSet0, bitSet1;
	static protected Module_3 g2_;
	static protected short z[][];

}
