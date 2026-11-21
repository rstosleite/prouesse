package prouesse;

import java.awt.datatransfer.SystemFlavorMap;
import java.util.Arrays;

import javax.swing.JOptionPane;

// Class Module_2B - implements Module 2B (Phase 1.2 of Algorithm P2; Sections 5.2.4, 5.3.2, 5.4 and 5.4.3 of the book).
//
// Role: with the results of Module 2A (maxDays0, maxDays1, working days of the complex Teachers, possibilities in
// bitSet_0 and bitSet_2), distributes the lessons of each (Teacher p, class a) pair between the two parts of the week: x[k] lessons in the
// 1st part (days d0 and d1) and A[k] - x[k] in the 2nd part (days d2, d3 and d4). There are only two types of unit:
//   - Case 2: one unit per (Teacher, class) pair; the decision variable is x[k] (see maximum_2);
//   - Case 10: one unit per Teacher, after the Teacher's last Case 2 unit; it is a validation unit (maximum_10) that
//     compares what has already been decided in the Case 2 units with the possibilities and sub-possibilities from Module 2A.
// There is also a final unit, of Case 5, which calls Modules 3B and 3A (case_50) to finalize the distribution among the
// five days; if they fail, the search returns to a Case 2 unit (main_V1).
public class Module_2B extends Probl {

	// Np0[p] and Np1[p]: maximum lessons of p in the 1st and 2nd parts; wp0[p]: number of working days of p on d2 and d3 (in the
	// possibility of the 2nd part under test); x_case__3[p]: 1 if the first day of the part of the week being validated is a working day
	// of p (according to the possibility or sub-possibility under test), 0 if not; ns_p0[p] and ns_p1[p]: workload of p in the 1st and
	// 2nd parts, known after the Case 2 units of p have been executed; sumP0 and sumP1: accumulated sums
	// of the Teachers' minimum lessons on d2 and d3; Ep[p]: minimum lessons of p on a day of the 2nd part; ns2[p][j]: best upper bound
	// available for the lessons of the pair (p, j-th class) in the part of the week being validated (the variable ns2 of the book,
	// Section 5.4); sumMax: sum of the maximum lessons of the working days of p in the 2nd part; case_10: index of the first
	// Case 10 unit from which the test of the sums of the minimum lessons per day applies (Section 5.4.2);
	// x2 and Ap2: arrays of the auxiliary search over the Teachers of maximum_10.
	static public int Np0[], Np1[], attemptCount, successCount2, wp0[], x_case__3[], ns_p0[], ns_p1[], sumP0[],
			sumP1[], Ep[], ns2[][], sumMax[], case_10, x2[], Ap2[];
	// complex_p0 and complex_p1: indicators defined in Module 2A (Section 5.2.1); workDaysP[p][d]: p works on day d
	// (three-day validation); firstPossibility: true while the first possibility is being testd (for non-complex
	// Teachers the result of the test does not depend on the possibility, so it is testd only once); test3: becomes false when a
	// Teacher's demand is not met by a possibility, which makes testing the others useless.
	static public boolean complex_p0[], complex_p1[], workDaysP[][], firstPossibility, test3;

	// Constructor: allocates the arrays used by the module.
	public Module_2B() {
		Dp = new int[Qp + 1];
		sumMax = new int[Qp + 1];
		workDaysP = new boolean[Qp + 1][5];
		arranjo = new int[3];
		wp0 = new int[Qp + 1];
		x_case__3 = new int[Qp + 1];
		ns_p0 = new int[Qp + 1];
		ns_p1 = new int[Qp + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		sumA_1_ = new int[Qa + 1];
		sumP0 = new int[Qp + 1];
		sumP1 = new int[Qp + 1];
		x2 = new int[Qp + 3];
		Ap2 = new int[Qp + 3];
		Ep = new int[Qp + 1];
		ns2 = new int[Qp + 1][];
		for (p = 1; p <= Qp; p++)
			ns2[p] = new int[nClassesP[p] + 1];
	}

	// Prepares the run of Module 2B (Section 5.3.2): obtains the data from Module 2A, computes Np0 and Np1, builds the units
	// (Case 2 and Case 10 per Teacher, and a final Case 5 unit) and the control variables. Returns the number of units (n2 + 1).
	public int preliminaries() {
		int aux;
		sumP0[0] = 0;
		sumP1[0] = 0;
		// Objects of Modules 3B and 3A that will be run after Case 2.
		g2_0 = new Module_3B();
		g2_ = new Module_3A();
		// Arrays maxDays0 and maxDays1 determined by Module 2A.
		maxDays0 = Module_2A.maxDays0;
		maxDays1 = Module_2A.maxDays1;
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		complex_p0 = Module_2A.complex_p0;
		complex_p1 = Module_2A.complex_p1;
		// Np0[p] and Np1[p] (Section 5.2.1): for complex Teachers, sum of the Mp of the working days fixed in bitSet_0 and bitSet_1;
		// for the others, number of working days times the largest Mp of the part of the week.
		for (p = 1; p <= Qp; p++) {
			if (complex_p0[p]) {
				Np0[p] = 0;
				for (k = 0; k <= 1; k++) {
					if (Module_2A.bitSet_0.get(2 * (p - 1) + k))
						Np0[p] += Mp[p][k];
				}
			} else
				Np0[p] = maxDays0[p] * Math.max(Mp[p][0], Mp[p][1]);
			if (complex_p1[p]) {
				Np1[p] = 0;
				for (k = 0; k <= 2; k++) {
					if (Module_2A.bitSet_1.get(3 * (p - 1) + k))
						Np1[p] += Mp[p][k + 2];
				}
			} else {
				aux = Math.max(Mp[p][2], Mp[p][3]);
				aux = Math.max(Mp[p][4], aux);
				Np1[p] = maxDays1[p] * aux;
			}
			System.out.println(p + " " + Np0[p] + " " + Np1[p] + " " + maxDays0[p] + " " + maxDays1[p]);
		}
		// Assembly of the units, in the order of the Teachers established by Module 2A (d2_): first the Case 2 units of each
		// class of the Teacher and then the Teacher's Case 10 unit (Section 5.3.2).
		k = 0;
		case_10 = 10000;
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
			k++;
			Ap[k] = p;
			case_[k] = 10;
			// Stores the index of the Case 10 unit of the (Qa + 1)-th Teacher; until then the test of the sums of the minimum lessons per day is not applied.
			if (i1 == Qa + 1)
				case_10 = k;
		}
		// n2 is the number of Case 2 and Case 10 units; the next unit is the Case 5 one (call to Modules 3B and 3A).
		n2 = k;
		case_[n2 + 1] = 5;
		Ap[n2 + 1] = 0;

		// Control variables: pointers and accumulators of the lessons of the Teacher and of the class in the two parts of the week (as in Module 2A).
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		// pointerP2[j]: position in Sp0_ and Sp1_ of Case 2 unit j; at the end of a Teacher's units a zeroed position is reserved.
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

		// Sa_0 and Sa_1: accumulated lessons per class; each class has 16 consecutive positions, starting at auxA[a].
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
		return n2 + 1;
	}

	// Two-day validation (Sections 5.4, 5.4.1 and 5.4.2), for the possibility of the 1st part whose working days are in
	// x_case__3 (day d0) and maxDays0 - x_case__3 (day d1). Considers the units up to iFinal. Returns true if the Teachers'
	// demands are met. May set test3 to false, indicating that no other possibility would do.
	private boolean two_days_validation(int iFinal) {
		int k, sumP_0, sumP_1, i, p, aux3, aux4, d_0, d_1, sum, d;
		boolean test;

		// Test of the sums of the minima (Section 5.4.2): on each day, the minimum lessons of each Teacher is the load in the 1st part minus what
		// the Teacher can teach on the other day; the sum of these minima cannot exceed Ma*Qa, the total number of lessons of the day.
		if (iFinal >= case_10) {
			for (d = 0; d <= 1; d++) {
				sum = 0;
				k = 0;
				while (true) {
					k++;
					p = d2_[k];
					if (d == 0)
						aux = ns_p0[p] - (maxDays0[p] - x_case__3[p] > 0 ? Mp[p][1] : 0);
					else
						aux = ns_p0[p] - (x_case__3[p] > 0 ? Mp[p][0] : 0);
					if (aux > 0)
						sum += aux;
					if (p == Ap[iFinal])
						break;
				}
				if (sum > Ma * Qa)
					return false;
			}
		}
		// Teachers' demand (Section 5.4.1): for each Teacher (going through the Case 2 and Case 10 units), computes how many
		// lessons they can teach on d0 and on d1 and compares with the load ns_p0[p].
		d_0 = 0;
		d_1 = 1;
		i = 1;
		test = false;
		while (true) {
			p = Ap[i];
			// Teachers with no working day in the 1st part, or non-complex ones (after the first possibility), are skipped: the result would always be the same.
			if (maxDays0[p] == 0 || (!complex_p0[p] && !firstPossibility)) {
				do
					i++;
				while (case_[i] == 2);
				test = true;
				i++;
				if (i > iFinal)
					break;
				continue;
			}
			// aux0 and aux1: working days on d0 and d1 (0 or 1); aux3 and aux4: maximum lesson capacity of the Teacher on d0 and d1;
			// if double lessons are allowed, the Teacher can have 2 lessons per day in each class.
			sumP_0 = 0;
			sumP_1 = 0;
			aux0 = x_case__3[p];
			aux1 = maxDays0[p] - aux0;
			aux3 = aux0 * Mp[p][d_0];
			aux4 = aux1 * Mp[p][d_1];
			if (!noDoublePeriodsP[p]) {
				aux0 *= 2;
				aux1 *= 2;
			}
			// For each class of the Teacher, sums what it can countribute on d0 and on d1 (at most the lessons x_[i] already assigned to the 1st part).
			do {
				k = x_[i];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
				i++;
			} while (case_[i] == 2);
			i++;
			// Lessons the Teacher can teach on the two days: limited by the capacity of each day and by the countributions of the classes.
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			// The Teacher's demand is met if the load ns_p0[p] fits in this capacity.
			test = ns_p0[p] <= aux0;
			// A failure of a non-complex Teacher or in the first possibility invalidates all the possibilities.
			if (!test)
				test3 = false;
			if (!test || i > iFinal)
				break;
		}
		firstPossibility = false;
		return test;
	}

	// Three-day validation (Sections 5.4, 5.4.1 and 5.4.2), analogous to the two-day one, for the sub-possibility of the 2nd part: x_case__3
	// indicates whether d2 is a working day; wp0 - x_case__3, whether d3 is; maxDays1 - wp0, whether d4 is. Uses the lessons A[i] - x_[i] of the 2nd part.
	private boolean three_days_validation(int iFinal) {
		int d, k, sumP_0, sumP_1, sumP_1_, i, p, aux3, aux4, aux4_, aux1_, d_0, d_1, d_2, sum;
		boolean test;

		// Test of the sums of the minima on d2, d3 and d4 (Section 5.4.2): minimum lessons of each Teacher on the day = load ns_p1 minus the
		// capacity of the other working days; the sum cannot exceed Ma*Qa.
		if (iFinal >= case_10) {
			k = 0;
			while (true) {
				k++;
				p = d2_[k];
				aux0 = x_case__3[p];
				aux1 = wp0[p] - aux0;
				aux1_ = maxDays1[p] - wp0[p];
				workDaysP[p][2] = aux0 > 0;
				workDaysP[p][3] = aux1 > 0;
				workDaysP[p][4] = aux1_ > 0;
				sumMax[p] = 0;
				for (d = 2; d <= 4; d++)
					sumMax[p] += (workDaysP[p][d] ? Mp[p][d] : 0);
				if (p == Ap[iFinal])
					break;
			}
			for (d = 2; d <= 4; d++) {
				sum = 0;
				k = 0;
				while (true) {
					k++;
					p = d2_[k];
					aux = ns_p1[p] - sumMax[p] + (workDaysP[p][d] ? Mp[p][d] : 0);
					if (aux > 0)
						sum += aux;
					if (p == Ap[iFinal])
						break;
				}
				if (sum > Ma * Qa)
					return false;
			}
		}
		// Teachers' demand on d2, d3 and d4 (formula of Section 5.4.1), comparing the capacity with ns_p1[p].
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		i = 1;
		test = false;
		while (true) {
			p = Ap[i];
			if (maxDays1[p] == 0 || (!complex_p1[p] && !firstPossibility)) {
				do
					i++;
				while (case_[i] == 2);
				test = true;
				i++;
				if (i > iFinal)
					break;
				continue;
			}
			sumP_0 = 0;
			sumP_1 = 0;
			sumP_1_ = 0;
			aux0 = x_case__3[p];
			aux1 = wp0[p] - aux0;
			aux1_ = maxDays1[p] - wp0[p];
			aux3 = aux0 * Mp[p][d_0];
			aux4 = aux1 * Mp[p][d_1];
			aux4_ = aux1_ * Mp[p][d_2];
			if (!noDoublePeriodsP[p]) {
				aux0 *= 2;
				aux1 *= 2;
				aux1_ *= 2;
			}
			do {
				k = A[i] - x_[i];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
				sumP_1_ += Math.min(aux1_, k);
				i++;
			} while (case_[i] == 2);
			i++;
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			aux0 += Math.min(aux4_, sumP_1_);
			test = ns_p1[p] <= aux0;
			if (!test)
				test3 = false;
			if (!test || i > iFinal)
				break;
		}
		firstPossibility = false;
		return test;
	}

	// Successor of Case 10 (validation, Section 5.4). Returns false (approved) or true (rejected, starts the backtracking).
	// Steps: (1) two-day validation: looks for a possibility of the 1st part (bitSet_0) compatible with the Case 2
	// units already defined; (2) three-day validation: looks for a possibility of the 2nd part (bitSet_2) and, among its
	// sub-possibilities, a compatible one. If either step finds none, the unit fails.
	public boolean maximum_10(int iFinal) {
		boolean success, maximum_;
		int count, possib, p, generator, i0, n2_, i, d_0, d_1, d_2, d, salvaI, sum;

		// When returning to this unit, the backtracking merely continues.
		if (x_[iFinal] > -1)
			return true;
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		// Teacher's workloads in the two parts: accumulated in the control variables up to the previous unit.
		p = Ap[iFinal];
		ns_p0[p] = Sp0_[pointerP2[iFinal - 1]];
		ns_p1[p] = Sp1_[pointerP2[iFinal - 1]];

		success = false;
		// count = number of possibilities of the 1st part.
		count = Module_2A.nPossib[0];
		if (count == 0) {
			System.out.print("error1");
			System.exit(0);
		}
		// ns2 (1st part): lessons already decided (x_[i]) for the previous Case 2 units; for those not yet executed, the weekly load A[i] (best available bound).
		for (i = 1; i <= iFinal; i++) {
			if (case_[i] == 2)
				ns2[Ap[i]][j_[i]] = x_[i];
		}
		for (i = iFinal + 1; i <= n2; i++) {
			if (case_[i] == 2)
				ns2[Ap[i]][j_[i]] = A[i];
		}

		test3 = true;
		firstPossibility = true;
		// Step 1 - two-day validation: tests each possibility of the 1st part, placing in x_case__3 whether d0 is a working day.
		for (possib = 1; possib <= count; possib++) {
			for (p = 1; p <= Qp; p++)
				x_case__3[p] = (Module_2A.bitSet_0.get(possib * Qp * 2 + 2 * (p - 1)) ? 1 : 0);
			// Does it meet the Teachers' demands?
			success = two_days_validation(iFinal);
			// If a Teacher's demand failed, no possibility will do: the unit is rejected.
			if (!test3)
				return true;
			// Does it also meet the classes' demands? (case__350_) If so, a single possibility is enough.
			if (success) {
				success = case__350_();
				if (success)
					break;
			}
		}
		if (!success)
			return (true);
		// Preparation for step 2: ns2 now represents the lessons of each pair in the 2nd part (A[i] - x_[i]).
		for (i = 1; i <= iFinal; i++) {
			if (case_[i] == 2)
				ns2[Ap[i]][j_[i]] = A[i] - x_[i];
		}
		// Dp[p]: minimum number of working days of p on d2, d3 and d4 to fulfill ns_p1[p] (uses the largest Mp first) and so
		// that each class of p gets its lessons in the 2nd part (1 per day, or 2 if allowed). Ep[p]: minimum lessons of p on a day, assuming
		// the other working days at maximum capacity. They are only needed for Teachers that are not complex_p2.
		p = Ap[iFinal];
		if (!Module_2A.complex_p2[p]) {
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][d + 2];
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 2; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p1[p])
					break;
			}
			i = iFinal - 1;
			case_[0] = 0;
			while (case_[i] == 2) {
				if (noDoublePeriodsP[p])
					Dp[p] = Math.max(Dp[p], A[i] - x_[i]);
				else
					Dp[p] = Math.max(Dp[p], (A[i] - x_[i] + 1) / 2);
				i--;
			}
			aux0 = 0;
			d = 2;
			for (k = 1; k <= maxDays1[p] - 1; k++) {
				aux0 += arranjo[d];
				d--;
			}
			Ep[p] = Math.max(ns_p1[p] - aux0, 0);
		}

		// Ap2 lists the Teachers (in the order of d2_) that are not complex_p2: for these, the days d2 and d3 are still to be decided. salvaI is the position
		// of the Teacher of the current unit (iFinal) in this list; n2_ counts one extra unit, for checking the classes.
		k = 0;
		salvaI = -1;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (p == Ap[iFinal])
				salvaI = k + 1;
			if (Module_2A.complex_p2[p]) {
				if (p == Ap[iFinal])
					salvaI = k;
				continue;
			}
			k++;
			Ap2[k] = p;
		}
		k++;
		n2_ = k;

		// Step 2 - three-day validation: for each possibility of the 2nd part (bitSet_2), wp0[p] = number (0, 1 or 2) of
		// working days of p on d2 and d3. For complex_p2 Teachers, x_case__3 is fixed (working day known since Module 2A).
		count = Module_2A.nPossib_2;
		if (count == 0) {
			System.out.println("error2_");
			System.exit(0);
		}
		firstPossibility = true;
		test3 = true;
		for (possib = 1; possib <= count; possib++) {
			for (p = 1; p <= Qp; p++) {
				test = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1));
				test2 = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1) + 1);
				wp0[p] = 2 * (test ? 1 : 0) + (test2 ? 1 : 0);
				if (Module_2A.complex_p2[p]) {
					test = Module_2A.bitSet_1.get(3 * (p - 1));
					x_case__3[p] = (test ? 1 : 0);
				}
			}
			// Backtracking search over the Teachers of Ap2: x2[i] = 1 if d2 is a working day of Teacher Ap2[i], 0 otherwise
			// (sub-possibilities of the possibility under test). If all units close, the validation succeeds.
			generator = -1;
			for (i = 1; i <= n2_; i++)
				x2[i] = generator;
			i = 1;
			success = false;
			do {
				if (i < n2_) {
					p = Ap2[i];
					x0 = x2[i];
					i0 = wp0[p];
					// Invalid values: greater than 1, greater than wp0[p], or 1 when p is not available on d2.
					if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
						maximum_ = true;
					else {
						// Up to the Teacher of the unit (salvaI), the minimum number of working days Dp is used to bound x0 from below.
						if (i <= salvaI) {
							if (Mp[p][d_2] == 0 || i0 == maxDays1[p])
								aux2 = 0;
							else
								aux2 = 1;
							x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
						} else
							x0++;
						// If the Teacher works on both d2 and d3, or can only work on d2, the value is forced to 1.
						if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
							x0 = 1;
						maximum_ = (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0));
						// Pruning: the accumulated sums of the minimum lessons on d2 and d3 cannot exceed Ma*Qa.
						if (!maximum_ && i <= salvaI) {
							maximum_ = (sumP0[i - 1] + (x0 != 0 ? Ep[p] : 0) <= Ma * Qa)
									&& (sumP1[i - 1] + (i0 - x0 != 0 ? Ep[p] : 0) <= Ma * Qa);
							maximum_ = !maximum_;
						}
						// At the unit's Teacher, runs the three-day validation with the partial sub-possibility defined so far.
						if ((i == salvaI || (i == 1 && salvaI == 0)) && !maximum_) {
							x_case__3[Ap2[i]] = x0;
							maximum_ = !three_days_validation(iFinal);
							if (!test3)
								return true;
						}
					}
				// Final unit of this search: checks the classes' demands on the three days (case__350).
				} else
					maximum_ = case__350(i);
				// Value accepted: updates x_case__3 and the accumulated sums of the minimum lessons and advances.
				if (!maximum_) {
					x2[i] = x0;
					if (i < n2_) {
						p = Ap2[i];
						x_case__3[p] = x0;
						if (i <= salvaI) {
							sumP0[i] = sumP0[i - 1] + (x0 != 0 ? Ep[p] : 0);
							sumP1[i] = sumP1[i - 1] + (wp0[p] - x0 != 0 ? Ep[p] : 0);
						}
					}
					i++;
					if (i > n2_) {
						success = true;
						break;
					}
				// No successor: backtracks (the search ends unsuccessfully if it backtracks beyond the first unit).
				} else {
					if (i == 1)
						break;
					x2[i] = generator;
					i--;
				}
			} while (true);
			if (success)
				break;
		}
		// Value of this unit's decision variable (meaningless). Returns true (failure) if no possibility was approved.
		x0 = 0;
		return (!success);
	}

	// Checks whether the classes' demands on d2, d3 and d4 (Ma lessons per class per day) can be met by the sub-possibility
	// under test (x_case__3, wp0, maxDays1). Returns true if they can NOT (failure), false if they can.
	public boolean case__350(int i) {
		int aux2, aux3, aux4, aux4_, aux1_, x0, d_0, d_1, d_2, sumA0, sumA1, sumA1_;
		boolean test;
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		sumA0 = 0;
		sumA1 = 0;
		sumA1_ = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
			sumA_1_[a] = 0;
		}
		test = false;
		// For each Teacher with working days in the 2nd part: lessons they can teach in each class on d2, d3 and d4 (limited by
		// 1 or 2 lessons per day, by the day's capacity Mp and by ns2[p][j]); accumulates the total capacity and that of each class.
		for (p = 1; p <= Qp; p++) {
			if (maxDays1[p] == 0)
				continue;
			x0 = x_case__3[p];
			aux0 = (noDoublePeriodsP[p] ? x0 : 2 * x0);
			aux1 = wp0[p] - x0;
			aux1_ = maxDays1[p] - wp0[p];
			if (!noDoublePeriodsP[p]) {
				aux1 *= 2;
				aux1_ *= 2;
			}
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux4_ = (maxDays1[p] - wp0[p] == 0 ? 0 : Mp[p][d_2]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux1_ = Math.min(aux1_, aux4_);
			aux2 = nClassesP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			sumA1_ += aux4_;
			for (j = 1; j <= aux2; j++) {
				a = list[p][j];
				sumA_0[a] += Math.min(aux0, ns2[p][j]);
				sumA_1[a] += Math.min(aux1, ns2[p][j]);
				sumA_1_[a] += Math.min(aux1_, ns2[p][j]);
			}
		}
		// Consistency check: the capacity of d4 should always meet Ma*Qa (guaranteed by Module 2A).
		if (sumA1_ < Ma * Qa) {
			System.out.println("ERROR 86");
			System.exit(0);
		}
		// The total capacities of d2 and d3 must reach Ma*Qa; then each class must have at least Ma possible lessons on each day.
		test = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
		if (!test)
			return true;
		for (a = 1; a <= Qa; a++) {
			test = sumA_0[a] >= Ma && sumA_1[a] >= Ma && sumA_1_[a] >= Ma;
			if (!test)
				break;
		}
		x0 = 0;
		return !test;
	}

	// Checks whether the classes' demands on d0 and d1 can be met by the possibility of the 1st part under test (x_case__3,
	// maxDays0). Analogous to case__350, but returns true when they can be met.
	public boolean case__350_() {
		int d_0, d_1, sumA0, sumA1, aux3;
		d_0 = 0;
		d_1 = 1;
		sumA0 = 0;
		sumA1 = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
		}
		for (p = 1; p <= Qp; p++) {
			if (maxDays0[p] == 0)
				continue;
			x0 = x_case__3[p];
			aux0 = (noDoublePeriodsP[p] ? x0 : 2 * x0);
			aux1 = maxDays0[p] - x0;
			if (!noDoublePeriodsP[p])
				aux1 *= 2;
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (maxDays0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux2 = nClassesP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			for (j = 1; j <= aux2; j++) {
				a = list[p][j];
				sumA_0[a] += Math.min(aux0, ns2[p][j]);
				sumA_1[a] += Math.min(aux1, ns2[p][j]);
			}
		}
		test = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
		if (test) {
			for (a = 1; a <= Qa; a++) {
				test = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
				if (!test)
					break;
			}
		}
		return test;
	}

	// Successor of Case 2 (Section 5.3.2, same computation as Case 2 of Module 2A): x0 = number of lessons of the pair (Ap[i], Aa[i]) in the 1st
	// part of the week; A[i] - x0 in the 2nd part. On the first visit it computes the bounds minX0 and maxX0[i]; on subsequent ones it increments x0.
	public boolean maximum_2() {
		int maxX0_, minX0;
		boolean initialized;
		p = Ap[i];
		x0 = x_[i];
		// There is no successor if x0 is already at the maximum possible lessons of the pair in the 1st part.
		if (x0 == 2 * maxDays0[p] || (noDoublePeriodsP[p] && x0 == maxDays0[p]))
			return true;
		// Trivial case: Np0[p] or Np1[p] is zero and all the lessons stay in a single part.
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
		// Sp0, Sp2, Sa0 and Sa2: lessons already assigned to the Teacher and the class, in each part, by the previous units.
		Sp0 = Sp0_[pointerP2[i] - 1];
		Sp2 = Sp1_[pointerP2[i] - 1];
		Sa0 = Sa_0[pointerA[i] - 1];
		Sa2 = Sa_1[pointerA[i] - 1];

		// Lower bound: what does not fit in the 2nd part (by working days, by Np1[p] and by the class's 3*Ma).
		minX0 = (noDoublePeriodsP[p] ? n0 - maxDays1[p] : n0 - 2 * maxDays1[p]);
		minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
		minX0 = Math.max(minX0, Sa2 + n0 - 3 * Ma);
		x0 = Math.max(x0, minX0);

		// Upper bound: what fits in the 1st part (by working days, by n0, by the remainder of Np0[p] and by the remainder of the class's 2*Ma).
		maxX0_ = (noDoublePeriodsP[p] ? maxDays0[p] : 2 * maxDays0[p]);
		maxX0_ = Math.min(maxX0_, n0);
		maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
		maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
		maxX0[i] = maxX0_;
		return x0 > maxX0_;
	}

	// Updates the control variables (accumulated lessons of the Teacher and of the class in the two parts) after x0 is accepted.
	public void update_2() {
		p = Ap[i];
		nLessons = A[i] - x0;
		aux = pointerP2[i];
		Sp0_[aux] = Sp0_[aux - 1] + x0;
		Sp1_[aux] = Sp1_[aux - 1] + nLessons;
		aux = pointerA[i];
		Sa_0[aux] = Sa_0[aux - 1] + x0;
		Sa_1[aux] = Sa_1[aux - 1] + nLessons;
	}

	// Alternative to the Case 5 unit (case_50) without Module 3B: runs only Module 3A, first for the days (d2, d3, d4) and
	// then for (d0, d1, d2). In case of failure, i1_ receives the index of the Case 2 unit to which the search must backtrack. Returns
	// false if it obtains the complete distribution and true if it fails.
	static private boolean case_5(boolean generator) {

		// First run of Module 3A (days d2, d3 and d4), with the lessons of the 2nd part in ns2.
		g2_.preliminaries(ns2, maxDays1);
		test = g2_.main2();
		if (test) {
			System.out.println("Closed for 3 days");
			g2_.update4();
		} else {
			// Failure: obtains the Teacher and the class at which Module 3A stopped and locates the corresponding last Case 2 unit.
			p = g2_.get_P();
			a = g2_.get_A();
			i1_ = n2;
			if (a != 0) {
				while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2)
					i1_--;
			} else {
				while (Ap[i1_] != p || case_[i1_] != 2)
					i1_--;
			}
			System.out.println("i0_=" + i1_ + " ");
			return true;
		}

		// Second run of Module 3A (days d0, d1 and d2).
		g2_.preliminaries0();
		test = g2_.main2();
		if (test) {
			g2_.update4();
			x0 = 0;
			return false;
		}
		p = g2_.get_P();
		a = g2_.get_A();
		i1_ = n2;
		if (a != 0) {
			while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2)
				i1_--;
		} else {
			while (Ap[i1_] != p || case_[i1_] != 2)
				i1_--;
		}
		System.out.println("i1_=" + i1_ + " ");
		return true;
	}

	// Case 5 unit: finalizes the distribution through Modules 3B and 3A (Section 5.3.2). Module 3B is tried for each
	// possibility of the 2nd part (bitSet_2), until one succeeds; then Module 3A distributes the lessons among d0, d1 and d2.
	// Returns false if both succeed; otherwise returns true and i1_ indicates the Case 2 unit to which the algorithm must backtrack.
	static private boolean case_50(boolean generator) {
		int min_, max_, meday, successCount;
		count = Module_2A.nPossib_2;
		if (count == 0) {
			System.out.print("error24");
			System.exit(0);
		}
		min_ = 10000;
		max_ = -1;
		meday = 0;
		successCount = 0;
		// preliminaries1 checks whether the possibility is feasible (has a feasible sub-possibility); preliminaries2 prepares the run of Module 3B.
		for (possib = 1; possib <= count; possib++) {
			test = g2_0.preliminaries1(ns2, possib);
			if (!test)
				continue;
			g2_0.preliminaries2(ns2, possib);
			successCount++;
			// Runs Module 3B for this possibility.
			success = g2_0.main_V3();
			if (success) {
				System.out.println();
				System.out.println("Closed for 3 days.");
				g2_0.update4();
				break;
			}
			// Failure of Module 3B: records the Case 2 unit (of the Teacher and class at which it stopped) to decide the backtracking.
			p = g2_0.getP();
			a = g2_0.getA();
			if (a != 0) {
				i1_ = n2 + 1;
				do
					i1_--;
				while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2);
				min_ = Math.min(min_, i1_);
				max_ = Math.max(max_, i1_);
				meday += i1_;
			} else {
				System.out.println("Error 023. Reached case_: " + Module_3B.case_[Module_3B.imax]);
				System.exit(0);
			}
		}
		if (successCount == 0) {
			System.out.println("successCount == 0");
			System.exit(0);
		}
		// Module 3B failed for all possibilities. The backtrack goes to the lowest-index unit recorded (if up to 10
		// possibilities were testd), to the medayn-index unit (if more were) or, if the user chose not to return
		// by the minimum, to the highest-index one.
		if (!success) {
			if (successCount <= 10)
				i1_ = min_;
			else
				i1_ = Math.floorDiv(meday, successCount);
			if (!generator)
				i1_ = max_;
			System.out.print("i0_=" + i1_ + " ");
			return true;
		}
		// Module 3B succeeded: runs Module 3A for the days (d0, d1, d2).
		g2_.preliminaries0();
		test = g2_.main2();
		if (test) {
			g2_.update4();
			x0 = 0;
			return false;
		}
		p = g2_.get_P();
		a = g2_.get_A();
		i1_ = n2;
		if (a != 0) {
			while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2)
				i1_--;
		} else {
			while (Ap[i1_] != p || case_[i1_] != 2)
				i1_--;
		}
		System.out.println("i1_=" + i1_ + " ");
		return true;
	}

	// Simplified Algorithm 2 of Chapter 3, for Module 2B. generator_1 is the user's choice about the backtracking of the Case 5 unit.
	// Returns true if the distribution was obtained and false if Module 2B finds no solution.
	public boolean main_V1(boolean generator_1) {
		int k, p0, generator, imax;
		boolean test;
		long initialTime;
		Object[] opcoes = { "Yes", "No" };
		imax = 0;
		// The book's 'generator' (generator = -1).
		generator = -1;
		for (i = 1; i <= n2 + 1; i++)
			x_[i] = generator;
		i = 1;
		initialTime = System.currentTimeMillis();
		do {
			nIterations++;
			// Control of the maximum execution time.
			if (nIterations >= 50000L) {
				nIterations = 0L;
				if ((System.currentTimeMillis() - initialTime) / 60000.0 > timeMaximum) {
					JOptionPane.showMessageDialog(null, "Execution exceeded the maximum time", "Result",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			// Selection of the successor according to the unit's case: 5, 2 or 10.
			if (case_[i] == 5)
				test = case_50(generator_1);
			else {
				if (case_[i] == 2)
					test = maximum_2();
				else
					test = maximum_10(i);
			}
			// Success: stores x0, updates the control variables (Case 2) and advances. If it went past the last unit, the distribution is complete.
			if (!test) {
				x_[i] = x0;
				if (case_[i] == 2)
					update_2();
				i++;
				if (i > imax) {
					imax = i;
					System.out.println(i + "/" + (n2 + 1) + " case_ = " + case_[i] + " p=" + Ap[i]);
				}
				if (i > n2 + 1) {
					System.out
							.println("Elapsed time (min): " + (System.currentTimeMillis() - initialTime) / 60000.0);
					return true;
				}
			// Failure: if it is the first unit, returns false (there is no backtracking from the first Case 2 unit). If the failure was
			// at the Case 5 unit, goes back to the unit indicated in i1_; otherwise, backtracks to the previous unit, skipping the Case 10 units.
			} else {
				if (i == 1)
					return false;
				if (case_[i] == 5)
					p0 = i1_;
				else
					p0 = i - 1;
				if (case_[p0] == 10)
					p0--;
				if (p0 == 0)
					return false;
				for (i = i; i > p0; i--)
					x_[i] = generator;
				i = p0;
			}
		} while (true);
	}
}
