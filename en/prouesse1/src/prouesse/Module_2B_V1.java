package prouesse;

import java.util.Arrays;
import javax.swing.JOptionPane;
import java.util.BitSet;

// Class Module_2B_V1 - implements Module 2 (Phase 1.2; Sections 4.1.2 and 4.3 of the book).
//
// Purpose: after Module 1 has fixed the lessons of day 0, distribute the remaining lessons of each Teacher, in each class, among
// the following 4 days (days 1 to 4 in the numbering permuted by Module 1), grouped into two pairs: (d_0,d_1) = (1,2) and (d_2,d_3) =
//   (3,4).
// Module 2 decides how many lessons of each (Teacher, class) go to the 1st pair (x) and how many to the 2nd pair (y = A - x), besides
// determining the working days of the Teachers; then it calls Module 3 (Module_3_V1) to split the lessons within each pair.
//
// Method: search of Chapter 3 with smart backtracking (Algorithm 2, method main_V2; z computed by determineZ from
// dependency). Each unit has a type, given by case_[i]; the order of the units is as follows (see preliminaries):
//   6   : (one unit, the first) controls freedom_degrees (maximization of the possibilities of working days, Section 4.7);
//   1, 10: for each Teacher, the number of working days in (d_0,d_1) and in (d_2,d_3) (for complex = true, which days; Sections 4.3.1 and
//     4.3.2);
//   105  : validation of units 1 and 10;
//   3    : for each Teacher, whether he/she works on day d_0 (Section 4.3.3);   35 : validation of the cases 3;
//   30   : the same for day d_2 (Section 4.3.4);                       305: validation of the cases 30;
//   2    : for each (Teacher, class), how many lessons fall in the 1st pair of days (Section 4.3.5);
//   100  : additional validation, right after the units of case 2 of each Teacher (see maximum_100);
//   5    : calls Module 3 for each pair of days, with all the memorized possibilities of working days.
// After unit 5 (index > n2) copies of the units of cases 3, 35, 30 and 305 are included; they are not part of the main search:
// they are used only by detPossibP to enumerate and memorize all the possible determinations of the working days (possibP).
public class Module_2B_V1 extends PhaseB2_Super {
	// freedom_degrees = control value of the maximization (Section 4.7): 1000 in the 1st stage (to find max_) and, in the 2nd, it starts at max_ and decreases
	//   by 1 at a time;
	// max_ = largest number of degrees of freedom (count2) found;  count2 = number of (Teacher, pair of days) whose single working day
	//   can be either of the two;
	// Ep[p] = E_p: minimum number of lessons that Teacher p needs to teach on a working day (Section 4.3.1). Other fields: x_case_3 (working days
	//   of the possibility under test), ns2 (lessons per class in the pair of days), listP (Teachers in the order of the units), case_2 and
	//   case_100 (unit indices), ns_p0/ns_p1 (lessons of the Teacher in each pair).
	private int freedom_degrees, max_, count2, Ep[], case_2, x_case_3[], ns2[][], sumA_0[], sumA_1[], case_100, listP[];
	static public int ns_p0[], ns_p1[];
	static private boolean test3, firstPossibility, complex_p2[];
	static private Module_3_V1 g2_;

	// Constructor: allocates the arrays specific to Module 2 (sized by Qp and Qa) and creates the Module 3 object.
	public Module_2B_V1() {
		possibP = new boolean[2][1026][Qp + 1];
		Ep = new int[Qp + 1];
		listP = new int[Qp + 1];
		Dp = new int[Qp + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		arranjo = new int[4];
		complex_p0 = new boolean[Qp + 1];
		complex_p2 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
		x_case_10 = new int[Qp + 1];
		nPossibP = new int[3];
		g2_ = new Module_3_V1();
		x_case_3 = new int[Qp + 1];
		ns_p0 = new int[Qp + 1];
		ns_p1 = new int[Qp + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		ns2 = new int[Qp + 1][];
		for (p = 1; p <= Qp; p++)
			ns2[p] = new int[nClassesP[p] + 1];
	}

	// Prepares the execution: computes the auxiliary variables of each Teacher, sorts the Teachers, defines the units (case_, Ap, Aa, j_, A),
	// the pointers of the control variables and the array z. Returns n2, the number of units of the search.
	public int preliminaries() {
		int d;
		// The maximization starts in the 1st stage (freedom_degrees = 1000, max_ still undefined).
		freedom_degrees = 1000;
		max_ = -1;
		// nDays_[x] = number of working days represented by the value x of the units of cases 1 and 10 (0, 1, 1, 2, 3); for complex = true,
		//   x=1 and x=2 are
		// the two ways of working a single day. d_0..d_3 = days 1 to 4 handled by the module (Phase 1.2).
		nDays_ = new int[] { 0, 1, 1, 2, 3 };
		d_0 = 1;
		d_1 = 2;
		d_2 = 3;
		d_3 = 4;
		days = new int[] { 0, 1, 2, 3, 4 };
		aux4 = 0;
		aux5 = 0;
		aux41 = 0;
		aux51 = 0;
		pMin = 0;
		for (p = 1; p <= Qp; p++) {
			// 1) Auxiliary variables per Teacher p: q_0[p][j] = lessons of the j-th class in the 4 days (workload minus those of day 0);
			// ns_p[p] = total number of lessons of the Teacher in those 4 days.
			ns_p[p] = 0;
			aux = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				q_0[p][j] = ns[p][j] - dd[p][j][0];
				ns_p[p] += q_0[p][j];
				aux += dd[p][j][0];
			}
			// Wp1[p] = W_1p: maximum number of working days in the 4 days = Wp0 minus 1 if the Teacher already works on day 0 (limited to 4).
			Wp1[p] = Wp0[p];
			if (aux > 0)
				Wp1[p]--;
			if (Wp1[p] > 4)
				Wp1[p] = 4;
			for (d = 0; d <= 3; d++)
				arranjo[d] = Mp[p][days[d + 1]];
			// Dp[p] = D_p: minimum number of working days in 1..4. The largest daily maximum numbers of lessons are added up until ns_p[p] is covered;
			// then, Dp is raised to the minimum required by each class (q_0 days if the Teacher cannot have double lessons; otherwise, half rounded
			//   up).
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 3; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p[p])
					break;
			}
			aux0 = 0;
			d = 3;
			for (k = 1; k <= Wp1[p] - 1; k++) {
				aux0 += arranjo[d];
				d--;
			}
			// Ep[p] = E_p: what remains of the workload after adding the Wp1-1 largest daily maxima, that is, the minimum number of lessons the Teacher
			//   has to teach on some working day.
			aux0 = ns_p[p] - aux0;
			Ep[p] = Math.max(aux0, 0);
			for (j = 1; j <= nClassesP[p]; j++) {
				if (noDoublePeriodsP[p])
					Dp[p] = Math.max(Dp[p], q_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q_0[p][j] + 1) / 2);
			}
			// complex_0p and complex_1p: the pair of days has two available days with different maximum numbers of lessons (the days are not interchangeable).
			//   In this file the variable complex_1p is called complex_p2.
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
			complex_p2[p] = Mp[p][d_2] > 0 && Mp[p][d_3] > 0 && Mp[p][d_2] != Mp[p][d_3];
		}

		for (p = 1; p <= Qp; p++) {
			d2_[p] = p;
			if (ns_p[p] == 0)
				amplitude[p] = 0.0f;
			else {
				// 2) Ordering of the Teachers (Section 4.6.2): amplitude[p] = number of distribution possibilities of the Teacher (Module_A);
				// Teachers with no lessons in the 4 days receive 0. Then the Teachers are sorted in increasing order (sort2) and d2_ holds the order.
				Module_A.preliminaries(p, Wp1[p]);
				amplitude[p] = (float) Module_A.main_0();
			}
		}
		// d2_[1..Qp] = Teachers in increasing order of number of possibilities. Teachers with ns_p <= 0 generate no units.
		Module_1B.sort2(1, Qp + 1);

		case_100 = 10000;
		k = 1;
		// 3) Assembly of the units. The first unit (index 1) is of case 6.
		case_[k] = 6;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// For each Teacher (in the order d2_): one unit of case 1 followed by one of case 10.
			case_[k] = 1;
			Ap[k] = p;
			k++;
			case_[k] = 10;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		// Unit of case 105: validates the decisions of cases 1 and 10.
		case_[k] = 105;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// Units of case 3 (one per Teacher; aux4 = index of the first) and, next, the validation unit of case 35.
			case_[k] = 3;
			if (aux4 == 0)
				aux4 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		// Unit of case 35.
		case_[k] = 35;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// Units of case 30 (one per Teacher; aux5 = index of the first) and validation unit of case 305.
			case_[k] = 30;
			if (aux5 == 0)
				aux5 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		// Unit of case 305.
		case_[k] = 305;

		aux = 0;
		case_2 = -1;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			for (j = 1; j <= nClassesP[p]; j++) {
				// Units of case 2: one for each (Teacher, class) that has lessons in the 4 days (A = number of those lessons).
				nLessons = q_0[p][j];
				if (nLessons > 0) {
					a = list[p][j];
					k++;
					Ap[k] = p;
					Aa[k] = a;
					j_[k] = j;
					case_[k] = 2;
					if (case_2 == -1)
						case_2 = k;
					A[k] = nLessons;
				}
			}
			k++;
			Ap[k] = p;
			// After the units of case 2 of each Teacher: one unit of case 100 (validation). listP holds the Teachers in order; case_100 holds the
			//   index of the unit when the counter reaches Qa+1.
			case_[k] = 100;
			aux++;
			listP[aux] = p;
			if (aux == Qa + 1)
				case_100 = k;
		}
		k += 1;
		n2 = k;
		// Unit of case 5 (n2 = its index = number of units of the search): calls Module 3.
		case_[k] = 5;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// Copies of units 3, 35, 30 and 305 after n2 (aux41 and aux51 = indices of the first copies of 3 and 30): used only by detPossibP.
			case_[k] = 3;
			if (aux41 == 0)
				aux41 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		case_[k] = 35;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			case_[k] = 30;
			if (aux51 == 0)
				aux51 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		case_[k] = 305;

		// 4) Pointers pointerP2 of the units of case 2 into the arrays Sp0_/Sp1_ (accumulated totals of the Teacher in the two pairs of days); between the
		// blocks of different Teachers a zeroed position is inserted, so that the 'previous value' of the 1st class of each Teacher is 0.
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				pointerP2[j] = aux2;
				aux2++;
				if (case_[j + 1] != 2 || Ap[j + 1] != Ap[j]) {
					Sp0_[aux2] = 0;
					Sp1_[aux2] = 0;
					aux2++;
				}
			}
		}

		for (j = 1; j <= Qa; j++) {
			// 5) Pointers pointerA into the arrays Sa_0/Sa_1 (accumulated totals per class): each class occupies a block of 16 positions;
			// the position preceding that of each unit holds the accumulated total before it.
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
		// 6) Allocates iMaxI_, computes z and prepares Module 3 (preliminaries2). Returns n2.
		iMaxI_ = new int[n2 + 2];
		determineZ(n2);
		g2_.preliminaries2();
		return n2;
	}

	// Case 100 (additional validation, executed as soon as all the decisions of case 2 of a Teacher have been made; iFinal = index of the
	//   unit).
	// Before calling Module 3, checks whether there is still some possibility of working days (possibP) compatible with the lessons already
	// distributed between the two pairs of days; if there is none, the unit fails and the search backtracks, avoiding useless calls to Module 3.
	// Returns true in case of failure.
	public boolean maximum_100(int iFinal) {
		boolean success;
		int count, possib, p, i, d_0, d_1;

		if (x_[iFinal] > -1)
			return true;
		d_0 = 1;
		d_1 = 2;
		p = Ap[iFinal];
		// Lessons already assigned to the Teacher in the 1st pair (ns_p0) and in the 2nd pair (ns_p1) by his/her units of case 2.
		ns_p0[p] = Sp0_[pointerP2[iFinal - 1]];
		ns_p1[p] = Sp1_[pointerP2[iFinal - 1]];
		success = false;
		// ns2[p][j] = lessons of the class in the 1st pair: value x of the units already decided; for the following units, the maximum is assumed (all
		//   A[i] lessons).
		for (i = case_2; i <= iFinal; i++) {
			if (case_[i] == 2)
				ns2[Ap[i]][j_[i]] = x_[i];
		}
		for (i = iFinal + 1; i <= n2; i++) {
			if (case_[i] == 2)
				ns2[Ap[i]][j_[i]] = A[i];
		}
		// 1st pair (d_0,d_1) = (1,2): tests each memorized possibility of working days; it suffices that one passes both tests
		//   (two_days_validation and case_350_).
		count = nPossibP[0];
		if (count == 0) {
			System.out.println("ERROR 401");
			System.exit(0);
		}
		test3 = true;
		firstPossibility = true;
		for (possib = 0; possib < count; possib++) {
			for (p = 1; p <= Qp; p++)
				x_case_3[p] = (possibP[0][possib][p] ? 1 : 0);
			success = two_days_validation(iFinal, d_0, d_1, wp0, ns_p0, complex_p0);
			if (!test3)
				return true;
			if (success) {
				success = case_350_(d_0, d_1, wp0);
				if (success)
					break;
			}
		}
		if (!success)
			return (true);
		// 2nd pair (d_2,d_3) = (3,4): the same, with the lessons A[i] - x_[i] (decided units) and the possibilities possibP[1].
		d_0 = 3;
		d_1 = 4;
		for (i = case_2; i <= iFinal; i++) {
			if (case_[i] == 2)
				ns2[Ap[i]][j_[i]] = A[i] - x_[i];
		}
		count = nPossibP[1];
		if (count == 0) {
			System.out.println("ERROR 402");
			System.exit(0);
		}
		test3 = true;
		firstPossibility = true;
		for (possib = 0; possib < count; possib++) {
			for (p = 1; p <= Qp; p++)
				x_case_3[p] = (possibP[1][possib][p] ? 1 : 0);
			;
			success = two_days_validation(iFinal, d_0, d_1, wp1, ns_p1, complex_p2);
			if (!test3)
				return true;
			if (success) {
				success = case_350_(d_0, d_1, wp1);
				if (success)
					break;
			}
		}
		x0 = 0;
		return !success;
	}

	// Checks, for a possibility of working days (x_case_3[p] = 1 if the Teacher works on the 1st day of the pair), whether each Teacher already
	//   handled can
	// accommodate the lessons assigned to him/her in the pair: the total ns_p0[p] must fit in the working days, considering the maximum number of lessons
	//   per day
	// and per class (1 or 2). If some Teacher cannot, test3 = false (the unit fails without examining the other possibilities).
	private boolean two_days_validation(int iFinal, int d_0, int d_1, int[] x_case_1, int[] ns_p0,
			boolean[] complex_p0) {
		int k, sumP_0, sumP_1, i, p, aux3, aux4, sum, d;
		boolean test;

		// From the moment case_100 is defined: for each day of the pair, the sum of the minimum number of lessons that the Teachers already handled have to teach
		//   on it cannot exceed Ma*Qa.
		if (iFinal >= case_100) {
			for (d = 0; d <= 1; d++) {
				sum = 0;
				k = 0;
				while (true) {
					k++;
					p = listP[k];
					if (d == 0)
						aux = ns_p0[p] - (x_case_1[p] - x_case_3[p] > 0 ? Mp[p][d_1] : 0);
					else
						aux = ns_p0[p] - (x_case_3[p] > 0 ? Mp[p][d_0] : 0);
					if (aux > 0)
						sum += aux;
					if (p == Ap[iFinal])
						break;
				}
				if (sum > Ma * Qa)
					return false;
			}
		}
		i = case_2;
		test = false;
		while (true) {
			p = Ap[i];
			if (x_case_1[p] == 0 || (!complex_p0[p] && !firstPossibility)) {
				do
					i++;
				while (case_[i] == 2);
				test = true;
				i++;
				if (i > iFinal)
					break;
				continue;
			}
			// For the Teacher: sumP_0/sumP_1 = how much the classes allow to be offered on the 1st/2nd day; the total ns_p0[p] is compared with what fits in the two
			//   days.
			sumP_0 = 0;
			sumP_1 = 0;
			aux0 = x_case_3[p];
			aux1 = x_case_1[p] - aux0;
			aux3 = aux0 * Mp[p][d_0];
			aux4 = aux1 * Mp[p][d_1];
			if (!noDoublePeriodsP[p]) {
				aux0 *= 2;
				aux1 *= 2;
			}
			do {
				k = (d_0 == 1 ? x_[i] : A[i] - x_[i]);
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
				i++;
			} while (case_[i] == 2);
			i++;
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			test = ns_p0[p] <= aux0;
			if (!test)
				test3 = false;
			if (!test || i > iFinal)
				break;
		}
		firstPossibility = false;
		return test;
	}

	// Analogous to case 35: with the possibility of working days x_case_3, checks whether the supply of lessons of the Teachers covers the demand of the
	//   two days
	// of the pair (Ma*Qa lessons per day in total and Ma per class), using the lessons per class ns2.
	public boolean case_350_(int d_0, int d_1, int[] x_case_1) {
		int sumA0, sumA1, aux3, x0, i;
		sumA0 = 0;
		sumA1 = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
		}
		for (p = 1; p <= Qp; p++) {
			if (ns_p[p] == 0 || x_case_1[p] == 0)
				continue;
			x0 = x_case_3[p];
			aux0 = (noDoublePeriodsP[p] ? x0 : 2 * x0);
			aux1 = x_case_1[p] - x0;
			if (!noDoublePeriodsP[p])
				aux1 *= 2;
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (x_case_1[p] - x0 == 0 ? 0 : Mp[p][d_1]);
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

	// Validations of cases 105, 35 and 305 (Sections 4.3.2 to 4.3.4). Each one is executed only once per visit (a new call, after
	// a backtrack, returns true). Returns false if the validation succeeded and true otherwise (the algorithm backtracks).
	public boolean maximum_especial() {
		int sumA0, sumA1, aux1, aux2;
		boolean test;
		x0 = x_[i];
		// Validation unit: admits only one execution (x_[i] = -1 the first time).
		if (x0 > -1)
			return true;
		x0 = 0;
		sumA0 = 0;
		sumA1 = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
		}

		// Case 105: with the decisions of cases 1 and 10 (wp0, wp1, Np0, Np1), the maximum supply of lessons of the Teachers in each pair of days must cover
		//   the demand:
		// sum of the maxima >= 2*Ma*Qa in each pair and, for each class, sum >= 2*Ma, where each Teacher countributes with min(1 or 2 lessons per day x
		//   days, N, lessons of the class).
		if (case_[i] == 105) {
			// count2 = degrees of freedom of the solution: number of (Teacher, pair) with a single working day and two available days with the same maximum
			//   (the day can be either of the two).
			count2 = 0;
			for (p = 1; p <= Qp; p++) {
				if (ns_p[p] <= 0)
					continue;
				if (wp0[p] == 1 && Mp[p][1] > 0 && Mp[p][2] > 0 && !complex_p0[p])
					count2++;
				if (wp1[p] == 1 && Mp[p][3] > 0 && Mp[p][4] > 0 && !complex_p2[p])
					count2++;
			}
			// In the 2nd stage, only solutions of cases 1 and 10 with exactly freedom_degrees degrees of freedom are accepted.
			if (freedom_degrees < 1000 && count2 != freedom_degrees)
				return true;
			for (p = 1; p <= Qp; p++) {
				if (ns_p[p] <= 0)
					continue;
				aux0 = (noDoublePeriodsP[p] ? wp0[p] : 2 * wp0[p]);
				aux1 = (noDoublePeriodsP[p] ? wp1[p] : 2 * wp1[p]);
				aux0 = Math.min(aux0, Np0[p]);
				aux1 = Math.min(aux1, Np1[p]);
				aux2 = nClassesP[p];
				sumA0 += Np0[p];
				for (j = 1; j <= aux2; j++)
					sumA_0[list[p][j]] += Math.min(aux0, q_0[p][j]);
				sumA1 += Np1[p];
				for (j = 1; j <= aux2; j++)
					sumA_1[list[p][j]] += Math.min(aux1, q_0[p][j]);
			}
			test = sumA0 >= 2 * Ma * Qa && sumA1 >= 2 * Ma * Qa;
			if (test) {
				for (a = 1; a <= Qa; a++) {
					test = sumA_0[a] >= 2 * Ma && sumA_1[a] >= 2 * Ma;
					if (!test)
						break;
				}
			}
			x0 = 0;
			return !test;
		}

		// Case 35: goes through the units of case 3 (from aux4, or from aux41 if it is in the copies after n2) and, with Np2/Np3 = maximum number of
		//   lessons of the Teacher on the
		// 1st and on the 2nd day of the pair, checks whether the supply covers Ma*Qa lessons on each day and Ma lessons per class on each day.
		if (case_[i] == 35) {
			i1 = (i <= n2 ? aux4 : aux41);
			while (case_[i1] == 3) {
				p = Ap[i1];
				aux0 = (noDoublePeriodsP[p] ? x_[i1] : 2 * x_[i1]);
				aux1 = wp0[p] - x_[i1];
				if (!noDoublePeriodsP[p])
					aux1 *= 2;
				x0 = x_[i1];
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
				Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
				aux0 = Math.min(aux0, Np2[p]);
				aux1 = Math.min(aux1, Np3[p]);
				aux2 = nClassesP[p];
				sumA0 += Np2[p];
				sumA1 += Np3[p];
				for (j = 1; j <= aux2; j++) {
					if (q_0[p][j] <= 0)
						continue;
					a = list[p][j];
					sumA_0[a] += Math.min(aux0, q_0[p][j]);
					sumA_1[a] += Math.min(aux1, q_0[p][j]);
				}
				i1++;
			}
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

		// Case 305: the same for the pair (d_2,d_3), from the units of case 30 (aux5 or aux51), with Np2_0 and Np3_0.
		if (case_[i] == 305) {
			i1 = (i <= n2 ? aux5 : aux51);
			while (case_[i1] == 30) {
				p = Ap[i1];
				aux0 = (noDoublePeriodsP[p] ? x_[i1] : 2 * x_[i1]);
				aux1 = wp1[p] - x_[i1];
				if (!noDoublePeriodsP[p])
					aux1 *= 2;
				x0 = x_[i1];
				Np2_0[p] = (x0 == 0 ? 0 : Mp[p][d_2]);
				Np3_0[p] = (wp1[p] - x0 == 0 ? 0 : Mp[p][d_3]);
				aux0 = Math.min(aux0, Np2_0[p]);
				aux1 = Math.min(aux1, Np3_0[p]);
				aux2 = nClassesP[p];
				sumA0 += Np2_0[p];
				sumA1 += Np3_0[p];
				for (j = 1; j <= aux2; j++) {
					if (q_0[p][j] <= 0)
						continue;
					a = list[p][j];
					sumA_0[a] += Math.min(aux0, q_0[p][j]);
					sumA_1[a] += Math.min(aux1, q_0[p][j]);
				}
				i1++;
			}
			test = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
			if (test) {
				for (a = 1; a <= Qa; a++) {
					test = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
					if (!test)
						break;
				}
			}
			x0 = 0;
			// In the 1st stage (freedom_degrees == 1000) and in the main search (i <= n2), if the validation passes it only records max_ = max(max_,
			//   count2)
			// and the unit returns false; main_V2 then forces the backtrack in order to keep enumerating all the solutions of cases 1 and 10.
			if (i > n2 || freedom_degrees < 1000)
				return !test;
			if (test) {
				System.out.println("max_ = " + max_);
				max_ = Math.max(max_, count2);
				return false;
			}
			return true;
		}
		x0 = 0;
		return false;
	}

	// Case 1 (Section 4.3.1): successor of x[i] for Teacher p = Ap[i]; x = number of working days in (d_0,d_1) (or, if complex_p0 =
	//   true,
	// 0 = none, 1 = only d_0, 2 = only d_1, 3 = both). Returns false if there is a successor (in x0).
	public boolean maximum_1() {
		int x0_;
		// Pruning: the sum of the products wp0 x Ep of the previous Teachers (units of even index) cannot exceed 2*Ma*Qa, the total number of lessons that
		//   the school can have in (d_0,d_1).
		if (x_[i] == -1) {
			aux0 = 0;
			for (k = 2; k < i; k += 2)
				aux0 += wp0[Ap[k]] * Ep[Ap[k]];
			if (aux0 > 2 * Ma * Qa)
				return true;
		}
		p = Ap[i];
		x0 = x_[i];
		// aux2 = minimum number of working days in (d_0,d_1): Dp minus the days of (d_2,d_3) on which the Teacher is available.
		aux2 = Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0) - (Mp[p][d_3] > 0 ? 1 : 0);
		// complex_p0: candidates x0 = 0, 1, 2, 3 with number of days nDays_[x0] >= aux2; rejects x0 > 3, more days than Wp1 or than the days
		//   available in (d_0,d_1).
		if (complex_p0[p]) {
			do {
				x0++;
				x0_ = nDays_[x0];
			} while (x0_ < aux2);
			return (x0 > 3 || x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0));
		// Otherwise: x0 = number of days, at least aux2; rejects x0 > 2, x0 > Wp1[p] or more days than those available.
		} else {
			x0 = Math.max(x0 + 1, aux2);
			return (x0 > 2 || x0 > Wp1[p] || x0 > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0));
		}
	}

	// Case 10 (Section 4.3.2): analogous to case 1, for the pair (d_2,d_3) (the parameters d_0 and d_1 receive here the days d_2 and d_3).
	// aux20 = minimum number of working days in that pair (Dp - wp0); aux2 = maximum number of lessons of the Teacher in (d_0,d_1) as defined in case 1.
	// Each candidate x0 must respect Wp1 - wp0, the availability and make it possible to fulfill the workload ns_p[p] (aux2 + supply of the current
	//   pair).
	public boolean maximum_10(int d_0, int d_1) {
		int x0_, aux20, aux;
		if (x_[i] == -1) {
			aux0 = 0;
			for (k = 3; k < i; k += 2)
				aux0 += wp1[Ap[k]] * Ep[Ap[k]];
			if (aux0 > 2 * Ma * Qa)
				return true;
		}
		p = Ap[i];
		x0 = x_[i];
		aux20 = Dp[p] - wp0[p];
		aux2 = 0;
		if (complex_p0[p]) {
			aux = x_case_1[p];
			if (aux > 0) {
				if (aux == 1)
					aux2 = Mp[p][1];
				else {
					if (aux == 2)
						aux2 = Mp[p][2];
					else
						aux2 = Mp[p][1] + Mp[p][2];
				}
			}
		} else {
			if (wp0[p] > 0) {
				if (wp0[p] == 1)
					aux2 = Math.max(Mp[p][1], Mp[p][2]);
				else
					aux2 = Mp[p][1] + Mp[p][2];
			}
		}
		if (complex_p2[p]) {
			while (true) {
				do {
					x0++;
					x0_ = nDays_[x0];
				} while (x0_ < aux20);
				if (x0 > 3 || wp0[p] + x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
					return true;
				aux = aux2;
				if (x0 > 0) {
					if (x0 == 1)
						aux += Mp[p][d_0];
					else {
						if (x0 == 2)
							aux += Mp[p][d_1];
						else
							aux += Mp[p][d_0] + Mp[p][d_1];
					}
				}
				if (ns_p[p] <= aux)
					return false;
			}
		} else {
			while (true) {
				x0 = Math.max(x0 + 1, aux20);
				if (x0 > 2 || wp0[p] + x0 > Wp1[p] || x0 > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
					return true;
				aux = aux2;
				if (x0 > 0) {
					if (x0 == 1)
						aux += Math.max(Mp[p][d_0], Mp[p][d_1]);
					else
						aux += Mp[p][d_0] + Mp[p][d_1];
				}
				if (ns_p[p] <= aux)
					return false;
			}
		}
	}

	// Case 3 (Section 4.3.3): x = 1 if the Teacher works on day d_0 and 0 if not. Constraints: x <= wp0; x = 0 if Mp[p][d_0] == 0;
	// x > 0 if necessary to reach the minimum days Dp (considering d_1 and wp1) and the number of days wp0 of the pair; if complex_p0 and wp0 ==
	//   1, the
	// value is a consequence of x_case_1 (1: works on d_0; 2: works on d_1). Returns false if there is a successor.
	public boolean maximum_3() {
		int i0, x0_;
		boolean test;
		p = Ap[i];
		x0 = x_[i];
		while (true) {
			i0 = wp0[p];
			if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
				return true;
			x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - wp1[p]);
			if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
				x0 = 1;
			if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
				return true;
			test = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
			test = test && ((!complex_p0[p]) || (i0 != 1) || (x_case_1[p] == 1 && x0 == 1)
					|| (x_case_1[p] == 2 && x0 == 0));
			if (test)
				return false;
		}
	}

	// Case 30 (Section 4.3.4): analogous to case 3, for day d_2 of the pair (d_2,d_3), using wp1, x_case_10 and Dp - (availability on d_3) -
	//   wp0.
	public boolean maximum_30(int d_0, int d_1) {
		int i0, x0_;
		boolean test;
		p = Ap[i];
		x0 = x_[i];
		while (true) {
			i0 = wp1[p];
			if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
				return true;
			x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][4] > 0 ? 1 : 0) - wp0[p]);
			if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
				x0 = 1;
			if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
				return true;
			test = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
			test = test && ((!complex_p2[p]) || (i0 != 1) || (x_case_10[p] == 1 && x0 == 1)
					|| (x_case_10[p] == 2 && x0 == 0));
			if (test)
				return false;
		}
	}

	// Case 2 (Section 4.3.5): x0 = number of lessons of (p, class Aa[i]) in the 1st pair of days; the others (A[i] - x0) fall in the 2nd pair.
	// Constraints: maximum number of lessons per day and per class (1 or 2) x working days of each pair (wp0, wp1); the Teacher's sum does not exceed
	// Np0 (1st pair) and Np1 (2nd pair); the sum in the class does not exceed 2*Ma in each pair. Uses the control variables Sp0_, Sp1_, Sa_0 and Sa_1.
	// Returns false if there is a successor.
	public boolean maximum_2() {
		int maxX0_, minX0;
		boolean initialized;
		p = Ap[i];
		x0 = x_[i];
		if (x0 == 2 * wp0[p] || (noDoublePeriodsP[p] && x0 == wp0[p]))
			return true;
		// Trivial case: if the Teacher has no possible lessons in one of the pairs, there is only one value (0 or all the lessons), tried only once.
		case_Trivial = Np0[p] == 0 || Np1[p] == 0;
		n0 = A[i];
		// When the unit has already been initialized, the bounds have not changed since the last visit: reuses maxX0[i] (stored the first time).
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
		// Totals already assigned before this unit: Sp0/Sp2 = lessons of the Teacher in the 1st/2nd pair (Sp0_, Sp1_); Sa0/Sa2 = lessons of the class in the 1st/2nd pair
		//   (Sa_0, Sa_1).
		Sp0 = Sp0_[pointerP2[i] - 1];
		Sp2 = Sp1_[pointerP2[i] - 1];
		Sa0 = Sa_0[pointerA[i] - 1];
		Sa2 = Sa_1[pointerA[i] - 1];

		// Lower bound: what does not fit in the 2nd pair (by working days wp1, by Np1 - Sp2 and by 2*Ma in the class).
		minX0 = (noDoublePeriodsP[p] ? n0 - wp1[p] : n0 - 2 * wp1[p]);
		minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
		minX0 = Math.max(minX0, Sa2 + n0 - 2 * Ma);
		x0 = Math.max(x0, minX0);
		// Upper bound: working days of the 1st pair, A[i], Np0 - Sp0 (remaining capacity of the Teacher) and 2*Ma - Sa0 (remaining capacity of the
		//   class).
		maxX0_ = (noDoublePeriodsP[p] ? wp0[p] : 2 * wp0[p]);
		maxX0_ = Math.min(maxX0_, n0);
		maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
		maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
		maxX0[i] = maxX0_;
		return x0 > maxX0_;
	}

	// Updates the linking variables after a unit of case 1 (with Np0, wp0, d_0, d_1, x_case_1, complex_p0) or of case 10 (with Np1,
	//   wp1,
	// d_2, d_3, x_case_10 and complex of the 2nd pair): Np = maximum number of lessons of the Teacher in the pair; wp = number of working days; x_case = value of
	//   x.
	public void update_1(int Np0[], int wp0[], int d_0, int d_1, int[] x_case_1, boolean[] complex_p0) {
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
			wp0[p] = nDays_[x0];
			x_case_1[p] = x0;
		} else {
			Np0[p] = x0 * Math.max(Mp[p][d_0], Mp[p][d_1]);
			wp0[p] = x0;
		}
	}

	// Updates the control and linking variables after a unit of case 2: accumulated totals of the Teacher (Sp0_, Sp1_) and of the class (Sa_0,
	//   Sa_1)
	// in the two pairs of days, and q2_ / q2_0 = lessons of (p, class) in the 1st and in the 2nd pair (inputs of Module 3).
	public void update_2() {
		p = Ap[i];
		nLessons = A[i] - x0;
		aux = pointerP2[i];
		Sp0_[aux] = Sp0_[aux - 1] + x0;
		Sp1_[aux] = Sp1_[aux - 1] + nLessons;
		aux = pointerA[i];
		Sa_0[aux] = Sa_0[aux - 1] + x0;
		Sa_1[aux] = Sa_1[aux - 1] + nLessons;
		q2_[p][j_[i]] = x0;
		q2_0[p][j_[i]] = nLessons;
	}

	// Enumerates and memorizes in possibP all the determinations of the working days for the pair exec (0: (d_0,d_1); 1: (d_2,d_3)) compatible
	//   with the
	// decisions of cases 1 and 10 (Section 4.2.2, remark 4). Uses the copies of units 3/35 (or 30/305) after n2 with a simple backtrack: when closing the
	//   validation
	// unit, the possibility is recorded (possibP[exec][k][p] = true if the Teacher works on the 1st day of the pair) and the search continues
	// until all the possibilities are exhausted.
	public void detPossibP(int exec) {
		int generator, i0, imax, j, k;
		boolean test;
		j = 0;
		imax = 0;
		generator = -1;
		for (i = aux41; i <= x_.length - 1; i++) {
			x_[i] = generator;
			if ((exec == 0 && case_[i] == 35) || (exec == 1 && case_[i] == 305))
				j = i;
		}

		i0 = (exec == 0 ? aux41 : aux51);
		i = i0;
		do {
			if (exec == 0) {
				if (case_[i] == 3)
					test = maximum_3();
				else
					test = maximum_especial();
			} else {
				if (case_[i] == 30)
					test = maximum_30(d_2, d_3);
				else
					test = maximum_especial();
			}
			if (!test) {
				x_[i] = x0;
				if (i == j) {
					k = nPossibP[exec];
					if (k >= 1026) {
						System.out.println("ERROR: THE SPACE TO STORE THE POSSIBILITIES WAS EXHAUSTED.");
					} else {
						for (i = i0; i <= j - 1; i++)
							possibP[exec][k][Ap[i]] = x_[i] != 0;
						nPossibP[exec]++;
						i = j;
					}
				} else {
					i++;
					if (i > imax) {
						imax = i;
						System.out.println("i=" + imax + " Case[i]=" + case_[i]);
					}
				}
			} else {
				if (i == i0) {
					System.out.println("Number of possibilities for exec = " + exec + ": " + nPossibP[exec]);
					return;
				}
				x_[i] = generator;
				i--;
			}
		} while (true);

	}

	// Main search of Module 2: Algorithm 2 of Chapter 3 (generator = generator = -1; iMaxI_ = iMax; z = array z).
	// Returns true if all the units were closed (distribution of the 4 days complete) and false if the problem has no solution.
	public boolean main_V2() {
		int p0, generator, k, i0, salvaI, imax_, min1, max1, meday, successCount;
		boolean test, generator1;
		long initialTime;
		Object[] opcoes = { "Yes", "No" };
		imax_ = 0;
		generator = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		iMaxI_[1] = 1;
		initialTime = System.currentTimeMillis();
		do {
			// Time control: every 50000 iterations it checks the elapsed time; if it exceeds the limit entered, the program is terminated.
			nIterations++;
			if (nIterations >= 50000L) {
				nIterations = 0L;
				if ((System.currentTimeMillis() - initialTime) / 60000.0 > timeMaximum) {
					JOptionPane.showMessageDialog(null, "Execution exceeded the maximum time", "Result",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			test = true;
			// 1) Obtaining the successor of x[i]: each type of unit calls the corresponding method (test = true means 'no successor / failure').
			switch (case_[i]) {
			// Case 6 (first unit): adjusts freedom_degrees (each new value is a restart of the search). On the 1st visit nothing changes. When returning to
			//   it:
			// if freedom_degrees < 1000, it fails when it reaches 0 and otherwise decreases by 1; if it is still 1000, it takes the value max_ (fails if max_ == -1, that
			//   is, no solution).
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
				break;
			}
			case 105: {
				test = maximum_especial();
				break;
			}
			case 35: {
				test = maximum_especial();
				break;
			}
			// Case 305: besides validating, in the 1st stage (freedom_degrees == 1000) the unit, even if successful, forces a backtrack to the unit of case
			//   105
			// (i1_), so as to enumerate all the solutions of cases 1 and 10 and find max_; if it fails, the backtrack is of one unit.
			case 305: {
				test = maximum_especial();
				if (!test) {
					if (freedom_degrees == 1000) {
						i1_ = i;
						do
							i1_--;
						while (case_[i1_] != 105);
						test = true;
					}
				} else
					i1_ = i - 1;
				break;
			}
			case 1: {
				test = maximum_1();
				break;
			}
			case 10: {
				test = maximum_10(d_2, d_3);
				break;
			}
			case 3: {
				test = maximum_3();
				break;
			}
			case 30: {
				test = maximum_30(d_2, d_3);
				break;
			}
			// Case 2: x0 = lessons of (p, class) in the 1st pair.
			case 2: {
				test = maximum_2();
				break;
			}
			// Case 100: additional validation after the units of case 2 of each Teacher (maximum_100).
			case 100: {
				test = maximum_100(i);
				break;
			}

			// Case 50: code of the previous version of the call to Module 3 (uses g2_.preliminariesNEW1); no unit has case_ == 50, so
			// this block is not executed in this version. Kept for reference.
			case 50: {
				exec = 0;
				success = false;
				count = nPossibP[exec];
				imax = 50000;
				for (possib = 0; possib < count; possib++) {
					aux = g2_.preliminariesNEW1(q2_, possib, exec);
					if (aux != 0) {
						imax = Math.min(imax, aux);
						continue;
					}
					aux = g2_.main2(q2_, exec, possib);
					if (aux == 0) {
						success = true;
						break;
					}
					imax = Math.min(imax, aux);
				}
				if (success) {
					g2_.update4(0);
					System.out.println();
					System.out.println("Closed for exec = 0");
					exec = 1;
					success = false;
					count = nPossibP[exec];
					imax = 50000;
					for (possib = 0; possib < count; possib++) {
						aux = g2_.preliminariesNEW1(q2_0, possib, exec);
						if (aux != 0) {
							imax = Math.min(imax, aux);
							continue;
						}
						aux = g2_.main2(q2_0, exec, possib);
						if (aux == 0) {
							success = true;
							g2_.update4(1);
							break;
						}
						imax = Math.min(imax, aux);
					}
				}
				if (success)
					x0 = 0;
				else {
					p = g2_.getP(imax);
					a = g2_.getA(imax);
					i1_ = n2;
					do
						i1_--;
					while (Ap[i1_] != p || Aa[i1_] != a);
					System.out.print("i1_=" + i1_ + " ");
				}
				test = !success;
				break;
			}

			// Case 5: calls Module 3 (Module_3_V1) for each pair of days, trying the memorized possibilities of working days.
			// For each possibility, g2_.preliminaries1 first applies the quick supply/demand tests (cases 35); only if it passes does it execute
			//   g2_.main2.
			// If a possibility closes all the units, g2_.update4 writes the distribution of the two days of the pair into dd. If none closes, the
			//   backtrack goes to the unit
			// of case 2 (i1_) corresponding to the Teacher and the class where Module 3 failed (the earliest among the failures, min1).
			// (Note: the code also computes an average of i1_, but the value actually used is min1, because generator1 is always true.)
			// If no possibility passes the quick tests (successCount == 0), the program is terminated with an error message.
			case 5: {
				exec = 0;
				success = false;
				count = nPossibP[exec];
				successCount = 0;
				generator1 = true;
				min1 = 10000;
				max1 = -1;
				meday = 0;
				for (possib = 0; possib < count; possib++) {
					test = g2_.preliminaries1(q2_, possib, exec);
					if (!test)
						continue;
					successCount++;
					aux = g2_.main2(q2_, exec, possib);
					if (aux == 0) {
						success = true;
						break;
					}
					p = g2_.getP(aux);
					a = g2_.getA(aux);
					i1_ = n2;
					do
						i1_--;
					while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2);
					min1 = Math.min(min1, i1_);
					max1 = Math.max(max1, i1_);
					meday += i1_;
				}
				if (successCount == 0) {
					System.out.println("Error 01: successCount == 0");
					System.exit(0);
				}
				if (!success) {
					if (successCount <= 10)
						i1_ = min1;
					else
						i1_ = Math.floorDiv(meday, successCount);
					i1_ = min1;
					if (!generator1)
						i1_ = max1;
					System.out.print("i0_=" + i1_ + " ");
					test = true;
				} else {
					g2_.update4(0);
					System.out.println();
					System.out.println("Closed for exec = 0");
					exec = 1;
					success = false;
					count = nPossibP[exec];
					successCount = 0;
					min1 = 10000;
					max1 = -1;
					meday = 0;
					for (possib = 0; possib < count; possib++) {
						test = g2_.preliminaries1(q2_0, possib, exec);
						if (!test)
							continue;
						successCount++;
						aux = g2_.main2(q2_0, exec, possib);
						if (aux == 0) {
							success = true;
							g2_.update4(1);
							break;
						}
						p = g2_.getP(aux);
						a = g2_.getA(aux);
						i1_ = n2;
						do
							i1_--;
						while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2);
						min1 = Math.min(min1, i1_);
						max1 = Math.max(max1, i1_);
						meday += i1_;
					}
					if (successCount == 0) {
						System.out.println("Error 02: successCount == 0");
						System.exit(0);
					}
					if (!success) {
						if (successCount <= 10)
							i1_ = min1;
						else
							i1_ = Math.floorDiv(meday, successCount);
						i1_ = min1;
						if (!generator1)
							i1_ = max1;
						System.out.print("i1_=" + i1_ + " ");
					}
					x0 = 0;
					test = !success;
				}
				break;
			}
			}

			// 2) Success: updates the linking/control variables (cases 1, 10 and 2), stores x0, and, when passing through case 305, memorizes all
			//   the
			// possibilities of working days (detPossibP, for both pairs). Then advances and updates iMaxI_ (furthest unit already
			//   reached).
			if (!test) {
				switch (case_[i]) {
				case 1: {
					update_1(Np0, wp0, d_0, d_1, x_case_1, complex_p0);
					break;
				}
				case 10: {
					update_1(Np1, wp1, d_2, d_3, x_case_10, complex_p2);
					break;
				}
				case 2: {
					update_2();
					break;
				}
				}
				x_[i] = x0;
				// Passed case 305: the decisions of cases 1, 10, 3 and 30 are validated, so the possibilities of working days are enumerated.
				if (case_[i] == 305) {
					salvaI = i;
					nPossibP[0] = 0;
					detPossibP(0);
					nPossibP[1] = 0;
					detPossibP(1);
					i = salvaI;
					JOptionPane.showMessageDialog(null, "Possibility numbers determined.", "Result",
							JOptionPane.INFORMATION_MESSAGE);
				}
				i++;
				if (i > imax_) {
					imax_ = i;
					System.out.println("V2 " + i + "/" + n2 + " case_ " + case_[i] + " p= " + Ap[i]);
				}
				// All units closed (including the call to Module 3): complete distribution of the 4 days obtained.
				if (i > n2) {
					System.out.println((float) nIterations / 1000000.0f);
					System.out
							.println("Elapsed time (min): " + (System.currentTimeMillis() - initialTime) / 60000.0);
					return true;
				}
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				// 3) No successor: backtrack of Algorithm 2. p0 = z[i][iMaxI_[i] - i] is the unit to backtrack to; for cases 5 and 305, p0 = i1_
				//   (defined above).
				// If i == 1 or p0 == 0 the problem has no solution. Then all the units after p0 return to the generator and the search continues at p0.
				if (case_[i] == 5 || case_[i] == 305)
					p0 = i1_;
				else
					p0 = z[i][iMaxI_[i] - i];
				// End of the search: all the possibilities have been exhausted.
				if (i == 1 || p0 == 0) {
					System.out.println("Searched all the possibilities.");
					return false;
				}
				// Backtrack from the first unit of case 2: jumps to the unit of case 105 (the backtrack continues automatically from it).
				if (i == case_2) {
					p0 = i - 1;
					System.out.println("WARNING: Backtracking from case_ 2.");
					JOptionPane.showMessageDialog(null, "Backtracking from case_ 2.", "Result",
							JOptionPane.INFORMATION_MESSAGE);
					while (case_[p0] != 105)
						p0--;
				}
				for (i = i; i > p0; i--)
					x_[i] = generator;
				i = p0;
			}
		} while (true);
	}

	// dependency(i,j) (Chapter 3 and Section 4.3.5): true if the decision variable x[j] depends on x[i]. There is no dependency if j <= i;
	// cases 3/35 do not influence cases 30/305 (and vice versa); two units of case 2 depend on each other only if they have the same
	//   Teacher or the same class; the unit of case 100 depends only on the units of case 2, and no unit depends on a unit of
	//   case 100;
	// in the other cases a dependency is assumed to exist.
	public boolean dependency(int i, int j) {
		if (j <= i)
			return false;
		if (case_[j] == 100)
			return case_[i] == 2;
		if (case_[i] == 100)
			return false;
		if ((case_[i] == 3 || case_[i] == 35) && (case_[j] == 30 || case_[j] == 305))
			return false;
		if (case_[i] == 2 && case_[j] == 2)
			return Ap[i] == Ap[j] || Aa[i] == Aa[j];
		else
			return true;
	}

	// Variant that considers all units dependent (not used in this class).
	public boolean dependency0(int i, int j) {
		return true;
	}

	// Computes the array z of Algorithm 2 (method determineArranjo of Chapter 3): z[i][l] = largest unit p < i on which some unit from
	// i to i+l depends (according to dependency), made cumulative in l. It says where to backtrack when the search does not advance beyond i+l.
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
}
