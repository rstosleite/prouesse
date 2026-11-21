package prouesse;

// Class Accessory_classes - encapsulates and initializes variables of its subclasses (Module_3A and Module_4).
//
// Role: declares and allocates the arrays that hold the search units and the control variables used by Modules 3A and 4
// (which extend this class). They serve the same purposes as the same-named ones in Probl (see the comments there), but are
// fields of this class, sized appropriately for the units of these modules.
public class Accessory_classes extends Probl {

	// Search units: Ap, Aa, j_, A, case_ and x_ (Teacher, class, position of the class, lessons, case and value of each unit); maxX0 (upper
	// bound of x_); iMaxI_ (the iMax array of Algorithm 2); i, n2 and x0 (index, number of units and successor).
	protected static int Ap[], Aa[], j_[], A[], case_[], x_[], maxX0[], i, n2, x0, iMaxI_[];

	// Control variables and pointers: Sp0_ and Sp1_ (accumulated lessons of the Teacher in the pair of days and on the third day), Sa_0 and Sa_1 (likewise
	// for classes), with the pointers pointerP2 and pointerA.
	protected static int Sp0_[], Sp1_[], Sa_0[], Sa_1[], pointerP2[], pointerA[];

	// Arrays z (search backtracking) and y2 (reusable solutions of Cases 3 and 4).
	protected static short z[][], y2[][];

	// Constructor: allocates the arrays, each with aux positions.
	public Accessory_classes() {
		aux = 0;
		for (p = 1; p <= Qp; p++)
			aux += nClassesP[p];
		// aux = 2 * (total number of classes of the Teachers) + 2 * Qp + 7: holds the units of all cases (one Case 1 and one Case 3 per Teacher, one
		// Case 2 and one Case 4 per (Teacher, class) pair, and the validation units).
		aux = 2 * aux + 2 * Qp + 7;
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		iMaxI_ = new int[aux];
		z = new short[aux][aux];
		// y2 reuses the array already created in Probl (by PhaseB2_E).
		y2 = Probl.y2;

		pointerP2 = new int[aux];
		pointerA = new int[aux];
		Sp0_ = new int[aux];
		Sp1_ = new int[aux];

		// Sa_0 and Sa_1: each class has 16 consecutive positions (one per unit of the class, plus a preceding position with value zero).
		aux = 16 * Qa + 2;
		Sa_0 = new int[aux];
		Sa_1 = new int[aux];
	}
}
