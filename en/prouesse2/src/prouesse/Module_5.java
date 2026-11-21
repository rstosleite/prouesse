package prouesse;

// Class Module_5 - implements Module 5 (Section 4.1.5 of the book).
//
// Role: given a complete distribution in dd, allocates the lessons of one weekday to the time slots of the day (1 to Ma). For
// each pair (Teacher p, class a) with lessons on the day (1 or 2 lessons), each unit i decides x[i], the time slot of the (first) lesson;
// if there are two lessons, they occupy the consecutive time slots x[i] and x[i] + 1 (double lessons). Constraints: a Teacher cannot
// have two lessons in the same time slot, nor lessons in unavailable time slots (unPeriods), and a class cannot have two lessons in the same time slot.
// It is called by Module 4 (and also by Double_Periods) to finalize the timetables of the days. It uses Algorithm 2 of Chapter 3
// (main method) with 'generator' equal to 0 (the time slots are numbered from 1).
public class Module_5 extends Probl {

	// imax: most advanced unit reached by the search; xi, aux1 and x0: time-slot auxiliaries.
	private int imax, p, a, i, a1, p1, h1, j1, xi, aux1, x0;
	// unavailableSlotP2[p][h] and unavailableSlotA2[a][h]: time slot h is occupied (or unavailable) for Teacher p and for class a, according to the
	// units already closed; unavailableSlotP1 and unavailableSlotA1: timerary copies used when looking for the next time slot; presentP[p]: p has lessons on the day.
	private boolean doublePeriodLesson, unavailableSlotP2[][], unavailableSlotA2[][], test, unavailableSlotP1[], unavailableSlotA1[], presentP[];

	// Constructor: allocates the arrays that mark occupied time slots.
	public Module_5() {
		presentP = new boolean[Qp + 1];
		unavailableSlotP1 = new boolean[Ma + 1];
		unavailableSlotA1 = new boolean[Ma + 1];
		unavailableSlotP2 = new boolean[Qp + 1][Ma + 1];
		unavailableSlotA2 = new boolean[Qa + 1][Ma + 1];
	}

	// Prepares the allocation of day d. Returns the number of units n (one for each (Teacher, class) pair with lessons on the day) or 0 if the
	// allocation is impossible (not enough available Teachers in some time slot).
	public int preliminaries(int d) {
		int n, i1, aux0, j, k;
		// presentP[p]: p has at least one lesson on day d.
		for (p = 1; p <= Qp; p++) {
			presentP[p] = false;
			for (j = 1; j <= nClassesP[p]; j++) {
				if (dd[p][j][d] > 0) {
					presentP[p] = true;
					break;
				}
			}
		}
		// Preliminary check: in each time slot k, the number of present and available Teachers must be at least Qa (one lesson per class).
		for (k = 1; k <= Ma; k++) {
			aux0 = 0;
			for (p = 1; p <= Qp; p++) {
				if (presentP[p] && !unPeriods[p][d][k])
					aux0++;
			}
			if (aux0 < Qa)
				return 0;
		}

		// Initial state of the time slots: for each Teacher, the time slots unavailable on the day (unPeriods) are occupied; no class time slot is occupied.
		for (h1 = 1; h1 <= Ma; h1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				unavailableSlotP2[p1][h1] = unPeriods[p1][d][h1];
			for (a1 = 1; a1 <= Qa; a1++)
				unavailableSlotA2[a1][h1] = false;
		}

		// Builds the units, in the order of the Teachers d2_: Ap, Aa and A[n] = number of lessons of the pair on the day (1 or 2).
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

	// Successor of unit i: looks for the smallest time slot x0 > x_[i] in which the lessons of the pair can be allocated. Returns false if
	// it found one (value in x0) and true if none exists.
	public boolean maximumXi_() {
		p = Ap[i];
		a = Aa[i];
		// If the pair has two lessons, they need two consecutive time slots.
		doublePeriodLesson = A[i] > 1;
		x0 = x_[i];
		// Copies, for the time slots after x0, the current occupation of the Teacher and of the class.
		for (h1 = x0 + 1; h1 <= Ma; h1++) {
			unavailableSlotP1[h1] = unavailableSlotP2[p][h1];
			unavailableSlotA1[h1] = unavailableSlotA2[a][h1];
		}
		// Frees time slot x0 + 1, which belongs to this unit's own previous allocation (double-lesson pair).
		if (x0 != 0 && doublePeriodLesson) {
			unavailableSlotP1[x0 + 1] = false;
			unavailableSlotA1[x0 + 1] = false;
		}
		// Last time slot in which the lesson may start: Ma - 1 if there are two lessons, otherwise Ma.
		aux1 = (doublePeriodLesson ? Ma - 1 : Ma);
		test = true;
		// Advances x0 to a time slot that is free for the Teacher and for the class (and, for two lessons, also in the following time slot).
		while (test && x0 < aux1) {
			x0++;
			test = unavailableSlotP1[x0] || unavailableSlotA1[x0] || (doublePeriodLesson && (unavailableSlotP1[x0 + 1] || unavailableSlotA1[x0 + 1]));
		}
		return test;
	}

	// Teacher of the most advanced unit reached by the search (for Module 4 to decide the backtracking).
	public int getP() {
		return (Ap[imax]);
	}

	// Class of the most advanced unit reached.
	public int getA() {
		return (Aa[imax]);
	}

	// Frees the time slots that unit i occupied (before being changed or abandoned).
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

	// Marks time slot x0 (and x0 + 1 if there are two lessons) as occupied for the Teacher and for the class.
	public void update2_(int i) {
		unavailableSlotP2[Ap[i]][x0] = true;
		unavailableSlotA2[Aa[i]][x0] = true;
		if (A[i] > 1) {
			unavailableSlotP2[Ap[i]][x0 + 1] = true;
			unavailableSlotA2[Aa[i]][x0 + 1] = true;
		}

	}

	// Writes the result of day d into the timetables: tableP[p][d][h] = class and tableA[a][d][h] = Teacher.
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

	// Algorithm 2 of Chapter 3. Returns true if all lessons of the day were allocated and false if it is not possible.
	public boolean main(int n2) {
		int p0, generator, k;
		imax = 0;
		// The 'generator' is 0 (no time slot chosen).
		generator = 0;
		for (i = 1; i <= n2; i++)
			x_[i] = generator;
		i = 1;
		iMaxI_[1] = 1;
		do {
			// Success on the unit: frees the previous time slot, records x0, marks the new time slot and advances.
			if (!this.maximumXi_()) {
				this.update3_(i);
				x_[i] = x0;
				this.update2_(i);
				i++;
				if (i > imax)
					imax = i;
				// All units closed.
				if (i > n2)
					return true;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Failure: backtracks to z[i][iMaxI_[i] - i], freeing the time slots of the abandoned units.
			} else {
				if (i == 1)
					return false;
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

	// Computes z[i][l] (Chapter 3): unit j depends on unit i if they have the same Teacher or the same class; then accumulates the maximum along l.
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
