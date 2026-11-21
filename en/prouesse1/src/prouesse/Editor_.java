package prouesse;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONException;

import prouesse.Editor_.TextType;

// Class Editor_ - graphical user interface (Swing) of the program.
//
// Role: display the main window with the 'import', 'export' and 'process' buttons and a panel of text tabs
// (NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS, RESULTS, TIPS and ERRORS). It does not solve the timetabling problem:
// it is limited to reading the JSON file with the data (import button), writing the file with the results (export button) and
// triggering the method executeProcess (process button), which is overridden by Prouesse5D_X (Section 4.8 of the book).
public class Editor_ extends JFrame {
	// Window components: buttons and panels with scroll bars (one per tab).
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
	// textAreas[] = text areas of the tabs (indexed by TextType);
	// sInput = countents of the imported JSON file (problem data);  sOutput = JSON with the results, written by the export button.
	protected JTextArea textAreas[];
	protected String sInput, sOutput;
	private final String extensao = "prouesse";

	// Tab identifiers; NUMERODEABAS is used only to size the array of text areas.
	protected enum TextType {
		NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS, RESULTS, TIPS, ERRORS, NUMERODEABAS
	}

	// Constructor: builds the window components.
	public Editor_() {
		initComponents();
	}

	// Creates and configures the components: buttons (with their event handlers), text areas, tabs and the window layout.
	private void initComponents() {
		setTitle("PROUESSE Timetable Program");

		// 1) Creation of the buttons, the tabbed panel and the text areas with scroll bars.
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

		// 2) Window configuration (the program exits when the window is closed; preferred size 800 x 400).
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(800, 400));

		// 3) 'export' button: calls salvarButtonPressionado.
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

		// 'process' button: calls processarButtonPressionado; read/JSON exceptions are only printed.
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

		// Button with no text and no function (apagarButtonPressionado is empty).
		apagarButton.setText("");
		apagarButton.setFocusable(false);
		apagarButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				apagarButtonPressionado(evt);
			}
		});

		// 4) Insertion of each text area, inside its scroll panel, as a tab of the tabbed panel.
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

		// 5) Scrollable tabs when they do not fit in the window.
		tabsTabbedPane.setFocusable(false);
		tabsTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// 6) Layout: buttons at the bottom and tabbed panel occupying the rest of the window; pack() adjusts the size.
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

	// 'export' button: opens a daylog box to choose the destination file (extension .json appended to the name)
	// and writes the countents of sOutput to it; write or close errors are shown in message boxes.
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

	// 'import' button: opens a daylog box to choose the JSON file with the problem data and
	// reads the file line by line, accumulating its countents in sInput (which will be interpreted by dataInput2).
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

	// 'process' button: delegates to executeProcess().
	private void processarButtonPressionado(java.awt.event.ActionEvent evt) throws JSONException, IOException {
		executeProcess();
	}

	private void apagarButtonPressionado(java.awt.event.ActionEvent evt) {
	}

	// Empty method, overridden by the executable class Prouesse5D_X, which performs all the processing.
	protected void executeProcess() throws JSONException, IOException {
	}
}
