package prouesse;

// Class Module_5 - implements Module 5 (Section 4.1.5 of the book): allocates to the time slots of a day the lessons of that day.
//
// Input: the complete distribution in dd (number of lessons of each Teacher and class on day d). For each day, the module decides in which
//   time slots
// (1 to Ma) each lesson takes place. Each unit i is a pair (Teacher Ap[i], class Aa[i]) with A[i] lessons on the day (1 or 2); x[i] is the time slot of the
// (first) lesson; if A[i] == 2 the two lessons are double lessons and occupy time slots x[i] and x[i]+1.
// Constraints: a Teacher cannot teach two lessons in the same time slot, nor can a class attend two lessons in the same time slot; a Teacher cannot
// be allocated to unavailable time slots (unPeriods). The search is Algorithm 2 of Chapter 3 (main), with dependency between
// units of the same Teacher or the same class (determineZ). If the search fails, getP/getA report the Teacher and the class where the module
// could not advance, so that Module 4 can backtrack to the corresponding unit. Results: tableP and tableA (result).
// Uses the unit arrays of Probl (Ap, Aa, A, x_, z, iMaxI_).
public class Module_5 extends Probl {
	// imax = furthest unit reached;  p, a, i = current Teacher, class and unit;  x0 = candidate time slot for unit i.
	// unavailableSlotP2[p][h] = true if Teacher p is busy or unavailable in time slot h;  unavailableSlotA2[a][h] = true if class a is busy in
	//   time slot h.
	// unavailableSlotP1[h], unavailableSlotA1[h] = working copies for the Teacher and the class of the current unit;  presentP[p] = the Teacher has lessons on the day.
	private int imax, p, a, i, a1, p1, h1, j1, xi, aux1, x0;
	private boolean doublePeriodLesson, unavailableSlotP2[][], unavailableSlotA2[][], test, unavailableSlotP1[], unavailableSlotA1[], presentP[];

	// Constructor: allocates the occupancy markers (per Teacher, per class and per time slot).
	public Module_5() {
		presentP = new boolean[Qp + 1];
		unavailableSlotP1 = new boolean[Ma + 1];
		unavailableSlotA1 = new boolean[Ma + 1];
		unavailableSlotP2 = new boolean[Qp + 1][Ma + 1];
		unavailableSlotA2 = new boolean[Qa + 1][Ma + 1];
	}

	// Prepares the allocation of day d. Returns the number n of units (Teacher/class pairs with lessons on the day) or 0 if it is already known to be
	//   impossible.
	public int preliminaries(int d) {
		int n, i1, aux0, j, k;
		for (p = 1; p <= Qp; p++) {
			// 1) Marks the Teachers who have lessons on day d.
			presentP[p] = false;
			for (j = 1; j <= nClassesP[p]; j++) {
				if (dd[p][j][d] > 0) {
					presentP[p] = true;
					break;
				}
			}
		}
		// 2) Preliminary check: in each time slot of the day there must be at least Qa Teachers present and available (one per class); if not,
		//   returns 0.
		for (k = 1; k <= Ma; k++) {
			aux0 = 0;
			for (p = 1; p <= Qp; p++) {
				if (presentP[p] && !unPeriods[p][d][k])
					aux0++;
			}
			if (aux0 < Qa)
				return 0;
		}

		for (h1 = 1; h1 <= Ma; h1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				// 3) Initial state: the Teacher's unavailable time slots count as occupied; the classes start out free.
				unavailableSlotP2[p1][h1] = unPeriods[p1][d][h1];
			for (a1 = 1; a1 <= Qa; a1++)
				unavailableSlotA2[a1][h1] = false;
		}

		// 4) Builds the units, Teacher by Teacher (in the order d2_) and, for each one, his/her classes with lessons on the day (A = number of lessons).
		n = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p1 = d2_[i1];
			for (j1 = 1; j1 <= nClassesP[p1]; j1++) {
				if (dd[p1][j1][d] > 0) {
					n++;
					Ap[n] = p1;
					Aa[n] = list[p1][j1];
					A[n] = dd[p1][j1][d];
				}
			}
		}
		return n;
	}

	// Equivalent to the 'successr': looks for the smallest time slot x0 > x_[i] in which unit i can be allocated. Returns false if one was found (in x0)
	// and true otherwise. doublePeriodLesson = the unit has 2 lessons, which require two consecutive free time slots.
	public boolean maximumXi_() {
		p = Ap[i];
		a = Aa[i];
		doublePeriodLesson = A[i] > 1;
		x0 = x_[i];
		// Copies, for the candidate time slots, the occupancy of the Teacher and of the class.
		for (h1 = x0 + 1; h1 <= Ma; h1++) {
			unavailableSlotP1[h1] = unavailableSlotP2[p][h1];
			unavailableSlotA1[h1] = unavailableSlotA2[a][h1];
		}
		// If the unit was already allocated at x0, the time slot x0+1 occupied by the unit itself is freed in the copy (time slot x0 is not a candidate).
		if (x0 != 0 && doublePeriodLesson) {
			unavailableSlotP1[x0 + 1] = false;
			unavailableSlotA1[x0 + 1] = false;
		}
		// Last possible starting time slot: Ma (single lesson) or Ma-1 (double lesson, which also uses the following time slot).
		aux1 = (doublePeriodLesson ? Ma - 1 : Ma);
		test = true;
		// Advances until a free time slot is found for the Teacher and the class (and also the following one, if double); the test remains true if there is none.
		while (test && x0 < aux1) {
			x0++;
			test = unavailableSlotP1[x0] || unavailableSlotA1[x0] || (doublePeriodLesson && (unavailableSlotP1[x0 + 1] || unavailableSlotA1[x0 + 1]));
		}
		return test;
	}

	// getP and getA: Teacher and class of the furthest unit reached (imax), where the allocation failed.
	public int getP() {
		return (Ap[imax]);
	}

	public int getA() {
		return (Aa[imax]);
	}

	// Frees the time slots occupied by unit i (undoes the marking made by update2_).
	public void update3_(int i) {
		xi = x_[i];
		if (xi != 0) {
			unavailableSlotP2[Ap[i]][xi] = false;
			unavailableSlotA2[Aa[i]][xi] = false;
			if (A[i] > 1) {
				unavailableSlotP2[Ap[i]][xi + 1] = false;
				unavailableSlotA2[Aa[i]][xi + 1] = false;
			}
		}
	}

	// Marks as occupied, for the Teacher and for the class, the chosen time slot x0 (and the following one, if double).
	public void update2_(int i) {
		unavailableSlotP2[Ap[i]][x0] = true;
		unavailableSlotA2[Aa[i]][x0] = true;
		if (A[i] > 1) {
			unavailableSlotP2[Ap[i]][x0 + 1] = true;
			unavailableSlotA2[Aa[i]][x0 + 1] = true;
		}

	}

	// Writes the results of day d: tableP[p][d][h] = class and tableA[a][d][h] = Teacher for each time slot h occupied by the lessons of the n
	//   units.
	public void result(int n, int d) {
		for (i = 1; i <= n; i++) {
			p = Ap[i];
			a = Aa[i];
			tableP[p][d][x_[i]] = a;
			tableA[a][d][x_[i]] = p;
			if (A[i] > 1) {
				tableP[p][d][x_[i] + 1] = a;
				tableA[a][d][x_[i] + 1] = p;
			}
		}
	}

	// Search of Module 5 (Algorithm 2 of Chapter 3). x_[i] = 0 means 'not yet allocated' (generator = generator = 0). Returns true if the n2
	//   units
	// have been allocated to the time slots of the day; false otherwise.
	public boolean main(int n2) {
		int p0, generator, k;
		imax = 0;
		generator = 0;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		iMaxI_[1] = 1;
		do {
			// With a free time slot: frees the unit's previous marking, stores x0, marks the new time slot and advances (updating iMaxI_).
			if (!this.maximumXi_()) {
				this.update3_(i);
				x_[i] = x0;
				this.update2_(i);
				i++;
				if (i > imax)
					imax = i;
				if (i > n2)
					return true;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				if (i == 1)
					return false;
				// No time slot: backtracks to the unit p0 indicated by z, freeing the time slots of the units after p0; if there is nowhere to backtrack, it fails.
				p0 = z[i][iMaxI_[i] - i];
				if (p0 == 0)
					return false;
				for (i = i; i > p0; i--) {
					if (x_[i] != generator) {
						this.update3_(i);
						x_[i] = generator;
					}
				}
				i = p0;
			}
		} while (true);
	}

	// Computes the array z (Chapter 3) for the dependency of Module 5: z[i][l] = largest unit preceding i with the same Teacher or the same
	// class as some unit from i to i+l, made cumulative in l.
	public void determineZ(int n_) {
		int p, i, l;
		for (i = 2; i <= n_; i++) {
			for (l = 0; l <= n_ - i; l++) {
				p = i - 1;
				while (p > 0 && Ap[p] != Ap[i + l] && Aa[p] != Aa[i + l])
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
