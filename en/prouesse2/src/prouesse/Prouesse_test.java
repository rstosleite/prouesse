package prouesse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import prouesse.Editor_.TextType;

// Class Prouesse_test - executable class of Algorithm P2 (Section 4.8 of the book and Section 5.4.3, item f).
//
// Role: controls and manages the execution of the other classes (countains the main method). Inherits from Accessory (which inherits from Editor_, the
// graphical interface). When the user presses the process button, the method executeProcess:
//   (a) reads the problem data from the JSON text in sInput (dataInput2) and checks its consistency (consistencyTests);
//   (b) if the file distribution.json exists with a reusable distribution (testDistribution), skips Phase 1;
//   (c) otherwise runs Phase 1 (Module 2A, which calls Module 2B, which in turn calls Modules 3B and 3A), undoes the permutation
//       of the days made in Phase 1 and saves the distribution obtained to distribution.json;
//   (d) runs Phases 2 and 3 (Module 4, which calls Module 5);
//   (e) inserts double lessons (Double_Periods);
//   (f) saves the new distribution and displays the timetables and the hints.
public class Prouesse_test extends Accessory {
	// g: object of the superclass Probl, which holds the problem data; gA1: Module 2A (Phase 1); g4: Module 4 (Phases 2 and 3).
	private static Probl g;
	private static Module_2A gA1;
	private static Module_4 g4;
	// k1_: 0 = the distribution will be obtained from scratch (Phase 1); 200 = there is a reusable distribution and execution countinues
	// with Phase 2; lastQp and lastQa: numbers of Teachers and classes of the saved distribution; maxUnavailablePeriods: maximum number of unavailable
	// time slots that each Teacher can have on each day without invalidating the distribution obtained.
	static private int k1_, n, aux[], aux2_, lastQp, lastQa, maxUnavailablePeriods[][];
	// solubilidade: result of Phase 1; lastResult and s10: texts displayed in the tabs of the interface.
	static private boolean solubilidade;
	static private String lastResult, s10;
	// Double_Periods_: object that inserts the double lessons; jsonObject, JSONdistrPA, array1, array2, restorationData and file: JSON reading and writing.
	private static Double_Periods Double_Periods_;
	private static JSONObject jsonObject;
	private static JSONArray JSONdistrPA, array1, array2;
	private static JSONObject restorationData;
	private static FileWriter file;
	private static String days_week[], sErrors_;

	// Constructor: initializes the interface (Accessory) and the auxiliary variables.
	public Prouesse_test() {
		super();
		aux = new int[5];
		sInput = "";
		days_week = new String[] {"", "", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", ""};
	}

	// Data consistency tests (Section 2.1 of the book). Accumulates the error messages in sErrors_ and returns true if there are none.
	// Checks, among other things: Teachers without classes; consistency among the double-lesson preferences, Mp and Wp0; that the maximum lessons per
	// day Mp[p][*] and the maximum number of days Wp0[p] allow the weekly load of each Teacher to be fulfilled (tests a and b); that the sum of the loads of each
	// class is Ma*5; that the sum of Mp on each day reaches Ma*Qa (c); that in each time slot there are at least Qa available Teachers (d); that
	// Mp[p][d] does not exceed the available time slots (e) nor the maximum possible given the Teacher's classes (f); that Wp0[p] does not exceed the
	// available days of p (g); and that each class of p can receive its lessons on the working days of p.
	public boolean consistencyTests() {
		boolean test1;
		int sum[], d, aux, aux1, aux2, j, k, k1, arranjo[], p, a;

		// sErrors_ (error messages of this check).
		sErrors_ = "";
		sum = new int[Qa + 1];
		arranjo = new int[5];
		// Each Teacher must have at least one class.
		for (p = 1; p <= Qp; p++) {
			if (nClassesP[p] == 0) {
				sErrors_ += "ERROR: No classes were entered for Teacher " + teacherName[p] + "\n";
			}
		}

		// Consistency between the double-lesson preferences and the maximum lessons per day, the number of days and the loads.
		for (p = 1; p <= Qp; p++) {
			test1 = true;
			for (d = 0; d <= 4; d++)
				test1 = test1 && Mp[p][d] < 2;
			if (withDoublePeriodsP[p] && test1)
				sErrors_ += "ERROR: It was entered that Teacher " + teacherName[p]
						+ " must have double lessons; however,\nit was also entered that they must not have more than one lesson on any day.\n";

			if (noDoublePeriodsP[p] && Wp0[p] == 1)
				sErrors_ += "ERROR: It was entered that Teacher " + teacherName[p]
						+ " must not have double lessons; however,\nit was also entered that they must attend on a single day of the week.\n";
			aux1 = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				aux1 += ns[p][j];
				if (withDoublePeriodsP[p] && ns[p][j] == 1)
					sErrors_ += "ERROR: It was entered that Teacher " + teacherName[p]
							+ " must have double lessons; however,\nthe weekly load in one of their classes is 1 lesson per week.\n";
			}
			// Test (a) of Section 2.1: the sum of the daily maxima must cover the Teacher's weekly load.
			aux2 = 0;
			for (d = 0; d <= 4; d++)
				aux2 += Mp[p][d];
			if (aux1 > aux2)
				sErrors_ += "ERROR: The maximum number of lessons per day was entered\nfor Teacher " + teacherName[p]
						+ ", but the values entered for this Teacher\n"
						+ "are insufficient for them to fulfill their weekly teaching load\n";
			// Test (b): the Wp0[p] largest daily maxima must cover the weekly load.
			for (d = 0; d <= 4; d++)
				arranjo[d] = Mp[p][d];
			Arrays.sort(arranjo);
			aux = 0;
			for (k = 0; k < Wp0[p]; k++)
				aux += arranjo[4 - k];
			if (aux1 > aux)
				sErrors_ += "ERROR: The maximum number of working days entered for Teacher " + teacherName[p]
						+ "\nis insufficient for them to fulfill their teaching load.\n";
		}

		// The sum of the weekly loads of the classes must be Ma * 5 (lessons per week of each class).
		for (a = 1; a <= Qa; a++)
			sum[a] = 0;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nClassesP[p]; j++)
				sum[list[p][j]] += ns[p][j];
		}
		for (a = 1; a <= Qa; a++) {
			if (sum[a] != Ma * 5)
				sErrors_ += "ERROR: The sum of the weekly loads of the subjects of class " + className[a]
						+ "\ndoes not match the total number of lessons attended during the week by each class.\n";
		}

		// Test (c): on each day, the sum of the Teachers' maxima must reach Ma*Qa, the total number of lessons of the day.
		for (d = 0; d <= 4; d++) {
			aux1 = 0;
			for (p = 1; p <= Qp; p++)
				aux1 += Mp[p][d];
			if (aux1 < Ma * Qa)
				sErrors_ += "ERROR: The maximum number of lessons on " + days_week[d+2]
						+ " of each Teacher was entered, but\nthe values entered are "
						+ "insufficient to serve all classes.\n";
		}

		// Test (d): in each time slot of each day, there must be at least Qa available Teachers (time slot available and Mp > 0).
		for (d = 0; d <= 4; d++) {
			for (k1 = 1; k1 <= Ma; k1++) {
				aux1 = Qp;
				for (p = 1; p <= Qp; p++) {
					if (unPeriods[p][d][k1] || Mp[p][d] == 0)
						aux1--;
				}
				if (aux1 < Qa)
					sErrors_ += "ERROR: " + aux1 + " is the number of Teachers available on " + days_week[d+2]
							+ ", in time slot " + k1 + ",\nnot enough to serve "
							+ "all classes in that time slot.\n";
			}
		}

		// Tests (e) and (f): Mp[p][d] cannot exceed the Teacher's available time slots on the day, nor the maximum possible given the number of classes.
		for (p = 1; p <= Qp; p++) {
			for (d = 0; d <= 4; d++) {
				aux1 = 0;
				for (k1 = 1; k1 <= Ma; k1++) {
					if (unPeriods[p][d][k1])
						aux1++;
				}
				if (Mp[p][d] > Ma - aux1)
					sErrors_ += "ERROR: The maximum number of lessons entered for Teacher " + teacherName[p] + " on "
							+ days_week[d+2] + " is greater than\nthe number of available time slots of that "
							+ "Teacher on that day.\n";
				if ((!noDoublePeriodsP[p]) && Mp[p][d] > 2 * nClassesP[p])
					sErrors_ += "ERROR: For Teacher " + teacherName[p] + " on " + days_week[d+2]
							+ " a very high maximum number of lessons was entered,\nconsidering that this Teacher "
							+ "has only " + nClassesP[p] + " classes.\n";
				if (noDoublePeriodsP[p] && Mp[p][d] > nClassesP[p])
					sErrors_ += "ERROR: For Teacher " + teacherName[p] + " on " + days_week[d+2]
							+ " a very high maximum number of lessons was entered,\nconsidering that this Teacher "
							+ "has only " + nClassesP[p]
							+ " classes and\nit was entered that they do not intend to have double lessons.\n";
			}
		}

		// Test (g): Wp0[p] cannot exceed the number of days on which p is available; prohibited double lessons require enough days.
		for (p = 1; p <= Qp; p++) {
			aux = 0;
			for (d = 0; d <= 4; d++) {
				if (Mp[p][d] > 0)
					aux++;
			}

			if (aux < Wp0[p])
				sErrors_ += "ERROR: The maximum number of working days entered for Teacher " + teacherName[p]
						+ " is not correct,\n"
						+ "considering the number of days on which this teacheressor must not have lessons.\n";
			test1 = false;
			for (j = 1; j <= nClassesP[p]; j++) {
				if (ns[p][j] > 1)
					test1 = true;
			}
			if (test1 && noDoublePeriodsP[p] && (aux < 2 || Wp0[p] < 2))
				sErrors_ += "ERROR: Teacher " + teacherName[p] + " does not attend enough days,\n"
						+ "considering that it was entered that this Teacher must not have double lessons.\n";
		}

		// Each class of p must be able to receive its lessons on the working days of p (with 1 or 2 lessons per day, according to the preference).
		for (p = 1; p <= Qp; p++) {
			aux1 = (noDoublePeriodsP[p] ? 1 : 2);
			aux = 0;
			for (d = 0; d <= 4; d++)
				aux += Math.min(Mp[p][d], aux1);
			aux = Math.min(aux, Wp0[p] * aux1);
			for (j = 1; j <= nClassesP[p]; j++) {
				if (ns[p][j] > aux)
					sErrors_ += "ERROR: Teacher " + teacherName[p] + " does not attend enough days "
							+ "to serve class " + className[list[p][j]] + "\n";
			}
		}
		return (sErrors_.equals(""));
	}

	// Reads the problem data from the JSON in sInput into the static variables: Ma ('m5'), Qp, Qa, Wp0 ('maxDaysP'), Mp ('maxP'),
	// nClassesP, list, ns ('ch' = weekly load of each class, indexed by the position j of the class), unPeriods, noDoublePeriodsP,
	// withDoublePeriodsP and the names of the Teachers and classes. Prints to the console the values read for ns and unPeriods.
	protected void dataInput2() throws JSONException, IOException {
		JSONArray array1, array2, Wp0_, noDoublePeriodsP_, withDoublePeriodsP_, list_, ch_, Mp_, unPeriods_, teacherName_, className_,
				nClassesP_;
		JSONObject jsonObject;
		jsonObject = new JSONObject(sInput);

		restorationData = jsonObject.getJSONObject("dados_restauracao");
		Ma = jsonObject.getInt("m5");
		Qp = jsonObject.getInt("qP");
		Qa = jsonObject.getInt("qA");
		Wp0_ = jsonObject.getJSONArray("maxDiasP");
		noDoublePeriodsP_ = jsonObject.getJSONArray("semGeminadasP");
		withDoublePeriodsP_ = jsonObject.getJSONArray("comGeminadasP");
		list_ = jsonObject.getJSONArray("lista");
		ch_ = jsonObject.getJSONArray("ch");
		Mp_ = jsonObject.getJSONArray("maxP");
		unPeriods_ = jsonObject.getJSONArray("horariosND");
		teacherName_ = jsonObject.getJSONArray("nomeP");
		className_ = jsonObject.getJSONArray("nomeA");
		nClassesP_ = jsonObject.getJSONArray("nTurmasP");

		// Allocates the variables with the necessary dimensions (up to 30 classes per Teacher, 5 days, Ma time slots per day).
		nClassesP = new int[Qp + 1];
		list = new int[Qp + 1][31];
		ns = new int[Qp + 1][31];
		Mp = new int[Qp + 1][5];
		unPeriods = new boolean[Qp + 1][5][Ma + 1];
		noDoublePeriodsP = new boolean[Qp + 1];
		withDoublePeriodsP = new boolean[Qp + 1];
		Wp0 = new int[Qp + 1];
		teacherName = new String[Qp + 1];
		className = new String[Qa + 1];

		// Fills the variables from the JSON arrays (the indices of the Teachers and classes start at 1).
		for (a = 1; a <= Qa; a++)
			className[a] = className_.getString(a);
		for (p = 1; p <= Qp; p++) {
			teacherName[p] = teacherName_.getString(p);
			Wp0[p] = Integer.parseInt(Wp0_.getString(p));
			array1 = new JSONArray(Mp_.get(p).toString());
			for (d = 0; d <= 4; d++)
				Mp[p][d] = Integer.parseInt(array1.getString(d));
			nClassesP[p] = Integer.parseInt(nClassesP_.get(p).toString());
			array1 = new JSONArray(list_.get(p).toString());
			array2 = new JSONArray(ch_.get(p).toString());
			for (k = 1; k <= nClassesP[p]; k++) {
				list[p][k] = Integer.parseInt(array1.get(k).toString());
				ns[p][k] = Integer.parseInt(array2.getString(k));
				System.out.println(ns[p][k]);
			}
			noDoublePeriodsP[p] = noDoublePeriodsP_.get(p).toString().equals("true");
			withDoublePeriodsP[p] = withDoublePeriodsP_.get(p).toString().equals("true");
			array1 = new JSONArray(unPeriods_.get(p).toString());
			for (d = 0; d <= 4; d++) {
				array2 = new JSONArray(array1.get(d).toString());
				for (k = 1; k <= Ma; k++) {
					unPeriods[p][d][k] = !array2.get(k).toString().equals("false");
					if (unPeriods[p][d][k])
						System.out.println("T");
					else
						System.out.println("F");
				}
			}
		}
	}

	// Checks whether the distribution read from the file (distrPA) can be reused for the current data: same number of Teachers and
	// classes; at most Ma lessons per class per day; at most 1 or 2 lessons per Teacher and class on each day; at most Mp[p][d] lessons
	// of the Teacher per day and Wp0[p] working days; and total number of lessons of each (Teacher, class) pair equal to the weekly load ns. Returns true if it is valid.
	public boolean testDistribution() {
		int a, j, aux;
		if (Qp != lastQp || Qa != lastQa)
			return false;

		for (a = 1; a <= Qa; a++) {
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux += distrPA[p1][a][k];
				if (aux > Ma)
					return false;
			}
		}
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

	// Runs the whole processing (see the class description).
	protected void executeProcess() throws JSONException, IOException {
		boolean test1;
		// If no data has been imported, warns the user.
		if (sInput.equals("")) {
			JOptionPane.showMessageDialog(null, "Press IMPORT to open file", "Result",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Step (a): reads the data and runs the consistency tests; in case of error, shows the messages in the ERRORS tab and stops.
		dataInput2();
		textAreas[TextType.TIPS.ordinal()].setText("");
		if (!consistencyTests()) {
			textAreas[TextType.ERRORS.ordinal()].setText(sErrors_);
			tabsTabbedPane.setSelectedIndex(TextType.ERRORS.ordinal());
			JOptionPane.showMessageDialog(null, "Data consistency error. Open the ERRORS tab", "Result",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Creates the Probl object and initializes its variables with the problem data.
		g = new Probl();
		g.initialization(Ma, Qp, Qa, nClassesP, list, ns, Mp, unPeriods, noDoublePeriodsP, withDoublePeriodsP, Wp0,
				teacherSequence);

		lastResult = "";
		// Step (b): checks whether distribution.json exists and, if it does, whether it is reusable (k1_ = 200).
		File file2_ = new File("distribution.json");
		temDistrPronta = file2_.exists();
		k1_ = 0;
		// Reads the saved distribution (distrPA[p][a][day], indexed by the class number a) and tests it.
		if (temDistrPronta) {
			String content = new String(Files.readAllBytes(Paths.get("distribution.json")));
			jsonObject = new JSONObject(content);
			lastQp = jsonObject.getInt("Nro_De_Teachers");
			lastQa = jsonObject.getInt("Nro_De_Classes");
			test1 = Qp == lastQp && Qa == lastQa;
			if (test1) {
				distrPA = new int[lastQp + 1][lastQa + 1][5];
				JSONdistrPA = jsonObject.getJSONArray("Distribution");
				for (p = 0; p <= Qp; p++) {
					array1 = new JSONArray(JSONdistrPA.get(p).toString());
					for (a = 0; a <= Qa; a++) {
						array2 = new JSONArray(array1.get(a).toString());
						for (d = 0; d <= 4; d++)
							distrPA[p][a][d] = Integer.parseInt(array2.get(d).toString());
					}
				}
				test1 = testDistribution();
			}

			if (!test1) {
				JOptionPane.showMessageDialog(null, "The last distribution obtained could not be reused", "",
						JOptionPane.INFORMATION_MESSAGE);
				temDistrPronta = false;
			} else
				k1_ = 200;
		}
		// Creates the modules that will be run.
		s = textAreas[TextType.RESULTS.ordinal()].getText();
		pointer = 0;
		gA1 = new Module_2A();
		gA1.initialization();
		g4 = new Module_4();
		// Step (c): without a reusable distribution, runs Phase 1 (Module 2A, which calls Module 2B and Modules 3B and 3A).
		if (k1_ == 0) {
			// Hints text displayed if Phase 1 finds no solution: suggests relaxing the days off and shows the sum of the maximum lessons per day.
			s_ = "The First Phase found no solution. What can be done: \n\n"
					+ "Try going to the Prouesse Configuration Page and removing some days-off indications by changing the maximum lessons per day\n"
					+ "of some teacheressors, in the \"Lessons per weekday\" tab, which lets you select one of two options that give the maximum lessons per day.\n "
					+ "For some teacheressors, fill in an option with few days-off indications and keep it selected.\n\n"
					+ "It is also important to keep the distribution of the days off of all teachers reasonably balanced across the days of the week.\n"
					+ "The following table shows the sum of the maximum numbers of lessons entered, per weekday, to give an idea of the distribution of the days off:\n\n";
			for (d = 0; d <= 4; d++) {
				aux1 = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux1 += Mp[p1][d];
				s_ += days_week[d+2] + ": " + aux1 + "\n";
			}
			// preliminaries builds the units of Module 2A and mainV2 runs the search; returns true if the distribution was obtained.
			n = gA1.preliminaries();
			solubilidade = gA1.mainV2();
			// Phase 1 without a solution: shows the hints and stops.
			if (!solubilidade) {
				JOptionPane.showMessageDialog(null, "First Phase without solution. Open the TIPS tab", "Result",
						JOptionPane.INFORMATION_MESSAGE);
				textAreas[TextType.RESULTS.ordinal()].setText("");
				textAreas[TextType.TIPS.ordinal()].setText(s_);
				tabsTabbedPane.setSelectedIndex(TextType.TIPS.ordinal());
				return;
			}
			// Phase 1 successful: the distribution is in dd.
			JOptionPane.showMessageDialog(null, "First Phase possible ", "Result", JOptionPane.INFORMATION_MESSAGE);
			lastResult = "";
			textAreas[TextType.RESULTS.ordinal()].setText(lastResult);
			textAreas[TextType.TIPS.ordinal()].setText("");
			tabsTabbedPane.setSelectedIndex(TextType.TIPS.ordinal());

			temDistrPronta = true;
			// Undoes the permutation of the days made in Module 2A: destino0[d] is the new position of the original day d; destino is the inverse permutation.
			for (d1 = 0; d1 <= 4; d1++)
				System.out.println(Probl.destino0[d1]);
			for (d1 = 0; d1 <= 4; d1++) {
				k1 = 0;
				while (Probl.destino0[k1] != d1)
					k1++;
				Probl.destino[d1] = k1;
			}
			// Returns the lessons in dd and the maxima in Mp to the original positions of the days.
			for (p1 = 1; p1 <= Qp; p1++) {
				for (k1 = 1; k1 <= nClassesP[p1]; k1++) {
					for (d1 = 0; d1 <= 4; d1++)
						aux[d1] = Probl.dd[p1][k1][d1];
					for (d1 = 0; d1 <= 4; d1++)
						Probl.dd[p1][k1][Probl.destino[d1]] = aux[d1];
				}
				for (d1 = 0; d1 <= 4; d1++)
					aux[d1] = Mp[p1][d1];
				for (d1 = 0; d1 <= 4; d1++)
					Mp[p1][Probl.destino[d1]] = aux[d1];
			}
			// Converts the distribution from dd (indexed by the position j of the class) to distrPA (indexed by the class number a).
			distrPA = new int[Qp + 1][Qa + 1][5];
			for (p1 = 1; p1 <= Qp; p1++) {
				for (a = 1; a <= Qa; a++) {
					for (k = 0; k <= 4; k++)
						distrPA[p1][a][k] = 0;
				}
				for (j = 1; j <= nClassesP[p1]; j++) {
					a = list[p1][j];
					for (k = 0; k <= 4; k++)
						distrPA[p1][a][k] = Probl.dd[p1][j][k];
				}
			}

			// Saves the distribution to distribution.json (JSON format with the number of Teachers, the number of classes and the distribution).
			jsonObject = new JSONObject();
			jsonObject.put("Nro_De_Teachers", Qp);
			jsonObject.put("Nro_De_Classes", Qa);
			jsonObject.put("Distribution", distrPA);

			file = new FileWriter("distribution.json");
			try {
				file.write(jsonObject.toString());
			} catch (Exception E) {
				E.printStackTrace();
			} finally {
				file.flush();
				file.close();
			}
			// Creates a new Module 2A object, releasing the state of Phase 1.
			gA1 = new Module_2A();
		}
		// Step (d): loads into dd the distribution (obtained now or read from the file), in the indexing by position j of the class.
		for (p1 = 1; p1 <= Qp; p1++) {
			for (j = 1; j <= nClassesP[p1]; j++) {
				a = list[p1][j];
				for (k = 0; k <= 4; k++)
					Probl.dd[p1][j][k] = distrPA[p1][a][k];
			}
		}

		// Phase 2: Module 4 with exec = 1 (distribution among days 0, 1 and 2; time slots are allocated for only two of them, see Module_4).
		n = g4.preliminaries(1);
		aux1 = 0;
		aux2 = 0;
		if (g4.main2()) {
			aux1 = 1;
			g4.result();
			JOptionPane.showMessageDialog(null, "Second Phase possible: Monday, Tuesday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Second Phase without solution: Monday, Tuesday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		}
		// Phase 3: Module 4 with exec = 2 (days 2, 3 and 4).
		n = g4.preliminaries(2);
		if (g4.main2()) {
			aux2 = 1;
			g4.result();
			JOptionPane.showMessageDialog(null, "Third Phase possible: Wednesday, Thursday, Friday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Third Phase without solution: Wednesday, Thursday, Friday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		}
		// If Phase 2 or Phase 3 failed, displays the hints (remove unavailable time slots) and shows, for each Teacher and day, the maximum number of
		// unavailable time slots compatible with the distribution obtained.
		if (aux1 == 0 || aux2 == 0) {
			JOptionPane.showMessageDialog(null, "Open the TIPS tab", "Result", JOptionPane.INFORMATION_MESSAGE);
			s_ = "The Second and/or Third Phase found no solution. What can be done: \n\n"
					+ "Try removing some indications of unavailable time slots.\n"
					+ "Try not to keep several unavailable time slots at the same time slot (for example, the first or the last time slot)\n"
					+ "of a given day.\n";
			if (aux1 == 0 && aux2 == 1)
				s_ += "Since only the Second Phase obtained no solution, you can make these changes only for Monday or Tuesday.\n";
			if (aux1 == 1 && aux2 == 0)
				s_ += "Since only the Third Phase obtained no solution, you can make these changes only for Wednesday, Thursday or Friday.\n";
			s_ += "Go to the Prouesse Configuration Page, make these changes and then \n"
					+ "return to this program to run the Second and Third Phases again. \n \n"
					+ "\nNext, for each Teacher, for each day, the maximum number of unavailable time slots is shown\n"
					+ "that can be set so that the last distribution obtained can be reused\n"
					+ "(This information will also be available on the Configuration Page):\n\n";
			for (p1 = 1; p1 <= Qp; p1++) {
				aux2_ = 0;
				for (d1 = 0; d1 <= 4; d1++) {
					for (a = 1; a <= Qa; a++)
						aux2_ += distrPA[p1][a][d1];
				}
				s_ += "Teacher p = " + p1 + ": ";
				for (d1 = 0; d1 <= 4; d1++) {
					aux1 = 0;
					for (a = 1; a <= Qa; a++)
						aux1 += distrPA[p1][a][d1];
					if (aux2_ > 0)
						s_ += Ma - aux1;
					else
						s_ += "*";
					if (d1 == 1)
						s_ += " ";
				}
				s_ += "   ";
				if (p1 % 5 == 0)
					s_ += "\n\n";
			}
			textAreas[TextType.RESULTS.ordinal()].setText(lastResult);
			textAreas[TextType.TIPS.ordinal()].setText(s_);
			tabsTabbedPane.setSelectedIndex(TextType.TIPS.ordinal());
			return;
		}
		// Step (e): Double_Periods tries to insert double lessons for the Teachers with withDoublePeriodsP true.
		Double_Periods_ = new Double_Periods();
		System.out.println(Double_Periods_.insertDoublePeriods());
		// Step (f): converts the final distribution to distrPA and saves it to distribution.json.
		distrPA = new int[Qp + 1][Qa + 1][5];
		for (p1 = 1; p1 <= Qp; p1++) {
			for (a = 1; a <= Qa; a++) {
				for (k = 0; k <= 4; k++)
					distrPA[p1][a][k] = 0;
			}
			for (j = 1; j <= nClassesP[p1]; j++) {
				a = list[p1][j];
				for (k = 0; k <= 4; k++)
					distrPA[p1][a][k] = Probl.dd[p1][j][k];
			}
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("Nro_De_Teachers", Qp);
		jsonObject.put("Nro_De_Classes", Qa);
		jsonObject.put("Distribution", distrPA);

		file = new FileWriter("distribution.json");
		try {
			file.write(jsonObject.toString());
		} catch (Exception E) {
			E.printStackTrace();
		} finally {
			file.flush();
			file.close();
		}

		// Hints text: for each Teacher and each day, how many unavailable time slots can be added without invalidating this distribution (Ma minus the lessons of the day).
		s_ = "\nNext, for each Teacher, for each day, the maximum number of unavailable time slots is shown\n"
				+ "that can be set so that the last distribution obtained can be reused\n"
				+ "(This information will also be available on the Configuration Page):\n\n";
		for (p1 = 1; p1 <= Qp; p1++) {
			aux2_ = 0;
			for (d1 = 0; d1 <= 4; d1++) {
				for (a = 1; a <= Qa; a++)
					aux2_ += distrPA[p1][a][d1];
			}
			s_ += "Teacher p = " + p1 + ": ";
			for (d1 = 0; d1 <= 4; d1++) {
				aux1 = 0;
				for (a = 1; a <= Qa; a++)
					aux1 += distrPA[p1][a][d1];
				if (aux2_ > 0)
					s_ += Ma - aux1;
				else
					s_ += "*";
				if (d1 == 1)
					s_ += " ";
			}
			s_ += "   ";
			if (p1 % 5 == 0)
				s_ += "\n\n";
		}
		textAreas[TextType.TIPS.ordinal()].setText(s_);
		// Builds the text of the timetables of the Teachers and classes (obtem_quadro_horários, from Accessory) and displays it in the RESULTS tab.
		lastResult += obtain_schedule_table(Probl.tableP, teacherName, className, Qp) + "\n"
				+ obtain_schedule_table(Probl.tableA, className, teacherName, Qa)
				+ "\n \nAttention to the content in the TIPS tab.";
		textAreas[TextType.RESULTS.ordinal()].setText(lastResult);
		tabsTabbedPane.setSelectedIndex(TextType.RESULTS.ordinal());
		// Prepares the output JSON (sOutput): timetables of the Teachers and classes, maximum unavailable time slots and restoration data.
		maxUnavailablePeriods = new int[Qp + 1][5];
		for (p1 = 1; p1 <= Qp; p1++) {
			for (d1 = 0; d1 <= 4; d1++) {
				aux1 = 0;
				for (a = 1; a <= Qa; a++)
					aux1 += distrPA[p1][a][d1];
				maxUnavailablePeriods[p1][d1] = Ma - aux1;
			}
		}
		jsonObject = new JSONObject();
		jsonObject.put("tableP", Probl.tableP);
		jsonObject.put("tableA", Probl.tableA);
		jsonObject.put("maxUnavailablePeriods", maxUnavailablePeriods);
		jsonObject.put("restorationData", restorationData);
		sOutput = jsonObject.toString();
		JOptionPane.showMessageDialog(null, "Press EXPORT to save file", "Result",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// Helper method: asks the user for an integer through a daylog box.
	private int readInt(String s) {
		String input;
		input = (String) JOptionPane.showInputDialog(null, s, "Input", JOptionPane.QUESTION_MESSAGE, icone, null,
				null);
		return Integer.parseInt(input);
	}

	// Entry point: opens the graphical interface window.
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Prouesse_test().setVisible(true);
			}
		});
	}
}
