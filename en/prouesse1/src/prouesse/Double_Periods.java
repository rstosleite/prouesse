package prouesse;

// Class Double_Periods - inserts double lessons into the final results (Sections 4.6.3 and 4.7.2 of the book).
//
// After Phases 2 and 3 have completed the timetables, for each Teacher p with withDoublePeriodsP[p] == true, an attempt is made to create, in each
// class a of p that does not yet have double lessons, a day with two consecutive lessons of p in that class. For this, a swap is made
//   that
// involves a second Teacher pL (who also teaches class a), a second class aL (taught by p and by pL) and a second day d3:
//                     class a         class aL
//    Teacher p          d3-   d+        d-    d3+
//    Teacher pL         d-    d3+       d3-   d+
// ('+' adds and '-' removes a lesson on the indicated day; d is the day on which p comes to have two lessons in a.) Each Teacher and each class
// keeps the same total number of lessons on d and on d3, so the distribution remains valid. After the swap, the lessons of days d and d3
//   are
// reallocated to time slots by Module 5; if this fails on either of the two days, the swap is undone.
public class Double_Periods extends Probl {
	// distr_pa, distr_paL, distr_pLa, distr_pLaL = copies of the rows of dd (lessons on the 5 days) of (p,a), (p,aL), (pL,a) and (pL,aL);
	// j, j1, j2, jL = positions of classes a, aL (of p) and a, aL (of pL) in the lists of the Teachers;  d, d3 = days of the swap;
	// delta_* = change in the double-lesson status of each pair (see delta);  attemptCount and successCount = statistics of the attempts;
	// distr0, tableP0 = backup copies of the lessons and time slots of days d and d3, to undo the swap;  g40_ = Module 5 object.
	private static int distr_pa[], distr_paL[], distr_pLa[], distr_pLaL[], j1, j2, p, d, k, a, h, d3, aL, l, j, jL, pL,
			delta_paL, delta_pLa, delta_pLaL, sum, attemptCount, successCount, p1, j0, distr0[][][], tableP0[][],
			aux0;
	private static boolean test, test2;
	static public Module_5 g40_;

	// Constructor: creates the Module 5 object and allocates the backup copies (distr0 per Teacher and class, and tableP0 per Teacher and time slot).
	public Double_Periods() {
		g40_ = new Module_5();
		distr0 = new int[Qp + 1][][];
		for (p1 = 1; p1 <= Qp; p1++)
			distr0[p1] = new int[nClassesP[p1] + 1][2];
		tableP0 = new int[Qp + 1][Ma + 1];
	}

	// Zeroes the timetable of the Teachers on day d1 (before Module 5 rewrites it).
	private static void initializeTables(int d1) {
		int p1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tableP[p1][d1][k1] = 0;
		}
	}

	// Evaluates the double-lesson status of a distribution row (lessons on the 5 days). test = true if there is no day with 2 lessons.
	// With semGem == false (see the calls): returns 1 if there is no double lesson and 0 if there is. With semGem == true: returns 0 if there is no double lesson and -1 if
	//   there is.
	// Thus, called before and after the swap (using as semGem the previous result == 1), it gives +1 if the pair lost the double lesson, -1 if it
	//   gained one and 0 if it did not change.
	private static int delta(int[] distr, boolean semGem) {
		boolean test;
		test = false;
		for (k = 0; k <= 4; k++)
			test = test || distr[k] > 1;
		test = !test;
		if (semGem) {
			if (test)
				return 0;
			else
				return -1;
		} else {
			if (test)
				return 1;
			else
				return 0;
		}
	}

	// Tries to create a double lesson for (p, a) by the swap described in the header of the class. Goes through, in order: the day d on which p has lessons
	//   in class a;
	// the Teacher pL, different from p, who teaches class a and has a lesson on d; the class aL, different from a, taught by p and by pL, with a lesson of p
	//   on d;
	// the day d3 different from d on which (p,a) and (pL,aL) have lessons. Returns true on the first successful swap and false if none works.
	private static boolean insertDoublePeriod(int p, int a) {
		boolean test;
		// Day d: only a day on which p has a lesson in class a is of interest.
		for (d = 0; d <= 4; d++) {
			if (distr_pa[d] == 0)
				continue;
			// Looks for another Teacher pL who teaches class a (j2 = position of a in the list of pL) and has a lesson on d.
			for (pL = 1; pL <= Qp; pL++) {
				if (pL == p)
					continue;
				j2 = 0;
				for (l = 1; l <= nClassesP[pL]; l++) {
					if (list[pL][l] == a) {
						j2 = l;
						break;
					}
				}
				if (j2 == 0 || dd[pL][j2][d] < 1)
					continue;
				// Looks for another class aL of p, with a lesson of p on d, that is also taught by pL (jL = position of aL in the list of pL).
				for (j1 = 1; j1 <= nClassesP[p]; j1++) {
					aL = list[p][j1];
					if (aL == a || dd[p][j1][d] < 1)
						continue;
					jL = 0;
					for (l = 1; l <= nClassesP[pL]; l++) {
						if (list[pL][l] == aL) {
							jL = l;
							break;
						}
					}
					if (jL == 0)
						continue;

					// Second day d3 (different from d) on which p has a lesson in a and pL has a lesson in aL.
					for (d3 = 0; d3 <= 4; d3++) {
						if (d3 == d || dd[p][j][d3] < 1 || dd[pL][jL][d3] < 1)
							continue;
						for (k = 0; k <= 4; k++) {
							// Copies the distribution rows of the four pairs involved and stores the previous double-lesson status of the three affected pairs (besides
							//   (p,a)).
							distr_pa[k] = dd[p][j][k];
							distr_paL[k] = dd[p][j1][k];
							distr_pLa[k] = dd[pL][j2][k];
							distr_pLaL[k] = dd[pL][jL][k];
						}
						delta_paL = delta(distr_paL, false);
						delta_pLa = delta(distr_pLa, false);
						delta_pLaL = delta(distr_pLaL, false);
						// Applies the swap to the copies: (p,aL) and (pL,a) lose a lesson on d and gain one on d3; (p,a) and (pL,aL) lose on d3 and gain on d.
						test2 = true;
						distr_paL[d]--;
						distr_pLa[d]--;
						distr_pa[d3]--;
						distr_pLaL[d3]--;
						distr_pLaL[d]++;
						distr_pa[d]++;
						distr_paL[d3]++;
						distr_pLa[d3]++;
						// Rejects if any pair would exceed the maximum number of lessons per day and class (1 or 2 depending on noDoublePeriodsP; (p,a) and (p,aL) up to 2).
						if (distr_pLaL[d] > (noDoublePeriodsP[pL] ? 1 : 2) || distr_pa[d] > 2 || distr_paL[d3] > 2
								|| distr_pLa[d3] > (noDoublePeriodsP[pL] ? 1 : 2))
							test2 = false;
						// Evaluates the effect on the double lessons: adds up the changes of (p,aL) and, if pL wants double lessons, of (pL,a) and (pL,aL); sum >= 2 is
						//   rejected.
						// If (p,aL) would lose its double lesson, requires aL > a; and if a pair of pL with desired double lessons would lose it, requires pL > p (ordering criterion
						// that seems to serve so that later swaps do not undo earlier ones).
						if (test2) {
							delta_paL = delta(distr_paL, delta_paL == 1);
							delta_pLa = delta(distr_pLa, delta_pLa == 1);
							delta_pLaL = delta(distr_pLaL, delta_pLaL == 1);
							sum = delta_paL;
							if (withDoublePeriodsP[pL]) {
								sum += delta_pLa;
								sum += delta_pLaL;
							}
							if (sum >= 2)
								test2 = false;
							else {
								if (delta_paL == 1)
									test2 = aL > a;
								if (test2 && withDoublePeriodsP[pL] && (delta_pLa == 1 || delta_pLaL == 1) && pL <= p)
									test2 = false;
							}
						}

						if (test2) {
							for (p1 = 1; p1 <= Qp; p1++) {
								for (j0 = 1; j0 <= nClassesP[p1]; j0++) {
									// Swap accepted on the copies: stores a backup copy (distr0, tableP0) of the lessons and time slots of days d and d3 ...
									distr0[p1][j0][0] = dd[p1][j0][d];
									distr0[p1][j0][1] = dd[p1][j0][d3];
								}
								for (j0 = 1; j0 <= Ma; j0++)
									tableP0[p1][j0] = tableP[p1][d][j0];
							}
							// ... and applies the swap to dd.
							dd[p][j1][d] = distr_paL[d];
							dd[pL][j2][d] = distr_pLa[d];
							dd[p][j][d3] = distr_pa[d3];
							dd[pL][jL][d3] = distr_pLaL[d3];
							dd[pL][jL][d] = distr_pLaL[d];
							dd[p][j][d] = distr_pa[d];
							dd[p][j1][d3] = distr_paL[d3];
							dd[pL][j2][d3] = distr_pLa[d3];
							test = true;
							// Reallocates to time slots, through Module 5, the lessons of day d and then those of day d3 (preliminaries, determineZ, main and
							//   result).
							// If both days work out, the swap holds and the function returns true.
							aux0 = g40_.preliminaries(d);
							if (aux0 > 0)
								g40_.determineZ(aux0);
							if (aux0 > 0 && g40_.main(aux0)) {
								initializeTables(d);
								g40_.result(aux0, d);
								aux0 = g40_.preliminaries(d3);
								if (aux0 > 0)
									g40_.determineZ(aux0);
								if (aux0 > 0 && g40_.main(aux0)) {
									initializeTables(d3);
									g40_.result(aux0, d3);

								} else
									test = false;
							} else
								test = false;
							// If both days were reallocated successfully (test), the swap holds and returns true; otherwise, restores dd and the timetables of the Teachers from
							//   the backup copies and tries the next combination.
							if (test)
								return true;
							else {
								for (p1 = 1; p1 <= Qp; p1++) {
									for (j0 = 1; j0 <= nClassesP[p1]; j0++) {
										dd[p1][j0][d] = distr0[p1][j0][0];
										dd[p1][j0][d3] = distr0[p1][j0][1];
									}
									for (j0 = 1; j0 <= Ma; j0++)
										tableP[p1][d][j0] = tableP0[p1][j0];

								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	// Goes through the Teachers with withDoublePeriodsP == true and, for each of their classes without any day with 2 lessons, tries to insert a double lesson
	//   (insertDoublePeriod).
	// At the end it rebuilds the timetables of the classes (tableA) from the timetables of the Teachers (tableP) and returns the fraction of successes
	//   (successCount / attemptCount).
	static public float insertDoublePeriods() {

		// Allocates the working rows and initializes d2_ (natural order of the Teachers, used by Module 5).
		distr_pa = new int[5];
		distr_paL = new int[5];
		distr_pLa = new int[5];
		distr_pLaL = new int[5];
		for (p = 1; p <= Qp; p++)
			d2_[p] = p;
		attemptCount = 0;
		successCount = 0;
		for (p = 1; p <= Qp; p++) {
			// Only the Teachers who want double lessons are of interest.
			if (!withDoublePeriodsP[p])
				continue;
			for (a = 1; a <= Qa; a++) {
				// For each class a: j = position of a in the Teacher's list (0 if the class is not the Teacher's).
				j = 0;
				for (l = 1; l <= nClassesP[p]; l++) {
					if (list[p][l] == a) {
						j = l;
						break;
					}
				}
				if (j == 0)
					continue;
				test = false;
				for (k = 0; k <= 4; k++) {
					distr_pa[k] = dd[p][j][k];
					test = test || distr_pa[k] > 1;
				}
				// If the class does not yet have double lessons (no day with more than 1 lesson), counts the attempt and tries to insert one.
				if (!test) {
					attemptCount++;
					if (insertDoublePeriod(p, a))
						successCount++;
				}
			}
		}

		for (d = 0; d <= 4; d++) {
			for (k = 1; k <= Ma; k++) {
				for (a = 1; a <= Qa; a++)
					// Rebuilds the timetables of the classes: zeroes tableA and fills it from tableP (the class tableP[p][d][k] is taught by Teacher p
					//   in that time slot).
					tableA[a][d][k] = 0;
			}
		}

		for (d = 0; d <= 4; d++) {
			for (k = 1; k <= Ma; k++) {
				for (p = 1; p <= Qp; p++) {
					a = tableP[p][d][k];
					tableA[a][d][k] = p;
				}
			}
		}
		// Prints the statistics and returns the success rate.
		System.out.println("successCount = " + successCount + " attemptCount = " + attemptCount);
		return (float) successCount / (float) attemptCount;
	}
}
