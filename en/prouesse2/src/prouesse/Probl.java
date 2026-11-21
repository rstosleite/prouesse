package prouesse;

// Class Probl - main superclass that encapsulates the main variables (problem data, search variables and results).
//
// Almost all classes of the program inherit, directly or indirectly, from Probl and share its static fields. The fields
// are grouped below. Some fields (for example pLast1, pLast2, pLast3, pMax, min0, completeSchedule, iMax_, d_0_, d_1_, x1,
// aux5, aux41, aux51, x_case_10, complex_p02, maxIterations and maxIterations2) are not used by the other classes of this version.
public class Probl {

	// Problem data (Section 2.1 of the book): Ma = lessons per class per day; Qp = number of Teachers; Qa = number of classes;
	// nClassesP[p] = number of classes of Teacher p; list[p][j] = number of the j-th class of p; ns[p][j] = weekly workload of p in
	// their j-th class; Mp[p][k] = maximum lessons of p on day k (0 to 4); Wp0[p] = maximum working days of p in the week.
	// Results and common variables: teacherSequence (array received in initialization, not used by the modules of this version); dd[p][j][k] = distribution (lessons
	// of p in the j-th class on day k); ns_p[p] = workload of p on the days considered by the running module; q_[p][j] = auxiliary
	// vector per Teacher and class; maxDays0 and maxDays1 = maximum working days in the two parts of the week (Module 2A);
	// destino0 and destino = permutation of the days made in Phase 1 and its inverse; timeMaximum = maximum execution time, in minutes.
	static protected int Ma, Qp, Qa, nClassesP[], list[][], ns[][], Mp[][], Wp0[], teacherSequence[], dd[][][], ns_p[],
			q_[][], iMax_, maxDays0[], maxDays1[], pLast1, pLast2, pLast3, destino[], destino0[], timeMaximum;

	// unPeriods[p][k][h] = true if p is not available on day k, in time slot h (h from 1 to Ma); noDoublePeriodsP and withDoublePeriodsP =
	// double-lesson preferences (Section 1.1 of the book); unavailableSlotP and unavailableSlotA = time-slot markers (allocated in PhaseA1_Super);
	// mantemP[p] = p has lessons on the three days considered by Module 4.
	static protected boolean unPeriods[][][], noDoublePeriodsP[], withDoublePeriodsP[], unavailableSlotP[][][], unavailableSlotA[][][],
			completeSchedule, mantemP[];

	// tableP[p][d][h] = number of the class of p on day d, in time slot h (0 = vacant); tableA[a][d][h] = number of the Teacher of class a on day d,
	// in time slot h (final timetables); d2_ = array for ordering days or Teachers; Sa0_ and Sa1_ = auxiliaries per class.
	static public int tableA[][][], tableP[][][], Sa0_[], Sa1_[], pMin, pMax, d2_[], min0[];
	// amplitude[] = measure used to order days or Teachers; initialTime = instant at which the execution-time count started.
	static public float amplitude[], initialTime;

	// Working variables of the modules: i, x0, p, j, a, k, aux* etc. (timerary indices and values); x0 = successor value found
	// by maximum*(); nLessons = number of lessons; Sp0, Sp2, Sa0 and Sa2 = values read from the control variables of Teacher and class.
	// wp0 and wp1 = working days of p in parts of the week; Np0 to Np3 (and variants) = linking variables: maximum lessons of p in
	// parts of the week or on days; Wp1 = maximum working days of p on the days of the module; days, d_0, d_1 and d_2 = days considered.
	// Search units (Chapter 3): case_[i] = case of unit i; Ap[i] and Aa[i] = Teacher and class of the unit; j_[i] = position of the class
	// in list; A[i] = lessons to distribute; x_[i] = current value of the decision variable; maxX0[i] = upper bound of x_[i]; n2 = number of units;
	// q2_[p][j] and q_0[p][j] = lessons of the pair (p, j) in parts of the week or on the days of the module; iMaxI_ = iMax array of Algorithm 2.
	// Control variables (accumulated lessons per Teacher and per class; Sp0_, Sp1_, Sp2_, Sp3_, Sa_0, Sa_1, Sa_4, Sa_5) and their pointers
	// (pointerP2, pointerP, pointerA, pointerA2), which indicate the position corresponding to each unit.
	static protected int i, x0, i1, p, j, a, k, nLessons, Sp0, Sa0, Sp2, Sa2, aux0, n0, aux1, aux2, wp0[], wp1[], Np0[],
			Np1[], Np2[], Np3[], Np2_0[], Np3_0[], Np2_[], Np3_[], x1, days[], d_0_, d_1_, d_0, d_1, d_2, Wp1[], imax,
			case_[], Ap[], Aa[], j_[], A[], x_[], maxX0[], p2_[], n2, q2_[][], q_0[][], pointerP2[], pointerA[], aux,
			Sp2_[], Sp3_[], Sa_4[], Sa_5[], pointerP[], pointerA2[], Sa_0[], Sa_1[], Sp0_[], Sp1_[], count, iMaxI_[], auxA[];

	// y2: short type used in PhaseB2_E to hold solutions of Cases 3 and 4 (reuse after backtracking); z: z array of Algorithm 2 (Chapter 3).
	static protected short y2[][], z[][];

	// test1, test2, test and success: auxiliary booleans; case_Trivial: true when the successor value is mandatory.
	static protected boolean test1, test2, case_Trivial;

	// Dp[p] = minimum working days of p; arranjo, nDays_, x_case_1 = auxiliaries of the modules; sumA_0, sumA_1 and sumA_1_ =
	// accumulated capacities per class; nPossibP, i1_, possib, exec = auxiliaries; x_case_10 and the others are not used.
	static protected int Dp[], arranjo[], nDays_[], x_case_1[], x_case_10[], d_3, aux4, aux5, aux41, aux51, sumA_0[],
			sumA_1[], sumA_1_[], nPossibP[], i1_, possib, exec;

	// complex_p0 = indicator of 'complex' Teachers (Section 5.2.1); possibP = auxiliary possibilities (Module A).
	static protected boolean complex_p0[], complex_p02[], possibP[][][], success, test;

	// Counters of search iterations (nIterations is checked every 50000 iterations for the time limit).
	static protected long maxIterations2, nIterations, maxIterations;

	// Objects of Modules 3A and 3B created by Module 2B.
	static protected Module_3A g2_;
	static protected Module_3B g2_0;

	// Empty constructor.
	public Probl() {
	};

	// Stores in the static variables the problem data (Section 2.1 of the book) received from the interface.
	public void initialization(int Ma, int Qp, int Qa, int[] nClassesP, int[][] list, int[][] ns, int[][] Mp,
			boolean[][][] unPeriods, boolean[] noDoublePeriodsP, boolean[] withDoublePeriodsP, int[] Wp0,
			int[] teacherSequence) {
		this.Ma = Ma;
		this.Qp = Qp;
		this.Qa = Qa;
		this.nClassesP = nClassesP;
		this.list = list;
		this.ns = ns;
		this.Mp = Mp;
		this.unPeriods = unPeriods;
		this.noDoublePeriodsP = noDoublePeriodsP;
		this.withDoublePeriodsP = withDoublePeriodsP;
		this.Wp0 = Wp0;
		this.teacherSequence = teacherSequence;
	}
}
