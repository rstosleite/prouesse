package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Class Accessory - methods used by the executable class Prouesse_test, which inherits from this class.
//
// Role: to keep, in static variables, the shared state of the interface (problem data read from the JSON, names of Teachers
// and classes, displayed texts) and to provide (1) the verification of an existing distribution (testDistribution) and (2) the formatting
// of the timetables as text (displayPeriodsPA2, displayPeriods2 and obtem_quadro_horários). It inherits from Editor_, which
// implements the visual components.
public class Accessory extends Editor_ {

	// Problem data (see Section 2.1 of the book): Ma, Qp, Qa, Mp (maximum lessons per day), Wp0 (maximum working days), nClassesP,
	// list (numbers of the classes of each Teacher), ns (weekly loads, indexed by the position of the class in list), unPeriods (unavailable
	// time slots), noDoublePeriodsP and withDoublePeriodsP; distrPA[p][a][k]: distribution in the 'by class' format (a is the number of the
	// class, not the position j in list) read from distribution.json; teacherName and className: names of the Teachers and of the classes; s, s_, s9 and sErrors_:
	// texts for the tabs of the interface; temDistrPronta: indicates whether there is a usable distribution.
	static protected int row, distrPA[][][], Ma, Qp, Qa, Mp[][], teacherSequence[], teachingLoadP[];
	static protected boolean test, test2, temDistrPronta;

	static protected int k, p, d1, p1, aux, aux1, aux2, k1, a, count, d, sum[], j, nClassesP[], list[][], ns[][], Wp0[];
	static protected boolean unPeriods[][][], noDoublePeriodsP[], withDoublePeriodsP[];

	static protected String sErrors_, teacherName[], className[], s9, s, s_;
	static protected char c;
	static protected int code, pointer, countRows, tabType;

	// Icon used in the daylog boxes.
	ImageIcon icone;

	// Constructor: initializes the interface (Editor_), loads the icon and records that there is no ready distribution yet.
	public Accessory() {
		super();
		icone = new ImageIcon("forward.gif");
		temDistrPronta = false;
	}

	// Checks whether the distribution stored in distrPA is valid for the current problem data, that is, whether it satisfies the feasibility
	// constraints of Section 2.2. Returns true if it can be reused. (Prouesse_test overrides this method with an
	// additional check of the numbers of Teachers and classes.)
	public boolean testDistribution() {
		int a, j;
		// The dimensions of the distribution must match the number of Teachers and classes of the problem.
		if (Qp != distrPA.length - 1 || Qa != distrPA[1].length - 1)
			return false;

		// On each day, each class gets at most Ma lessons.
		for (a = 1; a <= Qa; a++) {
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux += distrPA[p1][a][k];
				if (aux > Ma)
					return false;
			}
		}
		// For each Teacher: no class has more than 1 lesson per day (if noDoublePeriodsP) or 2; the day's total does not exceed Mp[p][k]; the number of working days does not exceed Wp0.
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
			if (count > Wp0[p1])
				return false;

			// The total number of lessons of each class of the Teacher must equal the corresponding weekly load ns.
			for (a = 1; a <= Qa; a++) {
				aux = 0;
				for (k = 0; k <= 4; k++)
					aux += distrPA[p1][a][k];
				if (aux > 0) {
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

	// Builds the timetable text of an entity k (a Teacher or a class): one line per time slot of the day and one column per weekday.
	// 'table' is tableP (class of each Teacher in each time slot) or tableA (Teacher of each class in each time slot); 'name' holds the names to display.
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

	// Builds the text with the timetables of all Teachers and all classes.
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

	// Builds the timetable text of Qp entities (Teachers or classes), laid out two at a time side by side, separated by '||'.
	public String obtain_schedule_table(int[][][] tableP, String[] teacherName, String[] className, int Qp) {
		int h, k, p, aux;
		String s = "";
		p = 1;
		while (p <= Qp) {
			// Header: number and name of the first and, if it exists, of the second timetable in the row.
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
			// Line with the weekdays.
			s += "MON       \tTUE       \tWED       \tTHU       \tFRI       \t||       \t";
			if (p + 1 <= Qp)
				s += "MON       \tTUE       \tWED       \tTHU       \tFRI       \t";
			s += "\n";
			// One line per time slot: for each day, the name of the class (or Teacher) or '---------' if the time slot is vacant.
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
			// Moves on to the next pair of timetables.
			p = p + 2;
		}
		return s;
	}

}
