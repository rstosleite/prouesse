package prouesse;

import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

// Class Module_1B - implements Module 1 (Phase 1.1; Sections 4.1.1 and 4.7.1 of the book). It is an improved version of Module_1.
//
// Purpose: decide which lessons (of which Teachers and classes) will take place on the first day and how many (0, 1 or 2 per Teacher and class).
// The results go to dd[p][j][0]. Since the days are reordered beforehand (Section 4.6.1), 'day 0' is the hardest day
// (smallest sum of the maximum numbers of lessons Mp), and not necessarily Monday; Prouesse5D_X restores the original order afterwards.
//
// Method: search of Chapter 3 (Algorithm 1: simple backtracking, generator = generator = -1), with the units below. Unlike Module_1,
// which decides day by day the attendance of each Teacher (cases 10 and 11 of the book), here it suffices to decide, per Teacher:
//   case 510: ns0 = total number of lessons of the Teacher on day 0 (exists only for Teachers with complex_p01 == true);
//   case 501: whether the Teacher works (1) or not (0) on day 0;
//   case 511: single unit, executed after all the 501/510; validates the total supply and computes nPeriods_P0/P1;
//   case 1  : validation of the Teacher (one per Teacher), before his/her units of case 2;
//   case 2  : number of lessons of class Aa[i] of the Teacher on day 0 (remainder on the other 4 days);
//   case 5  : unit after those of case 2 of each Teacher; only propagates the control variables to the next Teacher.
// The Teachers are handled in increasing order of the number of distribution possibilities (Section 4.6.2), computed by Module_A_.
// When all the units are closed, Module 2 (Module_2B) is called to complete the distribution.
public class Module_1B extends PhaseA1_Super {
	// complex_p01[p] = true if the positive maximum numbers of lessons Mp[p][d] of days 1 to 4 are not all equal (analogous to complex_0p);
	// arranjo2/maximum1[p][] = Mp[p][1..4] sorted in increasing order (maximum1[p][3] is the largest);
	// ns0[p], ns1[p] = total number of lessons of Teacher p on day 0 and on days 1 to 4 (defined by case 510);
	// Sa_2[p][a], Sa_3[p][a] = lessons already allocated to class a, on day 0 and on days 1 to 4, by the Teachers preceding p in the order (control
	//   variables);
	// Sp2_, Sp3_, pointerP = accumulated totals of lessons of the Teacher in the two groups of days and pointers of the units into them;
	// sumA_0, sumA_1 = sums per class (used by case 511);  g0 = Module_A object.
	private boolean complex_p01[];
	private int arranjo2[], maximum1[][], ns0[], ns1[], pointerP[], Sa_2[][], Sa_3[][], Sp2_[], Sp3_[], sumA_0[], sumA_1[];
	private Module_A g0;
	static private String days_week[];

	// Constructor: calls the constructor of PhaseA1_Super (which prepares the following modules) and allocates its own arrays.
	public Module_1B() {
		super();
		days_week = new String[] {"", "", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", ""};
		// Control arrays per Teacher and per class (Sa_2, Sa_3), sums per class and data per Teacher (complex_p01, maximum1, ns0, ns1).
		g0 = new Module_A();
		Sa_2 = new int[Qp + 1][Qa + 1];
		Sa_3 = new int[Qp + 1][Qa + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		complex_p01 = new boolean[Qp + 1];
		arranjo2 = new int[4];
		maximum1 = new int[Qp + 1][4];
		ns0 = new int[Qp + 1];
		ns1 = new int[Qp + 1];
		aux = 0;
		for (p = 1; p <= Qp; p++)
			aux += nClassesP[p];
		// Size of the arrays Sp2_, Sp3_ and pointerP: number of units of Module 1 (at most 4 per Teacher + one per class + slack).
		aux = 4 * Qp + aux + 7;
		Sp2_ = new int[aux];
		Sp3_ = new int[aux];
		pointerP = new int[aux];
	}

	// Selection sort, in increasing order, of positions first to nElementos-1 of the array amplitude[], permuting d2_[] along with it.
	// At the end, d2_[i] is the element (day or Teacher) that occupies the i-th position of the ordering. It is also used by Modules 2 and 4.
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

	// Returns the index of the smallest element of amplitude[] between positions i and nElementos-1.
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

	// Prepares the execution of Module 1: sorts the days (Section 4.6.1), sorts the Teachers (Section 4.6.2), defines the type (case_) and the data
	// (Ap, Aa, j_, A) of each unit and the pointers of the control variables. Returns n2, the number of units.
	public int preliminaries() {
		int aux0, p1, d1, d;
		// Warning to the user: the preparation (counting of distributions) and the search may take a long time.
		JOptionPane.showMessageDialog(null, "This action may take a few minutes", "Result",
				JOptionPane.INFORMATION_MESSAGE);
		// 1) For each day k, amplitude[k] = sum, over the Teachers, of the maximum numbers of lessons Mp[p][k]; d2_[k] = k.
		for (k = 0; k <= 4; k++) {
			aux0 = 0;
			for (p1 = 1; p1 <= Qp; p1++)
				aux0 += Mp[p1][k];
			d2_[k] = k;
			amplitude[k] = aux0;
			System.out.println("Sum of MaxP[*][] for " + days_week[k+2] + " " + aux0);
		}
		// 2) Sorts the days in increasing order of the sum of the maxima: the days with the smallest supply (the 'hardest') are placed in the first positions.
		sort2(0, 5);
		for (d1 = 0; d1 <= 4; d1++) {
			k = 0;
			while (d2_[k] != d1)
				k++;
			// destino0[d1] = new position of the original day d1 in the ordering.
			destino0[d1] = k;
		}
		for (p1 = 1; p1 <= Qp; p1++) {
			for (d1 = 0; d1 <= 4; d1++)
				// 3) Permutes the columns of Mp of each Teacher, so that Mp[p][0] becomes the maximum of the hardest day, and so on.
				aux_[d1] = Mp[p1][d1];
			for (d1 = 0; d1 <= 4; d1++)
				Mp[p1][destino0[d1]] = aux_[d1];
		}

		for (p = 1; p <= Qp; p++) {
			// 4) For each Teacher p: ns_p_[p] = total weekly workload.
			ns_p_[p] = 0;
			for (j = 1; j <= nClassesP[p]; j++)
				ns_p_[p] += ns[p][j];
			// complex_p01[p] = true if, among days 1 to 4, there are two positive and different maximum numbers of lessons (the working days are not
			//   interchangeable).
			complex_p01[p] = false;
			aux = -1;
			for (d1 = 1; d1 <= 4; d1++) {
				if (Mp[p][d1] > 0 && Mp[p][d1] != aux && aux != -1) {
					complex_p01[p] = true;
					break;
				}
				if (Mp[p][d1] > 0 && aux == -1)
					aux = Mp[p][d1];
			}
			for (d = 1; d <= 4; d++)
				// maximum1[p][] = maxima of days 1 to 4 in increasing order.
				arranjo2[d - 1] = Mp[p][d];
			Arrays.sort(arranjo2);
			for (k = 0; k <= 3; k++)
				maximum1[p][k] = arranjo2[k];
		}
		for (p = 1; p <= Qp; p++) {
			// 5) Ordering of the Teachers (Section 4.6.2): for each Teacher, Module_A_ counts how many distribution possibilities he/she has
			// between day 0 and the other days. Teachers with complex_p01 receive -10000 and go to the beginning; the others stay in increasing order of the
			//   number of possibilities.
			Module_A_.preliminaries(p);
			aux = Module_A_.main_0();
			System.out.println("No. of distributions for p = " + p + ": " + aux);
			if (complex_p01[p])
				aux -= 10000;
			amplitude[p] = (float) aux;
			d2_[p] = p;
		}
		// d2_[1..Qp] now countains the Teachers in the order in which they will be handled.
		sort2(1, Qp + 1);
		System.out.println();
		// 6) Builds the units, Teacher by Teacher (in the order d2_): if complex_p01, one unit of case 510 (lessons of the Teacher on day 0)...
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (complex_p01[p]) {
				k++;
				Ap[k] = p;
				A[k] = ns_p_[p];
				case_[k] = 510;
			}
			k++;
			Ap[k] = p;
			A[k] = ns_p_[p];
			// ... and one unit of case 501 (the Teacher works or not on day 0).
			case_[k] = 501;
		}
		k++;
		// A single unit of case 511, after the 510/501 units of all Teachers: joint validation of the supply of lessons.
		case_[k] = 511;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			k++;
			Ap[k] = p;
			// For each Teacher, in order: one unit of case 1 (validation), ...
			case_[k] = 1;
			for (j = 1; j <= nClassesP[p]; j++) {
				k++;
				Ap[k] = p;
				Aa[k] = list[p][j];
				j_[k] = j;
				A[k] = ns[p][j];
				// ... one unit of case 2 per class of the Teacher (A[k] = weekly workload in the class) ...
				case_[k] = 2;
			}
			k++;
			Ap[k] = p;
			// ... and one unit of case 5 (updates the state for the next Teacher).
			case_[k] = 5;
		}
		// n2 = number of units. Ap[n2+1] = 0 works as a sentinel.
		n2 = k;
		Ap[n2 + 1] = 0;
		// 7) Pointers pointerP of the units of case 2 into the arrays Sp2_/Sp3_ (accumulated totals of the Teacher on day 0 and on days 1 to 4).
		// Between one Teacher and the next a zeroed position is reserved, so that the position preceding that of the first class of each Teacher is
		//   0.
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				pointerP[j] = aux2;
				aux2++;
				if (j + 2 < n2 && case_[j + 1] != 2) {
					Sp2_[aux2] = 0;
					Sp3_[aux2] = 0;
					aux2++;
				}
			}
		}
		return n2;
	}

	// Equivalent to the method 'successr' of the book: tries to obtain the smallest x0 > x_[i] that satisfies the constraints of case case_[i].
	// Returns false if successful (the value is in x0) and true otherwise (no successor).
	// In the validation cases (511, 1 and 5) the unit admits only one execution (x_ = -1); a new call, after a backtrack, returns true.
	public boolean maximum() {
		int aux0, aux2, aux10, aux11, minX0, maxX0, sum_, d, p2, sumA0, sumA1;
		boolean case_Trivial, test;

		// Teacher of the current unit and current value of x[i] (x_[i] = -1 before the first attempt).
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Case 510: x0 = total number of lessons of the Teacher on day 0.
		// Minimum: what does not fit in days 1 to 4 (workload minus the sum of Mp on those days), at least x0+1;
		// maximum: the smaller of the workload and the maximum number of lessons of day 0.
		case 510: {
			minX0 = ns_p_[p];
			for (d = 1; d <= 4; d++)
				minX0 -= Mp[p][d];
			if (minX0 < 0)
				minX0 = 0;
			minX0 = Math.max(x0 + 1, minX0);
			maxX0 = Math.min(ns_p_[p], Mp[p][0]);
			if (maxX0 < minX0)
				return true;
			x0 = minX0;
			return false;
		}
		// Case 501: x0 = 1 if the Teacher works on day 0 and 0 otherwise. Candidates testd in increasing order (0, 1).
		// aux2 = number of days, among 1 to 4, on which the Teacher is available;  aux = maximum number of working days remaining for days 1 to
		//   4.
		case 501: {
			aux2 = 0;
			for (d = 1; d <= 4; d++)
				aux2 += (Mp[p][d] > 0 ? 1 : 0);
			while (true) {
				x0++;
				aux = Wp0[p] - x0;
				// Discards x0 > 1, x0 > Wp0[p] (maximum number of working days) and working on day 0 when Mp[p][0] == 0 (unavailable).
				if (x0 > 1 || x0 > Wp0[p])
					return true;
				if (x0 > (Mp[p][0] > 0 ? 1 : 0))
					return true;
				if (aux > aux2)
					aux = aux2;
				// Sufficiency test of the choice: if complex_p01, the capacity of day 0 (x0*Mp[p][0]) must cover ns0 and the sum of the 'aux' largest
				// maxima of days 1 to 4 must cover ns1; otherwise (all positive maxima equal), the total capacity must cover the
				//   workload.
				if (complex_p01[p]) {
					sum_ = x0 * Mp[p][0];
					test = sum_ >= ns0[p];
					if (test) {
						sum_ = 0;
						for (k = 0; k < aux; k++)
							sum_ += maximum1[p][3 - k];
						test = sum_ >= ns1[p];
					}
				} else
					test = x0 * Mp[p][0] + aux * maximum1[p][3] >= ns_p_[p];
				if (test)
					return false;
			}
		}
		// Case 511 (joint validation, executed only once): with the working days (maxDays0, maxDays1) and the lessons
		// on day 0 (ns0, ns1) already decided for all Teachers, computes the maximum number of lessons of each Teacher on day 0 (nPeriods_P0)
		// and on days 1 to 4 (nPeriods_P1) and checks the supply of lessons.
		case 511: {
			if (x0 > -1)
				return true;
			for (p = 1; p <= Qp; p++) {
				// nPeriods_P0 = maximum number of lessons on day 0; nPeriods_P1 = ns1 (if complex_p01) or the largest daily maximum times the number of
				//   working days on days 1 to 4.
				nPeriods_P0[p] = Mp[p][0] * maxDays0[p];
				if (complex_p01[p])
					nPeriods_P1[p] = ns1[p];
				else
					nPeriods_P1[p] = maximum1[p][3] * maxDays1[p];
			}
			// Total supply: sumA0/sumA1 add up nPeriods_P0/P1 of the Teachers and sumA_0[a]/sumA_1[a] add up, per class, what the Teachers can offer
			//   it (limited by 1 or 2 lessons per day and by ns).
			sumA0 = 0;
			sumA1 = 0;
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				aux0 = (noDoublePeriodsP[p] ? maxDays0[p] : 2 * maxDays0[p]);
				aux1 = (noDoublePeriodsP[p] ? maxDays1[p] : 2 * maxDays1[p]);
				aux0 = Math.min(aux0, nPeriods_P0[p]);
				aux1 = Math.min(aux1, nPeriods_P1[p]);
				aux2 = nClassesP[p];
				sumA0 += nPeriods_P0[p];
				sumA1 += nPeriods_P1[p];
				for (j = 1; j <= aux2; j++) {
					a = list[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
					sumA_1[a] += Math.min(aux1, ns[p][j]);
				}
			}
			// The supply must cover the demand: Ma*Qa lessons on day 0 and 4*Ma*Qa on the other days, in total and also for each class (Ma and 4*Ma).
			test = sumA0 >= Ma * Qa && sumA1 >= 4 * Ma * Qa;
			if (test) {
				for (a = 1; a <= Qa; a++) {
					test = sumA_0[a] >= Ma && sumA_1[a] >= 4 * Ma;
					if (!test)
						break;
				}
			}
			x0 = 0;
			// Fails (true) if the supply is insufficient.
			return !test;
		}

		// Case 1 (validation of the Teacher, before his/her units of case 2): estimates the maximum number of lessons the Teacher can teach on day 0 (aux10)
		// and on days 1 to 4 (aux11), considering the space still free in each of his/her classes (Sa_2, Sa_3); fails if, added together,
		// these maxima (limited by nPeriods_P0 and nPeriods_P1) do not cover the workload.
		case 1: {
			if (x0 > -1)
				return true;
			x0 = 0;
			aux10 = 0;
			aux11 = 0;
			aux0 = nClassesP[p];
			for (j = 1; j <= aux0; j++) {
				aux10 += Math.min(Math.min(Ma - Sa_2[p][list[p][j]], maxDays0[p] * (noDoublePeriodsP[p] ? 1 : 2)),
						ns[p][j]);
				aux11 += Math.min(Math.min(4 * Ma - Sa_3[p][list[p][j]], maxDays1[p] * (noDoublePeriodsP[p] ? 1 : 2)),
						ns[p][j]);
			}
			return Math.min(aux10, nPeriods_P0[p]) + Math.min(aux11, nPeriods_P1[p]) < ns_p_[p];
		}
		// Case 2: x0 = number of lessons of class Aa[i] of the Teacher on day 0; the remainder (A[i] - x0) falls on days 1 to 4.
		// Sp0/Sp2 = totals already assigned to the Teacher (in the other classes) on day 0 and on days 1 to 4; Sa0/Sa2 = totals already allocated to the class.
		case 2: {
			// If x0 has already reached the maximum number of lessons of the Teacher on day 0, there is no successor.
			if (nPeriods_P0[p] == x0)
				return true;
			// Trivial case: if the Teacher cannot teach on day 0 (or on the other days), there is only one possible value (0 or all the lessons), tried only
			//   once.
			case_Trivial = nPeriods_P0[p] == 0 || nPeriods_P1[p] == 0;
			if (case_Trivial && x0 > -1)
				return true;
			a = Aa[i];
			n0 = A[i];
			Sp0 = Sp2_[pointerP[i] - 1];
			Sp2 = Sp3_[pointerP[i] - 1];
			Sa0 = Sa_2[p][a];
			Sa2 = Sa_3[p][a];
			if (case_Trivial)
				x0 = (nPeriods_P0[p] == 0 ? 0 : n0);
			else
				x0++;
			// Upper bound of x0: at most 1 (or 2) lessons per working day, A[i] lessons, what remains of nPeriods_P0 and what remains in the class
			//   (Ma - Sa0).
			maxX0 = (noDoublePeriodsP[p] ? maxDays0[p] : 2 * maxDays0[p]);
			if (x0 > maxX0)
				return true;
			maxX0 = Math.min(maxX0, n0);
			maxX0 = Math.min(maxX0, nPeriods_P0[p] - Sp0);
			maxX0 = Math.min(maxX0, Ma - Sa0);

			// Lower bound: what does not fit in days 1 to 4, considering working days, what remains of nPeriods_P1 and the capacity of the class
			//   (4*Ma) on those days.
			minX0 = (noDoublePeriodsP[p] ? n0 - maxDays1[p] : n0 - 2 * maxDays1[p]);
			minX0 = Math.max(minX0, Sp2 + n0 - nPeriods_P1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - 4 * Ma);
			x0 = Math.max(x0, minX0);
			return x0 > maxX0;
		}

		// Case 5: validates on the first pass (x0 goes from -1 to 0); on a new call (after a backtrack) it returns true, and the backtracking countinues.
		case 5: {
			if (x0 < 0) {
				x0 = 0;
				return false;
			}
			return true;
		}

		}
		return false;
	}

	// Updates the control and linking variables after unit i received the value x0 (called by mainV1 for all the
	//   cases, except 511).
	public void update2_() {
		int i0, p2, aux, d;
		p = Ap[i];
		switch (case_[i]) {
		case 510: {
			// Case 510: records the Teacher's lessons on day 0 (ns0) and on the others (ns1).
			ns0[p] = x0;
			ns1[p] = ns_p_[p] - x0;
			break;
		}
		case 501: {
			// Case 501: maxDays0 = working days on day 0 (0 or 1); maxDays1 = remaining working days, limited to the number of days
			//   available among 1 to 4.
			maxDays0[p] = x0;
			maxDays1[p] = Wp0[p] - x0;
			aux2 = 0;
			for (d = 1; d <= 4; d++)
				aux2 += (Mp[p][d] > 0 ? 1 : 0);
			if (maxDays1[p] > aux2)
				maxDays1[p] = aux2;
			break;
		}

		case 2: {
			p = Ap[i];
			a = Aa[i];
			nLessons = A[i] - x0;
			aux = pointerP[i];
			// Case 2: accumulates the Teacher's lessons on day 0 (Sp2_) and on days 1 to 4 (Sp3_), considering the classes already handled.
			Sp2_[aux] = Sp2_[aux - 1] + x0;
			Sp3_[aux] = Sp3_[aux - 1] + nLessons;
			break;
		}

		case 5: {
			// Case 5: passes on to the next Teacher (p2) the lessons already allocated by all the preceding Teachers in each class:
			// copies Sa_2/Sa_3 from p to p2 and adds the lessons of p itself (units of case 2 immedaytely preceding).
			if (i < n2) {
				p = Ap[i];
				p2 = Ap[i + 1];
				for (a = 1; a <= Qa; a++) {
					Sa_2[p2][a] = Sa_2[p][a];
					Sa_3[p2][a] = Sa_3[p][a];
				}
				i0 = i - 1;
				do {
					x0 = x_[i0];
					a = Aa[i0];
					Sa_2[p2][a] += x0;
					Sa_3[p2][a] += A[i0] - x0;
					i0--;
				} while (case_[i0] == 2);
			}
		}
		}
	}

	// Executes the search of Module 1 (Algorithm 1 of Chapter 3, with simple backtracking; 'generator' is the generator, -1).
	// n = number of units. Returns true if Module 1 and Module 2 (called at the end) succeeded.
	public boolean mainV1(int generator, int n) {
		int imax;
		String input;
		boolean test;
		imax = 1;
		// Initializes all the decision variables with the generator.
		for (i = 1; i <= n; i++)
			x_[i] = generator;
		i = 1;
		// Asks the user for the maximum execution time (in minutes); every 50000 iterations the elapsed time is checked and the program is
		//   terminated if the limit is exceeded.
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
			// Successor obtained: stores x0 in x_[i], updates the control variables (except in case 511) and advances to the next unit.
			if (!maximum()) {
				x_[i] = x0;
				if (case_[i] != 511)
					update2_();
				i++;
				if (i > n) {
					// All units closed: writes the distribution of day 0 into dd and calls Module 2 (preliminaries + main_V2), whose result is the
					//   result of Phase 1.
					update4(n);
					g2_I_W_3.preliminaries();
					test = g2_I_W_3.main_V2();
					return test;
				}
				// Progress record (furthest unit reached).
				if (i > imax) {
					imax = i;
					System.out.println(i + "/" + n + " case_ " + case_[i]);
				}
			} else {
				// No successor: if it was at the first unit, the problem has no solution. Otherwise, backtracks one unit;
				// units of cases 1, 5 and 511 have no choice of their own, so the backtracking countinues one more unit.
				if (i == 1)
					return false;
				x_[i] = generator;
				i--;
				if (case_[i] == 1 || case_[i] == 5 || case_[i] == 511) {
					x_[i] = generator;
					i--;
				}
			}
		} while (true);
	}

	// Writes in dd[p][j][0] the number of lessons on day 0 determined by the units of case 2 (all positions are zeroed beforehand).
	public void update4(int n) {
		int i, p1, j1;
		for (p1 = 1; p1 <= Qp; p1++) {
			for (j1 = 1; j1 <= nClassesP[p1]; j1++)
				dd[p1][j1][0] = 0;
		}
		for (i = 1; i <= n; i++) {
			if (case_[i] == 2)
				dd[Ap[i]][j_[i]][0] = x_[i];
		}
	}
}
