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

// Class Prouesse5D_X - executable class (countains main) of Algorithm P1. Controls and manages the execution of the other classes (Section
//   4.8).
//
// Main flow (method executeProcess, triggered by the 'process' button):
//   a) reads the data from the imported JSON file (dataInput2);
//   b) validates the data (consistencyTests, Section 2.1);
//   c) checks whether there is a saved distribution (distribution.json) that can be reused (testDistribution);
//   d) if there is not: Phase 1 - Module 1 (Module_1B), which calls Module 2 (and this one, Module 3), obtains the complete distribution;
//   e) Phases 2 and 3 - Module 4 (which calls Module 5) redistributes the lessons among 3 days and allocates them to time slots;
//      then Double_Periods tries to insert double lessons into the results;
//   f) displays the timetables and prepares the output JSON (exported by the 'export' button).
public class Prouesse5D_X extends Accessory {
	// Module objects: g (Probl, holds the data), gA1 (Module 1), g4 (Module 4) and Double_Periods_ (adjustment of double lessons).
	// k1_ = 0: the process starts from Module 1; k1_ = 200: a reusable distribution already exists and the process countinues from Module 4.
	// lastQp/lastQa = numbers of Teachers and classes of the distribution saved in file;  maxUnavailablePeriods[p][d] = maximum number of unavailable
	// time slots that Teacher p can have on day d without invalidating the distribution obtained (shown to the user).
	private static Probl g;
	private static Module_1B gA1;
	private static Module_4 g4;
	static private int k1_, n, aux[], aux2_, lastQp, lastQa, maxUnavailablePeriods[][];
	static private boolean solubilidade;
	static private String lastResult, s10;
	private static Double_Periods Double_Periods_;
	private static JSONObject jsonObject;
	private static JSONArray JSONdistrPA, array1, array2;
	private static JSONObject restorationData;
	private static FileWriter file;
	private static String days_week[], sErrors_;

	// Constructor: initializes the interface (superclass) and leaves the imported content empty.
	public Prouesse5D_X() {
		super();
		aux = new int[5];
		sInput = "";
		days_week = new String[] {"", "", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", ""};
	}

	// Consistency tests of the data (Section 2.1 of the book). Accumulates in sErrors_ a message for each inconsistency found
	// and returns true if there was none. Note: sErrors_ is declared here as a local variable and hides the static field of the same
	// name (Accessory.sErrors_), which is the text that executeProcess displays in the ERRORS tab.
	public boolean consistencyTests() {
		boolean test1;
		int sum[], d, aux, aux1, aux2, j, k, k1, arranjo[], p, a;

		// Initialization: sum[a] will be used to add up the workloads of each class; arranjo[] to sort the maximum numbers of lessons per day.
		sErrors_ = "";
		sum = new int[Qa + 1];
		arranjo = new int[5];
		for (p = 1; p <= Qp; p++) {
			// Test 0: every Teacher must have classes provided.
			if (nClassesP[p] == 0) {
				sErrors_ += "ERROR: No classes were provided for Teacher " + teacherName[p] + "\n";
			}
		}

		for (p = 1; p <= Qp; p++) {
			// Tests per Teacher p:
			// - if p must have double lessons (withDoublePeriodsP), he/she cannot have a limit of 1 lesson per day on all days;
			// - if p must not have double lessons (noDoublePeriodsP), he/she cannot have a single working day (Wp0 == 1);
			// - withDoublePeriodsP is incompatible with a class of workload 1.
			test1 = true;
			for (d = 0; d <= 4; d++)
				test1 = test1 && Mp[p][d] < 2;
			if (withDoublePeriodsP[p] && test1)
				sErrors_ += "ERROR: It was stated that Teacher " + teacherName[p]
						+ " must have double lessons; however,\nit was also stated that he/she must not have more than one lesson on any day.\n";

			if (noDoublePeriodsP[p] && Wp0[p] == 1)
				sErrors_ += "ERROR: It was stated that Teacher " + teacherName[p]
						+ " must not have double lessons; however,\nit was also stated that he/she must attend only a single day of the week.\n";
			// aux1 = total weekly workload of Teacher p.
			aux1 = 0;
			for (j = 1; j <= nClassesP[p]; j++) {
				aux1 += ns[p][j];
				if (withDoublePeriodsP[p] && ns[p][j] == 1)
					sErrors_ += "ERROR: It was stated that Teacher " + teacherName[p]
							+ " must have double lessons; however,\nthe workload in one of his/her classes is 1 lesson per week.\n";
			}
			// Test (a): the sum of the maximum numbers of lessons per day, Mp[p][*], must be sufficient to fulfill the weekly workload.
			aux2 = 0;
			for (d = 0; d <= 4; d++)
				aux2 += Mp[p][d];
			if (aux1 > aux2)
				sErrors_ += "ERROR: The maximum number of lessons per day was provided\nfor Teacher " + teacherName[p]
						+ ", but the values provided for this Teacher\n"
						+ "are insufficient for him/her to fulfill his/her weekly workload\n";
			for (d = 0; d <= 4; d++)
				arranjo[d] = Mp[p][d];
			// Test (b): sorts the daily maxima and adds up the Wp0[p] largest ones; this total (maximum possible number of lessons with Wp0[p] days)
			// must also be sufficient for the workload.
			Arrays.sort(arranjo);
			aux = 0;
			for (k = 0; k < Wp0[p]; k++)
				aux += arranjo[4 - k];
			if (aux1 > aux)
				sErrors_ += "ERROR: The maximum number of working days provided for Teacher " + teacherName[p]
						+ "\nis insufficient for him/her to fulfill his/her workload.\n";
		}

		// Test per class: the sum of the workloads of all the Teachers in the class must be equal to Ma x 5 (total number of lessons in the week).
		for (a = 1; a <= Qa; a++)
			sum[a] = 0;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nClassesP[p]; j++)
				sum[list[p][j]] += ns[p][j];
		}
		for (a = 1; a <= Qa; a++) {
			if (sum[a] != Ma * 5)
				sErrors_ += "ERROR: The sum of the workloads of the subjects of class " + className[a]
						+ "\ndoes not match the total number of lessons attended during the week by each class.\n";
		}

		for (d = 0; d <= 4; d++) {
			aux1 = 0;
			for (p = 1; p <= Qp; p++)
				aux1 += Mp[p][d];
			// Test (c): on each day, the sum of the maximum numbers of lessons of the Teachers must be sufficient to serve all the classes (Ma x Qa lessons).
			if (aux1 < Ma * Qa)
				sErrors_ += "ERROR: The maximum number of lessons on " + days_week[d+2]
						+ " of each Teacher was provided, but\nthe values provided are"
						+ " insufficient to serve all the classes.\n";
		}

		for (d = 0; d <= 4; d++) {
			for (k1 = 1; k1 <= Ma; k1++) {
				// Test (d): on each day and time slot, the number of available Teachers (without an unavailable time slot and with Mp > 0) must be at least
				//   Qa.
				aux1 = Qp;
				for (p = 1; p <= Qp; p++) {
					if (unPeriods[p][d][k1] || Mp[p][d] == 0)
						aux1--;
				}
				if (aux1 < Qa)
					sErrors_ += "ERROR: " + aux1 + " is the number of Teachers available on " + days_week[d+2]
							+ ", in time slot " + k1 + ",\nan insufficient number to serve"
							+ " all the classes in that time slot.\n";
			}
		}

		for (p = 1; p <= Qp; p++) {
			for (d = 0; d <= 4; d++) {
				aux1 = 0;
				for (k1 = 1; k1 <= Ma; k1++) {
					if (unPeriods[p][d][k1])
						aux1++;
				}
				// Tests (e) and (f), per Teacher and per day:
				// (e) the maximum number of lessons provided cannot exceed the number of time slots available to the Teacher on the day;
				// (f) nor exceed the maximum possible given the number of classes (2 x nClassesP, or nClassesP if the Teacher must not have double lessons).
				if (Mp[p][d] > Ma - aux1)
					sErrors_ += "ERROR: The maximum number of lessons provided for Teacher " + teacherName[p] + " on "
							+ days_week[d+2] + " is greater than\nthe number of time slots available for the same "
							+ "Teacher on the same day.\n";
				if ((!noDoublePeriodsP[p]) && Mp[p][d] > 2 * nClassesP[p])
					sErrors_ += "ERROR: For Teacher " + teacherName[p] + " on " + days_week[d+2]
							+ " a maximum number of lessons that is too high was provided,\nconsidering that this Teacher "
							+ "has only " + nClassesP[p] + " classes.\n";
				if (noDoublePeriodsP[p] && Mp[p][d] > nClassesP[p])
					sErrors_ += "ERROR: For Teacher " + teacherName[p] + " on " + days_week[d+2]
							+ " a maximum number of lessons that is too high was provided,\nconsidering that this Teacher "
							+ "has only " + nClassesP[p]
							+ " classes and\nit was stated that he/she does not intend to have double lessons.\n";
			}
		}

		for (p = 1; p <= Qp; p++) {
			aux = 0;
			for (d = 0; d <= 4; d++) {
				if (Mp[p][d] > 0)
					aux++;
			}

			// Test (g): the maximum number of working days cannot exceed the number of days on which the Teacher is available (Mp > 0).
			if (aux < Wp0[p])
				sErrors_ += "ERROR: The maximum number of working days provided for Teacher " + teacherName[p]
						+ " is not correct,\n"
						+ "considering the number of days on which this teacheressor must not have lessons.\n";
			test1 = false;
			for (j = 1; j <= nClassesP[p]; j++) {
				if (ns[p][j] > 1)
					test1 = true;
			}
			// Without double lessons and with some class of more than 1 lesson, the Teacher needs at least 2 days of availability and of work.
			if (test1 && noDoublePeriodsP[p] && (aux < 2 || Wp0[p] < 2))
				sErrors_ += "ERROR: Teacher " + teacherName[p] + " does not attend a sufficient number of days,\n"
						+ "considering that it was stated that this Teacher must not have double lessons.\n";
		}

		for (p = 1; p <= Qp; p++) {
			// Final test: the maximum number of lessons of p in a single class is limited by the capacity of the days (1 or 2 lessons per day and per class, in up to
			//   Wp0 days); each class must fit within this limit.
			aux1 = (noDoublePeriodsP[p] ? 1 : 2);
			aux = 0;
			for (d = 0; d <= 4; d++)
				aux += Math.min(Mp[p][d], aux1);
			aux = Math.min(aux, Wp0[p] * aux1);
			for (j = 1; j <= nClassesP[p]; j++) {
				if (ns[p][j] > aux)
					sErrors_ += "ERROR: Teacher " + teacherName[p] + " does not attend a number of days "
							+ "sufficient to serve class " + className[list[p][j]] + "\n";
			}
		}
		// Returns true if no error message was accumulated.
		return (sErrors_.equals(""));
	}

	// Parses the JSON in sInput and fills in the data variables (Section 2.1). The lists in the JSON use index 1 for the first
	// element (position 0 unused), except the per-day vectors (days 0 to 4).
	protected void dataInput2() throws JSONException, IOException {
		JSONArray array1, array2, Wp0_, noDoublePeriodsP_, withDoublePeriodsP_, list_, ch_, Mp_, unPeriods_, teacherName_, className_,
				nClassesP_;
		JSONObject jsonObject;
		// Extracts from the JSON the fields: m5 (Ma), qP, qA, maxDaysP (Wp0), maxP (Mp), ch (ns), list, nClassesP, unPeriods, noDoublePeriodsP,
		//   withDoublePeriodsP and names.
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

		// Allocates the data arrays (each Teacher can have up to 30 classes) and copies the values read.
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

		for (a = 1; a <= Qa; a++)
			// Names of the classes.
			className[a] = className_.getString(a);
		for (p = 1; p <= Qp; p++) {
			// For each Teacher: name, Wp0, Mp (5 days), number of classes, classes (list) and workloads (ns).
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
			// Indicators of double lessons and unavailable time slots (the values printed to the console serve only for monitoring).
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

	// Tests whether the distribution read from the file (distrPA) can be reused for the current data; same tests as
	//   Accessory.testDistribution,
	// now using the dimensions read from the file (lastQp, lastQa). Returns true if it respects all the constraints of Section 2.2.
	public boolean testDistribution() {
		int a, j, aux;
		// Different number of Teachers or classes: the distribution belongs to another problem.
		if (Qp != lastQp || Qa != lastQa)
			return false;

		for (a = 1; a <= Qa; a++) {
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux += distrPA[p1][a][k];
				// In each class and day, no more than Ma lessons.
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
					// Per Teacher and day: no more than 1 (or 2) lessons per class, total for the day <= Mp, and working days <= Wp0.
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
					// The class must belong to the Teacher and the weekly total must be exactly the workload ns.
					while (j > 0 && list[p1][j] != a)
						j--;
					if (j == 0 || aux != ns[p1][j])
						return false;
				}
			}
		}
		return true;
	}

	// Executes the whole process of obtaining timetables (Section 4.8): reading and validation of the data, Phases 1, 2 and 3,
	// adjustment of double lessons and preparation of the results.
	protected void executeProcess() throws JSONException, IOException {
		boolean test1;
		// Step 0: if no file was imported, warns the user and terminates.
		if (sInput.equals("")) {
			JOptionPane.showMessageDialog(null, "Press IMPORT to open a file", "Result",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Step (a): extracts the data from the JSON and clears the TIPS tab.
		dataInput2();
		textAreas[TextType.TIPS.ordinal()].setText("");
		// Step (b): consistency tests; if there is an error, displays the ERRORS tab and terminates.
		if (!consistencyTests()) {
			textAreas[TextType.ERRORS.ordinal()].setText(sErrors_);
			tabsTabbedPane.setSelectedIndex(TextType.ERRORS.ordinal());
			JOptionPane.showMessageDialog(null, "Data consistency error. Open the ERRORS tab", "Result",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Copies the data into the static variables shared by all the modules (Probl).
		g = new Probl();
		g.initialization(Ma, Qp, Qa, nClassesP, list, ns, Mp, unPeriods, noDoublePeriodsP, withDoublePeriodsP, Wp0,
				teacherSequence);

		lastResult = "";
		// Step (c): checks whether distribution.json exists. If it exists and has the same number of Teachers and classes, the distribution
		// is read (distrPA format) and testd by testDistribution; if it can be reused, k1_ = 200 (skips Phases 1.1 and 1.2).
		File file2_ = new File("distribution.json");
		temDistrPronta = file2_.exists();
		k1_ = 0;
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
		s = textAreas[TextType.RESULTS.ordinal()].getText();
		// Creates the Module 1 object (Module_1B) and initializes it (allocates dd, tables and control variables).
		pointer = 0;
		gA1 = new Module_1B();
		gA1.initialization();
		// Step (d) - Phases 1.1 and 1.2 (only when there is no reusable distribution).
		// Prepares the hints text that will be displayed if the First Phase does not find a solution, with the sum of the maximum numbers of lessons per day.
		if (k1_ == 0) {
			s_ = "The First Phase found no solution. What can be done: \n\n"
					+ "Try going to the Prouesse Configuration Page and removing some slack indications, changing the maximum numbers of lessons per day\n"
					+ "of some teacheressors, in the tab \"Lessons per day of the week\", which lets you select one of two options that give the maximum numbers of lessons per day.\n "
					+ "For some teacheressors, fill in an option with few slack indications, and keep it selected.\n\n"
					+ "It is also important to keep the distribution of the days off of all the teacheressors somewhat balanced among the days of the week.\n"
					+ "The following table shows the sum of the maximum numbers of lessons provided, per day of the week, to give an idea of the distribution of the days off:\n\n";
			for (d = 0; d <= 4; d++) {
				aux1 = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux1 += Mp[p1][d];
				s_ += days_week[d+2] + ": " + aux1 + "\n";
			}
			// Module 1: preliminaries() permutes the days (hardest days first), sorts the Teachers and builds the units; returns the number of
			//   units.
			// mainV1(-1, n) executes the search (generator = -1); upon completion, Module 1 itself calls Module 2 (and this one Module 3). Returns
			//   true if there was a solution.
			n = gA1.preliminaries();
			solubilidade = gA1.mainV1(-1, n);
			// No solution: displays the TIPS tab with guidance for the user and terminates.
			if (!solubilidade) {
				JOptionPane.showMessageDialog(null, "First Phase w/o solution. Open the TIPS tab", "Result",
						JOptionPane.INFORMATION_MESSAGE);
				textAreas[TextType.RESULTS.ordinal()].setText("");
				textAreas[TextType.TIPS.ordinal()].setText(s_);
				tabsTabbedPane.setSelectedIndex(TextType.TIPS.ordinal());
				return;
			}
			JOptionPane.showMessageDialog(null, "First Phase possible ", "Result", JOptionPane.INFORMATION_MESSAGE);
			lastResult = "";
			textAreas[TextType.RESULTS.ordinal()].setText(lastResult);
			textAreas[TextType.TIPS.ordinal()].setText("");
			tabsTabbedPane.setSelectedIndex(TextType.TIPS.ordinal());

			// With a solution: undoes the permutation of the days made by Module 1. destino0[k] indicates the new position of the original day k;
			// here the inverse permutation (destino) is computed and the same swap of positions is applied to dd and to Mp,
			// restoring the original order of the days (Monday to Friday).
			temDistrPronta = true;
			for (d1 = 0; d1 <= 4; d1++)
				System.out.println(Probl.destino0[d1]);
			for (d1 = 0; d1 <= 4; d1++) {
				k1 = 0;
				while (Probl.destino0[k1] != d1)
					k1++;
				Probl.destino[d1] = k1;
			}
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
			// Converts dd (indexed by the position j of the class in the Teacher's list) to distrPA (indexed by the class number a), the format saved to
			//   file.
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

			// Saves the distribution to the file distribution.json (number of Teachers, number of classes and distribution).
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
			// Discards the Module 1 object (frees the working structures).
			gA1 = new Module_1B();
		}
		// Step (e) - Phases 2 and 3: creates Module 4 and copies the distribution (distrPA) into dd, the format used by the modules.
		g4 = new Module_4();
		for (p1 = 1; p1 <= Qp; p1++) {
			for (j = 1; j <= nClassesP[p1]; j++) {
				a = list[p1][j];
				for (k = 0; k <= 4; k++)
					Probl.dd[p1][j][k] = distrPA[p1][a][k];
			}
		}

		// Phase 2: preliminaries(1) prepares Module 4 for days 0, 1 and 2 (Monday to Wednesday); main2() redistributes the lessons
		// among those days and allocates days 0 and 1 to time slots (calling Module 5); result() fills in the timetables.
		n = g4.preliminaries(1);
		aux1 = 0;
		aux2 = 0;
		if (g4.main2()) {
			aux1 = 1;
			g4.result();
			JOptionPane.showMessageDialog(null, "Second Phase possible: Monday, Tuesday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Second Phase w/o solution: Monday, Tuesday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		}
		// Phase 3: the same for days 2, 3 and 4 (Wednesday to Friday); here the three days are allocated to time slots.
		n = g4.preliminaries(2);
		if (g4.main2()) {
			aux2 = 1;
			g4.result();
			JOptionPane.showMessageDialog(null, "Third Phase possible: Wednesday, Thursday, Friday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Third Phase w/o solution: Wednesday, Thursday, Friday ", "Result",
					JOptionPane.INFORMATION_MESSAGE);
		}
		// If Phase 2 and/or Phase 3 did not find a solution: builds and displays guidance (TIPS tab), including, per Teacher and day, the maximum
		//   number of unavailable time slots tolerated, and terminates.
		if (aux1 == 0 || aux2 == 0) {
			JOptionPane.showMessageDialog(null, "Open the TIPS tab", "Result", JOptionPane.INFORMATION_MESSAGE);
			s_ = "The Second and/or Third Phase did not find a solution. What can be done: \n\n"
					+ "Try removing some indications of unavailable time slots.\n"
					+ "Try not to keep several unavailable time slots at the same position in the day (for example, the first or the last time slot)\n"
					+ "of a given day.\n";
			if (aux1 == 0 && aux2 == 1)
				s_ += "Since only the Second Phase found no solution, you can make these changes only for Monday or Tuesday.\n";
			if (aux1 == 1 && aux2 == 0)
				s_ += "Since only the Third Phase found no solution, you can make these changes only for Wednesday, Thursday or Friday.\n";
			s_ += "Go to the Prouesse Configuration Page, make these changes and then \n"
					+ "return to this program to run the Second and Third Phases again. \n \n"
					+ "\nNext, for each Teacher, for each day, the maximum number of unavailable time slots is shown\n"
					+ "that can be set so as to allow the last distribution obtained to be reused\n"
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
		// Both phases found a solution. Adjustment of the results (Section 4.7.2): Double_Periods.insertDoublePeriods tries to create double lessons
		// for the Teachers who want them (withDoublePeriodsP), swapping lessons between days and Teachers; the distribution in dd and the timetables are updated.
		Double_Periods_ = new Double_Periods();
		System.out.println(Double_Periods_.insertDoublePeriods());
		// Converts the final distribution (dd, now with the double lessons) to the distrPA format and saves it again to distribution.json.
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

		// Text for the TIPS tab: for each Teacher and day, how many unavailable time slots can be defined without invalidating the distribution (Ma
		//   - lessons of the day); '*' if the Teacher has no lessons.
		s_ = "\nNext, for each Teacher, for each day, the maximum number of unavailable time slots is shown\n"
				+ "that can be set so as to allow the last distribution obtained to be reused\n"
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
		// Step (f): builds the text with the timetables of the Teachers and of the classes and displays it in the RESULTS tab.
		lastResult += obtain_schedule_table(Probl.tableP, teacherName, className, Qp) + "\n"
				+ obtain_schedule_table(Probl.tableA, className, teacherName, Qa)
				+ "\n \nAttention to the content present in the TIPS tab.";
		textAreas[TextType.RESULTS.ordinal()].setText(lastResult);
		tabsTabbedPane.setSelectedIndex(TextType.RESULTS.ordinal());
		// Computes maxUnavailablePeriods[p][d] = Ma - number of lessons of p on day d.
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
		// Builds the output JSON (timetables per Teacher and per class, maxUnavailablePeriods and restoration data), exported by the 'export' button.
		jsonObject.put("tableP", Probl.tableP);
		jsonObject.put("tableA", Probl.tableA);
		jsonObject.put("maxUnavailablePeriods", maxUnavailablePeriods);
		jsonObject.put("restorationData", restorationData);
		sOutput = jsonObject.toString();
		JOptionPane.showMessageDialog(null, "Press EXPORT to save the file", "Result",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// Auxiliary method (not used in this version) that reads an integer through a daylog box.
	private int readInt(String s) {
		String input;
		input = (String) JOptionPane.showInputDialog(null, s, "Input", JOptionPane.QUESTION_MESSAGE, icone, null,
				null);
		return Integer.parseInt(input);
	}

	// Entry point: opens the program window on the Swing event thread.
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Prouesse5D_X().setVisible(true);
			}
		});
	}
}
