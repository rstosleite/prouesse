package prouesse;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONException;

import prouesse.Editor_.TextType;

// Class Editor_ - displays the visual components of the user interface (Section 4.8 of the book).
//
// Role: window (JFrame) with four buttons (export, import, process and a fourth button with no function) and a stack of tabs with
// text areas. The 'import' button reads a JSON file with the problem data into the variable sInput; the 'process' button
// calls executeProcess (an empty method here, overridden in Prouesse_test); the 'export' button writes the contents of sOutput to a
// JSON file. The tabs are: NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS (data), RESULTS (timetables),
// TIPS (suggestions to the user) and ERRORS (messages from the consistency tests).
public class Editor_ extends JFrame {
	// Window buttons and tab components (scroll panes and text areas).
	private JButton abrirButton;
	private JButton salvarButton;
	private JButton processarButton;
	private JButton apagarButton;
	protected JTabbedPane tabsTabbedPane;
	private JScrollPane classesScrollPane;
	private JScrollPane maximumAulasScrollPane;
	private JScrollPane daysTrabalhoScrollPane;
	private JScrollPane dataDiversosScrollPane;
	private JScrollPane namesScrollPane;
	private JScrollPane resultsScrollPane;
	private JScrollPane dicasScrollPane;
	private JScrollPane errorsScrollPane;
	// textAreas[tipo]: text area of each tab, indexed by the values of the enum TextType.
	protected JTextArea textAreas[];

	// sInput: JSON text of the problem data; sOutput: JSON text with the results, to be saved on export.
	protected String sInput, sOutput;
	// Reference file extension.
	private final String extensao = "prouesse";

	// Types of text/tabs of the interface; NUMERODEABAS is the total number of tabs.
	protected enum TextType {
		NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS, RESULTS, TIPS, ERRORS, NUMERODEABAS
	}

	// Constructor: builds the window.
	public Editor_() {
		initComponents();
	}

	// Creates and configures the components: buttons, scroll panes, text areas, tabs and the window layout.
	private void initComponents() {
		// Window title.
		setTitle("PROUESSE Timetable Program");

		// Creation of the buttons and text areas.
		salvarButton = new JButton();
		abrirButton = new JButton();
		processarButton = new JButton();
		apagarButton = new JButton();
		tabsTabbedPane = new JTabbedPane();
		textAreas = new JTextArea[TextType.NUMERODEABAS.ordinal()];
		classesScrollPane = new JScrollPane();
		textAreas[TextType.TURMAS.ordinal()] = new JTextArea();
		maximumAulasScrollPane = new JScrollPane();
		textAreas[TextType.MAXIMODEAULAS.ordinal()] = new JTextArea();
		daysTrabalhoScrollPane = new JScrollPane();
		textAreas[TextType.DIASDETRABALHO.ordinal()] = new JTextArea();
		dataDiversosScrollPane = new JScrollPane();
		textAreas[TextType.DADOSDIVERSOS.ordinal()] = new JTextArea();
		resultsScrollPane = new JScrollPane();
		textAreas[TextType.NOMES.ordinal()] = new JTextArea();
		namesScrollPane = new JScrollPane();
		String s14 = "";
		textAreas[TextType.NOMES.ordinal()].setText(s14);
		textAreas[TextType.RESULTS.ordinal()] = new JTextArea();
		dicasScrollPane = new JScrollPane();
		textAreas[TextType.TIPS.ordinal()] = new JTextArea();
		errorsScrollPane = new JScrollPane();
		textAreas[TextType.ERRORS.ordinal()] = new JTextArea();

		// Exits the program when the window is closed; sets the preferred size.
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(800, 400));

		// 'export' button: calls salvarButtonPressionado.
		salvarButton.setText("export");
		salvarButton.setFocusable(false);
		salvarButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				salvarButtonPressionado(evt);
			}
		});

		// 'import' button: calls abrirButtonPressionado.
		abrirButton.setText("import");
		abrirButton.setFocusable(false);
		abrirButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				abrirButtonPressionado(evt);
			}
		});

		// 'process' button: calls processarButtonPressionado, which runs the processing.
		processarButton.setText("process");
		processarButton.setFocusable(false);
		processarButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					processarButtonPressionado(evt);
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				}
			}
		});

		// Fourth button (no text and no action defined).
		apagarButton.setText("");
		apagarButton.setFocusable(false);
		apagarButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				apagarButtonPressionado(evt);
			}
		});

		// Insertion of the text areas into the tabs, in the order of the enum TextType.
		textAreas[TextType.NOMES.ordinal()].setColumns(20);
		textAreas[TextType.NOMES.ordinal()].setRows(5);
		namesScrollPane.setViewportView(textAreas[TextType.NOMES.ordinal()]);
		tabsTabbedPane.insertTab("1", null, namesScrollPane, null, TextType.NOMES.ordinal());

		textAreas[TextType.TURMAS.ordinal()].setColumns(20);
		textAreas[TextType.TURMAS.ordinal()].setRows(5);
		classesScrollPane.setViewportView(textAreas[TextType.TURMAS.ordinal()]);
		tabsTabbedPane.insertTab("2", null, classesScrollPane, null, TextType.TURMAS.ordinal());

		textAreas[TextType.MAXIMODEAULAS.ordinal()].setColumns(20);
		textAreas[TextType.MAXIMODEAULAS.ordinal()].setRows(5);
		maximumAulasScrollPane.setViewportView(textAreas[TextType.MAXIMODEAULAS.ordinal()]);
		tabsTabbedPane.insertTab("3", null, maximumAulasScrollPane, null, TextType.MAXIMODEAULAS.ordinal());

		textAreas[TextType.DIASDETRABALHO.ordinal()].setColumns(20);
		textAreas[TextType.DIASDETRABALHO.ordinal()].setRows(5);
		daysTrabalhoScrollPane.setViewportView(textAreas[TextType.DIASDETRABALHO.ordinal()]);
		tabsTabbedPane.insertTab("4", null, daysTrabalhoScrollPane, null, TextType.DIASDETRABALHO.ordinal());

		textAreas[TextType.DADOSDIVERSOS.ordinal()].setColumns(20);
		textAreas[TextType.DADOSDIVERSOS.ordinal()].setRows(5);
		dataDiversosScrollPane.setViewportView(textAreas[TextType.DADOSDIVERSOS.ordinal()]);
		tabsTabbedPane.insertTab("5", null, dataDiversosScrollPane, null, TextType.DADOSDIVERSOS.ordinal());

		textAreas[TextType.RESULTS.ordinal()].setColumns(20);
		textAreas[TextType.RESULTS.ordinal()].setRows(5);
		resultsScrollPane.setViewportView(textAreas[TextType.RESULTS.ordinal()]);
		tabsTabbedPane.insertTab("results", null, resultsScrollPane, null, TextType.RESULTS.ordinal());

		textAreas[TextType.TIPS.ordinal()].setColumns(20);
		textAreas[TextType.TIPS.ordinal()].setRows(5);
		dicasScrollPane.setViewportView(textAreas[TextType.TIPS.ordinal()]);
		tabsTabbedPane.insertTab("tips", null, dicasScrollPane, null, TextType.TIPS.ordinal());

		textAreas[TextType.ERRORS.ordinal()].setColumns(20);
		textAreas[TextType.ERRORS.ordinal()].setRows(5);
		errorsScrollPane.setViewportView(textAreas[TextType.ERRORS.ordinal()]);
		tabsTabbedPane.insertTab("errors", null, errorsScrollPane, null, TextType.ERRORS.ordinal());

		// Window layout (GroupLayout): the tabs occupy the upper part and, below them, comes the row of buttons.
		tabsTabbedPane.setFocusable(false);
		tabsTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(abrirButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(salvarButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(processarButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(apagarButton)
						.addGap(0, 336, Short.MAX_VALUE))
				.addComponent(tabsTabbedPane));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(tabsTabbedPane, GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(abrirButton)
								.addComponent(processarButton).addComponent(apagarButton).addComponent(salvarButton,
										GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap()));

		pack();
	}

	// Writes sOutput to a JSON file chosen by the user (appends the '.json' extension to the name entered).
	private void salvarButtonPressionado(java.awt.event.ActionEvent evt) {
		JFileChooser destinoDialogo = new JFileChooser();
		FileNameExtensionFilter filtro = new FileNameExtensionFilter("JSON file", "json");
		destinoDialogo.setFileFilter(filtro);
		if (destinoDialogo.showSaveDialog(this) != JFileChooser.CANCEL_OPTION) {
			String destino = destinoDialogo.getSelectedFile().getPath() + ".json";

			BufferedWriter output = null;
			try {
				output = new BufferedWriter(new FileWriter(destino));
				output.write(sOutput);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error exporting file",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error closing file",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}
	}

	// Reads the JSON file chosen by the user and stores its contents, line by line, in sInput.
	private void abrirButtonPressionado(java.awt.event.ActionEvent evt) {
		JFileChooser origemDialogo = new JFileChooser();
		FileNameExtensionFilter filtro = new FileNameExtensionFilter("JSON file", "json");
		origemDialogo.setFileFilter(filtro);
		if (origemDialogo.showOpenDialog(this) != JFileChooser.CANCEL_OPTION) {
			String origem = origemDialogo.getSelectedFile().getPath();

			sInput = "";
			BufferedReader input = null;
			try {
				input = new BufferedReader(new FileReader(origem));

				String row;
				while ((row = input.readLine()) != null) {
					sInput += row;
					sInput += System.getProperty("line.separator");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error importing file",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				try {
					if (input != null) {
						input.close();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error closing file",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	// Action of the 'process' button: delegates to executeProcess.
	private void processarButtonPressionado(java.awt.event.ActionEvent evt) throws JSONException, IOException {
		executeProcess();
	}

	// Action of the fourth button: does nothing.
	private void apagarButtonPressionado(java.awt.event.ActionEvent evt) {
	}

	// Method to be overridden by the subclass (Prouesse_test), which processes the data.
	protected void executeProcess() throws JSONException, IOException {
	}
}
