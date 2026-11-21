package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Class Module_4 - implements Module 4 (Phases 2 and 3; Sections 4.1.4 and 4.5 of the book).
//
// Starting point: a complete distribution of the lessons in dd (obtained by Modules 1 and 2). This module considers three days (d_0, d_1,
//   d_2),
// redistributes the lessons of each Teacher and class among them (without changing the weekly totals or the days outside the set) and, for each new
// distribution, tries to allocate the lessons to time slots, one day at a time, through Module 5. If Module 5 fails, the search backtracks to the unit
//   that
// can correct the problem. Phase 2: days 0, 1 and 2 (preliminaries(1)), with allocation to time slots only of the days other than day 2;
// Phase 3: days 2, 3 and 4 (preliminaries(2)), with allocation of the three days. (Days in the original order; see Prouesse5D_X.)
//
// Of the three days, d_2 is the one with the smallest sum of maximum numbers of lessons (the hardest) and (d_0,d_1) forms the remaining pair. Units (see
//   preliminaries):
//   case 1 : per Teacher, number of working days in (d_0,d_1) (if complex_p0 = true: 0, 1, 2 or 3 according to the days; Section 4.5.1);
//   case 2 : per (Teacher, class), x = lessons in the pair (d_0,d_1); y = A - x lessons on d_2 (Section 4.5.3);
//   case 11: validation: with the lessons set aside for d_2, there must be at least Qa Teachers available in each time slot of d_2;
//   case 3 : per Teacher, whether he/she works on day d_0 (Section 4.5.2);
//   case 4 : per (Teacher, class), x = lessons on d_0 and q2_ - x lessons on d_1 (identical to case 4 of Module 3, Section 4.4.1);
//   case 5 : last unit; checks availability in each time slot of days d_0 and d_1 and calls Module 5 for the days to be allocated.
// The search is Algorithm 2 of Chapter 3 (main2); the dependency between units is in dependency() (Section 4.5.4).
public class Module_4 extends PhaseB2_Super {
	// exec = 1 (Phase 2) or 2 (Phase 3);  nWorkDays1 = working days of the Teacher in the pair (auxiliary of determineAmplitude);
	// aux3 = index of the unit of case 11;  Dp, arranjo, nDays_, complex_p0, x_case_1 = variables of cases 1 and 3 (D_p, complex_p,
	//   x_case1);
	// g4_ = Module 5 object;  presentP[p] = the Teacher has lessons on the day under test;  days2[] = days to be allocated to time slots (by Module 5);
	// periods[] = order in which the time slots of the day are examined in the validations (first, last, second, second to last ...);  y2[p][i] =
	//   copies of x_ (see main2).
	static protected int exec, d1, nWorkDays1, aux3, Dp[], arranjo[];
	static public Module_5 g4_;
	static protected boolean test1, test2, case_Trivial, presentP[], complex_p0[];
	static protected int iMin, days2[], nDays_[], periods[], x_case_1[];
	static protected short y2[][];
	ImageIcon icone;

	// Constructor: allocates the arrays specific to Module 4 and creates the Module 5 object.
	public Module_4() {
		presentP = new boolean[Qp + 1];
		g4_ = new Module_5();
		days2 = new int[4];
		y2 = new short[Qp + 1][x_.length];
		Dp = new int[Qp + 1];
		arranjo = new int[3];
		complex_p0 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
	}

	// Sorts the Teachers (result in d2_) that will be handled by the units of Module 4. Teachers with no lessons on the three days stay at the end.
	// The others are chosen one by one, greedily: each position is filled by the Teacher with the smallest value of a criterion that combines (a) his/her
	// number of distribution possibilities (amplitude, computed in preliminaries), and (b) how much he/she shares classes with the Teachers
	//   already
	// chosen (aux2, weighted by the number of classes with lessons, q_0[p][0]).
	static public void sort_teachers2() {
		float aux1, aux2[], aux3, min1, max0, max0_, delta;
		boolean list_[], test;
		int i, indMin, k, p, p1, j, j1, a, count;
		list_ = new boolean[Qp + 1];
		aux2 = new float[Qp + 1];
		count = 0;
		k = 0;
		for (i = 1; i <= Qp; i++) {
			if (q_0[i][0] > 0)
				count++;
			else {
				d2_[Qp - k] = i;
				k++;
				list_[i] = true;
			}
		}
		for (i = 1; i <= count; i++) {
			if (i == count) {
				p = 1;
				while (list_[p])
					p++;
				d2_[i] = p;
				list_[p] = true;
				continue;
			}
			max0 = (float) -10000.0;
			max0_ = (float) -10000.0;
			if (i > 1) {
				for (p = 1; p <= Qp; p++) {
					if (list_[p] || q_0[p][0] <= 0)
						continue;
					aux2[p] = 0.0f;
					aux3 = 0.0f;
					for (k = 1; k <= i - 1; k++) {
						p1 = d2_[k];
						aux3 += 0.5 * (float) q_0[p1][0];
						delta = aux3 / (float) q_0[p1][0];
						aux3 += 0.5 * (float) q_0[p1][0];
						for (j = nClassesP[p1]; j >= 1; j--) {
							a = list[p1][j];
							test = false;
							for (j1 = nClassesP[p]; j1 >= 1; j1--) {
								if (a == list[p][j1] && q_0[p][j1] > 0) {
									test = true;
									break;
								}
							}
							if (test)
								aux2[p] += delta;
						}
					}
					if (aux2[p] > 0.00001) {
						aux2[p] = (float) q_0[p][0] / aux2[p];
						if (aux2[p] > max0)
							max0 = aux2[p];
					} else
						aux2[p] = -1.0f;
					if (amplitude[p] > max0_)
						max0_ = amplitude[p];
				}
			}
			indMin = 0;
			min1 = 1000000000.0f;
			for (p = 1; p <= Qp; p++) {
				if (list_[p] || q_0[p][0] <= 0)
					continue;
				if (i > 1) {
					if (aux2[p] < -0.001)
						aux1 = 1.0f + amplitude[p] / max0_;
					else
						aux1 = aux2[p] / max0 + amplitude[p] / max0_;
				} else
					aux1 = amplitude[p];
				if (aux1 < min1) {
					indMin = p;
					min1 = aux1;
				}
			}
			p = indMin;
			if (p == 0) {
				System.out.println("ERROR 049");
				System.exit(0);
			}
			d2_[i] = p;
			list_[p] = true;
		}
	}

	// Prepares Module 4 for Phase 2 (x0 = 1) or for Phase 3 (x0 = 2): defines the days, the auxiliary variables of each Teacher, the order
	// of the Teachers and the units. Returns n2, the number of units.
	public int preliminaries(int x0) {
		int d;
		JOptionPane.showMessageDialog(null, "This action may take a few minutes", "Result",
				JOptionPane.INFORMATION_MESSAGE);
		// nDays_ = number of working days associated with each value of x of case 1 (complex). In periods[], the order in which the time slots are examined
		//   for each Ma (4, 5 or 6).
		nDays_ = new int[] { 0, 1, 1, 2, 3 };
		if (Ma == 4)
			periods = new int[] { 0, 1, 4, 2, 3 };
		if (Ma == 5)
			periods = new int[] { 0, 1, 5, 2, 4, 3 };
		if (Ma == 6)
			periods = new int[] { 0, 1, 6, 2, 5, 3, 4 };
		iMin = 0;
		exec = x0;
		// 1) Days of the set: Phase 3 -> days 2, 3 and 4; Phase 2 -> days 0, 1 and 2 (days[1..3]).
		if (exec == 2) {
			days[1] = 2;
			days[2] = 3;
			days[3] = 4;
		} else {
			days[1] = 0;
			days[2] = 1;
			days[3] = 2;
		}
		imax = 0;

		for (k = 1; k <= 3; k++) {
			// 2) Sorts the three days by the sum of the maximum numbers of lessons (sort2): d_2 = day with the smallest sum (hardest); d_0 and d_1 = the other two.
			//   days[1..3] = d_0, d_1, d_2.
			amplitude[k] = 0;
			for (p = 1; p <= Qp; p++)
				amplitude[k] += Mp[p][days[k]];
			d2_[k] = days[k];
		}
		Module_1.sort2(1, 4);
		System.out.println("d2_: " + d2_[1] + " " + d2_[2] + " " + d2_[3]);
		d_0 = d2_[2];
		d_1 = d2_[3];
		d_2 = d2_[1];
		days[1] = d_0;
		days[2] = d_1;
		days[3] = d_2;
		// 3) days2[] = days whose lessons will be allocated to time slots: Phase 2 -> the days of the set except day 2; Phase 3 -> d_0, d_1 and, lastly,
		//   d_2.
		if (exec == 1) {
			k = 1;
			if (d2_[1] != 2) {
				days2[k] = d2_[1];
				k++;
			}
			if (d2_[2] != 2) {
				days2[k] = d2_[2];
				k++;
			}
			if (d2_[3] != 2)
				days2[k] = d2_[3];
		} else {
			k = 1;
			if (d2_[1] != d_2) {
				days2[k] = d2_[1];
				k++;
			}
			if (d2_[2] != d_2) {
				days2[k] = d2_[2];
				k++;
			}
			if (d2_[3] != d_2)
				days2[k] = d2_[3];
			days2[3] = d_2;
		}

		// 4) Auxiliary variables per Teacher p: q_0[p][j] = lessons of class j on the three days; q_0[p][0] = number of classes with lessons; ns_p[p] =
		//   total number of lessons
		// on the three days; mantemP[p] = has lessons on the three days.
		aux2 = (exec == 1 ? 3 : 0);
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;

			q_0[p][0] = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				q_0[p][j] = 0;
				for (d1 = 1; d1 <= 3; d1++)
					q_0[p][j] += dd[p][j][days[d1]];
				ns_p[p] += q_0[p][j];
				if (q_0[p][j] > 0)
					q_0[p][0]++;
			}

			mantemP[p] = ns_p[p] > 0;
			// Wp1[p] initially = number of working days of the Teacher outside the set (on the two remaining days of the week, according to the
			//   current distribution).
			Wp1[p] = 0;
			for (d1 = aux2; d1 <= aux2 + 1; d1++) {
				aux1 = 0;
				for (j = 1; j <= nClassesP[p]; j++)
					aux1 += dd[p][j][d1];
				if (aux1 > 0)
					Wp1[p]++;
			}
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][days[d + 1]];
			// Dp[p] = D_p: minimum number of working days on the three days (sum of the largest maxima until ns_p is covered), raised to the minimum required by each
			//   class.
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
			// complex_p0: d_0 and d_1 are available days with different maximum numbers of lessons.
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
		}

		for (p = 1; p <= Qp; p++) {
			// 5) amplitude[p] = (number of ways of dividing the Teacher's lessons among the three days, by determineAmplitude) / ns_p[p]; then
			//   sort_teachers2 defines the order of the Teachers.
			d2_[p] = p;
			Wp1[0] = Wp1[p];
			for (i = 1; i <= 2; i++) {
				case_[i] = i;
				x_[i] = -1;
			}
			aux2 = determineAmplitude();
			if (ns_p[p] == 0)
				amplitude[p] = 0.0f;
			else
				amplitude[p] = (float) aux2 / (float) ns_p[p];
		}
		sort_teachers2();

		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			// 6) Builds the units in the order d2_: per Teacher, one of case 1, followed by one of case 2 per class (A = lessons of the class on the 3 days); Wp1
			//   = W_1p = Wp0 minus the working days outside the set (at most 3).
			Wp1[p] = Wp0[p] - Wp1[p];
			if (Wp1[p] > 3)
				Wp1[p] = 3;
			k++;
			case_[k] = 1;
			Ap[k] = p;
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
		// Unit of case 11 (aux3 = its index).
		case_[k] = 11;
		aux3 = k;
		Ap[k] = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			k++;
			// Per Teacher, one unit of case 3, followed by one of case 4 per class with lessons.
			case_[k] = 3;
			Ap[k] = p;
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
		k++;
		// Last unit, of case 5 (n2 = k).
		case_[k] = 5;
		n2 = k;

		// 7) Pointers of the units of case 4 to the accumulated totals Sp2_/Sp3_ (pointerP) and of the units of case 2 to Sp0_/Sp1_ (pointerP2),
		//   with a zeroed position between Teachers.
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2 - 1; j++) {
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
		for (j = 1; j <= n2 - 1; j++) {
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
			// Pointers per class (blocks of 16 positions) for the units of case 2 (pointerA, in Sa_0/Sa_1) and, afterwards, of case 4 (pointerA2, in
			//   Sa_4/Sa_5).
			Sa_0[auxA[j] - 1] = 0;
			Sa_1[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2 - 1; j++) {
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
		for (j = 1; j <= n2 - 1; j++) {
			if (case_[j] == 4) {
				pointerA2[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		return n2;
	}

	// Counts in how many ways the lessons of Teacher p (ns_p[p]) can be divided among the three days, respecting the daily maxima and Wp0
	//   (search with
	// two units: x_[1] = lessons on d_0 and x_[2] = lessons on d_1; the remainder goes to d_2). The result is used as the 'amplitude' of the Teacher.
	public int determineAmplitude() {
		int count, n;
		count = 0;
		n = 2;
		i = 1;
		do {
			if (!maximumXi_0()) {
				x_[i] = x0;
				update02_();
				i++;
				if (i > n) {
					count++;
					i = n;
				}
			} else {
				if (i == 1)
					return count;
				x_[i] = -1;
				i--;
			}
		} while (true);
	}

	// After closing unit 1, updates nWorkDays1 = working days already occupied including d_0.
	public void update02_() {
		switch (case_[i]) {
		case 1: {
			nWorkDays1 = (x0 == 0 ? Wp1[0] : Wp1[0] + 1);
			break;
		}
		}
	}

	// Successor of the units of determineAmplitude: case 1, lessons on d_0; case 2, lessons on d_1 (the remaining ones go to d_2). Returns true if
	//   there is no successor.
	public boolean maximumXi_0() {
		x0 = x_[i];
		switch (case_[i]) {
		case 1: {
			do {
				x0 = (x0 == -1 ? 0 : x0 + 1);
				test1 = x0 > Mp[p][d_0] || x0 > ns_p[p] || (x0 > 0 && Wp1[0] + 1 > Wp0[p]);
				test2 = !test1 && x0 + Mp[p][d_1] + Mp[p][d_2] >= ns_p[p];
			} while (!test1 && !test2);
			return test1;
		}

		case 2: {
			do {
				x0 = (x0 == -1 ? 0 : x0 + 1);
				nLessons = ns_p[p] - x_[i - 1] - x0;
				test1 = x0 > Mp[p][d_1] || nLessons < 0 || (x0 > 0 && nWorkDays1 + 1 > Wp0[p]);
				test2 = !test1 && nLessons <= Mp[p][d_2];
				if (test2) {
					aux0 = (x0 == 0 ? nWorkDays1 : nWorkDays1 + 1);
					if (nLessons > 0)
						aux0++;
					test2 = aux0 <= Wp0[p];
				}
			} while (!test1 && !test2);
			return test1;
		}
		}
		return true;
	}

	// Equivalent to the method 'successr': looks for the smallest x0 > x_[i] that satisfies the constraints of the case of unit i (Section 4.5).
	// Returns false if successful (value in x0) and true otherwise.
	public boolean maximum() {
		int aux2, i0, maxX0_, minX0, d, x0_, k_;
		boolean test, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Case 1 (Section 4.5.1): x0 = number of working days in (d_0,d_1) (or, if complex_p0, the encoding 0, 1, 2, 3).
		// Minimum: Dp minus day d_2 (if available). Maximum: 2 (or 3 in the encoding), Wp1 and the days available in (d_0,d_1).
		// Additional check: the capacity on d_2 (if a working day is left over) plus that of the chosen days must cover the workload ns_p.
		case 1: {
			if (complex_p0[p]) {
				while (true) {
					aux2 = Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0);
					do {
						x0++;
						x0_ = nDays_[x0];
					} while (x0_ < aux2);
					if (x0 > 3 || x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
						return true;
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

		// Case 3 (Section 4.5.2): x0 = 1 if the Teacher works on d_0 and 0 if not. Constraints: x0 <= wp0; 0 if Mp[p][d_0] == 0; minimum to reach
		//   the days Dp
		// and wp0; with complex_p0 and a single working day, the value is a consequence of x_case_1. Finally, the workload ns_p must be
		//   fulfilled
		// with the capacities of d_0, d_1 and d_2.
		case 3: {
			while (true) {
				i0 = wp0[p];
				if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
					return true;
				if (Mp[p][d_2] == 0 || i0 == Wp1[p])
					aux2 = 0;
				else
					aux2 = 1;
				x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
				if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
					x0 = 1;
				if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
					return true;
				aux2 = (Wp1[p] - i0 > 0 ? Mp[p][d_2] : 0);
				test = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
				test = test && ((!complex_p0[p]) || (i0 != 1) || (x_case_1[p] == 1 && x0 == 1)
						|| (x_case_1[p] == 2 && x0 == 0));
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

		// Case 2 (Section 4.5.3): x0 = lessons of (p, class) in the pair (d_0,d_1); y = A[i] - x0 stay on d_2. Constraints: the sum of the Teacher's lessons
		//   in the pair does not
		// exceed Np0, and on d_2 does not exceed Np1; in the class, 2*Ma in the pair and Ma on d_2; at most (1 or 2) lessons per day and class times the
		//   working days.
		case 2: {
			// There is no successor if x0 is already the maximum possible.
			if (x0 == 2 * wp0[p] || (noDoublePeriodsP[p] && x0 == wp0[p]))
				return true;
			// Trivial case: the Teacher has capacity only in the pair or only on d_2; there is a single value (0 or all the lessons), tried once. If already
			//   initialized, reuses maxX0[i].
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

			// Lower bound: what does not fit on d_2 (according to the remaining working days, Np1 and the Ma capacity of the class on d_2).
			aux0 = Math.min(Wp1[p] - wp0[p], 1);
			minX0 = (noDoublePeriodsP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);
			// If x0 exceeds what is possible without using one of the Teacher's working days on d_2: if the Teacher already has lessons on d_2 (Sp2 > 0) there is no
			//   successor;
			// otherwise the only option left is to place all the lessons of the class in the pair (x0 = n0).
			aux0 = (noDoublePeriodsP[p] ? Wp1[p] - 1 : 2 * Wp1[p] - 2);
			if (x0 > aux0) {
				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}
			// Upper bound: working days in the pair, A[i], remaining capacity of the Teacher in the pair (Np0 - Sp0) and of the class (2*Ma - Sa0).
			maxX0_ = (noDoublePeriodsP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Case 4 (Section 4.5.4, same as Case 4 of Module 3): x0 = lessons of (p, class) on d_0; the remainder of q2_ goes to d_1; same constraints as
		//   Module 3 (with N_2p = Np2, N_3p = Np3, and Ma per day).
		case 4: {
			n0 = q2_[p][j_[i]];
			if (n0 == 0) {
				if (x0 == -1) {
					x0 = 0;
					return false;
				} else
					return true;
			}
			if (x0 == 2 || (noDoublePeriodsP[p] && x0 == 1))
				return true;
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

		// Case 11 (validation, a single execution): with the lessons set aside for d_2 by the units of case 2, marks the Teachers with lessons on d_2
		// (presentP) and checks, in each time slot of d_2, whether at least Qa Teachers are present and available. In Phase 2, if d_2 is day 2, the
		//   validation is skipped.
		case 11: {
			if (x0 < 0) {
				x0 = 0;
				if (exec == 1 && d_2 == 2)
					return false;
				for (p = 1; p <= Qp; p++)
					presentP[p] = false;
				for (i0 = 1; i0 < i; i0++) {
					p = Ap[i0];
					presentP[p] = presentP[p] || (case_[i0] == 2 && A[i0] - x_[i0] > 0);
				}
				test2 = true;
				for (k = 1; k <= Ma; k++) {
					k_ = periods[k];
					aux0 = 0;
					for (p = 1; p <= Qp; p++) {
						if (presentP[p] && !unPeriods[p][d_2][k_])
							aux0++;
					}
					test2 = aux0 >= Qa;
					if (!test2)
						break;
				}
				return !test2;
			}
			return true;
		}

		// Case 5 (last unit): the distribution of the three days is complete. update4() writes into dd.
		// 1) Validation: in each time slot of days d_0 and d_1 there must be Qa Teachers present and available; if not, backtracks to the last unit
		//   of case 3.
		case 5: {
			update4();
			d = 0;
			for (d1 = 1; d1 <= 2; d1++) {
				d = days[d1];
				test2 = true;
				if (exec == 1 && d == 2)
					continue;
				for (p = 1; p <= Qp; p++) {
					presentP[p] = false;
					for (j = 1; j <= nClassesP[p]; j++) {
						if (dd[p][j][d] > 0) {
							presentP[p] = true;
							break;
						}
					}
				}
				for (k = 1; k <= Ma; k++) {
					k_ = periods[k];
					aux0 = 0;
					for (p = 1; p <= Qp; p++) {
						if (presentP[p] && !unPeriods[p][d][k_])
							aux0++;
					}
					test2 = aux0 >= Qa;
					if (!test2)
						break;
				}
				if (!test2)
					break;
			}
			if (!test2) {
				i1 = n2;
				do
					i1--;
				while (case_[i1] != 3);
				System.out.println("i1 = " + i1);
				return true;
			}

			test2 = true;
			// 2) Runs Module 5 for each day to be allocated (days2): preliminaries, determineZ and main. If all the days are allocated, case
			//   5 is
			// successful (returns false). If a day fails, g4_.getP/getA indicate the Teacher and the class; the backtrack goes to the unit of case 2 (if the
			//   day is d_2)
			// or of case 4 (if it is another day) corresponding (i1) and returns true.
			aux2 = (exec == 1 ? 2 : 3);
			for (d1 = 1; d1 <= aux2; d1++) {
				aux0 = g4_.preliminaries(days2[d1]);
				g4_.determineZ(aux0);
				test2 = g4_.main(aux0);
				if (!test2)
					break;
				else
					System.out.println("Closed for d1 = " + d1);
			}
			if (!test2) {
				p = g4_.getP();
				a = g4_.getA();
				i1 = n2;
				if (days2[d1] == d_2) {
					do
						i1--;
					while (case_[i1] != 11);
					do
						i1--;
					while (p != Ap[i1] || a != Aa[i1] || case_[i1] != 2);
				} else {
					do
						i1--;
					while (p != Ap[i1] || a != Aa[i1] || case_[i1] != 4);
				}
				System.out.println("i1 = " + i1);
				return true;
			}
			return false;
		}
		}
		return false;
	}

	// Updates the linking and control variables after unit i receives the value x0 (Section 4.5).
	public void update2_() {
		switch (case_[i]) {
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
				// Case 1: defines Np0 (maximum number of lessons of the Teacher in the pair), Np1 (maximum on d_2: 0 if no working day is left over), wp0 (days in the pair) and
				//   x_case_1. The version for complex_p0 uses the maxima of each day.
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

		// Case 3: Np2 = maximum of the Teacher on d_0 (0 if he/she does not work) and Np3 = maximum on d_1 (0 if he/she does not work).
		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			break;
		}
		case 2: {
			p = Ap[i];
			// Case 2: accumulates the lessons of the Teacher and of the class in the pair (Sp0_, Sa_0) and on d_2 (Sp1_, Sa_1); q2_[p][j] = x0, lessons of the class in the pair.
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

		case 4: {
			p = Ap[i];
			// Case 4: accumulates the lessons of the Teacher and of the class on d_0 (Sp2_, Sa_4) and on d_1 (Sp3_, Sa_5).
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

	// dependency(i,j) (Section 4.5.4): true if x[j] depends on x[i]. The units of case 1 do not depend on previous units; case 11
	//   depends only
	// on the units of case 2; those of case 3 only on case 1 of the same Teacher (and not even that if complex_p0); those of case 2 depend on case 1 of the
	//   same Teacher and on
	// units of case 2 of the same Teacher or class; those of case 4 depend on units of case 2 of the same Teacher and class, on case 3 of the same
	//   Teacher and on units
	// of case 4 of the same Teacher or class. In the other cases a dependency is assumed to exist.
	public boolean dependency(int i, int j) {
		int p1, p2, a1, a2;
		if (j <= i || case_[j] == 1 || case_[i] == 11)
			return false;
		if (case_[j] == 11)
			return case_[i] == 2;
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

	// Computes the array z of Algorithm 2 (Chapter 3, determineArranjo) from dependency(): z[i][l] = largest unit p < i on which
	//   some
	// unit from i to i+l depends, made cumulative in l.
	public void determineZ(int n_) {
		int p, i, l;
		z = new short[n_ + 1][n_ + 1];
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

	// Main search of Module 4 (Algorithm 2 of Chapter 3; generator = generator = -1). Returns true if all the units (including the time slot
	// allocation one, case 5) were closed; false if all the distributions for the three days were searched without success.
	public boolean main2() {
		int k, p0, generator, iMin, iMax4, i0, p, maxX0_;
		String input;
		iMin = 0;
		iMax4 = 0;
		generator = -1;
		determineZ(n2);
		// In Phase 2 asks the user for the maximum execution time and starts the timing (Phase 3 keeps counting from the same start).
		if (exec == 1) {
			input = (String) JOptionPane.showInputDialog(null, "Enter maximum execution time (min)", "Input",
					JOptionPane.QUESTION_MESSAGE, icone, null, null);
			timeMaximum = Math.round(Float.parseFloat(input));
			initialTime = System.currentTimeMillis();
			nIterations = 0L;
		}
		imax = 1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		iMaxI_[1] = 1;
		Ap[0] = 0;
		Ap[n2] = 0;
		do {
			nIterations++;
			if (nIterations >= 50000L) {
				nIterations = 0L;
				if ((System.currentTimeMillis() - initialTime) / 60000.0 > timeMaximum) {
					JOptionPane.showMessageDialog(null, "Execution exceeded the maximum time", "Result",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			// With a successor: stores x0 and updates the control and advances.
			// Optimization: after a backtrack that only changed the units from iMin on (of Teacher Ap[iMin]), upon reaching case 11 again the
			//   units of cases 3 and 4
			// of the previous Teachers are restored from copies (y2) of the solutions already found, without recomputation, and the search proceeds at
			//   Teacher Ap[iMin].
			if (!maximum()) {
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
				} else {
					x_[i] = x0;
					update2_();
					if (case_[i] == 11)
						iMax4 = i + 1;
					i++;
				}
				if (i > n2)
					return true;
				// Upon concluding the units of a Teacher in cases 3/4, stores in y2[p] a copy of x_ of the units after case 11 (for the optimization
				//   above).
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux3 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = (short) x_[i0];
				}
				if (i > imax) {
					imax = i;
					System.out.println(imax + "/" + n2);
				}
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				// No successor: backtrack of Algorithm 2. p0 = z[i][iMaxI_[i] - i], or, for case 5, i1 (unit computed in maximum for the Teacher and the
				//   class
				// where Module 5 failed). iMin records the smallest unit of cases 1/2 to which the search backtracked.
				if (i == 1) {
					System.out.println("Searched all the distributions for 3 days.");
					return false;
				}
				p0 = (case_[i] == 5 ? i1 : z[i][iMaxI_[i] - i]);
				if (case_[p0] <= 2 && case_[i] >= 3)
					iMin = p0;
				else {
					if (case_[p0] <= 2 && p0 < iMin)
						iMin = p0;
				}
				if (p0 == 0) {
					JOptionPane.showMessageDialog(null, "Contact the developer. Error code: 015", "",
							JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
				for (i = i; i > p0; i--)
					x_[i] = generator;
				i = p0;
			}
		} while (true);
	}

	// Writes into dd the distribution of the three days: zeroes dd on the days of days[1..3] and fills it with the result of cases 2 (d_2 = A - x) and 4
	//   (d_0 = x and d_1 = q2_ - x).
	public void update4() {
		int p, j, i, p1;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nClassesP[p]; j++) {
				for (d1 = 1; d1 <= 3; d1++)
					dd[p][j][days[d1]] = 0;
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

	// Zeroes the timetables (of Teachers and of classes) of day d1.
	private void initializeTables(int d1) {
		int p1, a1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tableP[p1][d1][k1] = 0;
			for (a1 = 1; a1 <= Qa; a1++)
				tableA[a1][d1][k1] = 0;
		}
	}

	// Called by Prouesse5D_X when the phase was successful: redoes, through Module 5, the allocation to time slots of each day of days2
	// (preliminaries, determineZ, main) and writes the time slots into the tables tableP and tableA (g4_.result).
	public void result() {
		for (d1 = 1; d1 <= (exec == 1 ? 2 : 3); d1++) {
			initializeTables(days2[d1]);
			aux0 = g4_.preliminaries(days2[d1]);
			g4_.determineZ(aux0);
			g4_.main(aux0);
			g4_.result(aux0, days2[d1]);
		}
	}
}
