package prouesse;

import javax.swing.ImageIcon;

// Class PhaseA1_Super - encapsulates and initializes variables of its subclass (Module_1/Module_1B), which executes Phase 1.1.
//
// Role: (1) declare variables used by Modules 1 and A; (2) in the constructor, instantiate the classes of Modules 2 and 3
// (via PhaseB2_E and Module_2B) and allocate the unit arrays; (3) in initialization(), allocate the result arrays
// (dd, tableP, tableA) and the control arrays. Module_A_ also inherits from this class.
public class PhaseA1_Super extends Probl {
	// Auxiliary and search variables of Module 1 (i, x0, p, a, j, k, nLessons, Sp0, Sp2, Sa0, Sa2, n0, n2...).
	// nPeriods_P0[p], nPeriods_P1[p] = maximum number of lessons of p on Monday (day 0) and on the other 4 days (linking variables of Module
	//   1);
	// ns_p_[p] = total weekly workload of Teacher p.
	// possibP, nPossibP, countador, iMin, nIterations2, test1, test2: auxiliary variables (reserved).
	static public int aux1, aux2, n2, Sp2, Sa2, i1, p, a, j, n0, x0, i, nLessons, aux, min_0, min_1, k, Sp0, Sa0,
			nPeriods_P0[], nPeriods_P1[], iMin, ns_p_[], aux_[];
	static public int nPossibP, possibP[][], countador;
	static public long nIterations2;
	static public boolean test1, test2;
	// g2_I_W_3 = instance of Module 2 (Module_2B), called at the end of Phase 1.1;  g2_E = object that initializes the variables of
	//   PhaseB2_Super.
	static public Module_2B g2_I_W_3;
	static public PhaseB2_E g2_E;
	ImageIcon icone;

	// Constructor: allocates structures that depend on the problem dimensions and creates the objects of the following modules.
	public PhaseA1_Super() {
		// Reserved auxiliary structures (possibP/countador/aux_) and day permutation arrays (destino0, destino).
		possibP = new int[100][4];
		countador = 0;
		aux_ = new int[5];
		destino0 = new int[5];
		destino = new int[5];
		// Per-Teacher arrays of Module 1: maximum numbers of working days (maxDays0/1) and of lessons (nPeriods_P0/1).
		maxDays0 = new int[Qp + 1];
		maxDays1 = new int[Qp + 1];
		nPeriods_P0 = new int[Qp + 1];
		nPeriods_P1 = new int[Qp + 1];
		// Creates PhaseB2_E (which allocates the arrays of PhaseB2_Super) and the Module 2 object; the constructor of the latter creates Module 3.
		// Thus, when Module 1 is created, the whole chain of modules is ready for use.
		g2_E = new PhaseB2_E();
		g2_I_W_3 = new Module_2B();
		// amplitude and d2_: sorting key and permutation used to sort days (indices 0 to 4) and Teachers (indices 1 to Qp).
		amplitude = new float[Qp + 1];
		d2_ = new int[Qp + 1];
		// mantemP, ns_p_, ns_p: per-Teacher indicators/arrays.
		mantemP = new boolean[Qp + 1];
		ns_p_ = new int[Qp + 1];
		ns_p = new int[Qp + 1];
		// Sizes the unit arrays (Probl): aux = 7 + total number of (Teacher, class) pairs.
		// z and iMaxI_ receive this size; the arrays Ap, Aa, j_, A, x_, maxX0 and case_ receive 4*Qp additional positions,
		// because Module 1 uses additional units per Teacher (cases 510, 501, 1 and 5, besides unit 511).
		aux = 7;
		for (p = 1; p <= Qp; p++)
			aux += nClassesP[p];
		z = new short[aux][aux];
		iMaxI_ = new int[aux];
		aux = 4 * Qp + aux;
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		d2_ = new int[Qp + 1];
	}

	// Allocates the result and control arrays (and zeroes them). Must be called before starting the processing (see Prouesse5D_X).
	public void initialization() {
		int p1, aux2, p;
		// Final timetables: one per Teacher (tableP) and one per class (tableA), with 5 days and Ma time slots per day.
		tableP = new int[Qp + 1][5][Ma + 1];
		tableA = new int[Qa + 1][5][Ma + 1];
		// Boolean occupancy markers (reserved) and totals of lessons per class (Sa0_, Sa1_).
		unavailableSlotP = new boolean[5][Qp + 1][Ma + 1];
		unavailableSlotA = new boolean[5][Qa + 1][Ma + 1];
		Sa0_ = new int[Qa + 1];
		Sa1_ = new int[Qa + 1];
		ns_p = new int[Qp + 1];
		d2_ = new int[Qp + 1];
		// dd[p] = matrix [class j][day 0..4] with the distribution of lessons;  q_[p] = auxiliary array per class of the Teacher.
		dd = new int[Qp + 1][][];
		q_ = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++) {
			dd[p1] = new int[nClassesP[p1] + 1][5];
			q_[p1] = new int[nClassesP[p1] + 1];
		}
	}

}
