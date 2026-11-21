package prouesse;

// Class Module_A - provides input for the ordering of the Teachers (Section 4.6.2 of the book): counts, for a Teacher, the number of
// possibilities of distributing his/her remaining lessons between the pairs of days (d0,d1) and (d2,d3). It is used by Module_2A and Module_2B.
//
// It works like Module_A_, but applied after Module 1: only the lessons of the four remaining days (days 1 to 4) are considered,
// that is, ns - dd[..][0]. Day 0 has already been decided and is fixed as 'no unit' (x_[1] = 0); the search is performed over days 2 to 5
// (indices 2 to 5 of x_, which correspond to days 1 to 4). Units: cases 10 (working days), 100 (lessons in the 1st pair), 11 (validation)
//   and 2.
public class Module_A extends PhaseA1_Super {
	// Private fields, with the same meaning as in Module_A_ (ns0/ns1 become the lessons of the 1st and of the 2nd pair of days; Wp01 = maximum number of
	//   working days in the 4 days).
	static private int d1, n2, p, maxDays0, maxDays1, Sp2_[], Sp3_[], pointerP[], nPeriods_P0, nPeriods_P1, ns0, ns1,
			ns_p_, nPossibP, possibP[][], Wp01;
	static private boolean test2;

	public Module_A() {
	}

	// Prepares the counting for Teacher p0, with Wp0_ = W_1p (maximum number of working days in the 4 days). The workload considered is
	// that of the 4-day period (ns minus the lessons of day 0); the units are the same as in Module_A_.
	static public void preliminaries(int p0, int Wp0_) {
		Sp2_ = new int[42];
		Sp3_ = new int[42];
		pointerP = new int[42];
		possibP = new int[45][2];
		p = p0;
		Wp01 = Wp0_;
		ns_p_ = 0;
		for (j = 1; j <= nClassesP[p]; j++)
			// Remaining workload (weekly minus that of day 0, decided by Module 1).
			ns_p_ += ns[p][j] - dd[p][j][0];
		nPossibP = 0;
		k = 0;
		for (d1 = 0; d1 <= 4; d1++) {
			k++;
			case_[k] = 10;
		}
		k++;
		case_[k] = 100;
		k++;
		case_[k] = 11;
		for (j = 1; j <= nClassesP[p]; j++) {
			k++;
			Aa[k] = list[p][j];
			j_[k] = j;
			// Lessons of the class in the 4 days (A) = workload of the class minus those of day 0.
			A[k] = ns[p][j] - dd[p][j][0];
			case_[k] = 2;
		}
		n2 = k;

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

	// Equivalent to the method 'successr' (returns false if there is a successor, in x0; true otherwise).
	static public boolean maximum_() {
		int aux0, aux10, aux11, minX0, maxX0, nDays, sum0;
		boolean case_Trivial, test;
		x0 = x_[i];
		switch (case_[i]) {
		// Case 10 (one unit per day): x0 = 0 or 1 (the Teacher does not work / works on the corresponding day), respecting Wp01 and the capacity
		//   for the workload.
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
				if (x0 > 1 || nDays + x0 > Wp01)
					return true;
				test = sum0 + (x0 == 0 ? 0 : Mp[p][i - 1]) >= ns_p_;
				if (test)
					return false;
			}
		}

		// Case 100: x0 = ns0 = total number of lessons in the 1st pair of days (d0,d1) = days 1 and 2; nPeriods_P1 = capacity of the 2nd pair (days 3 and 4).
		// Minimum: what does not fit in the 2nd pair; maximum: what fits in the 1st pair.
		case 100: {
			nPeriods_P0 = x_[2] * Mp[p][1] + x_[3] * Mp[p][2];
			nPeriods_P1 = x_[4] * Mp[p][3] + x_[5] * Mp[p][4];
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

		// Case 11: validation and elimination of duplicates. maxDays0 = working days in the 1st pair (x_[2] + x_[3]); maxDays1 = the remainder.
		// Discards pairs (ns0, maxDays0) already counted; checks whether the lessons of the classes, limited to 1 or 2 per day, cover the workload.
		case 11: {
			if (x0 > -1)
				return true;
			maxDays0 = x_[2] + x_[3];
			maxDays1 = Wp01 - maxDays0;
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
			aux10 = 0;
			aux11 = 0;
			aux0 = nClassesP[p];
			for (j = 1; j <= aux0; j++) {
				aux10 += Math.min((noDoublePeriodsP[p] ? maxDays0 : 2 * maxDays0), ns[p][j] - dd[p][j][0]);
				aux11 += Math.min((noDoublePeriodsP[p] ? maxDays1 : 2 * maxDays1), ns[p][j] - dd[p][j][0]);
			}
			x0 = 0;
			return Math.min(aux10, ns0) + Math.min(aux11, ns1) < ns_p_;
		}

		// Case 2: x0 = lessons of the class in the 1st pair of days; same constraints as Case 2 of Module 2 (Section 4.3.5), using ns0 and ns1 as limits
		//   on the lessons per pair.
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

	// Updates the Teacher's accumulated totals in the two pairs of days after a unit of case 2.
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

	// Counts the possibilities by backtracking search, without stopping at the first one (as in Module_A_). Returns 0 if the Teacher has no lessons in the 4
	//   days.
	// x_[1] = 0 fixes day 0 as already decided; the search starts at unit 2.
	static public int main_0() {
		int generator, count_0;
		if (ns_p_ == 0)
			return 0;
		count_0 = 0;
		generator = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		x_[1] = 0;
		i = 2;
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
				if (i == 2)
					return count_0;
				x_[i] = generator;
				i--;
			}
		} while (true);
	}

}
