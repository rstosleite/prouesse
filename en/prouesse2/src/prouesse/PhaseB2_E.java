package prouesse;

// Class PhaseB2_E - initializes variables of its superclass Probl.
//
// Role: the constructor allocates the arrays shared by the distribution modules (2A, 2B, 3A and 3B): linking variables
// (Np0 to Np3, wp0, wp1, Wp1), arrays of the search units (Ap, Aa, j_, A, x_, maxX0, case_, iMaxI_, z, y2) and control
// variables (Sp0_ to Sp3_, Sa_0, Sa_1, Sa_4, Sa_5, with their pointers). It is created by PhaseA1_Super.
public class PhaseB2_E extends Probl {

	// Constructor: allocates the arrays.
	public PhaseB2_E() {
		int p1;

		// q2_[p][j]: lessons of the pair (p, j-th class) in a part of the week (one of the sets of days of the distribution).
		q2_ = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++)
			q2_[p1] = new int[nClassesP[p1] + 1];

		// Array of the days considered.
		days = new int[4];

		// wp0 and wp1: working days of each Teacher in parts of the week.
		wp0 = new int[Qp + 1];
		wp1 = new int[Qp + 1];

		// Linking variables Np0 to Np3 (and variants with the suffixes _ and _0): maximum numbers of lessons of each Teacher in parts of the week or on days.
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		Np2 = new int[Qp + 1];
		Np3 = new int[Qp + 1];
		Np2_ = new int[Qp + 1];
		Np3_ = new int[Qp + 1];
		Np2_0 = new int[Qp + 1];
		Np3_0 = new int[Qp + 1];

		// Wp1[p]: maximum number of working days of p among the days of the module; auxA[a]: auxiliary pointer per class.
		Wp1 = new int[Qp + 1];

		auxA = new int[Qa + 1];

		// Number of positions for the units of Modules 2A and 2B: 2 * Qp + (total number of classes of the Teachers) + 7. The arrays Ap, Aa, j_,
		// A, x_, maxX0, case_ and iMaxI_ hold the data of the units; z is the backtracking matrix; pointerP2 and pointerA are pointers of the
		// control variables; Sp0_ and Sp1_ hold the accumulated lessons of the Teachers.
		aux = 0;
		for (p = 1; p <= Qp; p++)
			aux += nClassesP[p];
		aux = 2 * Qp + aux + 7;
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		iMaxI_ = new int[aux];
		z = new short[aux][aux];
		pointerP2 = new int[aux];
		pointerA = new int[aux];
		Sp0_ = new int[aux];
		Sp1_ = new int[aux];

		// Number of positions for the units of Modules 3A, 3B and 4: 2 * Qp + 2 * (total number of classes) + 7. y2[p][i] holds solutions of
		// Cases 3 and 4 (reuse after backtracking); pointerP and pointerA2 are pointers; Sp2_ and Sp3_ hold the accumulated lessons of the Teachers on days.
		aux = aux - 2 * Qp - 7;
		aux = 2 * Qp + 2 * aux + 7;
		y2 = new short[Qp + 1][aux];
		pointerP = new int[aux];
		pointerA2 = new int[aux];
		Sp2_ = new int[aux];
		Sp3_ = new int[aux];

		// Sa_0, Sa_1, Sa_4 and Sa_5: accumulated lessons per class; each class has 16 positions.
		aux = 16 * Qa + 2;
		Sa_0 = new int[aux];
		Sa_1 = new int[aux];
		Sa_4 = new int[aux];
		Sa_5 = new int[aux];
	}
}
