package prouesse;

import javax.swing.ImageIcon;

// Class PhaseA1_Super - encapsulates and initializes variables of its subclass Module_2A (and of Module_A).
//
// Role: declares and allocates the variables of Module 2A and the result structures (distribution and timetables). The
// constructor also creates the PhaseB2_E object (which allocates the shared arrays) and Module 2B, which Module 2A calls when it finishes.
public class PhaseA1_Super extends Probl {

	// Static variables inherited by Modules 2A and A: auxiliary indices and values (i, k, p, a, j, i1, n0, x0, n2, nLessons, aux*,
	// Sp0, Sp2, Sa0, Sa2) and some arrays (nPeriods_P0, nPeriods_P1, ns_p_, aux_, sumMax, sumMax2, Sp2_, Sp3_, pointerP) allocated
	// in the constructor. Some of them are not used by the classes derived from this one in this version, which declare their own fields with the same names.
	static public int aux1, aux2, n2, Sp2, Sa2, i1, p, a, j, n0, x0, i, nLessons, aux, min_0, min_1, k, Sp0, Sp2_[],
			Sp3_[], pointerP[], Sa0, nPeriods_P0[], nPeriods_P1[], iMin, ns_p_[], aux_[], sumMax[], sumMax2[][];

	// nPossibP, possibP and countador: auxiliary fields (Module_A declares its own fields with the same names for counting possibilities).
	static public int nPossibP, possibP[][], countador;
	// Iteration counter with long precision.
	static public long nIterations2;

	// Auxiliary boolean flags.
	static public boolean test1, test2;

	// g2_I_W_: Module 2B object; g2_E: object that allocates the shared arrays (PhaseB2_E).
	static public Module_2B g2_I_W_;
	static public PhaseB2_E g2_E;
	// Icon used in the daylog boxes.
	ImageIcon icone;

	// Constructor: allocates auxiliary arrays, creates PhaseB2_E and Module_2B and prepares the arrays for ordering days and Teachers.
	public PhaseA1_Super() {
		possibP = new int[100][4];
		sumMax = new int[5 * (Qp + 1)];
		sumMax2 = new int[5 * (Qp + 1)][Qa + 1];
		countador = 0;
		// aux_, destino0 and destino have 5 positions, one per weekday (destino0 and destino hold the permutation of the days and its inverse).
		aux_ = new int[5];
		destino0 = new int[5];
		destino = new int[5];
		nPeriods_P0 = new int[Qp + 1];
		nPeriods_P1 = new int[Qp + 1];
		// Creates the objects whose initialization depends on Qp, Qa and nClassesP.
		g2_E = new PhaseB2_E();
		g2_I_W_ = new Module_2B();
		amplitude = new float[Qp + 1];
		d2_ = new int[Qp + 1];
		mantemP = new boolean[Qp + 1];
		ns_p_ = new int[Qp + 1];
		ns_p = new int[Qp + 1];
		d2_ = new int[Qp + 1];
	}

	// Initializes the result structures (called before Phase 1).
	public void initialization() {
		int p1, p;
		// Timetables: tableP[p][day][time slot] (class of the Teacher) and tableA[a][day][time slot] (Teacher of the class).
		tableP = new int[Qp + 1][5][Ma + 1];
		tableA = new int[Qa + 1][5][Ma + 1];
		// unavailableSlotP and unavailableSlotA: time-slot markers per day, Teacher and class.
		unavailableSlotP = new boolean[5][Qp + 1][Ma + 1];
		unavailableSlotA = new boolean[5][Qa + 1][Ma + 1];
		Sa0_ = new int[Qa + 1];
		Sa1_ = new int[Qa + 1];
		ns_p = new int[Qp + 1];
		d2_ = new int[Qp + 1];
		// dd[p][j][day]: distribution (lessons of Teacher p in the j-th class on each of the 5 days); q_[p][j]: auxiliary vector.
		dd = new int[Qp + 1][][];
		q_ = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++) {
			dd[p1] = new int[nClassesP[p1] + 1][5];
			q_[p1] = new int[nClassesP[p1] + 1];
		}
	}

}
