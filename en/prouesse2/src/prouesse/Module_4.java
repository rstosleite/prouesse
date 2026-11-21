package prouesse;

import javax.swing.ImageIcon;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.JOptionPane;

// Class Module_4 - implements Module 4 (Sections 4.1.4 and 4.5 of the book), which runs Phases 2 and 3.
//
// Role: given a complete distribution of lessons (in dd), changes the distribution among three days and, for each new
// distribution obtained, tries to allocate the lessons of each day to the time slots of the day, calling Module 5 (class Module_5).
// In Phase 2 (exec == 1) the three days are 0, 1 and 2, but only the time slots of two of them are allocated (day 2 merely
// takes part in the redistribution, to provide flexibility); in Phase 3 (exec == 2) the three days are 2, 3 and 4 and all of them are allocated.
// The three days are labeled d_0, d_1 and d_2 according to their difficulty (preliminaries): d_2 is the hardest day (smallest sum of Mp).
//
// Cases of the units (case_[i]), in this order: 1 and 2 (per Teacher: a Case 1 followed by the Cases 2 of their classes), 11 (validation
// of the availability of Teachers in the time slots of d_2), 3 and 4 (per Teacher: a Case 3 followed by the Cases 4 of their classes) and 5 (call
// to Module 5). Cases 1 to 4 and 11 follow Sections 4.5.1 to 4.5.4 of the book; Case 5 exists only in the code.
// The 'generator' is generator = -1 and the 'successor' is the method maximum(), which returns false if it finds the successor (value in x0) and
// true if none exists. The search is carried out by main2 (Algorithm 2 of Chapter 3).
public class Module_4 extends Accessory_classes {

	// exec: 1 (Phase 2) or 2 (Phase 3); y2[p][i]: solutions of Cases 3 and 4 stored for reuse after backtracking;
	// Dp[p]: minimum working days of p among the three days; nWorkDays1: working days of p already counted (determineAmplitude).
	static protected int exec, d1, nWorkDays1, y2[][], aux3, Dp[], arranjo[];
	// g4_: Module 5 object, which allocates the lessons of a day to the time slots.
	static public Module_5 g4_;
	// presentP[p]: p has lessons on day d_2 (used in the time-slot validations); complex_p0[p]: Mp[p][d_0] and Mp[p][d_1] positive and distinct.
	static protected boolean test1, test2, case_Trivial, presentP[], complex_p0[];
	// iMin: furthest-back Case 1 or 2 unit reached by a backtrack (reuse of solutions); days2: days whose time slots
	// will be allocated by Module 5; nDays_: working days of the pair (d_0, d_1) for each Case 1 code; horários: order in which
	// the time slots of the day are checked; x_case_1[p]: code chosen in Case 1 for complex Teachers.
	static protected int iMin, days2[], nDays_[], periods[], x_case_1[], p1;
	ImageIcon icone;

	// Constructor: allocates the auxiliary variables and creates the Module 5 object.
	public Module_4() {
		q_0 = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++)
			q_0[p1] = new int[nClassesP[p1] + 1];
		presentP = new boolean[Qp + 1];
		g4_ = new Module_5();
		days2 = new int[4];
		y2 = new int[Qp + 1][x_.length];
		Dp = new int[Qp + 1];
		arranjo = new int[3];
		complex_p0 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
	}

	// Orders the Teachers (result in d2_) for assembling the units. Teachers without lessons on the three days go to the end. The
	// others are chosen one at a time: the first is the one with the smallest amplitude; each of the following is the one with the smallest value of the sum of
	// two normalized terms: (a) the ratio between the Teacher's number of classes and the weighted overlap of their classes with those
	// of the Teachers already ordered (the overlap weighs more for the most recently chosen Teachers) and (b) the Teacher's amplitude.
	static public void sort_teachers2() {
		float aux1, aux2[], aux3, min1, max0, max0_, delta;
		boolean listAa[], test;
		int i, indMin, k, p, p1, j, j1, a, count;
		listAa = new boolean[Qp + 1];
		aux2 = new float[Qp + 1];
		count = 0;
		k = 0;
		for (i = 1; i <= Qp; i++) {
			if (q_0[i][0] > 0)
				count++;
			else {
				d2_[Qp - k] = i;
				k++;
				listAa[i] = true;
			}
		}
		for (i = 1; i <= count; i++) {
			if (i == count) {
				p = 1;
				while (listAa[p])
					p++;
				d2_[i] = p;
				listAa[p] = true;
				continue;
			}
			max0 = (float) -10000.0;
			max0_ = (float) -10000.0;
			if (i > 1) {
				for (p = 1; p <= Qp; p++) {
					if (listAa[p] || q_0[p][0] <= 0)
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
				if (listAa[p] || q_0[p][0] <= 0)
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
			listAa[p] = true;
		}
	}

	// Prepares the run of Module 4 for Phase 2 (x0 = 1) or 3 (x0 = 2) and returns the number of units n2.
	// Steps: (1) defines the three days and orders them by difficulty; (2) obtains, for each Teacher, the lessons on the three days (q_0) and the
	// auxiliary variables; (3) orders the Teachers (sort_teachers2); (4) builds the units; (5) initializes the pointers of the control variables.
	public int preliminaries(int x0) {
		int d;
		JOptionPane.showMessageDialog(null, "This action may take a few minutes", "Result",
				JOptionPane.INFORMATION_MESSAGE);
		// nDays_[code]: number of working days of the pair (d_0, d_1) for codes 0 to 3 of Case 1.
		nDays_ = new int[] { 0, 1, 1, 2, 3 };

		// periods[k]: order in which the time slots of the day are checked (first, last, second, second to last, etc.), according to Ma (4, 5 or 6 lessons per day).
		if (Ma == 4)
			periods = new int[] { 0, 1, 4, 2, 3 };
		if (Ma == 5)
			periods = new int[] { 0, 1, 5, 2, 4, 3 };
		if (Ma == 6)
			periods = new int[] { 0, 1, 6, 2, 5, 3, 4 };
		// exec indicates the phase: 1 = days (0, 1, 2); 2 = days (2, 3, 4).
		iMin = 0;
		exec = x0;
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

		// amplitude[k] = sum of Mp over all Teachers on day days[k]; sort2 orders the three days in increasing order of this sum
		// (from hardest to easiest), result in d2_[1..3].
		for (k = 1; k <= 3; k++) {
			amplitude[k] = 0;
			for (p = 1; p <= Qp; p++)
				amplitude[k] += Mp[p][days[k]];
			d2_[k] = days[k];
		}
		Module_2A.sort2(1, 4);
		System.out.println("d2_: " + d2_[1] + " " + d2_[2] + " " + d2_[3]);

		// d_2 is the hardest day, d_0 the intermedayte one and d_1 the easiest. Day d_2 is the one that receives the remaining lessons of Case 2 and the one validated by Case 11.
		d_0 = d2_[2];
		d_1 = d2_[3];
		d_2 = d2_[1];
		days[1] = d_0;
		days[2] = d_1;
		days[3] = d_2;

		// days2: days whose time slots will be allocated by Module 5, in order of difficulty. In Phase 2, day 2 (Wednesday) is left out.
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
			days2[1] = d2_[1];
			days2[2] = d2_[2];
			days2[3] = d2_[3];
		}

		// Days outside the three considered: days 3 and 4 (Phase 2) or days 0 and 1 (Phase 3).
		aux2 = (exec == 1 ? 3 : 0);
		// For each Teacher: q_0[p][j] = lessons of the j-th class on the three days (sum of dd); ns_p[p] = total; q_0[p][0] = number of
		// classes with lessons; mantemP[p] = p has lessons on the three days; Wp1[p] = working days of p outside the three days.
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
			Wp1[p] = 0;
			for (d1 = aux2; d1 <= aux2 + 1; d1++) {
				aux1 = 0;
				for (j = 1; j <= nClassesP[p]; j++)
					aux1 += dd[p][j][d1];
				if (aux1 > 0)
					Wp1[p]++;
			}
			// Dp[p]: minimum working days of p among the three days (largest Mp first, until ns_p[p] is covered) and so that the lessons of each class fit (1 or 2 per day).
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
					Dp[p] = Math.max(Dp[p], q_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q_0[p][j] + 1) / 2);
			}
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
		}

		// amplitude[p]: number of possible distributions of p's lessons among the three days (determineAmplitude) divided by the
		// Teacher's number of lessons; used in sort_teachers2 (fewer distributions = more constrained Teacher).
		for (p = 1; p <= Qp; p++) {
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

		// Assembly of the units. For each Teacher with lessons, in the order of d2_: a Case 1 unit and a Case 2 unit for each class with lessons.
		// Wp1[p] becomes the maximum number of working days of p on the three days (Wp0 minus the working days outside them, at most 3).
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
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

		// Case 11 unit: validation of the time slots of day d_2.
		k++;
		case_[k] = 11;
		aux3 = k;
		Ap[k] = 0;
		// Case 3 and 4 units (one Case 3 per Teacher followed by the Cases 4 of their classes).
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			k++;
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

		// Final unit (Case 5): calls Module 5 to allocate the lessons to the time slots of the days.
		k++;
		case_[k] = 5;
		n2 = k;

		// Initial pointers and values of the control variables (Sp2_, Sp3_, Sp0_, Sp1_, Sa_0, Sa_1, Sa_4, Sa_5), as in Module 3A.
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

	// Counts the number of possible distributions of the lessons of Teacher p (global variable) among the three days: two units,
	// one for the lessons on d_0 and another for the lessons on d_1; the remainder goes to d_2.
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

	// Updates the number of working days already counted.
	public void update02_() {
		switch (case_[i]) {
		case 1: {
			nWorkDays1 = (x0 == 0 ? Wp1[0] : Wp1[0] + 1);
			break;
		}
		}
	}

	// Successor of the two units of determineAmplitude: x0 = lessons on d_0 (case 1) and on d_1 (case 2). The remaining lessons
	// must fit on d_2 and the number of working days cannot exceed Wp0[p].
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

	// Successor of Module 4 (Section 4.5). Returns false if it found the successor of x_[i] (value in x0) and true if none exists.
	public boolean maximum() {
		int aux2, i0, maxX0_, minX0, d, x0_, k_;
		boolean test, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Case 1 (Section 4.5.1): number of working days of p in the pair (d_0, d_1) (with codes from 0 to 3, if complex_p0). Identical to Case 1 of Module 3A.
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

		// Case 3 (Section 4.5.2): x0 = 1 if d_0 is a working day of p, 0 if not. Identical to Case 3 of Module 3A.
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

		// Case 2 (Section 4.5.3): x0 = lessons of the (Teacher, class) pair in the pair (d_0, d_1); A[i] - x0 go to d_2. Identical to Case 2 of Module 3A.
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

			aux0 = Math.min(Wp1[p] - wp0[p], 1);
			minX0 = (noDoublePeriodsP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);
			aux0 = (noDoublePeriodsP[p] ? Wp1[p] - 1 : 2 * Wp1[p] - 2);
			if (x0 > aux0) {
				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}
			maxX0_ = (noDoublePeriodsP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Case 4 (Section 4.5.4): x0 = lessons of the (Teacher, class) pair on d_0; the rest of the pair's lessons in the pair of days goes to d_1. Identical to Case 4 of Module 3A.
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

		// Case 11: validation of the lessons assigned to day d_2 (end of Section 4.2.3): in each time slot k of the day, the number of Teachers who have a
		// lesson on d_2 and are available (have no unavailable time slot) must be at least Qa, the number of classes.
		case 11: {

			if (x0 < 0) {
				x0 = 0;
				// In Phase 2, if d_2 is day 2 (whose time slots will not be allocated), nothing is checked.
				if (exec == 1 && d_2 == 2)
					return false;
				// presentP[p]: p has lessons on d_2 according to the Case 2 units already defined (A[i] - x_[i] > 0).
				for (p = 1; p <= Qp; p++)
					presentP[p] = false;
				for (i0 = 1; i0 < i; i0++) {
					p = Ap[i0];
					presentP[p] = presentP[p] || (case_[i0] == 2 && A[i0] - x_[i0] > 0);
				}
				test2 = true;
				// Counts, for each time slot, the present and available Teachers; if some time slot has fewer than Qa, the validation fails.
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

		// Case 5: the distribution among the three days is complete. Writes it to dd (update4) and tries to allocate the lessons to the time slots.
		case 5: {

			update4();
			// Preliminary check, for days d_0 and d_1 (d_2 was already checked in Case 11): in each time slot, there must be at least Qa available Teachers among those who have a lesson on the day.
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

			// If this check fails, the backtrack goes to the Case 3 unit closest to the end.
			if (!test2) {
				i1 = n2;
				do
					i1--;
				while (case_[i1] != 3);
				System.out.println("i1 = " + i1);
				return true;
			}

			// For each day in days2, runs Module 5 (preliminaries, determineZ and main). If Module 5 succeeds on all days,
			// Case 5 is approved (false).
			test2 = true;
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
			// Module 5 failed: the backtrack goes to the Case 2 unit (if the day is d_2) or the Case 4 unit (if it is d_0 or d_1) of the same Teacher and the same class at which Module 5 stopped.
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

	// Update of the control and linking variables after x0 is accepted (Sections 4.5.1 to 4.5.4). Identical to that of Module 3A.
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
				Np1[p] = (Wp1[p] - nDays_[x0] == 0 ? 0 : Mp[p][d_2]);
				wp0[p] = nDays_[x0];
				x_case_1[p] = x0;
			} else {

				if (x0 == 0)
					Np0[p] = 0;
				else {
					if (x0 == 1)
						Np0[p] = Math.max(Mp[p][d_0], Mp[p][d_1]);
					else
						Np0[p] = Mp[p][d_0] + Mp[p][d_1];
				}
				Np1[p] = (Wp1[p] - x0 == 0 ? 0 : Mp[p][d_2]);
				wp0[p] = x0;
			}
			break;
		}

		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			break;
		}
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

	// Method 'dependency' of Chapter 3, as listed in Section 4.5.4 of the book.
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

	// Algorithm 2 of Chapter 3. Returns true if it obtains a distribution for the three days with time slots allocated (Module 5) and false otherwise.
	// Reuses, in y2, the solutions of Cases 3 and 4 of the Teachers before the Teacher of iMin, as in Module 3A.
	public boolean main2() {
		int k, p0, generator, iMin, iMax4, i0, p, maxX0_;
		Object[] opcoes = { "Yes", "No" };
		String input;
		iMin = 0;
		iMax4 = 0;
		generator = -1;
		determineZ(n2);
		// In Phase 2, asks the user for the maximum execution time and starts the timer (Phase 3 continues with the same timer).
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
			// Time control: every 50000 iterations checks whether the maximum time has been exceeded.
			if (nIterations >= 50000L) {
				nIterations = 0L;
				if ((System.currentTimeMillis() - initialTime) / 60000.0 > timeMaximum) {
					JOptionPane.showMessageDialog(null, "Execution exceeded the maximum time", "Result",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			// Successor found: if we are at the Case 11 unit after a backtrack (iMin > 0), restores the solutions stored in y2 for the Teachers before that of iMin;
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
				// otherwise stores x0, updates the control variables and advances.
				} else {
					x_[i] = x0;
					update2_();
					if (case_[i] == 11)
						iMax4 = i + 1;
					i++;
				}
				// All units (including Case 5) closed: success.
				if (i > n2)
					return true;
				// When starting a new Teacher in the Case 3 and 4 units, stores in y2[p] the solutions up to the previous Teacher.
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux3 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = x_[i0];
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
			// Failure: backtracks to z[i][iMaxI_[i] - i] or, if the failure was at the Case 5 unit, to the unit indicated in i1.
			} else {
				if (i == 1) {
					System.out.println("Searched all distributions for 3 days.");

					return false;
				}

				p0 = (case_[i] == 5 ? i1 : z[i][iMaxI_[i] - i]);
				// iMin records the furthest-back Case 1 or 2 unit reached by the backtrack.
				if (case_[p0] <= 2 && case_[i] >= 3)
					iMin = p0;
				else {
					if (case_[p0] <= 2 && p0 < iMin)
						iMin = p0;
				}
				// Should not occur: backtrack beyond the first unit.
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

	// Writes the lessons of the three days into dd: d_2 receives A[i] - x_[i] (Case 2); in Case 4, d_0 receives x_[i] and d_1 receives the rest of the pair.
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

	// Clears, for day d1, the timetables of the Teachers (tableP) and of the classes (tableA).
	private void initializeTables(int d1) {
		int p1, a1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tableP[p1][d1][k1] = 0;
			for (a1 = 1; a1 <= Qa; a1++)
				tableA[a1][d1][k1] = 0;
		}
	}

	// Obtains the final timetables: for each day in days2, repeats the run of Module 5 and writes the result into the timetables.
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
