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

import prouesse.Editor_.TipoDeTexto;

// Classe Prouesse_teste - classe executável do Algoritmo P2 (Seção 4.8 do livro e Seção 5.4.3, item f).
//
// Papel: controla e gerencia a execução das demais classes (contém o método main). Herda de Horar_ (que herda de Editor_, a
// interface gráfica). Quando o usuário aciona o botão de processamento, o método executaProcessar:
//   (a) lê os dados do problema a partir do texto JSON em sEntrada (entradaDeDados2) e verifica sua consistência (testesDeConsistencia);
//   (b) se existe o arquivo distribuição.json com uma distribuição aproveitável (testaDistribuicao), pula a Fase 1;
//   (c) caso contrário executa a Fase 1 (Módulo 2A, que chama o Módulo 2B e este os Módulos 3B e 3A), desfaz a permutação
//       dos dias feita na Fase 1 e grava a distribuição obtida em distribuição.json;
//   (d) executa as Fases 2 e 3 (Módulo 4, que chama o Módulo 5);
//   (e) insere aulas geminadas (Resultado4);
//   (f) grava a nova distribuição e exibe os quadros de horários e as dicas.
public class Prouesse_teste extends Horar_ {
	// g: objeto da superclasse Probl, que guarda os dados do problema; gA1: Módulo 2A (Fase 1); g4: Módulo 4 (Fases 2 e 3).
	private static Probl g;
	private static Module_2A gA1;
	private static Module_4 g4;
	// k1_: 0 = a distribuição será obtida do zero (Fase 1); 200 = há uma distribuição aproveitável e a execução continua
	// pela Fase 2; lastQp e lastQa: números de Profs. e de turmas da distribuição gravada; maxHorariosND: máximo de horários
	// não disponíveis que cada Prof. pode ter em cada dia sem invalidar a distribuição obtida.
	static private int k1_, n, aux[], aux2_, lastQp, lastQa, maxHorariosND[][];
	// solubilidade: resultado da Fase 1; ultimoResultado e s10: textos exibidos nas abas da interface.
	static private boolean solubilidade;
	static private String ultimoResultado, s10;
	// Resultado4_: objeto que insere as aulas geminadas; objeto_JSON, JSONdistrPA, array1, array2, dados_restauracao e file: leitura e gravação de JSON.
	private static Resultado4 Resultado4_;
	private static JSONObject objeto_JSON;
	private static JSONArray JSONdistrPA, array1, array2;
	private static JSONObject dados_restauracao;
	private static FileWriter file;

	// Construtor: inicializa a interface (Horar_) e as variáveis auxiliares.
	public Prouesse_teste() {
		super();
		aux = new int[5];
		sEntrada = "";
	}

	// Testes de consistência dos dados (Seção 2.1 do livro). Acumula em sErros_ as mensagens de erro e retorna true se não há nenhum.
	// Verifica, entre outros: Profs. sem turmas; coerência entre as preferências de geminadas, Mp e Wp0; que os máximos de aulas por
	// dia Mp[p][*] e o máximo de dias Wp0[p] permitem cumprir a carga semanal de cada Prof. (testes a e b); que a soma das cargas de cada
	// turma é Ma*5; que a soma de Mp em cada dia atinge Ma*Qa (c); que em cada horário há pelo menos Qa Profs. disponíveis (d); que
	// Mp[p][d] não excede os horários disponíveis (e) nem o máximo possível dadas as turmas do Prof. (f); que Wp0[p] não excede os
	// dias disponíveis de p (g); e que cada turma de p pode receber suas aulas nos dias de trabalho de p.
	public boolean testesDeConsistencia() {
		boolean test1;
		int soma[], d, aux, aux1, aux2, j, k, k1, arranjo[], p, a;
		String sErros_;

		// sErros_ local (mensagens de erro desta verificação).
		sErros_ = "";
		soma = new int[Qa + 1];
		arranjo = new int[5];
		// Cada Prof. deve ter pelo menos uma turma.
		for (p = 1; p <= Qp; p++) {
			if (nTurmasP[p] == 0) {
				sErros_ += "ERRO: Não foram informadas as turmas do prof. " + nomeP[p] + "\n";
			}
		}

		// Coerência entre as preferências de aulas geminadas e os máximos de aulas por dia, o número de dias e as cargas.
		for (p = 1; p <= Qp; p++) {
			test1 = true;
			for (d = 0; d <= 4; d++)
				test1 = test1 && Mp[p][d] < 2;
			if (comGeminadasP[p] && test1)
				sErros_ += "ERRO: Foi informado que prof. " + nomeP[p]
						+ " deve ter aulas geminadas; porém,\ntambém foi informado que o mesmo não deve ter mais de uma aula em nenhum dia.\n";

			if (semGeminadasP[p] && Wp0[p] == 1)
				sErros_ += "ERRO: Foi informado que prof. " + nomeP[p]
						+ " não deve ter aulas geminadas; porém,\ntambém foi informado que o mesmo deve comparecer um único dia da semana.\n";
			aux1 = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				aux1 += ns[p][j];
				if (comGeminadasP[p] && ns[p][j] == 1)
					sErros_ += "ERRO: Foi informado que prof. " + nomeP[p]
							+ " deve ter aulas geminadas; porém,\na carga horária numa de suas turmas é de 1 aula por semana.\n";
			}
			// Teste (a) da Seção 2.1: a soma dos máximos por dia deve cobrir a carga semanal do Prof.
			aux2 = 0;
			for (d = 0; d <= 4; d++)
				aux2 += Mp[p][d];
			if (aux1 > aux2)
				sErros_ += "ERRO: Foi informado o número máximo de aulas por dia\ndo prof. " + nomeP[p]
						+ ", porém os valores informados para esse prof.\n"
						+ "são insuficientes para que o mesmo possa cumprir sua carga horária semanal\n";
			// Teste (b): os Wp0[p] maiores máximos por dia devem cobrir a carga semanal.
			for (d = 0; d <= 4; d++)
				arranjo[d] = Mp[p][d];
			Arrays.sort(arranjo);
			aux = 0;
			for (k = 0; k < Wp0[p]; k++)
				aux += arranjo[4 - k];
			if (aux1 > aux)
				sErros_ += "ERRO: O número máximo de dias em trabalho informado para o prof. " + nomeP[p]
						+ "\né insuficiente para que o mesmo possa cumprir sua carga horária.\n";
		}

		// A soma das cargas horárias das turmas deve ser Ma * 5 (aulas por semana de cada turma).
		for (a = 1; a <= Qa; a++)
			soma[a] = 0;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nTurmasP[p]; j++)
				soma[lista[p][j]] += ns[p][j];
		}
		for (a = 1; a <= Qa; a++) {
			if (soma[a] != Ma * 5)
				sErros_ += "ERRO: A soma das cargas horárias das disciplinas da turma " + nomeA[a]
						+ "\nnão coincide com o total de aulas assistidas durante a semana por cada turma.\n";
		}

		// Teste (c): em cada dia, a soma dos máximos dos Profs. deve atingir Ma*Qa, o total de aulas do dia.
		for (d = 0; d <= 4; d++) {
			aux1 = 0;
			for (p = 1; p <= Qp; p++)
				aux1 += Mp[p][d];
			if (aux1 < Ma * Qa)
				sErros_ += "ERRO: Foi informado o número máximo de aulas na " + (d + 2)
						+ "a-Feira de cada prof., porém\nos valores informados são"
						+ "insuficientes para atender a todas as turmas.\n";
		}

		// Teste (d): em cada horário de cada dia, deve haver ao menos Qa Profs. disponíveis (horário disponível e Mp > 0).
		for (d = 0; d <= 4; d++) {
			for (k1 = 1; k1 <= Ma; k1++) {
				aux1 = Qp;
				for (p = 1; p <= Qp; p++) {
					if (horariosND[p][d][k1] || Mp[p][d] == 0)
						aux1--;
				}
				if (aux1 < Qa)
					sErros_ += "ERRO: " + aux1 + " é o número de profs. disponíveis na " + (d + 2)
							+ "a-Feira, no horário " + k1 + ",\nnúmero insuficiente para atender"
							+ "a todas as turmas nesse horário.\n";
			}
		}

		// Testes (e) e (f): Mp[p][d] não pode exceder os horários disponíveis do Prof. no dia, nem o máximo possível dado o número de turmas.
		for (p = 1; p <= Qp; p++) {
			for (d = 0; d <= 4; d++) {
				aux1 = 0;
				for (k1 = 1; k1 <= Ma; k1++) {
					if (horariosND[p][d][k1])
						aux1++;
				}
				if (Mp[p][d] > Ma - aux1)
					sErros_ += "ERRO: O número máximo de aulas que foi informado para o prof. " + nomeP[p] + " na "
							+ (d + 2) + "a-Feira é maior que\no número de horários disponíveis do mesmo "
							+ "prof. no mesmo dia.\n";
				if ((!semGeminadasP[p]) && Mp[p][d] > 2 * nTurmasP[p])
					sErros_ += "ERRO: Foi informado para o prof. " + nomeP[p] + " na " + (d + 2)
							+ "a-Feira um número máximo de aulas muito alto,\nconsiderando que esse prof. "
							+ "tem apenas " + nTurmasP[p] + " turmas.\n";
				if (semGeminadasP[p] && Mp[p][d] > nTurmasP[p])
					sErros_ += "ERRO: Foi informado para o prof. " + nomeP[p] + " na " + (d + 2)
							+ "a-Feira um número máximo de aulas muito alto,\nconsiderando que esse prof. "
							+ "tem apenas " + nTurmasP[p]
							+ " turmas e\nfoi informado que o mesmo não pretende ter aula geminada.\n";
			}
		}

		// Teste (g): Wp0[p] não pode exceder o número de dias em que p está disponível; geminadas proibidas exigem dias suficientes.
		for (p = 1; p <= Qp; p++) {
			aux = 0;
			for (d = 0; d <= 4; d++) {
				if (Mp[p][d] > 0)
					aux++;
			}

			if (aux < Wp0[p])
				sErros_ += "ERRO: O número máximo de dias em trabalho informado para o prof. " + nomeP[p]
						+ " não está correto,\n"
						+ "considerando-se o número de dias para os quais não devem haver aulas deste professor.\n";
			test1 = false;
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (ns[p][j] > 1)
					test1 = true;
			}
			if (test1 && semGeminadasP[p] && (aux < 2 || Wp0[p] < 2))
				sErros_ += "ERRO: O prof. " + nomeP[p] + " não frequenta um número de dias suficiente,\n"
						+ "considerando-se que foi informado que este prof. não deverá ter aulas geminadas.\n";
		}

		// Cada turma de p deve poder receber suas aulas nos dias de trabalho de p (com 1 ou 2 aulas por dia, conforme a preferência).
		for (p = 1; p <= Qp; p++) {
			aux1 = (semGeminadasP[p] ? 1 : 2);
			aux = 0;
			for (d = 0; d <= 4; d++)
				aux += Math.min(Mp[p][d], aux1);
			aux = Math.min(aux, Wp0[p] * aux1);
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (ns[p][j] > aux)
					sErros_ += "ERRO: O prof. " + nomeP[p] + " não frequenta um número de dias "
							+ "suficiente para atender a turma " + nomeA[lista[p][j]] + "\n";
			}
		}
		return (sErros_.equals(""));
	}

	// Lê os dados do problema do JSON em sEntrada para as variáveis estáticas: Ma ('m5'), Qp, Qa, Wp0 ('maxDiasP'), Mp ('maxP'),
	// nTurmasP, lista, ns ('ch' = carga horária de cada turma, indexada pela posição j da turma), horariosND, semGeminadasP,
	// comGeminadasP e os nomes dos Profs. e das turmas. Imprime no console os valores lidos de ns e horariosND.
	protected void entradaDeDados2() throws JSONException, IOException {
		JSONArray array1, array2, Wp0_, semGeminadasP_, comGeminadasP_, lista_, ch_, Mp_, horariosND_, nomeP_, nomeA_,
				nTurmasP_;
		JSONObject objeto_JSON;
		objeto_JSON = new JSONObject(sEntrada);

		dados_restauracao = objeto_JSON.getJSONObject("dados_restauracao");
		Ma = objeto_JSON.getInt("m5");
		Qp = objeto_JSON.getInt("qP");
		Qa = objeto_JSON.getInt("qA");
		Wp0_ = objeto_JSON.getJSONArray("maxDiasP");
		semGeminadasP_ = objeto_JSON.getJSONArray("semGeminadasP");
		comGeminadasP_ = objeto_JSON.getJSONArray("comGeminadasP");
		lista_ = objeto_JSON.getJSONArray("lista");
		ch_ = objeto_JSON.getJSONArray("ch");
		Mp_ = objeto_JSON.getJSONArray("maxP");
		horariosND_ = objeto_JSON.getJSONArray("horariosND");
		nomeP_ = objeto_JSON.getJSONArray("nomeP");
		nomeA_ = objeto_JSON.getJSONArray("nomeA");
		nTurmasP_ = objeto_JSON.getJSONArray("nTurmasP");

		// Aloca as variáveis com as dimensões necessárias (até 30 turmas por Prof., 5 dias, Ma horários por dia).
		nTurmasP = new int[Qp + 1];
		lista = new int[Qp + 1][31];
		ns = new int[Qp + 1][31];
		Mp = new int[Qp + 1][5];
		horariosND = new boolean[Qp + 1][5][Ma + 1];
		semGeminadasP = new boolean[Qp + 1];
		comGeminadasP = new boolean[Qp + 1];
		Wp0 = new int[Qp + 1];
		nomeP = new String[Qp + 1];
		nomeA = new String[Qa + 1];

		// Preenche as variáveis a partir dos arranjos JSON (os índices dos Profs. e das turmas começam de 1).
		for (a = 1; a <= Qa; a++)
			nomeA[a] = nomeA_.getString(a);
		for (p = 1; p <= Qp; p++) {
			nomeP[p] = nomeP_.getString(p);
			Wp0[p] = Integer.parseInt(Wp0_.getString(p));
			array1 = new JSONArray(Mp_.get(p).toString());
			for (d = 0; d <= 4; d++)
				Mp[p][d] = Integer.parseInt(array1.getString(d));
			nTurmasP[p] = Integer.parseInt(nTurmasP_.get(p).toString());
			array1 = new JSONArray(lista_.get(p).toString());
			array2 = new JSONArray(ch_.get(p).toString());
			for (k = 1; k <= nTurmasP[p]; k++) {
				lista[p][k] = Integer.parseInt(array1.get(k).toString());
				ns[p][k] = Integer.parseInt(array2.getString(k));
				System.out.println(ns[p][k]);
			}
			semGeminadasP[p] = semGeminadasP_.get(p).toString().equals("true");
			comGeminadasP[p] = comGeminadasP_.get(p).toString().equals("true");
			array1 = new JSONArray(horariosND_.get(p).toString());
			for (d = 0; d <= 4; d++) {
				array2 = new JSONArray(array1.get(d).toString());
				for (k = 1; k <= Ma; k++) {
					horariosND[p][d][k] = !array2.get(k).toString().equals("false");
					if (horariosND[p][d][k])
						System.out.println("T");
					else
						System.out.println("F");
				}
			}
		}
	}

	// Verifica se a distribuição lida do arquivo (distrPA) pode ser aproveitada para os dados atuais: mesmo número de Profs. e
	// de turmas; no máximo Ma aulas por turma e dia; no máximo 1 ou 2 aulas por Prof. e turma em cada dia; no máximo Mp[p][d] aulas
	// do Prof. por dia e Wp0[p] dias de trabalho; e total de aulas de cada par (Prof., turma) igual à carga horária ns. Retorna true se é válida.
	public boolean testaDistribuicao() {
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
			cont = 0;
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (a = 1; a <= Qa; a++) {
					aux += distrPA[p1][a][k];
					if (distrPA[p1][a][k] > (semGeminadasP[p1] ? 1 : 2) || distrPA[p1][a][k] < 0)
						return false;
				}
				if (aux > 0)
					cont++;
				if (aux > Mp[p1][k])
					return false;
			}
			if (cont > Wp0[p1])
				return false;

			for (a = 1; a <= Qa; a++) {
				aux = 0;
				for (k = 0; k <= 4; k++)
					aux += distrPA[p1][a][k];
				if (aux > 0) {
					j = nTurmasP[p1];
					while (j > 0 && lista[p1][j] != a)
						j--;
					if (j == 0 || aux != ns[p1][j])
						return false;
				}
			}
		}
		return true;
	}

	// Executa todo o processamento (ver descrição da classe).
	protected void executaProcessar() throws JSONException, IOException {
		boolean teste1;
		// Sem dados importados, avisa o usuário.
		if (sEntrada.equals("")) {
			JOptionPane.showMessageDialog(null, "Pressione IMPORTAR para abrir arquivo", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Passo (a): leitura dos dados e testes de consistência; em caso de erro, mostra as mensagens na aba ERROS e encerra.
		entradaDeDados2();
		areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText("");
		if (!testesDeConsistencia()) {
			areasDeTexto[TipoDeTexto.ERROS.ordinal()].setText(sErros_);
			abasTabbedPane.setSelectedIndex(TipoDeTexto.ERROS.ordinal());
			JOptionPane.showMessageDialog(null, "Erro de consistência dos dados. Abra a aba ERROS", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Cria o objeto Probl e inicializa suas variáveis com os dados do problema.
		g = new Probl();
		g.inicializacao(Ma, Qp, Qa, nTurmasP, lista, ns, Mp, horariosND, semGeminadasP, comGeminadasP, Wp0,
				sequenciaProf);

		ultimoResultado = "";
		// Passo (b): verifica se existe distribuição.json e, se existir, se é aproveitável (k1_ = 200).
		File file2_ = new File("distribuicao.json");
		temDistrPronta = file2_.exists();
		k1_ = 0;
		// Lê a distribuição gravada (distrPA[p][a][dia], indexada pelo número da turma a) e a testa.
		if (temDistrPronta) {
			String content = new String(Files.readAllBytes(Paths.get("distribuicao.json")));
			objeto_JSON = new JSONObject(content);
			lastQp = objeto_JSON.getInt("Nro_De_Profs");
			lastQa = objeto_JSON.getInt("Nro_De_Turmas");
			teste1 = Qp == lastQp && Qa == lastQa;
			if (teste1) {
				distrPA = new int[lastQp + 1][lastQa + 1][5];
				JSONdistrPA = objeto_JSON.getJSONArray("Distribuicao");
				for (p = 0; p <= Qp; p++) {
					array1 = new JSONArray(JSONdistrPA.get(p).toString());
					for (a = 0; a <= Qa; a++) {
						array2 = new JSONArray(array1.get(a).toString());
						for (d = 0; d <= 4; d++)
							distrPA[p][a][d] = Integer.parseInt(array2.get(d).toString());
					}
				}
				teste1 = testaDistribuicao();
			}

			if (!teste1) {
				JOptionPane.showMessageDialog(null, "A última distribuição obtida não pôde ser aproveitada", "",
						JOptionPane.INFORMATION_MESSAGE);
				temDistrPronta = false;
			} else
				k1_ = 200;
		}
		// Cria os módulos que serão executados.
		s = areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].getText();
		apont = 0;
		gA1 = new Module_2A();
		gA1.inicializacao();
		g4 = new Module_4();
		// Passo (c): sem distribuição aproveitável, executa a Fase 1 (Módulo 2A, que chama o Módulo 2B e os Módulos 3B e 3A).
		if (k1_ == 0) {
			// Texto de dicas exibido se a Fase 1 não obtiver solução: sugere relaxar folgas e mostra a soma dos máximos de aulas por dia.
			s_ = "A 1a Fase não deu solução. O que pode ser feito: \n\n"
					+ "Experimente ir na Página de Configuração do Prouesse e retirar algumas indicações de folga, alterando os máximos de aulas por dia\n"
					+ "de alguns professores, na aba \"Aulas por dia da semana\", que permite selecionar uma entre duas opções que informam os máximos de aulas por dia.\n "
					+ "Para alguns professores, preencha uma opção com poucas indicações de folga, e a mantenha selecionada.\n\n"
					+ "Importante também manter a distribuição das folgas de todos os professores um pouco equilibrada entre os dias da semana.\n"
					+ "A tabela a seguir mostra a soma dos máximos de aulas informados, por dia da semana, para se ter ideia da distribuição dos dias de folga:\n\n";
			for (d = 0; d <= 4; d++) {
				aux1 = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux1 += Mp[p1][d];
				s_ += (d + 2) + "a-feira: " + aux1 + "\n";
			}
			// preliminares monta as unidades do Módulo 2A e principalV2 executa a busca; retorna true se a distribuição foi obtida.
			n = gA1.preliminares();
			solubilidade = gA1.principalV2();
			// Fase 1 sem solução: mostra as dicas e encerra.
			if (!solubilidade) {
				JOptionPane.showMessageDialog(null, "1a Fase s/ solução. Abra a aba DICAS", "Resultado",
						JOptionPane.INFORMATION_MESSAGE);
				areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setText("");
				areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText(s_);
				abasTabbedPane.setSelectedIndex(TipoDeTexto.DICAS.ordinal());
				return;
			}
			// Fase 1 bem-sucedida: a distribuição está em dd.
			JOptionPane.showMessageDialog(null, "1a fase possivel ", "Resultado", JOptionPane.INFORMATION_MESSAGE);
			ultimoResultado = "";
			areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setText(ultimoResultado);
			areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText("");
			abasTabbedPane.setSelectedIndex(TipoDeTexto.DICAS.ordinal());

			temDistrPronta = true;
			// Desfaz a permutação dos dias feita no Módulo 2A: destino0[d] é a nova posição do dia original d; destino é a permutação inversa.
			for (d1 = 0; d1 <= 4; d1++)
				System.out.println(Probl.destino0[d1]);
			for (d1 = 0; d1 <= 4; d1++) {
				k1 = 0;
				while (Probl.destino0[k1] != d1)
					k1++;
				Probl.destino[d1] = k1;
			}
			// Devolve às posições originais dos dias as aulas em dd e os máximos em Mp.
			for (p1 = 1; p1 <= Qp; p1++) {
				for (k1 = 1; k1 <= nTurmasP[p1]; k1++) {
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
			// Converte a distribuição de dd (indexada pela posição j da turma) para distrPA (indexada pelo número da turma a).
			distrPA = new int[Qp + 1][Qa + 1][5];
			for (p1 = 1; p1 <= Qp; p1++) {
				for (a = 1; a <= Qa; a++) {
					for (k = 0; k <= 4; k++)
						distrPA[p1][a][k] = 0;
				}
				for (j = 1; j <= nTurmasP[p1]; j++) {
					a = lista[p1][j];
					for (k = 0; k <= 4; k++)
						distrPA[p1][a][k] = Probl.dd[p1][j][k];
				}
			}

			// Grava a distribuição em distribuição.json (formato JSON com o número de Profs., o de turmas e a distribuição).
			objeto_JSON = new JSONObject();
			objeto_JSON.put("Nro_De_Profs", Qp);
			objeto_JSON.put("Nro_De_Turmas", Qa);
			objeto_JSON.put("Distribuicao", distrPA);

			file = new FileWriter("distribuicao.json");
			try {
				file.write(objeto_JSON.toString());
			} catch (Exception E) {
				E.printStackTrace();
			} finally {
				file.flush();
				file.close();
			}
			// Cria um novo objeto do Módulo 2A, liberando o estado da Fase 1.
			gA1 = new Module_2A();
		}
		// Passo (d): carrega em dd a distribuição (obtida agora ou lida do arquivo), na indexação por posição j da turma.
		for (p1 = 1; p1 <= Qp; p1++) {
			for (j = 1; j <= nTurmasP[p1]; j++) {
				a = lista[p1][j];
				for (k = 0; k <= 4; k++)
					Probl.dd[p1][j][k] = distrPA[p1][a][k];
			}
		}

		// Fase 2: Módulo 4 com exec = 1 (distribuição entre os dias 0, 1 e 2; os horários são alocados apenas para dois deles, ver Module_4).
		n = g4.preliminares(1);
		aux1 = 0;
		aux2 = 0;
		if (g4.principal2()) {
			aux1 = 1;
			g4.resultado();
			JOptionPane.showMessageDialog(null, "2a fase possível: 2a, 3a-Feira ", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "2a fase s/ solucao: 2a, 3a-Feira ", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
		}
		// Fase 3: Módulo 4 com exec = 2 (dias 2, 3 e 4).
		n = g4.preliminares(2);
		if (g4.principal2()) {
			aux2 = 1;
			g4.resultado();
			JOptionPane.showMessageDialog(null, "3a fase possível: 4a, 5a, 6a-Feira ", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "3a fase s/ solucao: 4a, 5a, 6a-Feira ", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
		}
		// Se a Fase 2 ou a Fase 3 falhou, exibe as dicas (retirar horários não disponíveis) e mostra, para cada Prof. e dia, o máximo de
		// horários não disponíveis compatível com a distribuição obtida.
		if (aux1 == 0 || aux2 == 0) {
			JOptionPane.showMessageDialog(null, "Abra a aba DICAS", "Resultado", JOptionPane.INFORMATION_MESSAGE);
			s_ = "A 2a e/ou a 3a Fase não deu (deram) solução. O que pode ser feito: \n\n"
					+ "Experimente retirar algumas indicações de horários não disponíveis.\n"
					+ "Tente não manter vários horários não disponíveis num mesmo horário (por exemplo, o primeiro ou o último horário)\n"
					+ "de um determinado dia.\n";
			if (aux1 == 0 && aux2 == 1)
				s_ += "Como somente a 2a Fase não obteve solução, você pode fazer estas alterações somente para 2a ou 3a-feira.\n";
			if (aux1 == 1 && aux2 == 0)
				s_ += "Como somente a 3a Fase não obteve solução, você pode fazer estas alterações somente para 4a, 5a ou 6a-feira.\n";
			s_ += "Vá à Página de Configuração do Prouesse, faça estas alterações e, depois, \n"
					+ "volte para este programa, para executar a 2a e a 3a Fases novamente. \n \n"
					+ "\nA seguir, para cada prof., para cada dia, é mostrada a quantidade máxima de horários não disponíveis\n"
					+ "que podem ser estabelecidos de modo a permitir que a última distribuição obtida possa ser aproveitada\n"
					+ "(Esta informação estará disponível também na Página de Configuração):\n\n";
			for (p1 = 1; p1 <= Qp; p1++) {
				aux2_ = 0;
				for (d1 = 0; d1 <= 4; d1++) {
					for (a = 1; a <= Qa; a++)
						aux2_ += distrPA[p1][a][d1];
				}
				s_ += "Prof. p = " + p1 + ": ";
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
			areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setText(ultimoResultado);
			areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText(s_);
			abasTabbedPane.setSelectedIndex(TipoDeTexto.DICAS.ordinal());
			return;
		}
		// Passo (e): Resultado4 tenta inserir aulas geminadas para os Profs. com comGeminadasP verdadeiro.
		Resultado4_ = new Resultado4();
		System.out.println(Resultado4_.insereGeminadas());
		// Passo (f): converte a distribuição final para distrPA e a grava em distribuição.json.
		distrPA = new int[Qp + 1][Qa + 1][5];
		for (p1 = 1; p1 <= Qp; p1++) {
			for (a = 1; a <= Qa; a++) {
				for (k = 0; k <= 4; k++)
					distrPA[p1][a][k] = 0;
			}
			for (j = 1; j <= nTurmasP[p1]; j++) {
				a = lista[p1][j];
				for (k = 0; k <= 4; k++)
					distrPA[p1][a][k] = Probl.dd[p1][j][k];
			}
		}

		JSONObject objeto_JSON = new JSONObject();
		objeto_JSON.put("Nro_De_Profs", Qp);
		objeto_JSON.put("Nro_De_Turmas", Qa);
		objeto_JSON.put("Distribuicao", distrPA);

		file = new FileWriter("distribuicao.json");
		try {
			file.write(objeto_JSON.toString());
		} catch (Exception E) {
			E.printStackTrace();
		} finally {
			file.flush();
			file.close();
		}

		// Texto de dicas: para cada Prof. e cada dia, quantos horários não disponíveis podem ser acrescentados sem invalidar esta distribuição (Ma menos as aulas do dia).
		s_ = "\nA seguir, para cada prof., para cada dia, é mostrada a quantidade máxima de horários não disponíveis\n"
				+ "que podem ser estabelecidos de modo a permitir que a última distribuição obtida possa ser aproveitada\n"
				+ "(Esta informação estará disponível também na Página de Configuração):\n\n";
		for (p1 = 1; p1 <= Qp; p1++) {
			aux2_ = 0;
			for (d1 = 0; d1 <= 4; d1++) {
				for (a = 1; a <= Qa; a++)
					aux2_ += distrPA[p1][a][d1];
			}
			s_ += "Prof. p = " + p1 + ": ";
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
		areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText(s_);
		// Monta o texto dos quadros de horários dos Profs. e das turmas (obtem_quadro_horários, de Horar_) e o exibe na aba RESULTADOS.
		ultimoResultado += obtem_quadro_horarios(Probl.tabelaP, nomeP, nomeA, Qp) + "\n"
				+ obtem_quadro_horarios(Probl.tabelaA, nomeA, nomeP, Qa)
				+ "\n \nAtenção para conteúdo presente na aba DICAS.";
		areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setText(ultimoResultado);
		abasTabbedPane.setSelectedIndex(TipoDeTexto.RESULTADOS.ordinal());
		// Prepara o JSON de saída (sSaida): quadros dos Profs. e das turmas, horários não disponíveis máximos e dados de restauração.
		maxHorariosND = new int[Qp + 1][5];
		for (p1 = 1; p1 <= Qp; p1++) {
			for (d1 = 0; d1 <= 4; d1++) {
				aux1 = 0;
				for (a = 1; a <= Qa; a++)
					aux1 += distrPA[p1][a][d1];
				maxHorariosND[p1][d1] = Ma - aux1;
			}
		}
		objeto_JSON = new JSONObject();
		objeto_JSON.put("tabelaP", Probl.tabelaP);
		objeto_JSON.put("tabelaA", Probl.tabelaA);
		objeto_JSON.put("maxHorariosND", maxHorariosND);
		objeto_JSON.put("dados_restauracao", dados_restauracao);
		sSaida = objeto_JSON.toString();
		JOptionPane.showMessageDialog(null, "Pressione EXPORTAR para salvar arquivo", "Resultado",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// Método auxiliar: pede ao usuário um número inteiro por caixa de diálogo.
	private int leiaInt(String s) {
		String entrada;
		entrada = (String) JOptionPane.showInputDialog(null, s, "Entrada", JOptionPane.QUESTION_MESSAGE, icone, null,
				null);
		return Integer.parseInt(entrada);
	}

	// Ponto de entrada: abre a janela da interface gráfica.
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Prouesse_teste().setVisible(true);
			}
		});
	}
}
