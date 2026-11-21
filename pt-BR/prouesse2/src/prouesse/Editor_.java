package prouesse;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONException;

import prouesse.Editor_.TipoDeTexto;

// Classe Editor_ - exibe os componentes visuais da interface com o usuário (Seção 4.8 do livro).
//
// Papel: janela (JFrame) com quatro botões (exportar, importar, processar e um quarto botão sem função) e uma pilha de abas com
// áreas de texto. O botão 'importar' lê um arquivo JSON com os dados do problema para a variável sEntrada; o botão 'processar'
// chama executaProcessar (método vazio aqui, redefinido em Prouesse_teste); o botão 'exportar' grava o conteúdo de sSaida em um
// arquivo JSON. As abas são: NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS (dados), RESULTADOS (quadros de horários),
// DICAS (sugestões ao usuário) e ERROS (mensagens dos testes de consistência).
public class Editor_ extends JFrame {
	// Botões da janela e componentes de abas (painéis de rolagem e áreas de texto).
	private JButton abrirButton;
	private JButton salvarButton;
	private JButton processarButton;
	private JButton apagarButton;
	protected JTabbedPane abasTabbedPane;
	private JScrollPane turmasScrollPane;
	private JScrollPane maximoAulasScrollPane;
	private JScrollPane diasTrabalhoScrollPane;
	private JScrollPane dadosDiversosScrollPane;
	private JScrollPane nomesScrollPane;
	private JScrollPane resultadosScrollPane;
	private JScrollPane dicasScrollPane;
	private JScrollPane errosScrollPane;
	// areasDeTexto[tipo]: área de texto de cada aba, indexada pelos valores do enum TipoDeTexto.
	protected JTextArea areasDeTexto[];

	// sEntrada: texto JSON dos dados do problema; sSaida: texto JSON com os resultados, a ser gravado ao exportar.
	protected String sEntrada, sSaida;
	// Extensão de arquivo de referência.
	private final String extensao = "prouesse";

	// Tipos de texto/abas da interface; NUMERODEABAS é o número total de abas.
	protected enum TipoDeTexto {
		NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS, RESULTADOS, DICAS, ERROS, NUMERODEABAS
	}

	// Construtor: monta a janela.
	public Editor_() {
		initComponents();
	}

	// Cria e configura os componentes: botões, painéis de rolagem, áreas de texto, abas e o layout da janela.
	private void initComponents() {
		// Título da janela.
		setTitle("PROUESSE Programa de Hor?rios");

		// Criação dos botões e das áreas de texto.
		salvarButton = new JButton();
		abrirButton = new JButton();
		processarButton = new JButton();
		apagarButton = new JButton();
		abasTabbedPane = new JTabbedPane();
		areasDeTexto = new JTextArea[TipoDeTexto.NUMERODEABAS.ordinal()];
		turmasScrollPane = new JScrollPane();
		areasDeTexto[TipoDeTexto.TURMAS.ordinal()] = new JTextArea();
		maximoAulasScrollPane = new JScrollPane();
		areasDeTexto[TipoDeTexto.MAXIMODEAULAS.ordinal()] = new JTextArea();
		diasTrabalhoScrollPane = new JScrollPane();
		areasDeTexto[TipoDeTexto.DIASDETRABALHO.ordinal()] = new JTextArea();
		dadosDiversosScrollPane = new JScrollPane();
		areasDeTexto[TipoDeTexto.DADOSDIVERSOS.ordinal()] = new JTextArea();
		resultadosScrollPane = new JScrollPane();
		areasDeTexto[TipoDeTexto.NOMES.ordinal()] = new JTextArea();
		nomesScrollPane = new JScrollPane();
		String s14 = "";
		areasDeTexto[TipoDeTexto.NOMES.ordinal()].setText(s14);
		areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()] = new JTextArea();
		dicasScrollPane = new JScrollPane();
		areasDeTexto[TipoDeTexto.DICAS.ordinal()] = new JTextArea();
		errosScrollPane = new JScrollPane();
		areasDeTexto[TipoDeTexto.ERROS.ordinal()] = new JTextArea();

		// Fecha o programa ao fechar a janela; define o tamanho preferencial.
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(800, 400));

		// Botão 'exportar': chama salvarButtonPressionado.
		salvarButton.setText("exportar");
		salvarButton.setFocusable(false);
		salvarButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				salvarButtonPressionado(evt);
			}
		});

		// Botão 'importar': chama abrirButtonPressionado.
		abrirButton.setText("importar");
		abrirButton.setFocusable(false);
		abrirButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				abrirButtonPressionado(evt);
			}
		});

		// Botão 'processar': chama processarButtonPressionado, que executa o processamento.
		processarButton.setText("processar");
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

		// Quarto botão (sem texto nem ação definida).
		apagarButton.setText("");
		apagarButton.setFocusable(false);
		apagarButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				apagarButtonPressionado(evt);
			}
		});

		// Inserção das áreas de texto nas abas, na ordem do enum TipoDeTexto.
		areasDeTexto[TipoDeTexto.NOMES.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.NOMES.ordinal()].setRows(5);
		nomesScrollPane.setViewportView(areasDeTexto[TipoDeTexto.NOMES.ordinal()]);
		abasTabbedPane.insertTab("1", null, nomesScrollPane, null, TipoDeTexto.NOMES.ordinal());

		areasDeTexto[TipoDeTexto.TURMAS.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.TURMAS.ordinal()].setRows(5);
		turmasScrollPane.setViewportView(areasDeTexto[TipoDeTexto.TURMAS.ordinal()]);
		abasTabbedPane.insertTab("2", null, turmasScrollPane, null, TipoDeTexto.TURMAS.ordinal());

		areasDeTexto[TipoDeTexto.MAXIMODEAULAS.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.MAXIMODEAULAS.ordinal()].setRows(5);
		maximoAulasScrollPane.setViewportView(areasDeTexto[TipoDeTexto.MAXIMODEAULAS.ordinal()]);
		abasTabbedPane.insertTab("3", null, maximoAulasScrollPane, null, TipoDeTexto.MAXIMODEAULAS.ordinal());

		areasDeTexto[TipoDeTexto.DIASDETRABALHO.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.DIASDETRABALHO.ordinal()].setRows(5);
		diasTrabalhoScrollPane.setViewportView(areasDeTexto[TipoDeTexto.DIASDETRABALHO.ordinal()]);
		abasTabbedPane.insertTab("4", null, diasTrabalhoScrollPane, null, TipoDeTexto.DIASDETRABALHO.ordinal());

		areasDeTexto[TipoDeTexto.DADOSDIVERSOS.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.DADOSDIVERSOS.ordinal()].setRows(5);
		dadosDiversosScrollPane.setViewportView(areasDeTexto[TipoDeTexto.DADOSDIVERSOS.ordinal()]);
		abasTabbedPane.insertTab("5", null, dadosDiversosScrollPane, null, TipoDeTexto.DADOSDIVERSOS.ordinal());

		areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setRows(5);
		resultadosScrollPane.setViewportView(areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()]);
		abasTabbedPane.insertTab("resultados", null, resultadosScrollPane, null, TipoDeTexto.RESULTADOS.ordinal());

		areasDeTexto[TipoDeTexto.DICAS.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.DICAS.ordinal()].setRows(5);
		dicasScrollPane.setViewportView(areasDeTexto[TipoDeTexto.DICAS.ordinal()]);
		abasTabbedPane.insertTab("dicas", null, dicasScrollPane, null, TipoDeTexto.DICAS.ordinal());

		areasDeTexto[TipoDeTexto.ERROS.ordinal()].setColumns(20);
		areasDeTexto[TipoDeTexto.ERROS.ordinal()].setRows(5);
		errosScrollPane.setViewportView(areasDeTexto[TipoDeTexto.ERROS.ordinal()]);
		abasTabbedPane.insertTab("erros", null, errosScrollPane, null, TipoDeTexto.ERROS.ordinal());

		// Layout da janela (GroupLayout): as abas ocupam a parte superior e, abaixo delas, fica a linha de botões.
		abasTabbedPane.setFocusable(false);
		abasTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(abrirButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(salvarButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(processarButton)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(apagarButton)
						.addGap(0, 336, Short.MAX_VALUE))
				.addComponent(abasTabbedPane));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(abasTabbedPane, GroupLayout.DEFAULT_SIZE, 285, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(abrirButton)
								.addComponent(processarButton).addComponent(apagarButton).addComponent(salvarButton,
										GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap()));

		pack();
	}

	// Grava sSaida em um arquivo JSON escolhido pelo usuário (acrescenta a extensão '.json' ao nome informado).
	private void salvarButtonPressionado(java.awt.event.ActionEvent evt) {
		JFileChooser destinoDialogo = new JFileChooser();
		FileNameExtensionFilter filtro = new FileNameExtensionFilter("Arquivo json", "json");
		destinoDialogo.setFileFilter(filtro);
		if (destinoDialogo.showSaveDialog(this) != JFileChooser.CANCEL_OPTION) {
			String destino = destinoDialogo.getSelectedFile().getPath() + ".json";

			BufferedWriter output = null;
			try {
				output = new BufferedWriter(new FileWriter(destino));
				output.write(sSaida);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao exportar arquivo",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao fechar arquivo",
							JOptionPane.ERROR_MESSAGE);
				}
			}

		}
	}

	// Lê o arquivo JSON escolhido pelo usuário e guarda seu conteúdo, linha a linha, em sEntrada.
	private void abrirButtonPressionado(java.awt.event.ActionEvent evt) {
		JFileChooser origemDialogo = new JFileChooser();
		FileNameExtensionFilter filtro = new FileNameExtensionFilter("Arquivo json", "json");
		origemDialogo.setFileFilter(filtro);
		if (origemDialogo.showOpenDialog(this) != JFileChooser.CANCEL_OPTION) {
			String origem = origemDialogo.getSelectedFile().getPath();

			sEntrada = "";
			BufferedReader input = null;
			try {
				input = new BufferedReader(new FileReader(origem));

				String linha;
				while ((linha = input.readLine()) != null) {
					sEntrada += linha;
					sEntrada += System.getProperty("line.separator");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao Importar arquivo",
						JOptionPane.ERROR_MESSAGE);
			} finally {
				try {
					if (input != null) {
						input.close();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Erro ao fechar arquivo",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	// Ação do botão 'processar': delega a executaProcessar.
	private void processarButtonPressionado(java.awt.event.ActionEvent evt) throws JSONException, IOException {
		executaProcessar();
	}

	// Ação do quarto botão: não faz nada.
	private void apagarButtonPressionado(java.awt.event.ActionEvent evt) {
	}

	// Método a ser redefinido pela subclasse (Prouesse_teste), que executa o processamento dos dados.
	protected void executaProcessar() throws JSONException, IOException {
	}
}
