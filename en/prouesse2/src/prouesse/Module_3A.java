package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Class Module_3A - implements Module 3A (Section 5.3.2 and Section 5.4.4 of the book).
//
// Role: finalizes the distribution of lessons among three days (d_0, d_1 and d_2), starting from a pre-distribution already made. It is
// a class very similar to Module 4 of Algorithm P1 (Sections 4.1.4 and 4.5), except that it does not call Module 5. It is called
// by Module 2B in two ways: (a) preliminaries(q2_, maxDays1), for the days (d2, d3, d4) (used in case_5, when
// Module 3B is not used); (b) preliminaries0(), for the days (d0, d1, d2), after days d3 and d4 already have their
// distribution in dd (this is the run that repeats day d2 to give more flexibility).
//
// Cases of the units (case_[i]), in this order: 1 and 2 (per Teacher: a Case 1 followed by the Cases 2 of their classes), 11 (verification unit,
// with no constraint of its own), 3 and 4 (per Teacher: a Case 3 followed by the Cases 4 of their classes):
//   - Case 1: x[k] = number of working days of the Teacher in the pair (d_0, d_1) (with a coding from 0 to 3 for complex Teachers);
//   - Case 2: x[k] = lessons of the (Teacher, class) pair in the pair (d_0, d_1); the difference from the total goes to day d_2;
//   - Case 3: x[k] = 1 if d_0 is a working day of the Teacher, 0 otherwise;
//   - Case 4: x[k] = lessons of the (Teacher, class) pair on d_0; the rest of the pair goes to d_1.
// The book's 'generator' is generator = -1 and the 'successor' is the method maximum(), which returns false if it finds the successor (value in
// x0) and true if there is no successor.
public class Module_3A extends Accessory_classes {

	// d_0, d_1 and d_2 (from Probl) indicate the three days of the run; Dp[p]: minimum working days of p among the three days
	// to fulfill their load and the lessons of each class; arranjo: auxiliary for sorting the maxima Mp.
	static protected int d1, nWorkDays1, aux3, Dp[], arranjo[];
	// complex_p0[p]: p has Mp[p][d_0] and Mp[p][d_1] positive and distinct (Section 4.1.4).
	static protected boolean test1, test2, case_Trivial, complex_p0[];
	// nDays_[x]: number of working days of the pair (d_0, d_1) corresponding to code x of Case 1;
	// x_case_1[p]: code (0 to 3) chosen in Case 1 for a complex Teacher; imax: most advanced unit reached.
	static protected int nDays_[], x_case_1[], imax;

	// Constructor: allocates this class's own arrays.
	public Module_3A() {
		Dp = new int[Qp + 1];
		arranjo = new int[3];
		complex_p0 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
	}

	// Prepares the run for the days (d0, d1, d2) = (0, 1, 2), after days 3 and 4 have already been distributed in dd.
	// Returns the number of units n2.
	public int preliminaries0() {
		int d;
		// nDays_[code] = number of working days of the Teacher in the pair (d_0, d_1) for codes 0, 1, 2, 3 of Case 1.
		nDays_ = new int[] { 0, 1, 1, 2, 3 };
		d_0 = 0;
		d_1 = 1;
		d_2 = 2;
		imax = 0;
		// For each Teacher: q_0[p][j] = lessons of the j-th class still without a day (ns minus the lessons of days 3 and 4); ns_p[p] = total of these
		// lessons; Wp1[p] = number of working days of p that already occur on days 3 and 4.
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;
			Wp1[p] = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				q_0[p][j] = ns[p][j];
				ns_p[p] += ns[p][j];
			}

			// Subtracts the lessons already distributed on days 3 and 4 (in dd) and counts the working days of p on those days.
			for (d1 = 3; d1 <= 4; d1++) {
				aux1 = 0;
				for (j = 1; j <= nClassesP[p]; j++) {
					aux2 = dd[p][j][d1];
					q_0[p][j] -= aux2;
					ns_p[p] -= aux2;
					aux1 += aux2;
				}
				if (aux1 > 0)
					Wp1[p]++;
			}
			// Dp[p]: minimum number of working days of p on days 0, 1 and 2: uses the largest Mp first, until ns_p[p] is covered...
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][d];
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 2; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p[p])
					break;
			}
			// ... and considers that, in each class, p has 1 lesson per day (if noDoublePeriodsP) or up to 2.
			for (j = 1; j <= nClassesP[p]; j++) {
				if (noDoublePeriodsP[p])
					Dp[p] = Math.max(Dp[p], q_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q_0[p][j] + 1) / 2);
			}
			// complex_p0: Teacher whose maxima on the first two days are positive and distinct.
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
		}

		// Assembly of the units. For each Teacher with lessons to distribute (in the order d2_ established in Module 2A): a Case 1 unit
		// followed by a Case 2 unit for each class with lessons.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			// Wp1[p] becomes the maximum number of working days of p on days 0, 1 and 2: Wp0 minus the working days on 3 and 4 (at most 3).
			Wp1[p] = Wp0[p] - Wp1[p];
			if (Wp1[p] > 3)
				Wp1[p] = 3;
			k++;
			case_[k] = 1;
			Ap[k] = p;
			Aa[k] = 0;
			count = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				nLessons = q_0[p][j];
				if (nLessons > 0) {
					a = list[p][j];
					count++;
					k++;
					Ap[k] = p;
					Aa[k] = a;
					j_[k] = j;
					case_[k] = 2;
					A[k] = nLessons;
				}
			}
		}

		// Case 11 unit: imposes no constraint (the successor only accepts the first visit).
		k++;
		case_[k] = 11;
		Aa[k] = 0;
		aux3 = k;
		Ap[k] = 0;
		// After Case 11: for each Teacher, a Case 3 unit followed by a Case 4 unit for each class with lessons.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			case_[k] = 3;
			Ap[k] = p;
			Aa[k] = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				if (q_0[p][j] > 0) {
					k++;
					Ap[k] = p;
					Aa[k] = list[p][j];
					j_[k] = j;
					case_[k] = 4;
				}
			}
		}
		// n2 = total number of units.
		n2 = k;

		// Control variables Sp2_ and Sp3_ (accumulated lessons of the Teacher on d_0 and d_1), indexed by the Case 4 units (pointerP);
		// at the end of a Teacher's units, a zeroed position is reserved.
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 4) {
				pointerP[j] = aux2;
				aux2++;
				if (case_[j + 1] != 4) {
					Sp2_[aux2] = 0;
					Sp3_[aux2] = 0;
					aux2++;
				}
			}
		}

		// Control variables Sp0_ and Sp1_ (accumulated lessons of the Teacher in the pair (d_0, d_1) and on d_2), indexed by the Case 2 units.
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				pointerP2[j] = aux2;
				aux2++;
				if (case_[j + 1] != 2) {
					Sp0_[aux2] = 0;
					Sp1_[aux2] = 0;
					aux2++;
				}
			}
		}

		// Control variables Sa_0 and Sa_1 (accumulated lessons of the class in the pair (d_0, d_1) and on d_2); each class has 16 positions.
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

		// Control variables Sa_4 and Sa_5 (accumulated lessons of the class on d_0 and on d_1), for the Case 4 units.
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
		return n2;
	}

	// Prepares the run for the days (d2, d3, d4) = (2, 3, 4), with the lessons of each (Teacher, class) pair on those days in q2_
	// (array ns2 of Module 2B) and maxDays1[p] as the limit on the working days of p. Structure analogous to that of preliminaries0().
	public int preliminaries(int[][] q2_, int[] maxDays1) {
		int d;
		// d_0, d_1 and d_2 become days 2, 3 and 4.
		nDays_ = new int[] { 0, 1, 1, 2, 3 };
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		imax = 0;
		// Here q_0[p][j] = q2_[p][j] (lessons of the pair in the 2nd part of the week) and ns_p[p] is the sum of these lessons.
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				ns_p[p] += q2_[p][j];
				q_0[p][j] = q2_[p][j];
			}
			for (d = 2; d <= 4; d++)
				arranjo[d - 2] = Mp[p][d];
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
					Dp[p] = Math.max(Dp[p], q_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q_0[p][j] + 1) / 2);
			}
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
		}

		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			// The limit on working days is that of the whole 2nd part of the week (maxDays1), at most 3.
			Wp1[p] = maxDays1[p];
			if (Wp1[p] > 3)
				Wp1[p] = 3;
			k++;
			case_[k] = 1;
			Ap[k] = p;
			Aa[k] = 0;
			count = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				nLessons = q_0[p][j];
				if (nLessons > 0) {
					a = list[p][j];
					count++;
					k++;
					Ap[k] = p;
					Aa[k] = a;
					j_[k] = j;
					case_[k] = 2;
					A[k] = nLessons;
				}
			}
		}

		k++;
		case_[k] = 11;
		Aa[k] = 0;
		aux3 = k;
		Ap[k] = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			case_[k] = 3;
			Ap[k] = p;
			Aa[k] = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				if (q_0[p][j] > 0) {
					k++;
					Ap[k] = p;
					Aa[k] = list[p][j];
					j_[k] = j;
					case_[k] = 4;
				}
			}
		}
		n2 = k;

		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 4) {
				pointerP[j] = aux2;
				aux2++;
				if (case_[j + 1] != 4) {
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
				if (case_[j + 1] != 2) {
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
		return n2;
	}

	// Teacher of the most advanced unit reached by the search (used by Module 2B to decide the backtracking).
	public int get_P() {
		return Ap[imax];
	}

	// Class of the most advanced unit reached (0 if the unit is of Case 1, 3 or 11).
	public int get_A() {
		return Aa[imax];
	}

	// Successor of Module 3A. Returns false if it found the successor of x_[i] (value in x0) and true if none exists.
	public boolean maximum() {
		int aux2, i0, maxX0_, minX0, d, x0_, k_;
		boolean test, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Case 1 (Sections 4.1.4 and 4.5.1): number of working days of p in (d_0, d_1).
		case 1: {
			// Complex Teacher: x0 from 0 to 3 (0 = no day, 1 = only d_0, 2 = only d_1, 3 = both); nDays_[x0] is the number of days.
			if (complex_p0[p]) {
				while (true) {

					// The minimum number of days in (d_0, d_1) is Dp[p] minus day d_2, if p can work on d_2.
					aux2 = Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0);
					do {
						x0++;
						x0_ = nDays_[x0];
					} while (x0_ < aux2);
					// No successor if x0 exceeds 3, if it exceeds the days remaining for the Teacher (Wp1) or the days available in (d_0, d_1).
					if (x0 > 3 || x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
						return true;
					// The total capacity (maximum lessons) with this choice must cover the load ns_p[p]: maxima of d_2 (if p can still
					// work on it) and of the chosen days of (d_0, d_1).
					aux2 = (Wp1[p] - x0_ > 0 ? Mp[p][d_2] : 0);
					if (x0 > 0) {
						if (x0 == 1)
							aux2 += Mp[p][d_0];
						else {
							if (x0 == 2)
								aux2 += Mp[p][d_1];
							else
								aux2 += Mp[p][d_0] + Mp[p][d_1];
						}
					}
					if (ns_p[p] <= aux2)
						return false;
				}
			// Non-complex Teacher: x0 = 0, 1 or 2 working days, and the capacity uses the larger Mp when there is a single day.
			} else {
				while (true) {
					x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0));
					if (x0 > 2 || x0 > Wp1[p] || x0 > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
						return true;
					aux2 = (Wp1[p] - x0 > 0 ? Mp[p][d_2] : 0);
					if (x0 > 0) {
						if (x0 == 1)
							aux2 += Math.max(Mp[p][d_0], Mp[p][d_1]);
						else
							aux2 += Mp[p][d_0] + Mp[p][d_1];
					}
					if (ns_p[p] <= aux2)
						return false;
				}
			}
		}

		// Case 3 (Section 4.5.2): x0 = 1 if d_0 is a working day of p, 0 if not. i0 = wp0[p] = number of working days in the pair
		// (d_0, d_1), defined by Case 1.
		case 3: {
			while (true) {
				i0 = wp0[p];
				// No successor if x0 > 1, x0 > i0 or if p is not available on d_0 and x0 > 0.
				if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
					return true;
				// Minimum value of x0: what is needed to reach Dp[p] days, considering d_1 and d_2.
				if (Mp[p][d_2] == 0 || i0 == Wp1[p])
					aux2 = 0;
				else
					aux2 = 1;
				x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
				// If p works on both days, or can only work on d_0, d_0 is a working day.
				if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
					x0 = 1;
				if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
					return true;
				aux2 = (Wp1[p] - i0 > 0 ? Mp[p][d_2] : 0);

				// For a complex Teacher with only one working day in the pair, the value must be consistent with the code chosen in Case 1.
				test = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
				test = test && ((!complex_p0[p]) || (i0 != 1) || (x_case_1[p] == 1 && x0 == 1)
						|| (x_case_1[p] == 2 && x0 == 0));
				// Checks that the capacity with this choice covers the load ns_p[p].
				if (test) {
					if (x0 == 0 && i0 == 1)
						aux2 += Mp[p][d_1];
					else {
						if (x0 == 1)
							aux2 += (i0 == 1 ? Mp[p][d_0] : Mp[p][d_0] + Mp[p][d_1]);
					}
					test = ns_p[p] <= aux2;
				}
				if (test)
					return false;
			}
		}

		// Case 2 (Section 4.5.3): x0 = lessons of the (Teacher, class Aa[i]) pair in (d_0, d_1); A[i] - x0 on d_2.
		case 2: {
			// There is no successor if x0 is already at the maximum possible in the pair of days.
			if (x0 == 2 * wp0[p] || (noDoublePeriodsP[p] && x0 == wp0[p]))
				return true;
			// Trivial case: Np0[p] (maximum in (d_0, d_1)) or Np1[p] (maximum on d_2) is zero; all the lessons stay in a single place.
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
			// Sp0, Sp2, Sa0 and Sa2: lessons already assigned, by the previous units, to the Teacher and the class in (d_0, d_1) and on d_2.
			Sp0 = Sp0_[pointerP2[i] - 1];
			Sp2 = Sp1_[pointerP2[i] - 1];
			Sa0 = Sa_0[pointerA[i] - 1];
			Sa2 = Sa_1[pointerA[i] - 1];

			// Lower bound of x0: what does not fit on d_2 (at most 1 or 2 lessons of the pair, if d_2 is a working day; Np1[p] for the Teacher; Ma for the class).
			aux0 = Math.min(Wp1[p] - wp0[p], 1);
			minX0 = (noDoublePeriodsP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);

			// If x0 exceeds what would fit with one fewer working day, the Teacher cannot have lessons on d_2: if they already have some (Sp2 > 0) there is no
			// successor; otherwise, all the lessons of the pair stay in (d_0, d_1).
			aux0 = (noDoublePeriodsP[p] ? Wp1[p] - 1 : 2 * Wp1[p] - 2);
			if (x0 > aux0) {

				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}

			// Upper bound of x0: what fits in (d_0, d_1) (by working days, by n0, by the remainder of Np0[p] and by the remainder of the class's 2*Ma).
			maxX0_ = (noDoublePeriodsP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Case 4 (Section 4.5.4): x0 = lessons of the (Teacher, class) pair on d_0; the rest of the pair's lessons in (d_0, d_1), n0, goes to d_1.
		case 4: {
			// n0 = lessons of the pair in the pair of days, defined in Case 2 (q2_).
			n0 = q2_[p][j_[i]];
			// No lessons: the only value is 0.
			if (n0 == 0) {
				if (x0 == -1) {
					x0 = 0;
					return false;
				} else
					return true;
			}
			// No successor if x0 is already 2 (or 1, when there is at most one lesson per day).
			if (x0 == 2 || (noDoublePeriodsP[p] && x0 == 1))
				return true;
			// Np2[p] and Np3[p]: maximum lessons of p on d_0 and on d_1 (defined in Case 3).
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
			// Sp0, Sp2, Sa0 and Sa2: lessons already assigned to the Teacher and the class on d_0 and on d_1 by the previous Case 4 units.
			Sp0 = Sp2_[pointerP[i] - 1];
			Sp2 = Sp3_[pointerP[i] - 1];
			Sa0 = Sa_4[pointerA2[i] - 1];
			Sa2 = Sa_5[pointerA2[i] - 1];

			// Limits of x0: what does not fit on d_1, and what fits on d_0 (by lessons per day, n0, Np2[p] and the class's Ma).
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

		// Case 11: accepts on the first visit (x0 becomes 0); when returning to this unit, the backtracking continues.
		case 11: {
			if (x0 < 0) {
				x0 = 0;
				return false;
			}
			return true;
		}
		}
		return false;
	}

	// Update of the control and linking variables after x0 is accepted (Sections 4.5.1 to 4.5.4).
	public void update2_() {
		switch (case_[i]) {
		// Case 1: defines Np0 (maximum lessons in (d_0, d_1)), Np1 (maximum on d_2), wp0 (working days in (d_0, d_1)) and,
		// for complex Teachers, stores the code x0 in x_case_1.
		case 1: {
			p = Ap[i];
			if (complex_p0[p]) {
				switch (x0) {
				case 0: {
					Np0[p] = 0;
					break;
				}
				case 1: {
					Np0[p] = Mp[p][d_0];
					break;
				}
				case 2: {
					Np0[p] = Mp[p][d_1];
					break;
				}
				case 3:
					Np0[p] = Mp[p][d_0] + Mp[p][d_1];
				}
				Np1[p] = (Wp1[p] - nDays_[x0] == 0 ? 0 : Mp[p][d_2]);
				wp0[p] = nDays_[x0];
				x_case_1[p] = x0;
			} else {
				Np0[p] = x0 * Math.max(Mp[p][d_0], Mp[p][d_1]);
				Np1[p] = (Wp1[p] - x0 == 0 ? 0 : Mp[p][d_2]);
				wp0[p] = x0;
			}
			break;
		}

		// Case 3: defines Np2 (maximum lessons on d_0) and Np3 (maximum on d_1).
		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			break;
		}
		// Case 2: accumulates the lessons of the Teacher and of the class in the pair of days and on d_2, and records in q2_ the lessons of the (Teacher, class) pair in (d_0, d_1).
		case 2: {
			p = Ap[i];
			nLessons = A[i] - x0;
			aux = pointerP2[i];
			Sp0_[aux] = Sp0_[aux - 1] + x0;
			Sp1_[aux] = Sp1_[aux - 1] + nLessons;
			aux = pointerA[i];
			Sa_0[aux] = Sa_0[aux - 1] + x0;
			Sa_1[aux] = Sa_1[aux - 1] + nLessons;
			q2_[p][j_[i]] = x0;
			break;
		}

		// Case 4: accumulates the lessons of the Teacher and of the class on d_0 and on d_1.
		case 4: {
			p = Ap[i];
			nLessons = q2_[p][j_[i]] - x0;
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

	// Method 'dependency' of Chapter 3 (see Section 4.5.4): indicates whether unit j depends on unit i (i < j). Case 1 and the
	// Case 11 units do not depend on any previous unit; Case 2 depends on the Teacher's Case 1 and on Case
	// 2 units of the same Teacher or the same class; Case 3 on the Case 1 of the same Teacher; Case 4 depends on the Case 2 of the same pair, on the
	// Case 3 of the same Teacher and on the Case 4 of the same Teacher or the same class.
	public boolean dependency(int i, int j) {
		int p1, p2, a1, a2;
		if (j <= i || case_[j] == 1 || case_[i] == 11 || case_[j] == 11)
			return false;
		p1 = Ap[i];
		a1 = Aa[i];
		p2 = Ap[j];
		a2 = Aa[j];
		if (complex_p0[p1] && case_[i] == 3)
			return false;
		if (case_[j] == 3)
			return case_[i] == 1 && p1 == p2;
		switch (case_[j]) {
		case (2): {
			switch (case_[i]) {
			case (1):
				return p1 == p2;
			case (2):
				return p1 == p2 || a1 == a2;
			case (3):
				return false;
			case (4):
				return false;
			}
			break;
		}
		case (4): {
			switch (case_[i]) {
			case (1):
				return false;
			case (2):
				return p1 == p2 && a1 == a2;
			case (3):
				return p1 == p2;
			case (4):
				return p1 == p2 || a1 == a2;
			}
		}
		}
		return true;
	}

	// determineArranjo of Chapter 3: computes z[i][l] (largest unit before i on which unit i + l depends) and then
	// accumulates the maximum along l, so that z[i][q - i] indicates where to backtrack.
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

	// Algorithm 2 of Chapter 3. Returns true if it obtains the distribution among the three days and false if it exhausts the possibilities.
	// In addition to Algorithm 2, it reuses the solutions already found for the Case 3 and 4 units of the Teachers that
	// precede the Teacher of the unit (iMin) to which the search backtracked, stored in y2: they remain valid.
	public boolean main2() {
		int k, p0, generator, iMin, iMax4, i0, p, maxX0_;
		Object[] opcoes = { "Yes", "No" };
		iMin = 0;
		iMax4 = 0;
		generator = -1;
		// Prepares z; all units start with the generator generator.
		determineZ(n2);
		imax = 1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		iMaxI_[1] = 1;
		Ap[0] = 0;
		Ap[n2 + 1] = 0;
		do {
			nIterations++;
			// If the successor was found:
			if (!maximum()) {
				// on reaching the Case 11 unit after a backtrack (iMin > 0), directly restores the solutions stored in y2
				// for the Case 3 and 4 units of the Teachers before the Teacher of iMin, instead of recomputing them...
				if (case_[i] == 11 && iMin > 0) {

					p0 = i + 1;
					while (p0 != iMax4 && Ap[iMin] != Ap[p0])
						p0++;
					iMax4 = i + 1;
					x_[i] = 0;
					p = Ap[p0 - 1];
					for (i = i + 1; i < p0; i++) {
						n0 = q2_[Ap[i]][j_[i]];
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
				// ...otherwise, simply stores x0, updates the control variables and advances.
				} else {
					x_[i] = x0;
					update2_();
					if (case_[i] == 11)
						iMax4 = i + 1;
					i++;
				}
				// All units closed: distribution obtained.
				if (i > n2)
					return true;

				// When the units of a new Teacher begin, stores in y2[p] the solution of the Case 3 and 4 units up to the previous Teacher p.
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux3 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = (short) x_[i0];
				}
				// imax: most advanced unit reached (consulted by get_P and get_A).
				if (i > imax) {
					imax = i;
				}
				// iMaxI_[k]: most advanced unit reached since the last visit to k (determines the backtrack).
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Failure: if it is the first unit, all distributions have been exhausted.
			} else {
				if (i == 1) {
					System.out.println("Searched all distributions for 3 days.");
					return false;
				}
				// Backtracks to p0 = z[i][iMaxI_[i] - i]; iMin records the furthest-back Case 1 or 2 unit reached, for the reuse described above.
				p0 = z[i][iMaxI_[i] - i];

				if (case_[p0] <= 2 && case_[i] >= 3)
					iMin = p0;
				else {
					if (case_[p0] <= 2 && p0 < iMin)
						iMin = p0;
				}
				if (p0 == 0) {

					return false;
				}
				// Resets the abandoned units to the generator.
				for (i = i; i > p0; i--)
					x_[i] = generator;
				i = p0;
			}
		} while (true);
	}

	// Writes the lessons per day into dd: d_2 receives A[i] - x_[i] (Case 2); in Case 4, d_0 receives x_[i] and d_1 receives the rest of the pair (q2_).
	public void update4() {
		int p, j, i, p1;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nClassesP[p]; j++) {
				dd[p][j][d_0] = 0;
				dd[p][j][d_1] = 0;
				dd[p][j][d_2] = 0;
			}
		}
		for (i = 1; i <= n2; i++) {
			p = Ap[i];
			j = j_[i];
			if (case_[i] == 2)
				dd[p][j][d_2] = A[i] - x_[i];
			else {
				if (case_[i] == 4) {
					dd[p][j][d_1] = q2_[p][j] - x_[i];
					dd[p][j][d_0] = x_[i];
				}
			}
		}
	}
}
