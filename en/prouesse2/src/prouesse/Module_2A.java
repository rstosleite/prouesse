package prouesse;

import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.util.Random;
import java.util.BitSet;

// Class Module_2A - implements Module 2A (Phase 1.1 of Algorithm P2; Sections 5.2.1, 5.2.2, 5.2.3 and 5.3.1 of the book).
//
// Role: it is the first step in obtaining the distribution of lessons. The week is divided into two parts: the first with
// days d0 and d1 (indices 0 and 1) and the second with days d2, d3 and d4 (indices 2, 3 and 4). This module decides, for each Teacher p:
//   - maxDays0[p] and maxDays1[p]: numbers (upper bounds) of working days in the 1st and 2nd parts of the week;
//   - ns0[p] and ns1[p] (Case 10): workload of p in the 1st and 2nd parts, for the Teachers with complex_p0 or complex_p1 true;
//   - x0_[p][*] and x1_[p][*]: the working days of p, for the Teachers with complex_p0 (1st part) or complex_p1 (2nd part) true.
// With this it computes the bounds Np0[p] and Np1[p] (maximum lessons of p in each part of the week) and splits
// the lessons of each (Teacher, class) pair between the two parts (Case 2 units), which makes it possible to validate the decisions.
// After all units are closed, it enumerates the 'possibilities' (bitSet_0, 1st part) and 'sub-possibilities'
// (bitSet_1, 2nd part), derives the 2nd-part 'possibilities' (bitSet_2) from bitSet_1 and calls Module 2B.
//
// The search uses Algorithm 2 of Chapter 3 (method mainV2): the book's 'generator' is the constant 'generator' (= -1) and the
// 'successor' is the method maximum(), which returns false when it finds the successor (value in x0) and true when none exists.
// Cases (case_[i]) of the units, in this order: 6 (control of freedom_degrees), 10 and 1 (per Teacher), 11 (validation), 2.
public class Module_2A extends PhaseA1_Super {
	// 'Complex' indicators per Teacher (Section 5.2.1): complex_p0[p] = Mp[p][0] and Mp[p][1] positive and distinct;
	// complex_p1[p] = among Mp[p][2], Mp[p][3] and Mp[p][4] there are two positive, distinct values;
	// complex_p2[p] = the same as complex_p0, but for days d2 and d3 (used later by Module 3B).
	static public boolean complex_p0[], complex_p1[], complex_p2[];
	// bitSet_0: possibilities of the 1st part (2 bits per Teacher: days d0 and d1; the first 2*Qp bits hold the fixed days of the
	// complex_p0 Teachers); bitSet_1: sub-possibilities of the 2nd part (3 bits per Teacher; the first 3*Qp bits hold the fixed days
	// of the complex_p1 Teachers); bitSet_2: possibilities of the 2nd part (2 bits per Teacher forming the number 0, 1 or 2 of
	// working days on d2 and d3). See Section 5.2.2.
	static public BitSet bitSet_0, bitSet_1, bitSet_2;
	// nPossib[0] and nPossib[1]: numbers of possibilities (bitSet_0) and sub-possibilities (bitSet_1) stored;
	// nPossib_2: number of possibilities of the 2nd part (bitSet_2); w: auxiliary variable; maxDays0[p] and maxDays1[p]:
	// bounds on the number of working days of p in the 1st and 2nd parts of the week.
	static public int nPossib[], nPossib_2, w, maxDays0[], maxDays1[];
	// sumA_0[a] and sumA_1[a]: sum, for class a, of the lessons the Teachers can offer in the 1st and 2nd parts of the week
	// (used in the validations of the classes' demands).
	static public int days[], sumA_0[], sumA_1[];
	// x0_[p][k] and x1_[p][k]: working days (1 or 0) fixed for p in the 1st and 2nd parts (result of detPossib_ with start true);
	// maximum0[p] = {smaller, larger} of Mp[p][0] and Mp[p][1]; maximum1[p] = Mp[p][2..4] in increasing order;
	// ns0[p] and ns1[p]: workload of p in the 1st and 2nd parts; xx[p][j]: working day under construction by detPossib_;
	// sumMax[p][j]: running sum, up to Teacher p, of the maximum lessons Mp on day j; freedom_degrees and max_: control the
	// maximization of the number of possibilities (Section 5.2.3); count2: measure of the number of possibilities of the current
	// solution of Cases 10 and 1; sumMax0: sum of all Mp (informational only).
	static private int x0_[][], x1_[][], maximum0[][], maximum1[][], ns0[], ns1[], arranjo2[], d, xx[][], sumMax[][],
			freedom_degrees, max_, count2, sumMax0;
	static private String days_week[];
	// Constructor: allocates the arrays and the BitSets used by the module.
	public Module_2A() {
		super();
		days_week = new String[] {"", "", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", ""};
		// Dimensions: one index per Teacher (from 1 to Qp) and one column per day of the part of the week considered.
		sumMax = new int[Qp + 1][3];
		xx = new int[Qp + 1][3];
		arranjo2 = new int[3];
		ns0 = new int[Qp + 1];
		ns1 = new int[Qp + 1];
		x0_ = new int[Qp + 1][2];
		x1_ = new int[Qp + 1][3];
		maximum0 = new int[Qp + 1][2];
		maximum1 = new int[Qp + 1][3];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		maxDays0 = new int[Qp + 1];
		maxDays1 = new int[Qp + 1];
		complex_p0 = new boolean[Qp + 1];
		complex_p1 = new boolean[Qp + 1];
		complex_p2 = new boolean[Qp + 1];
		// The BitSets have room for the limit of stored possibilities (see detPossib_ and detPossib_2).
		bitSet_0 = new BitSet(1028 * Qp * 2);
		bitSet_1 = new BitSet(20000 * Qp * 3);
		bitSet_2 = new BitSet(5000 * Qp * 2);
		nPossib = new int[2];
	}

	// Helper method: asks the user for an integer through a daylog box and returns it.
	private int readInt(String s) {
		String input;
		input = (String) JOptionPane.showInputDialog(null, s, "Input", JOptionPane.QUESTION_MESSAGE, icone, null,
				null);
		return Integer.parseInt(input);
	}

	// Sorts (by selection, in increasing order of amplitude) the elements with index first to nElementos-1 of the array amplitude,
	// permuting in parallel the array d2_ (which holds the identity of the day or Teacher corresponding to each element).
	static public void sort2(int first, int nElementos) {
		int i, j, t0;
		float t;
		for (i = first; i < nElementos - 1; i++) {
			j = indexSmaller2(i, nElementos);
			t = amplitude[i];
			amplitude[i] = amplitude[j];
			amplitude[j] = t;
			t0 = d2_[i];
			d2_[i] = d2_[j];
			d2_[j] = t0;
		}
	}

	// Returns the index of the smallest amplitude value in the range i..nElementos-1.
	static private int indexSmaller2(int i, int nElementos) {
		int m = i, j;
		float smaller;
		smaller = amplitude[m];
		for (j = i + 1; j <= nElementos - 1; j++)
			if (amplitude[j] < smaller) {
				m = j;
				smaller = amplitude[m];
			}
		return m;
	}

	// Prepares the run of Module 2A (commands of Phase 1.1, Section 5.3.1) and returns the number of units n2.
	// Steps: (1) orders the days of the week; (2) classifies the Teachers (complex_p0, complex_p1, complex_p2) and computes
	// maximum0/maximum1 and the weekly load ns_p_; (3) orders the Teachers by the number of possible distributions (Module_A);
	// (4) builds the units (arrays case_, Ap, Aa, A, j_); (5) initializes the pointers of the control variables and the array z.
	public int preliminaries() {
		int aux0, aux1, p1, d1, d;
		float totalPossib_0;
		int arranjo[];
		// The run starts in step 1 (freedom_degrees = 1000), whose purpose is to discover the value max_ (Section 5.2.3).
		freedom_degrees = 1000;
		max_ = -1;
		arranjo = new int[5];
		// The arrays Sp2_, Sp3_ and pointerP of PhaseA1_Super become the same arrays as Sp0_, Sp1_ and pointerP2 (memory reuse).
		PhaseA1_Super.Sp2_ = Probl.Sp0_;
		PhaseA1_Super.Sp3_ = Probl.Sp1_;
		PhaseA1_Super.pointerP = Probl.pointerP2;
		// Row 0 of sumMax is the starting point of the running sums in detPossib_.
		for (k = 0; k <= 2; k++)
			sumMax[0][k] = 0;
		// Notice to the user: the preparation may take a while.
		JOptionPane.showMessageDialog(null, "This action may take a few minutes", "Result",
				JOptionPane.INFORMATION_MESSAGE);
		// Ordering of the days: for each day k (0 to 4) the Mp[p][k] of all Teachers are summed; amplitude[k] receives the negative of this
		// sum, so that the increasing ordering puts first the days with the largest sum (the 'easiest' days).
		sumMax0 = 0;
		for (k = 0; k <= 4; k++) {
			aux0 = 0;
			for (p1 = 1; p1 <= Qp; p1++) {
				aux0 += Mp[p1][k];
			}
			sumMax0 += aux0;
			d2_[k] = k;
			amplitude[k] = -aux0;
			System.out.println("Sum of MaxP[*][] for " + (k + 2) + "a-feira: " + aux0);
		}
		// Orders the 5 days: d2_[0..4] holds the original days from easiest to hardest.
		sort2(0, 5);
		// Swaps positions 0 and 2 of d2_: the easiest day comes to occupy position 2 (Wednesday), that is, it supports the 2nd part of the
		// week (d2, d3 and d4), in which the two hardest days stay at positions 3 and 4 (Section 5.4.3, item c).
		aux0 = d2_[0];
		d2_[0] = d2_[2];
		d2_[2] = aux0;
		// destino0[d] = new position of the original day d.
		for (d1 = 0; d1 <= 4; d1++) {
			k = 0;
			while (d2_[k] != d1)
				k++;
			destino0[d1] = k;
		}
		// Permutes, for each Teacher, the values of Mp[p][*] according to the new order of the days. The permutation is undone later
		// (in the executable class), after the distribution is obtained.
		for (p1 = 1; p1 <= Qp; p1++) {
			for (d1 = 0; d1 <= 4; d1++)
				aux_[d1] = Mp[p1][d1];
			for (d1 = 0; d1 <= 4; d1++)
				Mp[p1][destino0[d1]] = aux_[d1];
		}
		// Shows the sums of Mp per day after the permutation.
		System.out.println("Sum of MaxP after the permutations among the days:");
		for (k = 0; k <= 4; k++) {
			aux0 = 0;
			for (p1 = 1; p1 <= Qp; p1++)
				aux0 += Mp[p1][k];

			System.out.println("Sum of MaxP[*][] for " + days_week[k+2] + " " + aux0);
		}

		// Classification of the Teachers (Section 5.2.1). complex_p0 and complex_p2 compare only two days; complex_p1 compares the three days of the 2nd part.
		for (p = 1; p <= Qp; p++) {
			complex_p0[p] = Mp[p][0] > 0 && Mp[p][1] > 0 && Mp[p][0] != Mp[p][1];
			complex_p2[p] = Mp[p][2] > 0 && Mp[p][3] > 0 && Mp[p][2] != Mp[p][3];
			// complex_p1: true if, among Mp[p][2], Mp[p][3] and Mp[p][4], there are two distinct positive values.
			complex_p1[p] = false;
			w = Mp[p][2];
			aux = Mp[p][3];
			if (w != 0 && aux != 0 && w != aux)
				complex_p1[p] = true;
			else {
				w = Math.max(w, aux);
				aux = Mp[p][4];
				if (w != 0 && aux != 0 && w != aux)
					complex_p1[p] = true;
			}
			// maximum0[p] holds (smaller, larger) of Mp[p][0] and Mp[p][1]; maximum1[p] holds Mp[p][2], Mp[p][3] and Mp[p][4] in increasing order.
			maximum0[p][0] = Math.min(Mp[p][0], Mp[p][1]);
			maximum0[p][1] = Math.max(Mp[p][0], Mp[p][1]);
			for (d = 2; d <= 4; d++)
				arranjo2[d - 2] = Mp[p][d];
			Arrays.sort(arranjo2);
			for (k = 0; k <= 2; k++)
				maximum1[p][k] = arranjo2[k];

			// ns_p_[p] = weekly workload of p (sum of the lessons in all their classes).
			ns_p_[p] = 0;
			for (j = 1; j <= nClassesP[p]; j++)
				ns_p_[p] += ns[p][j];
		}
		// Ordering of the Teachers: for each Teacher the class Module_A determines the number of possible distributions of their lessons
		// between the two pairs of days; the Teachers with fewer possibilities go to the beginning (Section 5.4.3, item a).
		totalPossib_0 = 0;
		for (p = 1; p <= Qp; p++) {
			Module_A.preliminaries(p);
			aux = Module_A.main_0();
			amplitude[p] = (float) aux;
			totalPossib_0 += aux;
			System.out.println("Number of distributions for p = " + p + ": " + aux);
		}
		System.out.println("Total of possib. for all p (Phase 1 only) = " + totalPossib_0);
		// d2_[1..Qp] now holds the Teachers in increasing order of number of distributions.
		for (p = 1; p <= Qp; p++)
			d2_[p] = p;
		System.out.println("Ordering of the Teachers in d2_:");
		sort2(1, Qp + 1);
		for (p = 1; p <= Qp; p++)
			System.out.print(d2_[p] + " ");
		System.out.println();

		// Assembly of the units (Section 5.3.1). Unit 1 is of Case 6 (adjusts freedom_degrees).
		k = 1;
		case_[k] = 6;
		// For each Teacher (in the established order): a Case 10 unit (only if complex_p0 or complex_p1) followed by a
		// Case 1 unit; A[k] holds the Teacher's weekly workload.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (complex_p0[p] || complex_p1[p]) {
				k++;
				Ap[k] = p;
				A[k] = ns_p_[p];
				case_[k] = 10;
			}
			k++;
			Ap[k] = p;
			A[k] = ns_p_[p];
			case_[k] = 1;
		}
		// Case 11 unit: validation of the Case 10 and 1 units.
		k++;
		case_[k] = 11;
		// Case 2 units: one for each (Teacher p, class list[p][j]) pair; A[k] = ns[p][j] lessons to be split; j_[k] = position j of the class.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			for (j = 1; j <= nClassesP[p]; j++) {
				k++;
				Ap[k] = p;
				Aa[k] = list[p][j];
				j_[k] = j;
				A[k] = ns[p][j];
				case_[k] = 2;
			}
		}
		// n2 = total number of units.
		n2 = k;

		// Sentinel: marks the end of the array Ap (used to detect the change of Teacher).
		Ap[n2 + 1] = 0;
		// Pointers of the control variables Sp0_ and Sp1_ (accumulated lessons of the Teacher in the two parts of the week): each Case 2
		// unit gets the position pointerP2[j]; when the Teacher changes, a position with value 0 is reserved to restart the sums.
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
		// Control variables Sa_0 and Sa_1 (accumulated lessons of the class in the two parts): each class a has 16 consecutive
		// positions starting at auxA[a]; the position before the first one holds the initial value 0.
		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_0[auxA[j] - 1] = 0;
			Sa_1[auxA[j] - 1] = 0;
		}
		// pointerA[j] = position of Sa_0 and Sa_1 corresponding to Case 2 unit j (each new unit of the same class uses the next position).
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				pointerA[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		// Prepares the array z, which indicates to which unit the search must backtrack (Chapter 3).
		determineZ(n2);
		return n2;
	}

	// 'Successor' method of Module 2A (Sections 5.2.2 and 5.2.3): for unit i looks for the next value x0 > x_[i] that meets the
	// constraints of the case of case_[i]. Returns false if it found one (value in x0) and true if there is no successor.
	public boolean maximum() {
		int i0, sumA0, sumA1, minX0, aux, sum_, maxX0_;
		boolean test, initialized;
		// p is the Teacher of the unit; x0 starts from the current value of the decision variable (-1 = unit not yet tried).
		p = Ap[i];
		x0 = x_[i];

		switch (case_[i]) {
		// Case 6: control unit for the maximization of possibilities (Section 5.2.3). On the 1st visit (x_[i] == -1) it merely
		// advances. When the search returns to this unit: if freedom_degrees < 1000 (2nd step), it reduces it by 1 (fails if it has already
		// reached 0); if it is still 1000 (end of the 1st step), it moves to the 2nd step with freedom_degrees = max_ (fails if max_ is still -1).
		case 6: {
			x0 = 0;
			test = false;
			if (x_[i] > -1) {
				if (freedom_degrees < 1000) {
					if (freedom_degrees == 0)
						test = true;
					else
						freedom_degrees--;
				} else {
					freedom_degrees = max_;
					System.out.println("freedom_degrees = " + freedom_degrees);
					if (max_ == -1)
						test = true;
				}
			}
			return test;
		}

		// Case 10: x0 is the workload of p in the 1st part of the week (ns0). Bounds: at least what does not fit in the 2nd part
		// (ns_p_ minus the sum of Mp on days d2, d3 and d4) and at most what fits in the 1st part (Mp[p][0] + Mp[p][1]).
		case 10: {
			if (x0 > -1) {
				x0++;
				return x0 > maxX0[i];
			}
			minX0 = Math.max(0, ns_p_[p] - Mp[p][2] - Mp[p][3] - Mp[p][4]);
			maxX0[i] = Math.min(ns_p_[p], Mp[p][0] + Mp[p][1]);
			x0 = Math.max(minX0, x0 + 1);
			return x0 > maxX0[i];
		}

		// Case 1: x0 is the number of working days of p in the 1st part of the week (maxDays0). aux2 = number of days of the 2nd part on which p is available.
		case 1: {
			aux2 = (Mp[p][2] > 0 ? 1 : 0) + (Mp[p][3] > 0 ? 1 : 0) + (Mp[p][4] > 0 ? 1 : 0);
			while (true) {
				x0++;
				// aux = days that would remain for the 2nd part (Wp0[p] - x0).
				aux = Wp0[p] - x0;
				// No value greater than x0 will do if x0 exceeds 2, Wp0[p] or the number of available days of p in the 1st part.
				if (x0 > 2 || x0 > Wp0[p])
					return true;
				if (x0 > (Mp[p][0] > 0 ? 1 : 0) + (Mp[p][1] > 0 ? 1 : 0))
					return true;
				// In the 2nd part, p cannot work more days than the available ones.
				if (aux > aux2)
					aux = aux2;
				// Checks whether the workload fits. For complex_p0/complex_p1 Teachers, ns0 and ns1 were already fixed by Case 10: the sum of the
				// x0 largest Mp of the 1st part must reach ns0[p] and that of the aux largest Mp of the 2nd part must reach ns1[p].
				if (complex_p0[p] || complex_p1[p]) {
					sum_ = 0;
					for (k = 0; k < x0; k++)
						sum_ += maximum0[p][1 - k];
					test = sum_ >= ns0[p];
					if (test) {
						sum_ = 0;
						for (k = 0; k < aux; k++)
							sum_ += maximum1[p][2 - k];
						test = sum_ >= ns1[p];
					}
				// For the other Teachers, it is enough that the maximum capacity in the two parts covers the weekly workload ns_p_[p]. If the test is
				// satisfied, x0 is the successor.
				} else
					test = x0 * maximum0[p][1] + aux * maximum1[p][2] >= ns_p_[p];
				if (test)
					return false;
			}
		}

		// Case 110: variant of Case 11 (validates detPossib_ before computing the bounds). No unit with this case is
		// created by preliminaries(), so this block is not executed.
		case 110: {
			if (x0 > -1)
				return true;
			count2 = 0;
			for (p = 1; p <= Qp; p++) {
				if (maxDays0[p] == 1 && Mp[p][0] > 0 && Mp[p][1] > 0 && !complex_p0[p])
					count2 += 1;
				if (complex_p1[p])
					continue;
				aux = 0;
				for (k = 2; k <= 4; k++) {
					if (Mp[p][k] > 0)
						aux++;
				}
				k = Wp0[p] - maxDays0[p];
				if (aux == 3 && (k == 1 || k == 2))
					count2 += 3;
				if (aux == 2 && k == 1)
					count2 += 2;
			}
			if (freedom_degrees < 1000 && count2 != freedom_degrees)
				return true;

			test = detPossib_(0, true);
			if (!test)
				return true;
			for (p = 1; p <= Qp; p++) {
				if (complex_p0[p])
					Np0[p] = x0_[p][0] * Mp[p][0] + x0_[p][1] * Mp[p][1];
				else
					Np0[p] = maximum0[p][1] * maxDays0[p];
			}
			test = detPossib_(1, true);
			if (!test)
				return true;
			for (p = 1; p <= Qp; p++) {
				if (complex_p1[p])
					Np1[p] = x1_[p][0] * Mp[p][2] + x1_[p][1] * Mp[p][3] + x1_[p][2] * Mp[p][4];
				else
					Np1[p] = maximum1[p][2] * maxDays1[p];
			}

			sumA0 = 0;
			sumA1 = 0;
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				aux0 = (noDoublePeriodsP[p] ? maxDays0[p] : 2 * maxDays0[p]);
				aux1 = (noDoublePeriodsP[p] ? maxDays1[p] : 2 * maxDays1[p]);
				aux0 = Math.min(aux0, Np0[p]);
				aux1 = Math.min(aux1, Np1[p]);
				aux2 = nClassesP[p];
				sumA0 += Np0[p];
				sumA1 += Np1[p];
				for (j = 1; j <= aux2; j++) {
					a = list[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
					sumA_1[a] += Math.min(aux1, ns[p][j]);
				}
			}
			test = sumA0 >= 2 * Ma * Qa && sumA1 >= 3 * Ma * Qa;
			if (test) {
				for (a = 1; a <= Qa; a++) {
					test = sumA_0[a] >= 2 * Ma && sumA_1[a] >= 3 * Ma;
					if (!test)
						break;
				}
			}
			x0 = 0;
			if (freedom_degrees < 1000)
				return !test;
			if (test)
				max_ = Math.max(max_, count2);
			return true;
		}

		// Case 11: validation of the decisions of Cases 10 and 1 (Section 5.2.2). It only takes effect going forward; when returning to this unit
		// (x0 > -1) the backtracking merely continues.
		case 11: {
			if (x0 > -1)
				return true;
			// count2 is a measure of the number of possibilities that the current solution of Cases 10 and 1 allows (Section 5.2.3). For each Teacher
			// p, it adds: 1, if p works on only one of the two days of the 1st part, both available, and is not complex_p0; and, if p is not complex_p1,
			// 3, if p has all three days of the 2nd part available and works on 1 or 2 of them, or 2, if p has two days available and works on 1.
			count2 = 0;
			for (p = 1; p <= Qp; p++) {
				if (maxDays0[p] == 1 && Mp[p][0] > 0 && Mp[p][1] > 0 && !complex_p0[p])
					count2 += 1;
				if (complex_p1[p])
					continue;
				aux = 0;
				for (k = 2; k <= 4; k++) {
					if (Mp[p][k] > 0)
						aux++;
				}
				k = Wp0[p] - maxDays0[p];
				if (aux == 3 && (k == 1 || k == 2))
					count2 += 3;
				if (aux == 2 && k == 1)
					count2 += 2;
			}
			// In the 2nd step (freedom_degrees < 1000) only the solution whose count2 equals freedom_degrees is accepted.
			if (freedom_degrees < 1000 && count2 != freedom_degrees)
				return true;

			// Provisional bounds on lessons in the two parts: for complex Teachers, the workload ns0/ns1; for the others,
			// the maximum of one day times the number of working days.
			for (p = 1; p <= Qp; p++) {
				if (complex_p0[p])
					Np0[p] = ns0[p];
				else
					Np0[p] = maximum0[p][1] * maxDays0[p];
				if (complex_p1[p])
					Np1[p] = ns1[p];
				else
					Np1[p] = maximum1[p][2] * maxDays1[p];
			}

			// Verification of the demands: sumA0 and sumA1 sum the bounds Np0 and Np1 of all Teachers; sumA_0[a] and sumA_1[a] sum
			// the countribution of each Teacher to class a (limited by the class's maximum lessons per day and by ns[p][j]).
			sumA0 = 0;
			sumA1 = 0;
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				aux0 = (noDoublePeriodsP[p] ? maxDays0[p] : 2 * maxDays0[p]);
				aux1 = (noDoublePeriodsP[p] ? maxDays1[p] : 2 * maxDays1[p]);
				aux0 = Math.min(aux0, Np0[p]);
				aux1 = Math.min(aux1, Np1[p]);
				aux2 = nClassesP[p];
				sumA0 += Np0[p];
				sumA1 += Np1[p];
				for (j = 1; j <= aux2; j++) {
					a = list[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
					sumA_1[a] += Math.min(aux1, ns[p][j]);
				}
			}
			// The total demands (2*Ma*Qa in the 1st part and 3*Ma*Qa in the 2nd) and those of each class (2*Ma and 3*Ma) must be met.
			test = sumA0 >= 2 * Ma * Qa && sumA1 >= 3 * Ma * Qa;
			if (test) {
				for (a = 1; a <= Qa; a++) {
					test = sumA_0[a] >= 2 * Ma && sumA_1[a] >= 3 * Ma;
					if (!test)
						break;
				}
			}
			// Looks for (detPossib_ with start true) a way to choose the working days of all Teachers; those found
			// are stored in x0_ (1st part) and x1_ (2nd part).
			test = test && detPossib_(0, true);
			if (!test)
				return true;
			test = detPossib_(1, true);
			if (!test)
				return true;
			// With the working days defined, Np0 and Np1 of the complex Teachers are recomputed with the maxima of the chosen days.
			for (p = 1; p <= Qp; p++) {
				if (complex_p0[p])
					Np0[p] = x0_[p][0] * Mp[p][0] + x0_[p][1] * Mp[p][1];
				if (complex_p1[p])
					Np1[p] = x1_[p][0] * Mp[p][2] + x1_[p][1] * Mp[p][3] + x1_[p][2] * Mp[p][4];
			}
			// Value of the decision variable of this unit (meaningless in itself).
			x0 = 0;
			// 2nd step: the unit continues if the validation was positive. 1st step: it only records in max_ the largest count2 that passed the
			// validation and returns true, so that the search proceeds by enumerating all the solutions of Cases 10 and 1.
			if (freedom_degrees < 1000)
				return !test;
			if (test)
				max_ = Math.max(max_, count2);
			return true;
		}

		// Case 2: x0 is the number of lessons of the pair (p, class Aa[i]) in the 1st part of the week; A[i] - x0 is the number in the 2nd part.
		// On the first visit the bounds minX0 and maxX0[i] are computed; on the following ones x0 is merely incremented.
		case 2: {
			// There is no successor if x0 has already reached the maximum possible lessons in the 1st part (1 or 2 per working day).
			if (x0 == 2 * maxDays0[p] || (noDoublePeriodsP[p] && x0 == maxDays0[p]))
				return true;
			// Trivial case: one of the bounds Np0[p] or Np1[p] is zero, and all the lessons stay in the other part (single value).
			case_Trivial = Np0[p] == 0 || Np1[p] == 0;
			n0 = A[i];
			initialized = x0 > -1;
			if (case_Trivial) {
				if (x0 > -1)
					return true;
				x0 = (Np0[p] == 0 ? 0 : n0);
			} else
				x0++;
			if (initialized)
				return x0 > maxX0[i];
			// Sp0, Sp2: lessons already assigned to the Teacher (up to the previous unit) in the 1st and 2nd parts; Sa0, Sa2: the same for class a.
			a = Aa[i];
			Sp0 = Sp0_[pointerP2[i] - 1];
			Sp2 = Sp1_[pointerP2[i] - 1];
			Sa0 = Sa_0[pointerA[i] - 1];
			Sa2 = Sa_1[pointerA[i] - 1];

			// Lower bound: the 2nd part cannot hold more than (maxDays1 or 2*maxDays1) lessons of the pair, nor more than Np1[p] lessons of the Teacher, nor more than 3*Ma of the class.
			minX0 = (noDoublePeriodsP[p] ? n0 - maxDays1[p] : n0 - 2 * maxDays1[p]);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - 3 * Ma);
			x0 = Math.max(x0, minX0);

			// Upper bound: the 1st part cannot hold more than (maxDays0 or 2*maxDays0) lessons of the pair, nor more than n0, nor more
			// than the remainder of Np0[p] for the Teacher, nor more than the remainder of 2*Ma for the class.
			maxX0_ = (noDoublePeriodsP[p] ? maxDays0[p] : 2 * maxDays0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}
		}
		return true;
	}

	// Updates the control and linking variables after x0 has been accepted for unit i (Section 5.2.2).
	public void update2() {
		p = Ap[i];
		switch (case_[i]) {
		// Case 10: records the Teacher's workloads in the two parts of the week.
		case 10: {
			ns0[p] = x0;
			ns1[p] = ns_p_[p] - x0;
			break;
		}
		// Case 1: defines maxDays0 and maxDays1 (the latter limited to the number of available days in the 2nd part).
		case 1: {
			maxDays0[p] = x0;
			maxDays1[p] = Wp0[p] - x0;
			aux2 = (Mp[p][2] > 0 ? 1 : 0) + (Mp[p][3] > 0 ? 1 : 0) + (Mp[p][4] > 0 ? 1 : 0);
			if (maxDays1[p] > aux2)
				maxDays1[p] = aux2;
			break;
		}
		// Case 2: accumulates the lessons of the Teacher (Sp0_, Sp1_) and of the class (Sa_0, Sa_1) in each part of the week.
		case 2: {
			nLessons = A[i] - x0;
			aux = pointerP2[i];
			Sp0_[aux] = Sp0_[aux - 1] + x0;
			Sp1_[aux] = Sp1_[aux - 1] + nLessons;
			aux = pointerA[i];
			Sa_0[aux] = Sa_0[aux - 1] + x0;
			Sa_1[aux] = Sa_1[aux - 1] + nLessons;
			break;
		}

		}
	}

	// Algorithm 2 of Chapter 3 (search with intelligent backtracking). Returns true if Module 2A (and also 2B) obtains a solution.
	public boolean mainV2() {
		int k, imax, p1, p0, generator;
		boolean test, generator_;
		String input;
		Object[] opcoes = { "Yes", "No" };

		// Initialization: all units receive the 'generator' (generator = -1) and the search starts at i = 1.
		imax = 0;
		generator = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		// Maximum execution time asked of the user (in minutes); checked every 50000 iterations.
		input = (String) JOptionPane.showInputDialog(null, "Enter maximum execution time (min)", "Input",
				JOptionPane.QUESTION_MESSAGE, icone, null, null);
		timeMaximum = Math.round(Float.parseFloat(input));
		nIterations = 0L;
		initialTime = System.currentTimeMillis();
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
			// If maximum() succeeds: stores x0, updates the control variables and advances to the next unit.
			if (!maximum()) {
				x_[i] = x0;
				update2();
				i++;
				if (i > imax) {
					imax = i;
					System.out.println(i + "/" + n2 + " p=" + Ap[i] + " case_ " + case_[i]);
				}
				// All units closed: Module 2A ends. Computes the possibilities (update4), asks the user how the return to
				// Case 2 should be made and calls Module 2B (preliminaries and main_V1).
				if (i > n2) {
					System.out.println();
					System.out.println("sumMax0 = " + sumMax0);
					update4(n2);
					k = JOptionPane.showOptionDialog(null, "Return to case_ 2 by generator?", "",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
					generator_ = k == JOptionPane.YES_OPTION;
					g2_I_W_.preliminaries();
					test = g2_I_W_.main_V1(generator_);
					return test;
				}
				// Updates iMaxI_ (most advanced unit reached after each previous unit), used to determine the backtrack.
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Failure: backtracks to unit p0 = z[i][iMaxI_[i] - i], the smallest one that can still influence the cause of the failure.
			} else {
				if (i == 1)
					return false;
				p0 = z[i][iMaxI_[i] - i];
				if (p0 == 0)
					return false;
				// Resets the abandoned units to the generator.
				for (i = i; i > p0; i--)
					x_[i] = generator;
				i = p0;
			}
		} while (true);
	}

	// Obtains the possibilities: stores in bitSet_0 and bitSet_1 the fixed working days of the complex Teachers and calls
	// detPossib_ (1st part, 2nd part) and detPossib_2 (possibilities of the 2nd part).
	public void update4(int n) {
		int p, k;
		for (p = 1; p <= Qp; p++) {
			if (complex_p0[p]) {
				for (k = 0; k <= 1; k++)
					bitSet_0.set(2 * (p - 1) + k, x0_[p][k] != 0);
			}
			if (complex_p1[p]) {
				for (k = 0; k <= 2; k++)
					bitSet_1.set(3 * (p - 1) + k, x1_[p][k] != 0);
			}
		}

		// Stores in bitSet_0 all the possibilities of the 1st part.
		nPossib[0] = 0;
		detPossib_(0, false);
		// Stores in bitSet_1 all the sub-possibilities of the 2nd part.
		nPossib[1] = 0;
		detPossib_(1, false);
		// Derives from bitSet_1 the possibilities of the 2nd part (bitSet_2).
		detPossib_2();
		System.out.println(" ");
		System.out.println("Number of possibilities: " + nPossib[0] + " " + nPossib_2);
	}

	// Enumerates ways of choosing the working days of all Teachers (Section 5.2.2). exec = 0: 1st part (days 0 and 1);
	// exec = 1: 2nd part (days 2, 3 and 4). xx[p][j] = 1 (p works on the j-th day of the part), 0 or -1 (not yet defined).
	// If start is true, stops at the first complete way found, copying it to x0_ or x1_, and returns true (false if none
	// exists). If start is false, goes through all the ways, storing the valid ones in bitSet_0 or bitSet_1.
	private static boolean detPossib_(int exec, boolean start) {
		int days[], maxCols, i, j, k, sum, p, x0, possib;
		boolean maximum, test;

		if (exec == 0) {
			days = new int[] { 0, 1 };
			maxCols = 1;
		} else {
			days = new int[] { 2, 3, 4 };
			maxCols = 2;
		}

		// Initial state: no day defined; the search goes through the Teachers (i) and the days of the part (j) in order.
		for (p = 1; p <= Qp; p++) {
			for (j = 0; j <= maxCols; j++)
				xx[p][j] = -1;
		}
		i = 1;
		j = 0;
		while (true) {
			// For complex Teachers, with start false, the working days are those already fixed in x0_ or x1_ (single value).
			x0 = xx[i][j];
			p = i;
			if (!start && ((exec == 0 && complex_p0[i]) || (exec == 1 && complex_p1[i]))) {
				maximum = x0 > -1;
				if (!maximum)
					x0 = (exec == 0 ? x0_[p][j] : x1_[p][j]);
			// Other cases: tries x0 = 0 and x0 = 1 (only 0 if p is not available on that day).
			} else {
				if (x0 >= 1 || (x0 >= 0 && Mp[i][days[j]] == 0))
					maximum = true;
				else {
					while (true) {
						x0++;
						if (x0 > 1 || (x0 > 0 && Mp[i][days[j]] == 0)) {
							maximum = true;
							break;
						}
						// Pruning: the sum of the maxima of the Teachers up to i on this day, plus what the remaining Teachers could offer (at most Ma
						// each), must reach the day's demand, Ma*Qa.
						test = sumMax[i - 1][j] + x0 * Mp[i][days[j]] + (Qp - i) * Ma >= Qa * Ma;
						// When the last day of the part is defined, checks the set of days of p: the number of days must be maxDays0[p] (1st part)
						// or at most maxDays1[p] (2nd part), and the capacity must meet the workload.
						if (test && j == maxCols) {
							xx[i][j] = x0;
							sum = 0;
							for (k = 0; k <= maxCols; k++)
								sum += xx[i][k];
							if (exec == 0)
								test = sum == maxDays0[i];
							else
								test = sum <= maxDays1[i];
							if (test) {
								// Complex Teacher: the sum of the Mp of the chosen days must reach the workload ns0 or ns1.
								if (complex_p0[i] || complex_p1[i]) {
									sum = 0;
									for (k = 0; k <= maxCols; k++)
										sum += xx[i][k] * Mp[i][days[k]];
									test = sum >= (exec == 0 ? ns0[i] : ns1[i]);
								// Non-complex Teacher: in the 2nd part, the capacity of the two parts must cover the weekly workload.
								} else {
									if (exec == 1) {
										sum = maxDays0[i] * maximum0[i][1];
										for (k = 0; k <= maxCols; k++)
											sum += xx[i][k] * Mp[i][days[k]];
										test = sum >= ns_p_[i];
									}
								}
							}
						}
						if (test) {
							maximum = false;
							break;
						}
					}
				}
			}
			// Value accepted: records it, accumulates the sum of the day's maxima and advances to the next day or Teacher.
			if (!maximum) {
				xx[i][j] = x0;
				sumMax[i][j] = sumMax[i - 1][j] + x0 * Mp[i][days[j]];
				j++;
				if (j > maxCols) {
					i++;
					j = 0;
					// Went past the last Teacher: complete way of choosing the working days.
					if (i > Qp) {
						// start true: stores the way found in x0_ or x1_ and returns true.
						if (start) {
							for (p = 1; p <= Qp; p++) {
								for (j = 0; j <= maxCols; j++) {
									if (exec == 0)
										x0_[p][j] = xx[p][j];
									else
										x1_[p][j] = xx[p][j];
								}
							}
							return true;
						}

						// start false: stores the way in bitSet_0 (possib * Qp * 2 + ...) or bitSet_1 (possib * Qp * 3 + ...); for
						// complex Teachers copies the fixed bits from the start of the BitSet. It is only counted (nPossib) if it passes validation().
						System.out.print(".");
						possib = nPossib[exec] + 1;
						// Space limit: stops if the maximum number of storable possibilities (1026) or sub-possibilities (19998) was reached.
						if ((exec == 0 && possib == 1026) || (exec == 1 && possib == 19998))
							return true;
						if (exec == 0) {
							for (p = 1; p <= Qp; p++) {
								if (complex_p0[p]) {
									for (k = 0; k <= maxCols; k++)
										bitSet_0.set(possib * Qp * 2 + 2 * (p - 1) + k, bitSet_0.get(2 * (p - 1) + k));
								} else {
									for (k = 0; k <= maxCols; k++)
										bitSet_0.set(possib * Qp * 2 + 2 * (p - 1) + k, xx[p][k] != 0);
								}
							}
						} else {
							for (p = 1; p <= Qp; p++) {
								if (complex_p1[p]) {
									for (k = 0; k <= maxCols; k++)
										bitSet_1.set(possib * Qp * 3 + 3 * (p - 1) + k, bitSet_1.get(3 * (p - 1) + k));
								} else {
									for (k = 0; k <= maxCols; k++)
										bitSet_1.set(possib * Qp * 3 + 3 * (p - 1) + k, xx[p][k] != 0);
								}
							}
						}
						// If it does not pass the validation, the space will be reused by the next way.
						if (validation(exec, possib))
							nPossib[exec]++;
						// Positions the search at the last element to continue looking for other ways (backtracking).
						i = Qp;
						j = maxCols;
					}
				}
			// No successor: resets the position and backtracks. The enumeration ends (returning false) when it backtracks beyond the 1st Teacher.
			} else {
				xx[i][j] = -1;
				j--;
				if (j < 0) {
					j = maxCols;
					i--;
					if (i < 1)
						return false;
				}
			}
		}
	}

	// Validates a possibility (exec = 0) or sub-possibility (exec = 1) stored at possib: (1) on each day, the sum of the
	// Mp of the working Teachers must reach Ma*Qa; (2) for each class, on each day, the working Teachers must be able to
	// offer at least Ma lessons (each Teacher offers at most 1 lesson per day in the class if noDoublePeriodsP, otherwise 2; and at most
	// Mp[p][day] and ns[p][j]).
	private static boolean validation(int exec, int possib) {
		int k, sumA0, aux0;
		boolean x0;
		if (exec == 0)
			days = new int[] { 0, 1 };
		else
			days = new int[] { 2, 3, 4 };
		for (k = 0; k < days.length; k++) {
			sumA0 = 0;
			for (p = 1; p <= Qp; p++) {
				if (exec == 0)
					x0 = bitSet_0.get(possib * Qp * 2 + 2 * (p - 1) + k);
				else
					x0 = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1) + k);
				if (x0)
					sumA0 += Mp[p][days[k]];
			}
			if (sumA0 < Ma * Qa)
				return false;
		}
		for (k = 0; k < days.length; k++) {
			for (a = 1; a <= Qa; a++)
				sumA_0[a] = 0;
			for (p = 1; p <= Qp; p++) {
				if (exec == 0)
					x0 = bitSet_0.get(possib * Qp * 2 + 2 * (p - 1) + k);
				else
					x0 = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1) + k);
				if (!x0)
					continue;
				aux0 = (noDoublePeriodsP[p] ? 1 : 2);
				aux0 = Math.min(aux0, Mp[p][days[k]]);
				aux2 = nClassesP[p];
				for (j = 1; j <= aux2; j++) {
					a = list[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
				}
			}
			for (a = 1; a <= Qa; a++) {
				if (sumA_0[a] < Ma)
					return false;
			}
		}
		return true;
	}

	// Obtains the possibilities of the 2nd part (bitSet_2) from the sub-possibilities (bitSet_1): for each Teacher counts how many
	// of the days d2 and d3 are working days (0, 1 or 2, in 2 bits) and discards the repeated ones.
	private static void detPossib_2() {
		int possib, k, p, m;
		boolean test, test2;

		nPossib_2 = 0;
		// For each sub-possibility, builds the candidate possibility at position k.
		for (possib = 1; possib <= nPossib[1]; possib++) {
			k = nPossib_2 + 1;
			for (p = 1; p <= Qp; p++) {
				test = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1));
				test2 = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1) + 1);
				if (test && test2) {
					bitSet_2.set(k * Qp * 2 + 2 * (p - 1), true);
					bitSet_2.set(k * Qp * 2 + 2 * (p - 1) + 1, false);
				} else {
					if (test != test2) {
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1), false);
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1) + 1, true);
					} else {
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1), false);
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1) + 1, false);
					}
				}
			}
			// The first is always accepted.
			if (possib == 1) {
				nPossib_2++;
				continue;
			}
			// Compares with those already stored (positions 1 to k-1); if equal to one of them, it is not counted (position k will be reused).
			test = true;
			for (m = 1; m < k; m++) {
				test2 = true;
				for (p = 1; p <= Qp; p++) {
					if (bitSet_2.get(m * Qp * 2 + 2 * (p - 1)) != bitSet_2.get(k * Qp * 2 + 2 * (p - 1)) || bitSet_2
							.get(m * Qp * 2 + 2 * (p - 1) + 1) != bitSet_2.get(k * Qp * 2 + 2 * (p - 1) + 1)) {
						test2 = false;
						break;
					}
				}
				if (test2) {
					test = false;
					break;
				}
			}
			// New possibility: counted (limit of 4998 stored).
			if (test) {
				nPossib_2++;
				if (nPossib_2 == 4998)
					return;
			}
		}
	}

	// Dependency: not used by the efficient version (determineZ).
	public boolean dependency0(int i, int j) {
		return true;
	}

	// Method 'dependency' (Chapter 3): indicates whether unit j depends on unit i (i < j). The units of Cases 1, 10 and 11
	// depend on all previous ones; in Case 2 the dependency is on the Case 1 of the same Teacher and on Case 2 units of the same Teacher or the same class.
	public boolean dependency(int i, int j) {
		if (j <= i)
			return false;
		if (case_[j] == 1 || case_[j] == 10)
			return true;
		// Note: the following two conditions are never reached, since the previous test already returns true for Cases 1 and 10.
		if (case_[j] == 10)
			return case_[i] == 6;
		if (case_[j] == 1)
			return case_[i] == 6 || (case_[i] == 10 && Ap[i] == Ap[j]);
		if (case_[j] == 11)
			return true;

		if (case_[j] == 2) {
			if (case_[i] == 1)
				return Ap[i] == Ap[j];
			if (case_[i] == 2)
				return Ap[i] == Ap[j] || Aa[i] == Aa[j];
			return false;
		}

		return true;
	}

	// determineArranjo of Chapter 3: fills z[i][l] with the largest p < i on which unit i + l depends; then accumulates the
	// maximum along l, so that z[i][q - i] indicates where to backtrack.
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
}
