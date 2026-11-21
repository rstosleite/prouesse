package prouesse;

import java.util.BitSet;

// Class Module_3_V1 - implements Module 3 (Phase 1.2; Sections 4.1.3 and 4.4 of the book).
//
// Purpose: Module 2 distributes the lessons of each (Teacher, class) between two pairs of days, (d_0,d_1) = (1,2) and (d_2,d_3) = (3,4).
//   This module
// completes the distribution within a pair: for each unit (Teacher p, class a) it decides x = number of lessons (0, 1 or 2) on the 1st day of the
// pair; on the 2nd day there remain y = q2_[p][j] - x lessons. It is called once per pair of days and per possibility of working days (possibP).
// All the units are of the same type (Case 4 of Section 4.4.1). Constraints (Section 4.4.1): the total number of lessons of each Teacher on the two days
//   does not
// exceed N_2p (1st day) and N_3p (2nd day); the total of each class on each day does not exceed Ma; at most 1 lesson per day and per class
// for Teachers without double lessons, and 2 otherwise. The search is Algorithm 2 of Chapter 3 (main2); the dependency between units is:
// x[j] depends on x[i] when the units have the same Teacher or the same class (see determineZ).
// Control variables: Sp2_, Sp3_ (totals of the Teacher on each day of the pair) and Sa_4, Sa_5 (totals of the class on each day of the pair).
// The unit arrays (Ap, Aa, j_, x_, maxX0, z, iMaxI_) are those of the superclass Probl, different from those used by Module 2
//   (PhaseB2_Super).
// In addition to Case 4, this version includes quick supply and demand tests (preliminaries1 and case_35*), executed before the search,
// which discard possibilities of working days without the need to go through the units.
// Note: the private field ns_p_ is read in several methods of this class, but is not assigned a value at any point in this file
// (it would remain null); it is necessary to check whether it is assigned by another part of the program.
public class Module_3_V1 extends PhaseB2_Super {

	// Private copies (aliases) of the unit arrays of Probl: Ap, Aa, j_, x_, maxX0, z, iMaxI_;  n2 = number of units;  x0 =
	//   candidate successor;
	// q2_ = lessons of each (Teacher, class) in the pair of days being handled;  x_case_3[p] = 1 if, in the possibility under test, the Teacher works on the
	//   1st day of the pair;
	// arranjo[2] = maximum numbers of lessons on the two days (auxiliary);  d_0, d_1 = days of the pair;  nDaysWork = wp0 or wp1 (working days of the Teacher
	//   in the pair);
	// ns_p_[p] = lessons of the Teacher in the pair (see the note in the header).
	private int Ap[], Aa[], j_[], x_[], maxX0[], i, n2, x0, q2_[][], iMaxI_[], x_case_3[], arranjo[], ns_p_[], d_0, d_1,
			nDaysWork[];
	private short z[][];

	// Empty constructor; the preparation is done by preliminaries2().
	public Module_3_V1() {
	}

	// Prepares the module (called once, at the end of preliminaries of Module 2): links the private arrays to those of Probl, builds the units
	// and zeroes the distribution dd of the 4 days.
	public void preliminaries2() {
		int k;
		// Allocates the auxiliary arrays specific to this version.
		x_case_3 = new int[Qp + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		arranjo = new int[2];
		// Aliases: this module uses the unit arrays of Probl (the same as Module 1, already finished); Module 2 uses those of PhaseB2_Super.
		z = Probl.z;
		iMaxI_ = Probl.iMaxI_;
		Ap = Probl.Ap;
		Aa = Probl.Aa;
		j_ = Probl.j_;
		x_ = Probl.x_;
		maxX0 = Probl.maxX0;
		// Units: for each Teacher (in the same order d2_ as Module 2) with lessons and for each class of his/hers with lessons in the 4 days (q_0 > 0),
		// one unit (Ap = Teacher, Aa = class, j_ = position of the class in the Teacher's list). Same order as the units of case 2 of Module 2.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			for (j = 1; j <= nClassesP[p]; j++) {
				if (q_0[p][j] > 0) {
					k++;
					Ap[k] = p;
					Aa[k] = list[p][j];
					j_[k] = j;
				}
			}
		}
		n2 = k;
		Ap[k + 1] = 0;

		// Computes the array z and zeroes the distribution dd of the days handled (which will be filled in by update4).
		determineZ(n2);
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nClassesP[p]; j++) {
				for (k = 1; k <= 4; k++)
					dd[p][j][k] = 0;
			}
		}
	}

	// Quick test, before the search, for the pair of days exec (0: (1,2); 1: (3,4)) and the possibility of working days possib:
	// checks whether the supply of lessons is sufficient (case_35) and whether each Teacher can fulfill his/her lessons of the pair (case_35_).
	// Returns true if the possibility passes the tests (only then is it worth executing main2).
	public boolean preliminaries1(int[][] Aq2_, int possib, int exec) {
		boolean test;
		q2_ = Aq2_;
		if (exec == 0) {
			// Sets, according to the pair: ns_p = lessons of each Teacher in the pair (ns_p0 or ns_p1 of Module_2B_V1), the days d_0 and d_1 and the number of
			//   working days
			// (wp0 or wp1). Note: this assignment replaces the reference of the static array ns_p, shared with the other classes.
			ns_p = Module_2B_V1.ns_p0;
			d_0 = 1;
			d_1 = 2;
			nDaysWork = wp0;
		} else {
			ns_p = Module_2B_V1.ns_p1;
			d_0 = 3;
			d_1 = 4;
			nDaysWork = wp1;
		}
		for (p = 1; p <= Qp; p++) {
			if (ns_p_[p] <= 0)
				continue;
			// x_case_3[p] = 1 if, in this possibility, the Teacher works on the 1st day of the pair.
			x_case_3[p] = (possibP[exec][possib][p] ? 1 : 0);
		}
		test = case_35(nDaysWork);
		if (test)
			test = case_35_(nDaysWork);
		d_0 = 1;
		d_1 = 2;
		return test;
	}

	// Analogous to case 35 of Module 2, now with the lessons per class already defined (q2_): Np = maximum number of lessons of each Teacher on each day of the
	//   pair;
	// each Teacher offers each class at most min(1 or 2 lessons per day x days, Np, lessons of the class). The supply must cover Ma*Qa per day and
	//   Ma per class and per day.
	public boolean case_35(int[] wp0) {
		int aux3, aux4, sumA0, sumA1;
		boolean test;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
		}
		sumA0 = 0;
		sumA1 = 0;
		for (p = 1; p <= Qp; p++) {
			if (ns_p_[p] <= 0)
				continue;
			x0 = x_case_3[p];
			aux0 = (noDoublePeriodsP[p] ? x0 : 2 * x0);
			aux1 = wp0[p] - x0;
			if (!noDoublePeriodsP[p])
				aux1 *= 2;
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux2 = nClassesP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			for (j = 1; j <= aux2; j++) {
				a = list[p][j];
				sumA_0[a] += Math.min(aux0, q2_[p][j]);
				sumA_1[a] += Math.min(aux1, q2_[p][j]);
			}
		}
		test = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
		if (!test)
			return false;
		for (a = 1; a <= Qa; a++) {
			test = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
			if (!test)
				return false;
		}
		return true;
	}

	// For each Teacher, checks whether the lessons of the pair fit in the two days, considering the maximum number of lessons per day (Mp) and per class (1 or 2
	//   per day).
	// Returns false if some Teacher cannot fulfill his/her lessons.
	private boolean case_35_(int[] wp0) {
		int k, sumP_0, sumP_1, p, aux3, aux4;
		boolean test;
		for (p = 1; p <= Qp; p++) {
			if (ns_p_[p] <= 0) {
				test = true;
				continue;
			}
			sumP_0 = 0;
			sumP_1 = 0;
			aux0 = x_case_3[p];
			aux1 = wp0[p] - aux0;
			aux3 = (aux0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (aux1 == 0 ? 0 : Mp[p][d_1]);
			if (!noDoublePeriodsP[p]) {
				aux0 *= 2;
				aux1 *= 2;
			}
			aux2 = nClassesP[p];
			for (j = 1; j <= aux2; j++) {
				k = q2_[p][j];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
			}
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			test = ns_p_[p] <= aux0;
			if (!test)
				return false;
		}
		return true;
	}


	// Variant of preliminaries1, not used in the current version of Module 2: instead of true/false, returns the index of the unit where the
	// cause of the failure would be (class or Teacher that violates the tests of case_35NEW and case_35_NEW), or 0 if the tests are satisfied.
	public int preliminariesNEW1(int[][] Aq2_, int possib, int exec) {
		int aux;
		q2_ = Aq2_;
		if (exec == 0) {
			ns_p = Module_2B_V1.ns_p0;
			d_0 = 1;
			d_1 = 2;
			nDaysWork = wp0;
		} else {
			ns_p = Module_2B_V1.ns_p1;
			d_0 = 3;
			d_1 = 4;
			nDaysWork = wp1;
		}
		for (p = 1; p <= Qp; p++) {
			if (ns_p_[p] <= 0)
				continue;
			x_case_3[p] = (possibP[exec][possib][p] ? 1 : 0);
		}
		a = case_35NEW(nDaysWork);
		k = n2;
		if (a > 0) {
			while (Aa[k] != a)
				k--;
		}
		aux = k;
		if (a == 0)
			aux = n2 + 1;
		p = case_35_NEW(nDaysWork);
		if (a == 0 && p == 0)
			return 0;
		k = n2;
		if (p != 0) {
			while (Ap[k] != p)
				k--;
		} else
			k = n2 + 1;
		aux = Math.min(aux, k);
		d_0 = 1;
		d_1 = 2;
		return aux;
	}

	// Like case_35, but returns the number of the first class whose supply does not cover the demand (or 0 if all cover it).
	public int case_35NEW(int[] wp0) {
		int aux3, aux4, sumA0, sumA1;
		boolean test;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
		}
		sumA0 = 0;
		sumA1 = 0;
		for (p = 1; p <= Qp; p++) {
			if (ns_p_[p] <= 0)
				continue;
			x0 = x_case_3[p];
			aux0 = (noDoublePeriodsP[p] ? x0 : 2 * x0);
			aux1 = wp0[p] - x0;
			if (!noDoublePeriodsP[p])
				aux1 *= 2;
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux2 = nClassesP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			for (j = 1; j <= aux2; j++) {
				a = list[p][j];
				sumA_0[a] += Math.min(aux0, q2_[p][j]);
				sumA_1[a] += Math.min(aux1, q2_[p][j]);
			}
		}
		for (a = 1; a <= Qa; a++) {
			test = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
			if (!test)
				return a;
		}
		return 0;
	}

	// Like case_35_, but returns the number of the first Teacher who cannot fulfill his/her lessons in the pair (or 0).
	private int case_35_NEW(int[] wp0) {
		int k, sumP_0, sumP_1, p, aux3, aux4;
		boolean test;
		for (p = 1; p <= Qp; p++) {
			if (ns_p_[p] <= 0) {
				test = true;
				continue;
			}
			sumP_0 = 0;
			sumP_1 = 0;
			aux0 = x_case_3[p];
			aux1 = wp0[p] - aux0;
			aux3 = (aux0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (aux1 == 0 ? 0 : Mp[p][d_1]);
			if (!noDoublePeriodsP[p]) {
				aux0 *= 2;
				aux1 *= 2;
			}
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux2 = nClassesP[p];
			for (j = 1; j <= aux2; j++) {
				k = q2_[p][j];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
			}
			arranjo[0] = Math.min(Mp[p][d_0], sumP_0);
			arranjo[1] = Math.min(Mp[p][d_1], sumP_1);
			aux0 = (wp0[p] == 1 ? Math.max(arranjo[0], arranjo[1]) : arranjo[0] + arranjo[1]);
			test = ns_p_[p] <= aux0;
			if (!test)
				return p;
		}
		return 0;
	}

	// getP and getA: return the Teacher and the class of unit imax, so that Module 2 knows where this module failed (and to which unit of case
	//   2 to backtrack).
	public int getP(int imax) {
		return Ap[imax];
	}

	public int getA(int imax) {
		return Aa[imax];
	}

	// Case 4 (Section 4.4.1): successor of x[i], number of lessons of (p, class) on the 1st day of the pair. Returns false if there is a successor (in x0), true
	//   otherwise.
	public boolean maximum_4() {
		int maxX0_, minX0, p;
		boolean initialized;
		p = Ap[i];
		x0 = x_[i];
		// n0 = lessons of (p, class) in the pair. If n0 == 0, the only possible value is 0 (accepted on the 1st visit).
		n0 = q2_[p][j_[i]];
		if (n0 == 0) {
			if (x0 == -1) {
				x0 = 0;
				return false;
			} else
				return true;
		}
		// There is no successor if x0 is already the maximum per day and class (2, or 1 for Teachers without double lessons).
		if (x0 == 2 || (noDoublePeriodsP[p] && x0 == 1))
			return true;
		// Trivial case: the Teacher works on only one of the two days of the pair (N_2p or N_3p equal to 0); there is a single value (0 or n0), tried once.
		case_Trivial = Np2[p] == 0 || Np3[p] == 0;
		initialized = x0 > -1;
		if (case_Trivial) {
			// If the unit has already been visited, the bounds have not changed: reuses maxX0[i].
			if (initialized)
				return true;
			x0 = (Np2[p] == 0 ? 0 : n0);
		} else
			x0++;
		if (initialized)
			return x0 > maxX0[i];
		// Values already accumulated before the unit: Sp0/Sp2 = lessons of the Teacher on the 1st/2nd day (Sp2_, Sp3_); Sa0/Sa2 = lessons of the class on the 1st/2nd day
		//   (Sa_4, Sa_5).
		a = Aa[i];
		Sp0 = Sp2_[pointerP[i] - 1];
		Sp2 = Sp3_[pointerP[i] - 1];
		Sa0 = Sa_4[pointerA2[i] - 1];
		Sa2 = Sa_5[pointerA2[i] - 1];

		// Lower bound: what does not fit on the 2nd day (max. per day, remaining capacity N_3p - Sp2 of the Teacher and Ma - Sa2 of the class).
		minX0 = (noDoublePeriodsP[p] ? n0 - 1 : n0 - 2);
		minX0 = Math.max(minX0, Sp2 + n0 - Np3[p]);
		minX0 = Math.max(minX0, Sa2 + n0 - Ma);
		// Upper bound: max. per day (1 or 2), n0, remaining capacity of the Teacher on the 1st day (N_2p - Sp0) and of the class (Ma - Sa0).
		maxX0_ = (noDoublePeriodsP[p] ? 1 : 2);
		maxX0_ = Math.min(maxX0_, n0);
		maxX0_ = Math.min(maxX0_, Np2[p] - Sp0);
		maxX0_ = Math.min(maxX0_, Ma - Sa0);
		x0 = Math.max(x0, minX0);
		maxX0[i] = maxX0_;
		return x0 > maxX0_;
	}

	// Updates the control variables after unit i receives the value x0: accumulated totals of the Teacher (Sp2_, Sp3_) and of the class (Sa_4,
	//   Sa_5)
	// on the 1st and on the 2nd day of the pair (the 2nd day receives nLessons = q2_ - x0).
	public void update_4_() {
		p = Ap[i];
		nLessons = q2_[p][j_[i]] - x0;
		aux = pointerP[i];
		Sp2_[aux] = Sp2_[aux - 1] + x0;
		Sp3_[aux] = Sp3_[aux - 1] + nLessons;
		aux = pointerA2[i];
		Sa_4[aux] = Sa_4[aux - 1] + x0;
		Sa_5[aux] = Sa_5[aux - 1] + nLessons;
	}

	// Writes in dd the distribution obtained: for exec == 0 on days d_0 and d_1 (x and q2_ - x); for exec == 1 on days d_2 and d_3.
	public void update4(int exec) {
		int i;
		for (i = 1; i <= n2; i++) {
			p = Ap[i];
			j = j_[i];
			if (exec == 0) {
				dd[p][j][d_1] = q2_[p][j] - x_[i];
				dd[p][j][d_0] = x_[i];
			} else {
				dd[p][j][d_3] = q2_[p][j] - x_[i];
				dd[p][j][d_2] = x_[i];
			}
		}
	}

	// Search of Module 3 (Algorithm 2 of Chapter 3; generator = generator = -1) for the pair of days exec and the possibility of working days
	//   possib,
	// with Aq2_ = lessons of each (Teacher, class) in the pair. Returns 0 if all the units were closed (success); otherwise, returns
	// imax, the furthest unit reached, so that Module 2 knows where to backtrack.
	public int main2(int[][] Aq2_, int exec, int possib) {
		int p0, generator, k, imax;
		q2_ = Aq2_;
		for (p = 1; p <= Qp; p++) {
			if (ns_p[p] <= 0)
				continue;
			// For each Teacher with lessons: x0 = 1 if, in the possibility under test, he/she works on the 1st day of the pair. Defines N_2p (Np2: maximum number of lessons on the
			//   1st day,
			// 0 if he/she does not work) and N_3p (Np3: maximum on the 2nd day, 0 if he/she does not work on it), according to wp0 (pair 1) or wp1 (pair 2) and the days of the pair.
			x0 = (possibP[exec][possib][p] ? 1 : 0);
			if (exec == 0) {
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
				Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			} else {
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_2]);
				Np3[p] = (wp1[p] - x0 == 0 ? 0 : Mp[p][d_3]);
			}
		}

		// Pointers pointerP (into Sp2_/Sp3_, with a zeroed position between Teachers) and pointerA2 (into Sa_4/Sa_5, block of 16 positions per class).
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			pointerP[j] = aux2;
			aux2++;
			if (Ap[j + 1] != Ap[j]) {
				Sp2_[aux2] = 0;
				Sp3_[aux2] = 0;
				aux2++;
			}
		}

		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_4[auxA[j] - 1] = 0;
			Sa_5[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2; j++) {
			pointerA2[j] = auxA[Aa[j]];
			auxA[Aa[j]]++;
		}

		// Initialization of the search: all decision variables with the generator.
		imax = 0;
		generator = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		iMaxI_[1] = 1;
		do {
			nIterations++;
			// Units without lessons in the pair (q2_ == 0) have no choice: going forward they receive x = 0 (passing on the control variables) and are skipped;
			// when backtracking, those immedaytely before the current unit (of the same Teacher) are undone and skipped.
			if (q2_[Ap[i]][j_[i]] == 0) {
				if (x_[i] == -1) {
					do {
						x_[i] = 0;
						aux = pointerP[i];
						Sp2_[aux] = Sp2_[aux - 1];
						Sp3_[aux] = Sp3_[aux - 1];
						aux = pointerA2[i];
						Sa_4[aux] = Sa_4[aux - 1];
						Sa_5[aux] = Sa_5[aux - 1];
						i++;
						iMaxI_[i] = i;
					} while (x_[i] == -1 && Ap[i] == Ap[i - 1] && q2_[Ap[i]][j_[i]] == 0);
					if (i > imax)
						imax = i;
					if (i > n2)
						return 0;
					iMaxI_[i] = i;
					for (k = 1; k < i; k++) {
						if (i > iMaxI_[k])
							iMaxI_[k] = i;
					}
				} else {
					while (x_[i] == 0 && Ap[i] == Ap[i - 1] && q2_[Ap[i]][j_[i]] == 0) {
						x_[i] = generator;
						i--;
					}
				}
			}
			// With a successor: stores x0, updates the control, advances and updates iMaxI_ (furthest unit already reached). If i > n2, all the
			//   units have been closed.
			if (!maximum_4()) {
				x_[i] = x0;
				update_4_();
				i++;
				if (i > imax)
					imax = i;
				if (i > n2)
					return 0;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				// No successor: backtrack of Algorithm 2 to p0 = z[i][iMaxI_[i] - i]; the units after p0 return to the generator. If there is nowhere to backtrack,
				//   returns imax (failure).
				if (i == 1)
					return imax;
				p0 = z[i][iMaxI_[i] - i];
				if (p0 == 0)
					return imax;
				for (i = i; i > p0; i--)
					x_[i] = generator;
				i = p0;
			}
		} while (true);
	}

	// Computes the array z (method determineArranjo of Chapter 3) for the dependency of Module 3: z[i][l] = largest unit preceding i with
	//   the
	// same Teacher or the same class as some unit from i to i+l, made cumulative in l.
	public void determineZ(int n_) {
		int p, i, l;
		for (i = 2; i <= n_; i++) {
			for (l = 0; l <= n_ - i; l++) {
				p = i - 1;
				while (p > 0 && Ap[p] != Ap[i + l] && Aa[p] != Aa[i + l])
					p--;
				z[i][l] = (short) p;
			}
		}
		for (l = 1; l <= n_ - 1; l++) {
			for (i = 2; i <= n_ - l; i++) {
				if (z[i][l - 1] > z[i][l])
					z[i][l] = z[i][l - 1];
			}
		}
	}
}
