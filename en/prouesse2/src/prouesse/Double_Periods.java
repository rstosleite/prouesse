package prouesse;

// Class Double_Periods - inserts double lessons into the final results (Sections 4.6.3, 4.7.2 and 5.4.3, item b, of the book).
//
// Role: after the timetables of all days are ready (Phases 2 and 3), tries to improve the result for the
// Teachers p with withDoublePeriodsP[p] true: for each class a of p that does not yet have any day with two lessons of p (double
// lessons), looks for swaps of lessons with another Teacher pL and another class aL, involving two days d and d3, that make
// two lessons of (p, a) appear on day d. The swaps made in the distribution dd are:
//                  class a            class aL
//        Teacher p   d3-  d+            d-   d3+
//        Teacher pL  d-   d3+           d3-  d+
// (the + sign indicates the addition of a lesson on the day and the - sign indicates the removal of a lesson). After the swap, the time slots of
// days d and d3 are allocated again with Module 5; if this fails on either day, the swap is undone.
public class Double_Periods extends Probl {

	// distr_pa, distr_paL, distr_pLa and distr_pLaL: lessons per day of the pairs (p, a), (p, aL), (pL, a) and (pL, aL); j, j1, j2 and jL: positions
	// of the classes a, aL (for p) and a, aL (for pL) in the list arrays; delta_*: change in the double lessons of each pair due to the swap;
	// attemptCount and successCount: number of classes attempted and of those that received double lessons; distr0 and tableP0: backup copies
	// of the distribution and of the Teachers' time slots, used to undo a swap that failed.
	private static int distr_pa[], distr_paL[], distr_pLa[], distr_pLaL[], j1, j2, p, d, k, a, h, d3, aL, l, j, jL, pL,
			delta_paL, delta_pLa, delta_pLaL, sum, attemptCount, successCount, p1, j0, distr0[][][], tableP0[][],
			aux0;
	private static boolean test, test2;
	// g40_: Module 5 object, used to reallocate the time slots of the changed days.
	static public Module_5 g40_;

	// Constructor: creates Module 5 and allocates the backup-copy arrays.
	public Double_Periods() {
		g40_ = new Module_5();
		distr0 = new int[Qp + 1][][];
		for (p1 = 1; p1 <= Qp; p1++)
			distr0[p1] = new int[nClassesP[p1] + 1][2];
		tableP0 = new int[Qp + 1][Ma + 1];
	}

	// Clears the Teachers' time slots (tableP) of day d1, before writing the new result.
	private static void initializeTables(int d1) {
		int p1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tableP[p1][d1][k1] = 0;
		}
	}

	// Measures the double-lesson situation of a distribution by day (distr[0..4]). First computation (semGem = false): returns 1 if the
	// distribution has no day with more than one lesson (no double lessons) and 0 if it has. Second computation, made after the swap with
	// semGem = true if there was no double lesson before: returns 0 if there is still none and -1 if there now is one (gain); with
	// semGem = false: returns 0 if a double lesson remains and 1 if it was lost (loss).
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

	// Tries to insert double lessons of (p, a) through one of the swaps described in the header. Returns true if some swap succeeded.
	private static boolean insertDoublePeriod(int p, int a) {
		boolean test;
		// d: day on which p has a lesson in class a (it will be the day of the future double lesson).
		for (d = 0; d <= 4; d++) {
			if (distr_pa[d] == 0)
				continue;
			// pL: another Teacher who also teaches class a (position j2) and has a lesson in it on d.
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
				// aL: another class of p with a lesson on d that is also a class of pL (position jL in pL's list).
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

					// d3: second day, on which p has a lesson in a and pL has a lesson in aL.
					for (d3 = 0; d3 <= 4; d3++) {
						if (d3 == d || dd[p][j][d3] < 1 || dd[pL][jL][d3] < 1)
							continue;

						// Copies the lessons of the four pairs on the five days and records the double-lesson situation before the swap.
						for (k = 0; k <= 4; k++) {
							distr_pa[k] = dd[p][j][k];
							distr_paL[k] = dd[p][j1][k];
							distr_pLa[k] = dd[pL][j2][k];
							distr_pLaL[k] = dd[pL][jL][k];
						}
						delta_paL = delta(distr_paL, false);
						delta_pLa = delta(distr_pLa, false);
						delta_pLaL = delta(distr_pLaL, false);
						test2 = true;

						// Applies the swap from the table in the header.
						distr_paL[d]--;
						distr_pLa[d]--;
						distr_pa[d3]--;
						distr_pLaL[d3]--;
						distr_pLaL[d]++;
						distr_pa[d]++;
						distr_paL[d3]++;
						distr_pLa[d3]++;
						// Rejects the swap if some class exceeds the maximum number of lessons per day (1 if the Teacher does not allow double lessons, otherwise 2).
						if (distr_pLaL[d] > (noDoublePeriodsP[pL] ? 1 : 2) || distr_pa[d] > 2 || distr_paL[d3] > 2
								|| distr_pLa[d3] > (noDoublePeriodsP[pL] ? 1 : 2))
							test2 = false;
						// Evaluates the effect on the double lessons: the swap must not cause a net loss of two or more double lessons; a loss in (p, aL) is only
						// accepted if aL > a, and a loss for Teacher pL, who prefers double lessons, is only accepted if pL > p (avoids cyclic swaps).
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

						// Swap accepted: saves a backup copy of the lessons and of the time slots of day d, writes the new distribution and tries to reallocate the
						// time slots of days d and d3 with Module 5.
						if (test2) {

							// Backup copy (lessons of days d and d3 for all pairs and the Teachers' time slots on day d).
							for (p1 = 1; p1 <= Qp; p1++) {
								for (j0 = 1; j0 <= nClassesP[p1]; j0++) {
									distr0[p1][j0][0] = dd[p1][j0][d];
									distr0[p1][j0][1] = dd[p1][j0][d3];
								}
								for (j0 = 1; j0 <= Ma; j0++)
									tableP0[p1][j0] = tableP[p1][d][j0];
							}
							// Writes the new distribution into dd.
							dd[p][j1][d] = distr_paL[d];
							dd[pL][j2][d] = distr_pLa[d];
							dd[p][j][d3] = distr_pa[d3];
							dd[pL][jL][d3] = distr_pLaL[d3];
							dd[pL][jL][d] = distr_pLaL[d];
							dd[p][j][d] = distr_pa[d];
							dd[p][j1][d3] = distr_paL[d3];
							dd[pL][j2][d3] = distr_pLa[d3];
							// Runs Module 5 for day d; if it succeeds, writes the time slots and repeats for day d3.
							test = true;
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
							// Success on both days: the swap is kept.
							if (test)
								return true;
							// Failure: restores the previous distribution and time slots.
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
		// No swap is possible for (p, a).
		return false;
	}

	// Goes through the Teachers with withDoublePeriodsP and their classes without double lessons and calls insertDoublePeriod. At the end, rebuilds the class time slots
	// (tableA) from the Teachers' time slots (tableP) and returns the fraction of successful attempts.
	static public float insertDoublePeriods() {

		distr_pa = new int[5];
		distr_paL = new int[5];
		distr_pLa = new int[5];
		distr_pLaL = new int[5];

		// d2_ defines the natural order of the Teachers for Module 5.
		for (p = 1; p <= Qp; p++)
			d2_[p] = p;
		attemptCount = 0;
		successCount = 0;
		// For each Teacher who prefers double lessons and each class a of theirs...
		for (p = 1; p <= Qp; p++) {
			if (!withDoublePeriodsP[p])
				continue;
			for (a = 1; a <= Qa; a++) {
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
				// ...if there is not yet any day with more than one lesson of (p, a), tries to insert a double lesson.
				for (k = 0; k <= 4; k++) {
					distr_pa[k] = dd[p][j][k];
					test = test || distr_pa[k] > 1;
				}

				if (!test) {
					attemptCount++;
					if (insertDoublePeriod(p, a))
						successCount++;
				}
			}
		}

		// Rebuilds tableA: clears it and then fills it with the Teacher of each class in each time slot.
		for (d = 0; d <= 4; d++) {
			for (k = 1; k <= Ma; k++) {
				for (a = 1; a <= Qa; a++)
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
		System.out.println("successCount = " + successCount + " attemptCount = " + attemptCount);
		return (float) successCount / (float) attemptCount;
	}
}
