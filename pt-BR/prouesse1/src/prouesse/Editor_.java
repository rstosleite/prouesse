package prouesse;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONException;

import prouesse.Editor_.TipoDeTexto;

// Classe Editor_ - interface gráfica (Swing) do programa.
//
// Papel: exibir a janela principal com os botões 'importar', 'exportar', 'processar' e um painel de abas de texto
// (NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS, RESULTADOS, DICAS e ERROS). Ela não resolve o problema de horários:
// limita-se a ler o arquivo JSON com os dados (botão importar), a gravar o arquivo com os resultados (botão exportar) e a
// acionar o método executaProcessar (botão processar), que é sobrescrito por Prouesse5D_X (Seção 4.8 do livro).
public class Editor_ extends JFrame {
	// Componentes da janela: botões e painéis com barras de rolagem (um por aba).
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
	// areasDeTexto[] = áreas de texto das abas (indexadas por TipoDeTexto);
	// sEntrada = conteúdo do arquivo JSON importado (dados do problema);  sSaida = JSON com os resultados, gravado pelo botão exportar.
	protected JTextArea areasDeTexto[];
	protected String sEntrada, sSaida;
	private final String extensao = "prouesse";

	// Identificadores das abas; NUMERODEABAS serve apenas para dimensionar o arranjo de áreas de texto.
	protected enum TipoDeTexto {
		NOMES, TURMAS, MAXIMODEAULAS, DIASDETRABALHO, DADOSDIVERSOS, RESULTADOS, DICAS, ERROS, NUMERODEABAS
	}

	// Construtor: monta os componentes da janela.
	public Editor_() {
		initComponents();
	}

	// Cria e configura os componentes: botões (com seus tratadores de eventos), áreas de texto, abas e o layout da janela.
	private void initComponents() {
		setTitle("PROUESSE Programa de Horários");

		// 1) Criação dos botões, do painel de abas e das áreas de texto com barras de rolagem.
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

		// 2) Configuração da janela (fecha o programa ao ser fechada; tamanho preferido 800 x 400).
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setPreferredSize(new java.awt.Dimension(800, 400));

		// 3) Botão 'exportar': chama salvarButtonPressionado.
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

		// Botão 'processar': chama processarButtonPressionado; exceções de leitura/JSON são apenas impressas.
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

		// Botão sem texto e sem função (apagarButtonPressionado está vazio).
		apagarButton.setText("");
		apagarButton.setFocusable(false);
		apagarButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				apagarButtonPressionado(evt);
			}
		});

		// 4) Inserção de cada área de texto, dentro do seu painel de rolagem, como uma aba do painel de abas.
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

		// 5) Abas roláveis quando não cabem na janela.
		abasTabbedPane.setFocusable(false);
		abasTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		// 6) Layout: botões na parte inferior e painel de abas ocupando o restante da janela; pack() ajusta o tamanho.
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

	// Botão exportar: abre uma caixa de diálogo para escolher o arquivo de destino (extensão .json acrescentada ao nome)
	// e grava nele o conteúdo de sSaida; erros de escrita ou de fechamento são exibidos em caixas de mensagem.
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

	// Botão importar: abre uma caixa de diálogo para escolher o arquivo JSON com os dados do problema e
	// le o arquivo linha por linha, acumulando o conteúdo em sEntrada (que sera interpretado por entradaDeDados2).
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

	// Botão processar: delega para executaProcessar().
	private void processarButtonPressionado(java.awt.event.ActionEvent evt) throws JSONException, IOException {
		executaProcessar();
	}

	private void apagarButtonPressionado(java.awt.event.ActionEvent evt) {
	}

	// Método vazio, sobrescrito pela classe executável Prouesse5D_X, que realiza todo o processamento.
	protected void executaProcessar() throws JSONException, IOException {
	}
}
