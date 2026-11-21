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

// Classe Prouesse5D_X - classe executável (contém main) do Algoritmo P1. Controla e gerencia a execução das outras classes (Seção
//   4.8).
//
// Fluxo principal (método executaProcessar, acionado pelo botão 'processar'):
//   a) le os dados do arquivo JSON importado (entradaDeDados2);
//   b) valida os dados (testesDeConsistencia, Seção 2.1);
//   c) verifica se existe uma distribuição salva (distribuicao.json) que possa ser aproveitada (testaDistribuicao);
//   d) se não existir: Fase 1 - Módulo 1 (Module_1B), que chama o Módulo 2 (e este, o Módulo 3), obtém a distribuição completa;
//   e) Fases 2 e 3 - Módulo 4 (que chama o Módulo 5) redistribui as aulas entre 3 dias e as aloca nos horários;
//      depois Resultado4 tenta inserir aulas geminadas nos resultados;
//   f) exibe os quadros de horários e prepara o JSON de saída (exportado pelo botão 'exportar').
public class Prouesse5D_X extends Horar_ {
	// Objetos dos módulos: g (Probl, guarda os dados), gA1 (Módulo 1), g4 (Módulo 4) e Resultado4_ (ajuste das aulas geminadas).
	// k1_ = 0: processo começa do Módulo 1; k1_ = 200: já existe uma distribuição aproveitável e o processo segue do Módulo 4.
	// lastQp/lastQa = números de Profs. e turmas da distribuição salva em arquivo;  maxHorariosND[p][d] = máximo de horários não
	// disponíveis que o Prof. p pode ter no dia d sem invalidar a distribuição obtida (exibido ao usuário).
	private static Probl g;
	private static Module_1B gA1;
	private static Module_4 g4;
	static private int k1_, n, aux[], aux2_, lastQp, lastQa, maxHorariosND[][];
	static private boolean solubilidade;
	static private String ultimoResultado, s10;
	private static Resultado4 Resultado4_;
	private static JSONObject objeto_JSON;
	private static JSONArray JSONdistrPA, array1, array2;
	private static JSONObject dados_restauracao;
	private static FileWriter file;

	// Construtor: inicializa a interface (superclasse) e deixa vazio o conteúdo importado.
	public Prouesse5D_X() {
		super();
		aux = new int[5];
		sEntrada = "";
	}

	// Testes de consistência dos dados (Seção 2.1 do livro). Acumula em sErros_ uma mensagem para cada inconsistência encontrada
	// e retorna true se não houve nenhuma. Obs.: sErros_ é declarada aqui como variável local e oculta o campo estático de mesmo
	// nome (Horar_.sErros_), que é o texto que executaProcessar exibe na aba ERROS.
	public boolean testesDeConsistencia() {
		boolean test1;
		int soma[], d, aux, aux1, aux2, j, k, k1, arranjo[], p, a;
		String sErros_;

		// Inicialização: soma[a] sera usada para somar as cargas horárias de cada turma; arranjo[] para ordenar os máximos de aulas por dia.
		sErros_ = "";
		soma = new int[Qa + 1];
		arranjo = new int[5];
		for (p = 1; p <= Qp; p++) {
			// Teste 0: todo Prof. deve ter turmas informadas.
			if (nTurmasP[p] == 0) {
				sErros_ += "ERRO: Não foram informadas as turmas do prof. " + nomeP[p] + "\n";
			}
		}

		for (p = 1; p <= Qp; p++) {
			// Testes por Prof. p:
			// - se p deve ter aulas geminadas (comGeminadasP), não pode ter limite de 1 aula por dia em todos os dias;
			// - se p não deve ter aulas geminadas (semGeminadasP), não pode ter um único dia de trabalho (Wp0 == 1);
			// - comGeminadasP é incompatível com turma de carga horária 1.
			test1 = true;
			for (d = 0; d <= 4; d++)
				test1 = test1 && Mp[p][d] < 2;
			if (comGeminadasP[p] && test1)
				sErros_ += "ERRO: Foi informado que prof. " + nomeP[p]
						+ " deve ter aulas geminadas; porém,\ntambém foi informado que o mesmo não deve ter mais de uma aula em nenhum dia.\n";

			if (semGeminadasP[p] && Wp0[p] == 1)
				sErros_ += "ERRO: Foi informado que prof. " + nomeP[p]
						+ " não deve ter aulas geminadas; porém,\ntambém foi informado que o mesmo deve comparecer um único dia da semana.\n";
			// aux1 = carga horária semanal total do Prof. p.
			aux1 = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				aux1 += ns[p][j];
				if (comGeminadasP[p] && ns[p][j] == 1)
					sErros_ += "ERRO: Foi informado que prof. " + nomeP[p]
							+ " deve ter aulas geminadas; porém,\na carga horária numa de suas turmas é de 1 aula por semana.\n";
			}
			// Teste (a): a soma dos máximos de aulas por dia, Mp[p][*], deve ser suficiente para cumprir a carga horária semanal.
			aux2 = 0;
			for (d = 0; d <= 4; d++)
				aux2 += Mp[p][d];
			if (aux1 > aux2)
				sErros_ += "ERRO: Foi informado o número máximo de aulas por dia\ndo prof. " + nomeP[p]
						+ ", porém os valores informados para esse prof.\n"
						+ "são insuficientes para que o mesmo possa cumprir sua carga horária semanal\n";
			for (d = 0; d <= 4; d++)
				arranjo[d] = Mp[p][d];
			// Teste (b): ordena os máximos diários e soma os Wp0[p] maiores; esse total (máximo de aulas possível com Wp0[p] dias)
			// também deve ser suficiente para a carga horária.
			Arrays.sort(arranjo);
			aux = 0;
			for (k = 0; k < Wp0[p]; k++)
				aux += arranjo[4 - k];
			if (aux1 > aux)
				sErros_ += "ERRO: O número máximo de dias em trabalho informado para o prof. " + nomeP[p]
						+ "\né insuficiente para que o mesmo possa cumprir sua carga horária.\n";
		}

		// Teste por turma: a soma das cargas horárias de todos os Profs. na turma deve ser igual a Ma x 5 (total de aulas da semana).
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

		for (d = 0; d <= 4; d++) {
			aux1 = 0;
			for (p = 1; p <= Qp; p++)
				aux1 += Mp[p][d];
			// Teste (c): em cada dia, a soma dos máximos de aulas dos Profs. deve ser suficiente para atender a todas as turmas (Ma x Qa aulas).
			if (aux1 < Ma * Qa)
				sErros_ += "ERRO: Foi informado o número máximo de aulas na " + (d + 2)
						+ "a-Feira de cada prof., porém\nos valores informados são"
						+ "insuficientes para atender a todas as turmas.\n";
		}

		for (d = 0; d <= 4; d++) {
			for (k1 = 1; k1 <= Ma; k1++) {
				// Teste (d): em cada dia e horário, a quantidade de Profs. disponíveis (sem horário não disponível e com Mp > 0) deve ser pelo menos
				//   Qa.
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

		for (p = 1; p <= Qp; p++) {
			for (d = 0; d <= 4; d++) {
				aux1 = 0;
				for (k1 = 1; k1 <= Ma; k1++) {
					if (horariosND[p][d][k1])
						aux1++;
				}
				// Testes (e) e (f), por Prof. e por dia:
				// (e) o máximo de aulas informado não pode superar o número de horários disponíveis do Prof. no dia;
				// (f) nem superar o máximo possível dado o número de turmas (2 x nTurmasP, ou nTurmasP se o Prof. não deve ter geminadas).
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

		for (p = 1; p <= Qp; p++) {
			aux = 0;
			for (d = 0; d <= 4; d++) {
				if (Mp[p][d] > 0)
					aux++;
			}

			// Teste (g): o número máximo de dias de trabalho não pode superar o número de dias em que o Prof. tem disponibilidade (Mp > 0).
			if (aux < Wp0[p])
				sErros_ += "ERRO: O número máximo de dias em trabalho informado para o prof. " + nomeP[p]
						+ " não está correto,\n"
						+ "considerando-se o número de dias para os quais não devem haver aulas deste professor.\n";
			test1 = false;
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (ns[p][j] > 1)
					test1 = true;
			}
			// Sem aulas geminadas e com alguma turma de mais de 1 aula, o Prof. precisa de pelo menos 2 dias de disponibilidade e de trabalho.
			if (test1 && semGeminadasP[p] && (aux < 2 || Wp0[p] < 2))
				sErros_ += "ERRO: O prof. " + nomeP[p] + " não frequenta um número de dias suficiente,\n"
						+ "considerando-se que foi informado que este prof. não deverá ter aulas geminadas.\n";
		}

		for (p = 1; p <= Qp; p++) {
			// Teste final: o máximo de aulas de p numa mesma turma é limitado pela capacidade dos dias (1 ou 2 aulas por dia e por turma, em até
			//   Wp0 dias); cada turma deve caber nesse limite.
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
		// Retorna true se nenhuma mensagem de erro foi acumulada.
		return (sErros_.equals(""));
	}

	// Interpreta o JSON em sEntrada e preenche as variáveis de dados (Seção 2.1). As listas do JSON usam índice 1 para o primeiro
	// elemento (posição 0 não usada), exceto os vetores por dia (dias 0 a 4).
	protected void entradaDeDados2() throws JSONException, IOException {
		JSONArray array1, array2, Wp0_, semGeminadasP_, comGeminadasP_, lista_, ch_, Mp_, horariosND_, nomeP_, nomeA_,
				nTurmasP_;
		JSONObject objeto_JSON;
		// Extrai do JSON os campos: m5 (Ma), qP, qA, maxDiasP (Wp0), maxP (Mp), ch (ns), lista, nTurmasP, horariosND, semGeminadasP,
		//   comGeminadasP e nomes.
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

		// Aloca os arranjos de dados (cada Prof. pode ter até 30 turmas) e copia os valores lidos.
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

		for (a = 1; a <= Qa; a++)
			// Nomes das turmas.
			nomeA[a] = nomeA_.getString(a);
		for (p = 1; p <= Qp; p++) {
			// Para cada Prof.: nome, Wp0, Mp (5 dias), número de turmas, turmas (lista) e cargas horárias (ns).
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
			// Indicadores de aulas geminadas e horários não disponíveis (os valores impressos no console servem apenas para acompanhamento).
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

	// Testa se a distribuição lida do arquivo (distrPA) pode ser aproveitada para os dados atuais; mesmos testes de
	//   Horar_.testaDistribuicao,
	// agora usando as dimensões lidas do arquivo (lastQp, lastQa). Retorna true se respeita todas as restrições da Seção 2.2.
	public boolean testaDistribuicao() {
		int a, j, aux;
		// Número de Profs. ou de turmas diferente: a distribuição pertence a outro problema.
		if (Qp != lastQp || Qa != lastQa)
			return false;

		for (a = 1; a <= Qa; a++) {
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux += distrPA[p1][a][k];
				// Em cada turma e dia, não mais que Ma aulas.
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
					// Por Prof. e dia: não mais que 1 (ou 2) aulas por turma, total do dia <= Mp, e dias de trabalho <= Wp0.
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
					// A turma deve pertencer ao Prof. e o total semanal deve ser exatamente a carga horária ns.
					while (j > 0 && lista[p1][j] != a)
						j--;
					if (j == 0 || aux != ns[p1][j])
						return false;
				}
			}
		}
		return true;
	}

	// Executa todo o processo de obtenção de horários (Seção 4.8): leitura e validação dos dados, Fases 1, 2 e 3,
	// ajuste das aulas geminadas e preparação dos resultados.
	protected void executaProcessar() throws JSONException, IOException {
		boolean teste1;
		// Passo 0: se nenhum arquivo foi importado, avisa o usuário e encerra.
		if (sEntrada.equals("")) {
			JOptionPane.showMessageDialog(null, "Pressione IMPORTAR para abrir arquivo", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Passo (a): extrai os dados do JSON e limpa a aba DICAS.
		entradaDeDados2();
		areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText("");
		// Passo (b): testes de consistência; se houver erro, exibe a aba ERROS e encerra.
		if (!testesDeConsistencia()) {
			areasDeTexto[TipoDeTexto.ERROS.ordinal()].setText(sErros_);
			abasTabbedPane.setSelectedIndex(TipoDeTexto.ERROS.ordinal());
			JOptionPane.showMessageDialog(null, "Erro de consistência dos dados. Abra a aba ERROS", "Resultado",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		// Copia os dados para as variáveis estáticas compartilhadas por todos os módulos (Probl).
		g = new Probl();
		g.inicializacao(Ma, Qp, Qa, nTurmasP, lista, ns, Mp, horariosND, semGeminadasP, comGeminadasP, Wp0,
				sequenciaProf);

		ultimoResultado = "";
		// Passo (c): verifica se existe distribuicao.json. Se existir e tiver o mesmo número de Profs. e de turmas, a distribuição
		// é lida (formato distrPA) é testada por testaDistribuicao; se for aproveitável, k1_ = 200 (pula as Fases 1.1 e 1.2).
		File file2_ = new File("distribuicao.json");
		temDistrPronta = file2_.exists();
		k1_ = 0;
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
		s = areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].getText();
		// Cria o objeto do Módulo 1 (Module_1B) e o inicializa (aloca dd, tabelas e variáveis de controle).
		apont = 0;
		gA1 = new Module_1B();
		gA1.inicializacao();
		// Passo (d) - Fases 1.1 e 1.2 (só quando não há distribuição aproveitável).
		// Prepara o texto de dicas que sera exibido se a 1a Fase não obtiver solução, com a soma dos máximos de aulas por dia.
		if (k1_ == 0) {
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
			// Módulo 1: preliminares() permuta os dias (dias mais difíceis primeiro), ordena os Profs. e monta as unidades; devolve o número de
			//   unidades.
			// principalV1(-1, n) executa a busca (gerador = -1); ao concluir, o próprio Módulo 1 chama o Módulo 2 (e este o Módulo 3). Retorna
			//   true se houve solução.
			n = gA1.preliminares();
			solubilidade = gA1.principalV1(-1, n);
			// Sem solução: exibe a aba DICAS com orientações para o usuário e termina.
			if (!solubilidade) {
				JOptionPane.showMessageDialog(null, "1a Fase s/ solução. Abra a aba DICAS", "Resultado",
						JOptionPane.INFORMATION_MESSAGE);
				areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setText("");
				areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText(s_);
				abasTabbedPane.setSelectedIndex(TipoDeTexto.DICAS.ordinal());
				return;
			}
			JOptionPane.showMessageDialog(null, "1a fase possivel ", "Resultado", JOptionPane.INFORMATION_MESSAGE);
			ultimoResultado = "";
			areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setText(ultimoResultado);
			areasDeTexto[TipoDeTexto.DICAS.ordinal()].setText("");
			abasTabbedPane.setSelectedIndex(TipoDeTexto.DICAS.ordinal());

			// Com solução: desfaz a permutação dos dias feita pelo Módulo 1. destino0[k] indica a posição nova do dia original k;
			// aqui calcula-se a permutação inversa (destino) e aplica-se a mesma troca de posições a dd e a Mp,
			// restaurando a ordem original dos dias (segunda a sexta-feira).
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
			// Converte dd (indexado pela posição j da turma na lista do Prof.) para distrPA (indexado pelo número da turma a), formato gravado em
			//   arquivo.
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

			// Grava a distribuição no arquivo distribuicao.json (número de Profs., de turmas e distribuição).
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
			// Descarta o objeto do Módulo 1 (libera as estruturas de trabalho).
			gA1 = new Module_1B();
		}
		// Passo (e) - Fases 2 e 3: cria o Módulo 4 e copia a distribuição (distrPA) para dd, o formato usado pelos módulos.
		g4 = new Module_4();
		for (p1 = 1; p1 <= Qp; p1++) {
			for (j = 1; j <= nTurmasP[p1]; j++) {
				a = lista[p1][j];
				for (k = 0; k <= 4; k++)
					Probl.dd[p1][j][k] = distrPA[p1][a][k];
			}
		}

		// Fase 2: preliminares(1) prepara o Módulo 4 para os dias 0, 1 e 2 (segunda a quarta-feira); principal2() redistribui as aulas
		// entre esses dias e aloca nos horários os dias 0 e 1 (chamando o Módulo 5); resultado() preenche os quadros de horários.
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
		// Fase 3: o mesmo para os dias 2, 3 e 4 (quarta a sexta-feira); aqui os três dias são alocados nos horários.
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
		// Se a Fase 2 e/ou a Fase 3 não obteve solução: monta e exibe orientações (aba DICAS), incluindo, por Prof. e dia, o máximo de
		//   horários não disponíveis tolerado, e termina.
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
		// Ambas as fases deram solução. Ajuste dos resultados (Seção 4.7.2): Resultado4.insereGeminadas tenta criar aulas geminadas
		// para os Profs. que as desejam (comGeminadasP), trocando aulas de dias e Profs.; a distribuição em dd e os quadros são atualizados.
		Resultado4_ = new Resultado4();
		System.out.println(Resultado4_.insereGeminadas());
		// Converte a distribuição final (dd, agora com as aulas geminadas) para o formato distrPA e a grava novamente em distribuicao.json.
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

		// Texto para a aba DICAS: para cada Prof. e dia, quantos horários não disponíveis podem ser definidos sem invalidar a distribuição (Ma
		//   - aulas do dia); '*' se o Prof. não tem aulas.
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
		// Passo (f): monta o texto com os quadros de horários dos Profs. e das turmas e o exibe na aba RESULTADOS.
		ultimoResultado += obtem_quadro_horarios(Probl.tabelaP, nomeP, nomeA, Qp) + "\n"
				+ obtem_quadro_horarios(Probl.tabelaA, nomeA, nomeP, Qa)
				+ "\n \nAtenção para conteúdo presente na aba DICAS.";
		areasDeTexto[TipoDeTexto.RESULTADOS.ordinal()].setText(ultimoResultado);
		abasTabbedPane.setSelectedIndex(TipoDeTexto.RESULTADOS.ordinal());
		// Calcula maxHorariosND[p][d] = Ma - número de aulas de p no dia d.
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
		// Monta o JSON de saída (quadros por Prof. e por turma, maxHorariosND e dados de restauração), exportado pelo botão 'exportar'.
		objeto_JSON.put("tabelaP", Probl.tabelaP);
		objeto_JSON.put("tabelaA", Probl.tabelaA);
		objeto_JSON.put("maxHorariosND", maxHorariosND);
		objeto_JSON.put("dados_restauracao", dados_restauracao);
		sSaida = objeto_JSON.toString();
		JOptionPane.showMessageDialog(null, "Pressione EXPORTAR para salvar arquivo", "Resultado",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// Método auxiliar (não utilizado nesta versão) que le um inteiro por caixa de diálogo.
	private int leiaInt(String s) {
		String entrada;
		entrada = (String) JOptionPane.showInputDialog(null, s, "Entrada", JOptionPane.QUESTION_MESSAGE, icone, null,
				null);
		return Integer.parseInt(entrada);
	}

	// Ponto de entrada: abre a janela do programa na thread de eventos do Swing.
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Prouesse5D_X().setVisible(true);
			}
		});
	}
}
