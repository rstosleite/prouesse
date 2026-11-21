package prouesse;

// Class Module_A - support for the ordering of the Teachers (Section 5.4.3, item a, and Section 4.6.2 of the book).
//
// Role: for a Teacher p, counts in how many ways the lessons of p can be distributed between the two parts of the week
// (days d0 and d1 versus d2, d3 and d4), considering all of their classes. Module 2A uses this count (method main_0)
// to sort the Teachers in increasing order of possibilities. The count is made by an exhaustive search with the same
// search algorithm of Chapter 3, without intelligent backtracking: when all units are closed it counts one solution and countinues.
// Units (case_[i]): five of Case 10 (x = 1 if p works on day i - 1, 0 if not), one of Case 100 (workload of p
// in the 1st part: ns0; the rest is ns1), one of Case 11 (validation) and one of Case 2 per class of p (lessons of the class in the 1st part).
public class Module_A extends PhaseA1_Super {

	// Sp2_, Sp3_ and pointerP: accumulators of the Teacher's lessons in the two parts (as in Module 2A); ns_p_: weekly load of p;
	// ns0 and ns1: lessons of p in the 1st and 2nd parts; maxDays0 and maxDays1: maximum working days of p in the two parts;
	// possibP[][]: pairs (ns0, maxDays0) already counted (avoids counting the same combination twice); nPossibP: their number.
	static private int d1, n2, p, maxDays0, maxDays1, Sp2_[], Sp3_[], pointerP[], nPeriods_P0, nPeriods_P1, ns0, ns1,
			ns_p_, nPossibP, possibP[][];
	static private boolean test2;

	// Empty constructor: the class has only static methods.
	public Module_A() {
	}

	// Prepares the count for Teacher p0: computes the weekly load and builds the units.
	static public void preliminaries(int p0) {
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
		// Units of Case 10: one for each weekday (working days of p).
		for (d1 = 0; d1 <= 4; d1++) {
			k++;
			case_[k] = 10;
		}
		// Unit of Case 100: splits the workload of p between the 1st and 2nd parts.
		k++;
		case_[k] = 100;
		// Unit of Case 11: validates the previous choices and avoids repeated combinations.
		k++;
		case_[k] = 11;
		// Units of Case 2: one for each class of p, with A[k] = ns[p][j] lessons to be split between the two parts.
		for (j = 1; j <= nClassesP[p]; j++) {
			k++;
			Aa[k] = list[p][j];
			j_[k] = j;
			A[k] = ns[p][j];
			case_[k] = 2;
		}
		n2 = k;

		// Pointers to the accumulated sums of p's lessons in the two parts (only for the units of Case 2).
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

	// Successor of unit i. Returns false if it found the successor of x_[i] (value in x0) and true if none exists.
	static public boolean maximum_() {
		int aux0, aux10, aux11, minX0, maxX0, nDays, sum0;
		boolean case_Trivial, test;
		x0 = x_[i];
		switch (case_[i]) {
		// Case 10: x0 = 1 if p works on day i - 1. The number of working days cannot exceed Wp0[p] and the capacity of the
		// days already chosen plus that of all following days must cover the load ns_p_.
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
				if (x0 > 1 || nDays + x0 > Wp0[p])
					return true;
				test = sum0 + (x0 == 0 ? 0 : Mp[p][i - 1]) >= ns_p_;
				if (test)
					return false;
			}
		}

		// Case 100: x0 = ns0, lessons of p in the 1st part. Minimum: what does not fit in the 2nd part; maximum: what fits in the 1st part,
		// according to the working days chosen in the units of Case 10.
		case 100: {
			nPeriods_P0 = x_[1] * Mp[p][0] + x_[2] * Mp[p][1];
			nPeriods_P1 = x_[3] * Mp[p][2] + x_[4] * Mp[p][3] + x_[5] * Mp[p][4];
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

		// Case 11: defines maxDays0 (working days in the 1st part) and maxDays1 (rest of Wp0[p]). If the combination (ns0, maxDays0) has already
		// been counted, the unit fails (so as not to count it again). It also fails if the lessons of the classes cannot be split
		// according to ns0, ns1, maxDays0 and maxDays1.
		case 11: {
			if (x0 > -1)
				return true;
			maxDays0 = x_[1] + x_[2];
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

		// Case 2: x0 = lessons of the class in the 1st part, with A[i] - x0 in the 2nd part, respecting ns0, ns1 and the number of lessons per day
		// (1 or 2). Trivial case: ns0 or ns1 is zero and all lessons go in a single part.
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

	// Updates the accumulated lessons of the Teacher in the two parts after x0 is accepted (Case 2).
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

	// Counts, by exhaustive search, the possible distributions of Teacher p (each complete solution counts one) and returns the number.
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
				// Complete solution: counts it and countinues the search (goes back to the last unit).
				if (i > n2) {
					i = n2;
					count_0++;
				}
			} else {
				// End of the search: there are no more solutions.
				if (i == 1)
					return count_0;
				x_[i] = generator;
				i--;
			}
		} while (true);
	}

}
