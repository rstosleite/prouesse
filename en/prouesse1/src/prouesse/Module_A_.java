package prouesse;

// Class Module_A_ - counts, for a Teacher, the number of possibilities of distributing his/her lessons between day 0 and the other days
// (Sections 4.6.2 and 4.7 of the book, Teacher ordering feature). It is used by Module_1B.
//
// Method: the same search scheme as the other modules (Chapter 3, Algorithm 1, generator = -1), but without stopping at the first solution:
// each time all the units are closed the solution is counted and the search countinues (see main_0), so that the
// result is the total number of possibilities. All members are static; the units of Teacher p are:
//   cases 10 (5 units): the Teacher works (1) or not (0) on each of the 5 days;
//   case 100: ns0 = total number of lessons of the Teacher on day 0 (the remainder, ns1, occurs on days 1 to 4);
//   case 11 : validation, which also eliminates repetitions of the pair (ns0, maxDays0);
//   case 2  : for each class of the Teacher, how many of his/her lessons occur on day 0.
// Note: the counter uses the shared static unit arrays (case_, Aa, A, x_ ...); Module_1B rebuilds its
// own units only after counting the possibilities of all Teachers.
public class Module_A_ extends PhaseA1_Super {
	// Private fields: p = Teacher under analysis; n2 = number of units; maxDays0/maxDays1 = working days on day 0 and on days 1 to 4;
	// nPeriods_P0/nPeriods_P1 = maximum number of lessons on day 0 and on days 1 to 4; ns0/ns1 = lessons of the Teacher on day 0 and on days 1 to 4;
	// ns_p_ = weekly workload of the Teacher; Sp2_/Sp3_/pointerP = accumulated totals of lessons in the two groups of days and pointers;
	// possibP/nPossibP = pairs (ns0, maxDays0) already counted (to avoid counting twice); test2 = timerary flag.
	static private int d1, n2, p, maxDays0, maxDays1, Sp2_[], Sp3_[], pointerP[], nPeriods_P0, nPeriods_P1, ns0, ns1,
			ns_p_, nPossibP, possibP[][];
	static private boolean test2;

	public Module_A_() {
	}

	// Prepares the counting for Teacher p0: computes the weekly workload and builds the units (5 of case 10, one of case 100,
	// one of case 11 and one of case 2 per class) and the pointers of the control variables.
	static public void preliminaries(int p0) {
		// Small arrays, sufficient for a single Teacher (the dimensions allow up to 30 classes).
		Sp2_ = new int[42];
		Sp3_ = new int[42];
		pointerP = new int[42];
		possibP = new int[45][2];
		p = p0;
		ns_p_ = 0;
		for (j = 1; j <= nClassesP[p]; j++)
			ns_p_ += ns[p][j];
		nPossibP = 0;
		k = 0;
		// Units of case 10: one for each day of the week (indices 1 to 5 correspond to days 0 to 4).
		for (d1 = 0; d1 <= 4; d1++) {
			k++;
			case_[k] = 10;
		}
		k++;
		// Unit of case 100 (lessons of the Teacher on day 0), followed by the unit of case 11 (validation).
		case_[k] = 100;
		k++;
		case_[k] = 11;
		for (j = 1; j <= nClassesP[p]; j++) {
			k++;
			Aa[k] = list[p][j];
			j_[k] = j;
			A[k] = ns[p][j];
			// Units of case 2: one per class of the Teacher (A = weekly workload in the class).
			case_[k] = 2;
		}
		n2 = k;

		// Pointers pointerP of the units of case 2 into the arrays Sp2_/Sp3_ (position 0 is 0: accumulated totals before the 1st class).
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				pointerP[j] = aux2;
				aux2++;
			}
		}
	}

	// Equivalent to the method 'successr': looks for the smallest x0 > x_[i] that satisfies the constraints of unit i.
	// Returns false if successful (value in x0) and true if there is no successor.
	static public boolean maximum_() {
		int aux0, aux10, aux11, minX0, maxX0, nDays, sum0;
		boolean case_Trivial, test;
		x0 = x_[i];
		switch (case_[i]) {
		// Case 10 (day i-1): x0 = 0 or 1 (does not work / works). Computes nDays = working days already decided and sum0 = sum of the
		// maximum numbers of lessons of the days already chosen as working days plus those of all the days still to be decided.
		case 10: {
			if (x0 >= 1)
				return true;
			nDays = 0;
			sum0 = 0;
			for (k = 1; k <= i - 1; k++) {
				nDays += x_[k];
				sum0 += (x_[k] == 0 ? 0 : Mp[p][k - 1]);
			}
			for (k = i + 1; k <= 5; k++)
				sum0 += Mp[p][k - 1];
			while (true) {
				x0++;
				// Rejects x0 > 1 and excess working days; accepts the value if the capacity sum0 (plus day i-1, if worked) covers the
				//   workload.
				if (x0 > 1 || nDays + x0 > Wp0[p])
					return true;
				test = sum0 + (x0 == 0 ? 0 : Mp[p][i - 1]) >= ns_p_;
				if (test)
					return false;
			}
		}

		// Case 100: x0 = ns0 = total number of lessons on day 0. Capacity of day 0 (nPeriods_P0) and of days 1 to 4 (nPeriods_P1) given by the chosen
		// working days. Minimum: what does not fit in days 1 to 4; maximum: what fits on day 0 (and the workload).
		case 100: {
			nPeriods_P0 = x_[1] * Mp[p][0];
			nPeriods_P1 = x_[2] * Mp[p][1] + x_[3] * Mp[p][2] + x_[4] * Mp[p][3] + x_[5] * Mp[p][4];
			minX0 = Math.max(0, ns_p_ - nPeriods_P1);
			minX0 = Math.max(x0 + 1, minX0);
			maxX0 = Math.min(ns_p_, nPeriods_P0);
			if (maxX0 < minX0)
				return true;
			x0 = minX0;
			ns0 = x0;
			ns1 = ns_p_ - x0;
			return false;

		}

		// Case 11: validation executed once for each combination of the previous cases. maxDays0 = does the Teacher work on day 0?; maxDays1 = remaining
		//   days.
		// If the pair (ns0, maxDays0) has already been considered, returns true (does not count a duplicate); otherwise records it in possibP.
		case 11: {
			if (x0 > -1)
				return true;
			maxDays0 = x_[1];
			maxDays1 = Wp0[p] - maxDays0;
			test2 = false;
			if (nPossibP > 0) {
				for (k = 0; k <= nPossibP - 1; k++) {
					test2 = ns0 == possibP[k][0] && maxDays0 == possibP[k][1];
					if (test2)
						break;
				}
			}
			if (!test2) {
				k = nPossibP;
				nPossibP++;
				possibP[k][0] = ns0;
				possibP[k][1] = maxDays0;
			} else
				return true;
			// Estimate of the maximum number of lessons on day 0 and on days 1 to 4 (1 or 2 lessons per day and class); fails if, limited by ns0 and ns1, it does not cover
			//   the workload.
			aux10 = 0;
			aux11 = 0;
			aux0 = nClassesP[p];
			for (j = 1; j <= aux0; j++) {
				aux10 += Math.min((noDoublePeriodsP[p] ? maxDays0 : 2 * maxDays0), ns[p][j]);
				aux11 += Math.min((noDoublePeriodsP[p] ? maxDays1 : 2 * maxDays1), ns[p][j]);
			}
			x0 = 0;
			return Math.min(aux10, ns0) + Math.min(aux11, ns1) < ns_p_;
		}

		// Case 2: x0 = lessons of the class on day 0. Bounds: upper = min(2*working days on day 0 (or 1), lessons of the class, ns0 - total already
		//   assigned to the Teacher on day 0);
		// lower = what does not fit in days 1 to 4 (analogous to Case 2 of Module 2, Section 4.3.5, with 'ns0' and 'ns1' in place of N_0p and N_1p).
		case 2: {
			if (ns0 == x0)
				return true;
			case_Trivial = ns0 == 0 || ns1 == 0;
			if (case_Trivial && x0 > -1)
				return true;
			a = Aa[i];
			n0 = A[i];
			Sp0 = Sp2_[pointerP[i] - 1];
			Sp2 = Sp3_[pointerP[i] - 1];
			if (case_Trivial)
				x0 = (ns0 == 0 ? 0 : n0);
			else
				x0++;
			maxX0 = (noDoublePeriodsP[p] ? maxDays0 : 2 * maxDays0);
			if (x0 > maxX0)
				return true;
			maxX0 = Math.min(maxX0, n0);
			maxX0 = Math.min(maxX0, ns0 - Sp0);

			minX0 = (noDoublePeriodsP[p] ? n0 - maxDays1 : n0 - 2 * maxDays1);
			minX0 = Math.max(minX0, Sp2 + n0 - ns1);
			x0 = Math.max(x0, minX0);
			return x0 > maxX0;
		}
		}
		return true;
	}

	// Updates the accumulated totals (Sp2_ on day 0 and Sp3_ on the other days) after a unit of case 2.
	static public void update2() {
		int aux;
		if (case_[i] == 2) {
			a = Aa[i];
			nLessons = A[i] - x0;
			aux = pointerP[i];
			Sp2_[aux] = Sp2_[aux - 1] + x0;
			Sp3_[aux] = Sp3_[aux - 1] + nLessons;
		}
	}

	// Goes through all possibilities by backtracking search (Algorithm 1) and returns how many were found (count_0).
	// When the last unit is closed the solution is counted and the search simply countinues from it (i = n2).
	static public int main_0() {
		int generator, count_0;
		count_0 = 0;
		generator = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		do {
			if (!maximum_()) {
				x_[i] = x0;
				update2();
				i++;
				if (i > n2) {
					i = n2;
					count_0++;
				}
			} else {
				if (i == 1)
					return count_0;
				x_[i] = generator;
				i--;
			}
		} while (true);
	}

}
