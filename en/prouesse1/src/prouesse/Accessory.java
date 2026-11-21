package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Class Accessory - support methods of the executable class Prouesse5D_X (which inherits from this class).
//
// Role: hold the shared state of the interface (problem data read from the JSON, names of Teachers and classes, texts
// displayed) and provide (1) the verification of an existing distribution and (2) the formatting of the timetables as text.
// Inherits from Editor_, which implements the visual components.
public class Accessory extends Editor_ {
	// Static fields: problem data (Ma, Qp, Qa, Mp, nClassesP, list, ns, Wp0, unPeriods, noDoublePeriodsP, withDoublePeriodsP; see Probl)
	// and distrPA[p][a][k] = distribution in the 'per class' format (a is the class number, not the position j in list);
	// teacherName[] and className[] = names of the Teachers and of the classes; s, s_, s9, sErrors_ = texts for the tabs of the interface;
	// temDistrPronta = indicates that a usable distribution exists (file distribution.json).
	static protected int row, distrPA[][][], Ma, Qp, Qa, Mp[][], teacherSequence[], teachingLoadP[];
	static protected boolean test, test2, temDistrPronta;
	static protected int k, p, d1, p1, aux, aux1, aux2, k1, a, count, d, sum[], j, nClassesP[], list[][], ns[][], Wp0[];
	static protected boolean unPeriods[][][], noDoublePeriodsP[], withDoublePeriodsP[];
	static protected String sErrors_, teacherName[], className[], s9, s, s_;
	static protected char c;
	static protected int code, pointer, countRows, tabType;

	ImageIcon icone;

	public Accessory() {
		super();
		// Constructor: initializes the icon used in the daylog boxes and records that there is no ready distribution yet.
		icone = new ImageIcon("forward.gif");
		temDistrPronta = false;
	}

	// Checks whether the distribution stored in distrPA is valid for the current problem data, that is, whether it respects the
	// feasibility constraints of Section 2.2. Returns true if it can be reused.
	public boolean testDistribution() {
		int a, j;
		// 1) The dimensions of the distribution must match the number of Teachers and classes of the problem.
		if (Qp != distrPA.length - 1 || Qa != distrPA[1].length - 1)
			return false;

		// 2) In each class and on each day, the total number of lessons (summing over all Teachers) cannot exceed Ma.
		for (a = 1; a <= Qa; a++) {
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux += distrPA[p1][a][k];
				if (aux > Ma)
					return false;
			}
		}
		// 3) For each Teacher: on each day, the number of lessons per class must be at most 1 (no double lessons) or 2; the total for the day cannot
		//   exceed Mp;
		for (p1 = 1; p1 <= Qp; p1++) {
			count = 0;
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (a = 1; a <= Qa; a++) {
					aux += distrPA[p1][a][k];
					if (distrPA[p1][a][k] > (noDoublePeriodsP[p1] ? 1 : 2) || distrPA[p1][a][k] < 0)
						return false;
				}
				if (aux > 0)
					count++;
				if (aux > Mp[p1][k])
					return false;
			}
			// The number of days with lessons cannot exceed Wp0[p1] (maximum number of working days).
			if (count > Wp0[p1])
				return false;

			for (a = 1; a <= Qa; a++) {
				aux = 0;
				for (k = 0; k <= 4; k++)
					aux += distrPA[p1][a][k];
				if (aux > 0) {
					// 4) For each class that has lessons with the Teacher, the position j of that class in the Teacher's list is searched for (j == 0: the class is not the Teacher's);
					// the total number of lessons in the week must be exactly ns[p1][j].
					j = nClassesP[p1];
					while (j > 0 && list[p1][j] != a)
						j--;
					if (j == 0 || aux != ns[p1][j])
						return false;
				}
			}
		}
		return true;
	}

	// Builds the text of the timetable of a Teacher (or class) k, with the days in the columns and the time slots in the rows.
	// table = tableP or tableA; name[] = names that will be displayed in each cell ('----------' if there is no lesson).
	public String displayPeriodsPA2(int[][][] table, String[] name, int k) {
		int h1, k1, aux;
		String s;
		s = "MON        TUE        WED        THU        FRI        \n";
		for (h1 = 1; h1 <= Ma; h1++) {
			for (k1 = 0; k1 <= 4; k1++) {
				aux = table[k][k1][h1];
				if (aux != 0)
					s += name[aux] + " ";
				else
					s += "---------- ";
			}
			s += "\n";
		}
		return s;
	}

	// Builds the text with the timetables of all Teachers and, then, of all classes.
	public String displayPeriods2() {
		int k;
		String s;
		s = "";
		for (k = 1; k <= Qp; k++) {
			s += "\nPROF. " + k + " " + teacherName[k] + "\n";
			s += displayPeriodsPA2(Probl.tableP, className, k);
		}
		for (k = 1; k <= Qa; k++) {
			s += "\nCLASS " + k + " " + className[k] + "\n";
			s += displayPeriodsPA2(Probl.tableA, teacherName, k);
		}
		return s;
	}

	// Builds the text of the timetables for display in the results tab: the timetables are presented two at a time, side by side
	// (Teacher/class p and p+1), separated by '||', with columns separated by tabs.
	// tableP = table with the time slots; teacherName = names of the rows (timetables); className = names of the cells; Qp = number of timetables.
	public String obtain_schedule_table(int[][][] tableP, String[] teacherName, String[] className, int Qp) {
		int h, k, p, aux;
		String s = "";
		p = 1;
		while (p <= Qp) {
			s += p + "\t" + teacherName[p] + "\t";
			for (k = 1; k <= 3; k++)
				s += " \t";
			s += "||      \t";
			if (p + 1 <= Qp) {
				s += (p + 1) + "\t" + teacherName[p + 1] + "\t";
				for (k = 1; k <= 3; k++)
					s += " \t";
			}
			s += "\n";
			s += "MON       \tTUE       \tWED       \tTHU       \tFRI       \t||       \t";
			if (p + 1 <= Qp)
				s += "MON       \tTUE       \tWED       \tTHU       \tFRI       \t";
			s += "\n";
			for (h = 1; h <= Ma; h++) {
				for (k = 0; k <= 4; k++) {
					aux = tableP[p][k][h];
					if (aux != 0)
						s += className[aux] + " \t";
					else
						s += "---------\t";
				}
				s += "||      \t";
				if (p + 1 <= Qp) {
					for (k = 0; k <= 4; k++) {
						aux = tableP[p + 1][k][h];
						if (aux != 0)
							s += className[aux] + " \t";
						else
							s += "---------\t";
					}
				}
				s += "\n";
			}
			s += "\n";
			p = p + 2;
		}
		return s;
	}

}
