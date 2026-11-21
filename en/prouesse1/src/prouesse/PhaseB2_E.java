package prouesse;

// Class PhaseB2_E - initializes (allocates) the variables of its superclass PhaseB2_Super.
//
// Role: everything this class does is in the constructor, which sizes the arrays according to the problem data
// (Qp, Qa and nClassesP). It is instantiated only once, by the constructor of PhaseA1_Super.
public class PhaseB2_E extends PhaseB2_Super {

	// Constructor: allocates all arrays used by Modules 2, 3 and 4.
	public PhaseB2_E() {
		int p1, aux_;
		// Arrays per Teacher and per class of the Teacher: q_0, q2_ and q2_0 (lessons of the class on the days of each phase). Row p has nClassesP[p]+1 positions
		//   (classes are numbered starting from 1).
		q_0 = new int[Qp + 1][];
		q2_ = new int[Qp + 1][];
		q2_0 = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++) {
			q_0[p1] = new int[nClassesP[p1] + 1];
			q2_[p1] = new int[nClassesP[p1] + 1];
			q2_0[p1] = new int[nClassesP[p1] + 1];
		}
		// Arrays indexed by Teacher (position 0 unused): working days (wp0, wp1, Wp1) and maximum numbers of lessons (Np0..Np3_0).
		days = new int[4];
		wp0 = new int[Qp + 1];
		wp1 = new int[Qp + 1];
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		Np2 = new int[Qp + 1];
		Np3 = new int[Qp + 1];
		Np2_ = new int[Qp + 1];
		Np3_ = new int[Qp + 1];
		Np2_0 = new int[Qp + 1];
		Np3_0 = new int[Qp + 1];
		Wp1 = new int[Qp + 1];
		// Arrays indexed by class: auxA (next free position in the class block) and Sa02_/Sa03_ (reserved).
		auxA = new int[Qa + 1];
		Sa02_ = new int[Qa + 1];
		Sa03_ = new int[Qa + 1];
		// Computes the maximum size of the unit arrays: aux_ = total number of (Teacher, class) pairs.
		// The number of units of Module 2 is on the order of 7*Qp + aux_ (or 2*Qp + 2*aux_), plus some slack; the larger of the two values is used.
		aux_ = 0;
		for (p = 1; p <= Qp; p++)
			aux_ += nClassesP[p];
		aux = Math.max(7 * Qp + aux_ + 10, 2 * Qp + 2 * aux_ + 6);
		// Unit arrays (see Probl): Teacher, class, index j, number of lessons A, decision variable x, bound maxX0, case and iMaxI_.
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		iMaxI_ = new int[aux];
		// Matrix z of Algorithm 2, with dimension n x n.
		z = new short[aux][aux];

		// Per-Teacher control variables of case 2 of Module 2 (Sp0_, Sp1_) and pointers pointerP2 and pointerA.
		Sp0_ = new int[aux];
		Sp1_ = new int[aux];
		pointerP2 = new int[aux];
		pointerA = new int[aux];

		// Per-Teacher control variables of Module 3/4 (Sp2_, Sp3_) and pointers pointerP and pointerA2 (smaller number of units).
		aux = 2 * Qp + 2 * aux_ + 6;
		Sp2_ = new int[aux];
		Sp3_ = new int[aux];
		pointerP = new int[aux];
		pointerA2 = new int[aux];
		// Per-class control variables (Sa_0, Sa_1, Sa_4, Sa_5): a block of 16 positions for each class, plus slack.
		aux = 16 * Qa + 2;
		Sa_0 = new int[aux];
		Sa_1 = new int[aux];
		Sa_4 = new int[aux];
		Sa_5 = new int[aux];
	}
}
