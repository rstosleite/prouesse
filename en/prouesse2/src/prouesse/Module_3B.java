package prouesse;

import java.util.Arrays;
import javax.swing.JOptionPane;

// Class Module_3B - implements Module 3B (Sections 5.2.5 and 5.4.4 of the book).
//
// Role: finalizes the distribution of lessons among the three days d2, d3 and d4 (2nd part of the week), starting from the pre-distribution
// made by Module 2B (lessons of each (Teacher, class) pair in the 2nd part, received in q2_0 = ns2). It is run once for each
// 'possibility' of the 2nd part (index possib in bitSet_2, from Module 2A), which already gives the number wp0[p] of working days
// of each Teacher between d2 and d3. Module 2B calls, in this order: preliminaries1 (checks whether the possibility is feasible),
// preliminaries2 (builds the units) and main_V3 (search); if the search succeeds, update4 writes the result into dd.
//
// It is similar to Module 4 of Algorithm P1, but the Case 3 and Case 4 units are ordered differently and there are
// no Case 1 units. Cases (case_[i]), in this order: 2 (lessons of the (Teacher, class) pair on d2 and d3; the rest goes to d4),
// 3 (1 if d2 is a working day of the Teacher, 0 if not; only for Teachers that are not complex_p2), 35 (validation of the Cases 3) and 4 (lessons
// of the pair on d2; the rest of the lessons of d2 and d3 goes to d3). The 'generator' is generator = -1 and the 'successor' is the method maximum(),
// which returns false if it finds the successor (in x0) and true if none exists.
public class Module_3B extends Probl {

	// Private copies of the references to the arrays of Accessory_classes (shared with Module 3A): units
	// (Ap, Aa, j_, A, case_), values (x_), search control (iMaxI_, z, y2). q2_0[p][j]: lessons of the pair (p, j-th class) in the 2nd part.
	private int Ap[], Aa[], j_[], A[], x_[], i, n2, x0, iMaxI_[], d2_[], q2_0[][], maxDays1[];
	// ns_p[p]: lessons of p in the 2nd part; wp0[p]: working days of p on d2 and d3 (by the possibility); Np0, Np1, Np2 and Np3:
	// maximum lessons of p in (d2, d3), on d4, on d2 and on d3; x_case__3[p]: 1 if d2 is a working day of p, 0 if not;
	// sumA_0, sumA_1 and sumA_1_: accumulated capacity per class on d2, d3 and d4; Dp[p]: minimum working days of p on d2, d3 and d4.
	private int k, i1, p, a, j, nLessons, aux1, p1, ns_p[], wp0[], Np2[], Np3[], Np0[], Np1[], sumA_0[], sumA_1[],
			sumA_1_[], x_case__3[], Dp[];
	private int Sp0_[], Sp1_[], Sa_0[], Sa_1[], pointerP2[], pointerA[], arranjo[], d, aux35, aux, aux35_;
	// p2_[k]: position, in the Teacher order d2_, of the Teacher of unit k; used to reuse solutions after backtracking (main_V3).
	static public int case_[], imax, p2_[];
	private short z[][], y2[][];

	// Constructor: obtains the references of the shared arrays and allocates the class's own arrays.
	public Module_3B() {
		this.Ap = Accessory_classes.Ap;
		this.Aa = Accessory_classes.Aa;
		this.j_ = Accessory_classes.j_;
		this.A = Accessory_classes.A;
		this.x_ = Accessory_classes.x_;
		this.iMaxI_ = Accessory_classes.iMaxI_;
		this.case_ = Accessory_classes.case_;
		this.z = Accessory_classes.z;
		this.y2 = Accessory_classes.y2;
		p2_ = new int[x_.length];
		arranjo = new int[3];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		sumA_1_ = new int[Qa + 1];
		ns_p = new int[Qp + 1];
		wp0 = new int[Qp + 1];
		Np2 = new int[Qp + 1];
		Np3 = new int[Qp + 1];
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		x_case__3 = new int[Qp + 1];
		Dp = new int[Qp + 1];
		q_0 = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++)
			q_0[p1] = new int[nClassesP[p1] + 1];
		Sp0_ = Accessory_classes.Sp0_;
		Sp1_ = Accessory_classes.Sp1_;
		Sa_0 = Accessory_classes.Sa_0;
		Sa_1 = Accessory_classes.Sa_1;
		pointerP2 = Accessory_classes.pointerP2;
		pointerA = Accessory_classes.pointerA;
	}

	// Checks whether the possibility possib (of the 2nd part) is feasible: computes, for each Teacher with lessons in the 2nd part, wp0 and Dp, and
	// searches (detPossibP) for a sub-possibility that meets the demands of the classes and of the Teachers; returns true if one exists
	// (Section 5.4.4). Prepares the Case 3 units and the Case 35 unit used by detPossibP.
	public boolean preliminaries1(int[][] q2_0, int possib) {
		boolean test, test2;
		this.q2_0 = q2_0;
		d2_ = Module_2A.d2_;
		maxDays1 = Module_2A.maxDays1;
		// Days of the run: d2, d3 and d4 (values 2, 3 and 4).
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		days[1] = d_0;
		days[2] = d_1;
		days[3] = d_2;
		// For each Teacher: ns_p = lessons in the 2nd part; wp0 comes from the two bits of bitSet_2 (number of working days between d2 and d3).
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;
			for (j = 1; j <= nClassesP[p]; j++)
				ns_p[p] += q2_0[p][j];
			if (ns_p[p] == 0)
				continue;
			test = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1));
			test2 = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1) + 1);
			wp0[p] = 2 * (test ? 1 : 0) + (test2 ? 1 : 0);
			// Teacher complex_p2: day d2 is already definitively a working day (or not), according to bitSet_1; checks the load against that of Module 2B.
			if (Module_2A.complex_p2[p]) {
				test = Module_2A.bitSet_1.get(3 * (p - 1));
				x_case__3[p] = (test ? 1 : 0);
				if (ns_p[p] != Module_2B.ns_p1[p]) {
					System.out.println("ERROR 401.");
					System.exit(0);
				}
			}
			// Dp[p]: minimum working days of p on d2, d3 and d4 (largest Mp first, until ns_p[p] is covered) and so that each class of p
			// gets its lessons (1 per day if noDoublePeriodsP, otherwise up to 2).
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][days[d + 1]];
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 2; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p[p])
					break;
			}
			for (j = 1; j <= nClassesP[p]; j++) {
				if (noDoublePeriodsP[p])
					Dp[p] = Math.max(Dp[p], q2_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q2_0[p][j] + 1) / 2);
			}
		}
		// Preparation units: one Case 3 for each Teacher with lessons that is not complex_p2, followed by one Case 35.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			if (Module_2A.complex_p2[p]) {
				x0 = x_case__3[p];
				continue;
			}
			k++;
			case_[k] = 3;
			Ap[k] = p;
		}
		// Case 35 unit (validation of the Cases 3).
		k++;
		case_[k] = 35;
		aux35_ = k;
		// Searches for a way to define x_case__3 for all Teachers that meets the demands.
		return detPossibP();
	}

	// Prepares the run of Module 3B for the possibility possib, after approval in preliminaries1: computes Np0 and Np1 and
	// builds the units in the order: Case 2 (all pairs), Case 3 (Teachers that are not complex_p2), Case 35 and Case 4 (all pairs).
	public void preliminaries2(int[][] q2_0, int possib) {
		// Np0[p]: maximum lessons in (d2, d3); Np1[p]: maximum on d4. For a complex Teacher, uses the working days fixed in bitSet_1.
		for (p = 1; p <= Qp; p++) {
			x0 = wp0[p];
			if (Module_2A.complex_p2[p]) {
				Np0[p] = 0;
				for (k = 0; k <= 1; k++) {
					test = Module_2A.bitSet_1.get(3 * (p - 1) + k);
					Np0[p] += (test ? Mp[p][k + 2] : 0);
				}
				test = Module_2A.bitSet_1.get(3 * (p - 1) + 2);
				Np1[p] = (test ? Mp[p][d_2] : 0);
			} else {
				Np0[p] = x0 * Math.max(Mp[p][d_0], Mp[p][d_1]);
				Np1[p] = (maxDays1[p] - x0 == 0 ? 0 : Mp[p][d_2]);
			}
		}

		// Case 2 units (only pairs with lessons in the 2nd part): A[k] = lessons of the pair in the 2nd part; p2_[k] = position of the Teacher in d2_.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			for (j = 1; j <= nClassesP[p]; j++) {
				nLessons = q2_0[p][j];
				if (nLessons > 0) {
					a = list[p][j];
					k++;
					Ap[k] = p;
					p2_[k] = i1;
					Aa[k] = a;
					j_[k] = j;
					case_[k] = 2;
					A[k] = nLessons;
				}
			}
		}
		// Case 3 units (Teachers not complex_p2). For complex_p2, Np2 and Np3 are already defined directly.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			if (Module_2A.complex_p2[p]) {
				x0 = x_case__3[p];
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
				Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
				continue;
			}
			k++;
			case_[k] = 3;
			Ap[k] = p;
			p2_[k] = i1;
			Aa[k] = 0;
		}
		// Case 35 unit: validation of the results of Cases 2 and 3.
		k++;
		case_[k] = 35;
		aux35 = k;
		Aa[k] = 0;
		// Case 4 units: distribute the lessons between d2 and d3.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			for (j = 1; j <= nClassesP[p]; j++) {
				nLessons = q2_0[p][j];
				if (nLessons > 0) {
					k++;
					Ap[k] = p;
					p2_[k] = i1;
					Aa[k] = list[p][j];
					j_[k] = j;
					case_[k] = 4;
				}
			}
		}
		// n2 = number of units.
		n2 = k;

		// Control variables and pointers (accumulated lessons per Teacher and per class), as in Modules 2A and 3A.
		Ap[n2 + 1] = 0;
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 4) {
				pointerP[j] = aux2;
				aux2++;
				if (Ap[j + 1] != Ap[j]) {
					Sp2_[aux2] = 0;
					Sp3_[aux2] = 0;
					aux2++;
				}
			}
		}
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				pointerP2[j] = aux2;
				aux2++;
				if (Ap[j + 1] != Ap[j]) {
					Sp0_[aux2] = 0;
					Sp1_[aux2] = 0;
					aux2++;
				}
			}
		}

		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_0[auxA[j] - 1] = 0;
			Sa_1[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				pointerA[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}

		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_4[auxA[j] - 1] = 0;
			Sa_5[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 4) {
				pointerA2[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		// Prepares the array z (search backtracking).
		determineZ(n2);
	}

	// Teacher of the most advanced unit reached (for Module 2B to decide the backtracking).
	public int getP() {
		return (Ap[imax]);
	}

	// Class of the most advanced unit reached.
	public int getA() {
		return (Aa[imax]);
	}

	// Validation of the classes' demands (equivalent to Case 35): returns true if, with the working days in x_case__3 and
	// wp0, each class can receive Ma lessons on each of the days d2, d3 and d4, and the total capacities reach Ma*Qa.
	public boolean case__35() {
		int aux2, aux3, aux4, sumA0, sumA1, sumA1_, aux4_, aux1_, x0;
		boolean test;
		sumA0 = 0;
		sumA1 = 0;
		sumA1_ = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
			sumA_1_[a] = 0;
		}
		for (p = 1; p <= Qp; p++) {
			if (ns_p[p] <= 0)
				continue;
			x0 = x_case__3[p];
			aux0 = (noDoublePeriodsP[p] ? x0 : 2 * x0);
			aux1 = wp0[p] - x0;
			aux1_ = Module_2A.maxDays1[p] - wp0[p];
			if (!noDoublePeriodsP[p]) {
				aux1 *= 2;
				aux1_ *= 2;
			}
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux4_ = (Module_2A.maxDays1[p] - wp0[p] == 0 ? 0 : Mp[p][d_2]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux1_ = Math.min(aux1_, aux4_);
			aux2 = nClassesP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			sumA1_ += aux4_;
			for (j = 1; j <= aux2; j++) {
				a = list[p][j];
				sumA_0[a] += Math.min(aux0, q2_0[p][j]);
				sumA_1[a] += Math.min(aux1, q2_0[p][j]);
				sumA_1_[a] += Math.min(aux1_, q2_0[p][j]);
			}
		}
		if (sumA1_ < Ma * Qa) {
			System.out.println("ERROR 049");
			System.exit(0);
		}
		test = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa && sumA1_ >= Ma * Qa;
		if (!test)
			return false;
		for (a = 1; a <= Qa; a++) {
			test = sumA_0[a] >= Ma && sumA_1[a] >= Ma && sumA_1_[a] >= Ma;
			if (!test)
				return false;
		}
		return true;
	}

	// Validation of the Teachers' demands: for each Teacher with lessons, the capacity on d2, d3 and d4 (limited by Mp, by 1 or 2
	// lessons per class per day, and by the lessons of each class) must cover ns_p[p]. Returns false if some demand is not met.
	private boolean case__35_() {
		int k, sumP_0, sumP_1, sumP_1_, p, aux3, aux4, aux4_, aux1_, d_0, d_1, d_2;
		boolean test;

		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		for (p = 1; p <= Qp; p++) {
			if (ns_p[p] == 0) {
				test = true;
				continue;
			}
			sumP_0 = 0;
			sumP_1 = 0;
			sumP_1_ = 0;
			aux0 = x_case__3[p];
			aux1 = wp0[p] - aux0;
			aux1_ = maxDays1[p] - wp0[p];
			aux3 = (aux0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (aux1 == 0 ? 0 : Mp[p][d_1]);
			aux4_ = (aux1_ == 0 ? 0 : Mp[p][d_2]);
			if (!noDoublePeriodsP[p]) {
				aux0 *= 2;
				aux1 *= 2;
				aux1_ *= 2;
			}
			aux2 = nClassesP[p];
			for (j = 1; j <= aux2; j++) {
				k = q2_0[p][j];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
				sumP_1_ += Math.min(aux1_, k);
			}
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			aux0 += Math.min(aux4_, sumP_1_);
			test = ns_p[p] <= aux0;
			if (!test)
				return false;
		}
		return true;
	}

	// Successor of Module 3B. Returns false if it found the successor of x_[i] (value in x0) and true if none exists.
	public boolean maximum() {
		int aux2, i0, maxX0_, minX0, aux3, aux4, sumA0, sumA1;
		boolean test, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {

		// Case 3: x0 = 1 if d2 is a working day of p, 0 if not (with wp0[p] working days between d2 and d3). The smallest value
		// respects the minimum number of days Dp[p], the availability of p on d2 and d3 and wp0[p].
		case 3: {
			i0 = wp0[p];
			if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
				return true;
			if (Mp[p][d_2] == 0 || i0 == maxDays1[p])
				aux2 = 0;
			else
				aux2 = 1;
			x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
			if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
				x0 = 1;
			if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
				return true;
			return false;
		}

		// Case 2: x0 = lessons of the (Teacher, class) pair in (d2, d3); A[i] - x0 go to d4. Analogous to Case 2 of Module 3A, with maxDays1[p] as the
		// limit on the working days of p among d2, d3 and d4 and wp0[p] as the working days between d2 and d3.
		case 2: {
			if (x0 == 2 * wp0[p] || (noDoublePeriodsP[p] && x0 == wp0[p]))
				return true;
			case_Trivial = Np0[p] == 0 || Np1[p] == 0;
			n0 = A[i];
			initialized = x0 > -1;
			if (case_Trivial) {
				if (initialized)
					return true;
				x0 = (Np0[p] == 0 ? 0 : n0);
			} else
				x0++;
			if (initialized)
				return x0 > maxX0[i];
			a = Aa[i];
			Sp0 = Sp0_[pointerP2[i] - 1];
			Sp2 = Sp1_[pointerP2[i] - 1];
			Sa0 = Sa_0[pointerA[i] - 1];
			Sa2 = Sa_1[pointerA[i] - 1];

			// Lower bound of x0: what does not fit on d4 (by working day of p on d4, Np1[p] and Ma of the class).
			aux0 = Math.min(maxDays1[p] - wp0[p], 1);
			minX0 = (noDoublePeriodsP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);
			// If x0 exceeds what would fit with one fewer working day, p cannot have lessons on d4.
			aux0 = (noDoublePeriodsP[p] ? maxDays1[p] - 1 : 2 * maxDays1[p] - 2);
			if (x0 > aux0) {
				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}

			// Upper bound of x0: what fits in (d2, d3).
			maxX0_ = (noDoublePeriodsP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Case 4: x0 = lessons of the (Teacher, class) pair on d2; the rest of the pair's q_0 lessons in (d2, d3) goes to d3.
		case 4: {
			// n0 = lessons of the pair in (d2, d3), determined in Case 2.
			n0 = q_0[p][j_[i]];
			if (n0 == 0) {
				if (x0 == -1) {
					x0 = 0;
					return false;
				} else
					return true;
			}
			if (x0 == 2 || (noDoublePeriodsP[p] && x0 == 1))
				return true;
			// Np2[p] and Np3[p]: maximum lessons of p on d2 and d3 (defined by Case 3 or directly, for complex_p2 Teachers).
			case_Trivial = Np2[p] == 0 || Np3[p] == 0;
			initialized = x0 > -1;
			if (case_Trivial) {
				if (initialized)
					return true;
				x0 = (Np2[p] == 0 ? 0 : n0);
			} else
				x0++;
			if (initialized)
				return x0 > maxX0[i];
			a = Aa[i];
			// Sp0, Sp2, Sa0 and Sa2: lessons already assigned to the Teacher and the class on d2 and on d3 by the previous Case 4 units.
			Sp0 = Sp2_[pointerP[i] - 1];
			Sp2 = Sp3_[pointerP[i] - 1];
			Sa0 = Sa_4[pointerA2[i] - 1];
			Sa2 = Sa_5[pointerA2[i] - 1];

			minX0 = (noDoublePeriodsP[p] ? n0 - 1 : n0 - 2);
			minX0 = Math.max(minX0, Sp2 + n0 - Np3[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			maxX0_ = (noDoublePeriodsP[p] ? 1 : 2);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np2[p] - Sp0);
			maxX0_ = Math.min(maxX0_, Ma - Sa0);
			x0 = Math.max(x0, minX0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}
		// Case 35: validation of the results of Cases 2 and 3. It only takes effect going forward; when returning to this unit the backtracking continues.
		case 35: {
			if (x0 == 0)
				return true;
			sumA0 = 0;
			sumA1 = 0;
			// For each class, sums the possible countribution of each Teacher on d2 and on d3, limited by 1 or 2 lessons per day, by the
			// day's capacity Mp and by the lessons q2_0 of the pair.
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				if (ns_p[p] <= 0)
					continue;
				x0 = x_case__3[p];
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
					sumA_0[a] += Math.min(aux0, q2_0[p][j]);
					sumA_1[a] += Math.min(aux1, q2_0[p][j]);
				}
			}
			// Requires a total capacity of Ma*Qa on each day and of Ma per class per day.
			test = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
			if (test) {
				for (a = 1; a <= Qa; a++) {
					test = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
					if (!test)
						break;
				}
			}
			x0 = 0;
			return !test;
		}
		}
		return true;
	}

	// Update of the control and linking variables after x0 is accepted.
	public void update2_() {
		switch (case_[i]) {

		// Case 3: defines Np2, Np3 and x_case__3.
		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			x_case__3[p] = x0;
			break;
		}
		// Case 2: accumulates lessons of the Teacher and of the class in (d2, d3) and on d4; q_0 receives the lessons of the pair in (d2, d3).
		case 2: {
			p = Ap[i];
			nLessons = A[i] - x0;
			aux = pointerP2[i];
			Sp0_[aux] = Sp0_[aux - 1] + x0;
			Sp1_[aux] = Sp1_[aux - 1] + nLessons;
			aux = pointerA[i];
			Sa_0[aux] = Sa_0[aux - 1] + x0;
			Sa_1[aux] = Sa_1[aux - 1] + nLessons;
			q_0[p][j_[i]] = x0;
			break;
		}

		// Case 4: accumulates lessons of the Teacher and of the class on d2 and on d3.
		case 4: {
			p = Ap[i];
			nLessons = q_0[p][j_[i]] - x0;
			aux = pointerP[i];
			Sp2_[aux] = Sp2_[aux - 1] + x0;
			Sp3_[aux] = Sp3_[aux - 1] + nLessons;
			aux = pointerA2[i];
			Sa_4[aux] = Sa_4[aux - 1] + x0;
			Sa_5[aux] = Sa_5[aux - 1] + nLessons;
			break;
		}
		}
	}

	// Algorithm 2 of Chapter 3. Returns true if it obtains the distribution among d2, d3 and d4 for the possibility and false if it does not.
	// Reuses the solutions of the Cases 4 of Teachers before the Teacher of pMin (stored in y2) after a backtrack.
	public boolean main_V3() {
		int k, p0, generator, iMax4, i0, pMin, p, maxX0_;
		pMin = 0;
		iMax4 = 0;
		generator = -1;
		// imax: most advanced unit reached (used by getP and getA).
		imax = 1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		iMaxI_[1] = 1;
		// Sentinels of the arrays Ap.
		Ap[aux35] = 0;
		Ap[n2 + 1] = 0;
		do {
			nIterations++;
			if (!maximum()) {
				// When passing through the Case 35 unit after a backtrack, restores from y2 the Case 4 solutions of the Teachers before the Teacher of pMin, without recomputing them...
				if (case_[i] == 35 && pMin > 0) {
					p0 = i + 1;
					while (p0 != iMax4 && pMin != p2_[p0])
						p0++;
					iMax4 = i + 1;
					x_[i] = 0;
					iMaxI_[i] = p0;
					p = d2_[p2_[p0 - 1]];
					for (i = i + 1; i < p0; i++) {
						n0 = q_0[Ap[i]][j_[i]];
						Sp0 = Sp2_[pointerP[i] - 1];
						Sa0 = Sa_4[pointerA2[i] - 1];
						maxX0_ = (noDoublePeriodsP[Ap[i]] ? 1 : 2);
						maxX0_ = Math.min(maxX0_, n0);
						maxX0_ = Math.min(maxX0_, Np2[Ap[i]] - Sp0);
						maxX0_ = Math.min(maxX0_, Ma - Sa0);
						maxX0[i] = maxX0_;
						x0 = y2[p][i];
						x_[i] = x0;
						update2_();
						iMaxI_[i] = p0;
					}
					i = p0;
					pMin = 0;
				// ...otherwise stores x0, updates the control variables and advances.
				} else {
					x_[i] = x0;
					update2_();
					if (case_[i] == 35)
						iMax4 = i + 1;
					i++;
				}
				// All units closed: distribution obtained.
				if (i > n2) {
					System.out.println();
					return true;
				}
				// When starting the Case 4 units of a new Teacher, stores in y2[p] the Case 4 solutions up to the previous Teacher.
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux35 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = (short) x_[i0];
				}
				if (i > imax)
					imax = i;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Failure: backtracks to p0 = z[i][iMaxI_[i] - i]; pMin records the smallest Teacher reached by the backtrack, when it starts from Case 4 and reaches a Case 2 or 3.
			} else {
				if (i == 1)
					return false;
				p0 = z[i][iMaxI_[i] - i];
				if (case_[p0] <= 3 && case_[i] == 4) {
					pMin = p2_[p0];
					if (pMin == 0) {
						System.out.println(case_[p0] + " (test9) ");
						System.exit(0);
					}
				} else {
					if (case_[p0] != 4 && p2_[p0] < pMin) {
						pMin = p2_[p0];
						if (pMin == 0) {
							System.out.println(case_[p0] + " (test10) ");
							System.exit(0);
						}
					}
				}
				if (p0 == 0)
					return false;
				// Resets the abandoned units to the generator.
				for (i = i; i > p0; i--)
					x_[i] = generator;
				i = p0;
			}
		} while (true);
	}

	// Method 'dependency' of Chapter 3: indicates whether unit j depends on unit i (i < j). Case 2: depends on units of
	// Case 2 of the same Teacher or the same class; Cases 3 and 35: depend on the Case 3 units; Case 4: on the Case 2 of the same pair,
	// on the Case 3 of the same Teacher and on the Case 4 of the same Teacher or the same class.
	public boolean dependency(int i, int j) {
		int p1, p2, a1, a2;
		if (j <= i)
			return false;
		p1 = Ap[i];
		a1 = Aa[i];
		p2 = Ap[j];
		a2 = Aa[j];
		if (case_[j] == 2)
			return p1 == p2 || a1 == a2;
		if (case_[j] == 3 || case_[j] == 35)
			return case_[i] == 3;
		if (case_[j] == 4) {
			if (case_[i] == 2)
				return p1 == p2 && a1 == a2;
			if (case_[i] == 4)
				return p1 == p2 || a1 == a2;
			if (case_[i] == 3)
				return p1 == p2;
			if (case_[i] == 35)
				return false;
		}
		return true;
	}

	// determineArranjo of Chapter 3 (computes z[i][l] and accumulates the maximum along l).
	public void determineZ(int n_) {
		int p, i, l;
		for (i = 2; i <= n_; i++) {
			for (l = 0; l <= n_ - i; l++) {
				p = i - 1;
				while (p > 0 && (!this.dependency(p, i + l)))
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

	// Writes the lessons per day into dd for days 2, 3 and 4: d4 receives A[i] - x_[i] (Case 2); in Case 4, d2 receives x_[i] and d3 receives the rest of the pair.
	// Note: Module 3A later recomputes the distribution of d2 (it reads only d3 and d4 from dd).
	public void update4() {
		int i;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nClassesP[p]; j++) {
				dd[p][j][2] = 0;
				dd[p][j][3] = 0;
				dd[p][j][4] = 0;
			}
		}
		for (i = 1; i <= n2; i++) {
			p = Ap[i];
			j = j_[i];
			if (case_[i] == 2)
				dd[p][j][4] = A[i] - x_[i];
			if (case_[i] == 4) {
				dd[p][j][3] = q_0[p][j] - x_[i];
				dd[p][j][2] = x_[i];
			}
		}
	}

	// Searches, among the Case 3 values (search with backtracking), for a definition of x_case__3 that passes the demand tests of the classes
	// (case__35) and of the Teachers (case__35_). Returns true if it finds one and false if it exhausts the possibilities.
	public boolean detPossibP() {
		int generator;
		boolean test;
		generator = -1;
		for (i = 1; i <= aux35_; i++)
			x_[i] = generator;
		i = 1;
		do {
			// Case 3 units: uses maximum(). On the final unit (Case 35) applies the demand tests.
			if (case_[i] == 3)
				test = maximum();
			else {
				test = case__35();
				if (test)
					test = case__35_();
				if (test)
					return true;
				// If the tests fail, forces backtracking (test = true) to continue the search.
				test = true;
			}
			if (!test) {
				x_[i] = x0;
				if (case_[i] == 3)
					x_case__3[Ap[i]] = x0;
				i++;
				if (i > aux35_) {
					System.out.println("ERROR 047");
					System.exit(0);
				}
			} else {
				if (i == 1)
					return false;
				x_[i] = generator;
				i--;
			}
		} while (true);
	}
}
