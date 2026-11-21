package prouesse;

// Class Probl - main superclass of Algorithm P1.
//
// Role: gather, in static fields, the data of the timetabling problem (Section 2.1 of the book)
// and the main working variables that are shared by all modules.
// Practically all the other classes of Algorithm P1 inherit, directly or indirectly,
// from this class and therefore see the same arrays.
//
// Since it is the only class that does not execute any search algorithm, its code boils down
// to declaring the fields and copying the input data into them (method initialization).
public class Probl {

	// --- Problem data (Section 2.1) ---
	// Ma  = M_a: number of lessons per class, per day (in the code, 4, 5 or 6).
	// Qp  = Q_p: number of Teachers;  Qa = Q_a: number of classes.
	// nClassesP[p] = number of classes of Teacher p.
	// list[p][j] = number of the j-th class of Teacher p (each class has a number from 1 to Qa).
	// ns[p][j]    = weekly workload of Teacher p in his/her j-th class (indexed by j, not by the class number).
	// Mp[p][k]    = M_p: maximum number of lessons of Teacher p on day k (k = 0 is Monday; value 0 = day off/unavailability).
	// Wp0[p]      = W_0p: maximum number of working days of Teacher p in the week.
	// teacherSequence[]: array received together with the problem data (not consulted by the classes analyzed).
	//
	// --- Search arrays (Chapter 3 and Section 4.1) ---
	// Each module solves a search problem with n units; the unit with index i has:
	// case_[i] = type (case) of the unit; Ap[i] = Teacher and Aa[i] = class to which the unit refers;
	// j_[i] = position of class Aa[i] in the list array of Teacher Ap[i]; A[i] = number of lessons to be distributed;
	// x_[i] = current value of the decision variable x[i]; maxX0[i] = upper bound of x0 computed for unit i;
	// iMaxI_[i] = iMax[i] of Algorithm 2 (the furthest unit reached since i was visited).
	// z = array z of Algorithm 2 (indicates which unit to backtrack to); y2 is used only by Module 4.
	//
	// --- Results and support variables ---
	// dd[p][j][k]  = distribution d: number of lessons of Teacher p, in his/her j-th class, on day k (Section 2.2).
	// ns_p[p]      = total number of lessons of Teacher p on the days handled by the module being executed;
	// q_[p][j]     = auxiliary field allocated together with dd (not consulted by the classes analyzed).
	// maxDays0[p], maxDays1[p] = maximum number of working days of Teacher p on Monday (day 0) and on the other 4 days (Module 1).
	// destino0[], destino[]    = permutation of the days made by Module 1 (hardest days first) and its inverse.
	// amplitude[], d2_[]       = sorting key and corresponding permutation (used by sort2 to sort days and Teachers).
	// timeMaximum, initialTime, nIterations = control of the execution time limit entered by the user.
	// imax, pLast1..3, min0, pMin, pMax, iMax_, pointerP_, pointerP, maxIterations, maxIterations2, completeSchedule:
	// auxiliary/reserved variables; several are not used by the classes analyzed.
	static protected int Ma, Qp, Qa, nClassesP[], list[][], ns[][], Mp[][], Wp0[], teacherSequence[], Ap[], Aa[], j_[],
			A[], case_[], x_[], maxX0[], iMaxI_[], dd[][][], ns_p[], q_[][], iMax_, maxDays0[], maxDays1[], imax,
			pLast1, pLast2, pLast3, destino[], destino0[], pointerP_[], pointerP[], min0[], timeMaximum;
	// unPeriods[p][k][h] = true if Teacher p is NOT available on day k, in the h-th time slot (h from 1 to Ma).
	// noDoublePeriodsP[p] = true if p must not have double lessons; withDoublePeriodsP[p] = true if it is desirable that p have them (Section 1.1).
	// unavailableSlotP, unavailableSlotA, mantemP, completeSchedule: auxiliary/reserved boolean variables.
	static protected boolean unPeriods[][][], noDoublePeriodsP[], withDoublePeriodsP[], unavailableSlotP[][][], unavailableSlotA[][][],
			completeSchedule, mantemP[];
	// Final results (filled in by Module 5):
	// tableP[p][k][h] = number of the class that Teacher p teaches on day k, in time slot h (0 = no lesson);
	// tableA[a][k][h] = number of the Teacher who teaches class a on day k, in time slot h.
	// Sa0_, Sa1_, d2_, pMin, pMax: auxiliary arrays/variables.
	static public int tableA[][][], tableP[][][], Sa0_[], Sa1_[], pMin, pMax, d2_[];
	static public float amplitude[], initialTime;
	static protected long nIterations, maxIterations, maxIterations2;
	// The arrays y2 and z are of type short to save memory (z can have dimension n x n, with n = number of units).
	static protected short y2[][], z[][];

	// Empty constructor: the fields are filled in by initialization().
	public Probl() {
	};

	// Copies the problem data, read from the JSON file by the executable class (Prouesse5D_X), into the
	// static fields of this class, making them accessible to all modules.
	// Note that the parameter ch corresponds to the variable ns (weekly workload per Teacher and class).
	public void initialization(int Ma, int Qp, int Qa, int[] nClassesP, int[][] list, int[][] ch, int[][] Mp,
			boolean[][][] unPeriods, boolean[] noDoublePeriodsP, boolean[] withDoublePeriodsP, int[] Wp0,
			int[] teacherSequence) {
		this.Ma = Ma;
		this.Qp = Qp;
		this.Qa = Qa;
		this.nClassesP = nClassesP;
		this.list = list;
		this.ns = ch;
		this.Mp = Mp;
		this.unPeriods = unPeriods;
		this.noDoublePeriodsP = noDoublePeriodsP;
		this.withDoublePeriodsP = withDoublePeriodsP;
		this.Wp0 = Wp0;
		this.teacherSequence = teacherSequence;
	}
}
