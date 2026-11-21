package prouesse;

import javax.swing.ImageIcon;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.JOptionPane;

// Classe Module_4 - implementa o Módulo 4 (Seções 4.1.4 e 4.5 do livro), que executa as Fases 2 e 3.
//
// Papel: dada uma distribuição completa de aulas (em dd), altera a distribuição entre três dias e, para cada nova
// distribuição obtida, tenta alocar as aulas de cada dia nos horários do dia, chamando o Módulo 5 (classe Module_5).
// Na Fase 2 (exec == 1) os três dias são 0, 1 e 2, mas só são alocados os horários de dois deles (o dia 2 apenas
// participa da redistribuição, para dar flexibilidade); na Fase 3 (exec == 2) os três dias são 2, 3 e 4 e todos são alocados.
// Os três dias são rotulados d_0, d_1 e d_2 conforme sua dificuldade (preliminares): d_2 é o dia mais difícil (menor soma de Mp).
//
// Casos das unidades (case_[i]), nesta ordem: 1 e 2 (por Prof.: um Caso 1 seguido dos Casos 2 de suas turmas), 11 (validação
// da disponibilidade de Profs. nos horários de d_2), 3 e 4 (por Prof.: um Caso 3 seguido dos Casos 4 de suas turmas) e 5 (chamada
// do Módulo 5). Casos 1 a 4 e 11 seguem as Seções 4.5.1 a 4.5.4 do livro; o Caso 5 só existe no código.
// O 'gerador' é minimo = -1 e o 'sucessor' é o método maximo(), que devolve false se encontra o sucessor (valor em x0) e
// true se não existe. A busca é feita por principal2 (Algoritmo 2 do Capítulo 3).
public class Module_4 extends Classes_acessorias {

	// exec: 1 (Fase 2) ou 2 (Fase 3); y2[p][i]: soluções dos Casos 3 e 4 guardadas para reaproveitamento após recuos;
	// Dp[p]: mínimo de dias de trabalho de p entre os três dias; nDiasWork1: dias de trabalho de p já contados (determinaAmplitude).
	static protected int exec, d1, nDiasWork1, y2[][], aux3, Dp[], arranjo[];
	// g4_: objeto do Módulo 5, que aloca as aulas de um dia nos horários.
	static public Module_5 g4_;
	// presenteP[p]: p tem aulas no dia d_2 (usado nas validações dos horários); complex_p0[p]: Mp[p][d_0] e Mp[p][d_1] positivos e distintos.
	static protected boolean teste1, teste2, case_Trivial, presenteP[], complex_p0[];
	// iMin: unidade dos Casos 1 ou 2 mais atrás atingida por um recuo (reaproveitamento de soluções); dias2: dias cujos horários
	// serão alocados pelo Módulo 5; nDias_: dias de trabalho do par (d_0, d_1) para cada código do Caso 1; horários: ordem em que
	// os horários do dia são verificados; x_case_1[p]: código escolhido no Caso 1 para Profs. complexos.
	static protected int iMin, dias2[], nDias_[], horarios[], x_case_1[], p1;
	ImageIcon icone;

	// Construtor: aloca as variáveis auxiliares e cria o objeto do Módulo 5.
	public Module_4() {
		q_0 = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++)
			q_0[p1] = new int[nTurmasP[p1] + 1];
		presenteP = new boolean[Qp + 1];
		g4_ = new Module_5();
		dias2 = new int[4];
		y2 = new int[Qp + 1][x_.length];
		Dp = new int[Qp + 1];
		arranjo = new int[3];
		complex_p0 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
	}

	// Ordena os Profs. (resultado em d2_) para a montagem das unidades. Profs. sem aulas nos três dias vão para o final. Os
	// demais são escolhidos um a um: o primeiro é o de menor amplitude; cada um dos seguintes é o que tem menor valor da soma de
	// duas parcelas normalizadas: (a) a razão entre o número de turmas do Prof. e a sobreposição ponderada das turmas com as
	// dos Profs. já ordenados (a sobreposição pesa mais para os Profs. escolhidos mais recentemente) e (b) a amplitude do Prof.
	static public void ordena_profs2() {
		float aux1, aux2[], aux3, min1, max0, max0_, delta;
		boolean listAa[], teste;
		int i, indMin, k, p, p1, j, j1, a, cont;
		listAa = new boolean[Qp + 1];
		aux2 = new float[Qp + 1];
		cont = 0;
		k = 0;
		for (i = 1; i <= Qp; i++) {
			if (q_0[i][0] > 0)
				cont++;
			else {
				d2_[Qp - k] = i;
				k++;
				listAa[i] = true;
			}
		}
		for (i = 1; i <= cont; i++) {
			if (i == cont) {
				p = 1;
				while (listAa[p])
					p++;
				d2_[i] = p;
				listAa[p] = true;
				continue;
			}
			max0 = (float) -10000.0;
			max0_ = (float) -10000.0;
			if (i > 1) {
				for (p = 1; p <= Qp; p++) {
					if (listAa[p] || q_0[p][0] <= 0)
						continue;
					aux2[p] = 0.0f;
					aux3 = 0.0f;
					for (k = 1; k <= i - 1; k++) {
						p1 = d2_[k];
						aux3 += 0.5 * (float) q_0[p1][0];
						delta = aux3 / (float) q_0[p1][0];
						aux3 += 0.5 * (float) q_0[p1][0];
						for (j = nTurmasP[p1]; j >= 1; j--) {
							a = lista[p1][j];
							teste = false;
							for (j1 = nTurmasP[p]; j1 >= 1; j1--) {
								if (a == lista[p][j1] && q_0[p][j1] > 0) {
									teste = true;
									break;
								}
							}
							if (teste)
								aux2[p] += delta;
						}
					}
					if (aux2[p] > 0.00001) {
						aux2[p] = (float) q_0[p][0] / aux2[p];
						if (aux2[p] > max0)
							max0 = aux2[p];
					} else
						aux2[p] = -1.0f;
					if (amplitude[p] > max0_)
						max0_ = amplitude[p];
				}
			}
			indMin = 0;
			min1 = 1000000000.0f;
			for (p = 1; p <= Qp; p++) {
				if (listAa[p] || q_0[p][0] <= 0)
					continue;
				if (i > 1) {
					if (aux2[p] < -0.001)
						aux1 = 1.0f + amplitude[p] / max0_;
					else
						aux1 = aux2[p] / max0 + amplitude[p] / max0_;
				} else
					aux1 = amplitude[p];
				if (aux1 < min1) {
					indMin = p;
					min1 = aux1;
				}
			}
			p = indMin;
			if (p == 0) {
				System.out.println("ERRO 049");
				System.exit(0);
			}
			d2_[i] = p;
			listAa[p] = true;
		}
	}

	// Prepara a execução do Módulo 4 para a Fase 2 (x0 = 1) ou 3 (x0 = 2) e retorna o número de unidades n2.
	// Etapas: (1) define os três dias e os ordena por dificuldade; (2) obtém, para cada Prof., as aulas nos três dias (q_0) e as
	// variáveis auxiliares; (3) ordena os Profs. (ordena_profs2); (4) monta as unidades; (5) inicializa os ponteiros das variáveis de controle.
	public int preliminares(int x0) {
		int d;
		JOptionPane.showMessageDialog(null, "Esta ação poderá levar alguns minutos", "Resultado",
				JOptionPane.INFORMATION_MESSAGE);
		// nDias_[código]: quantidade de dias de trabalho do par (d_0, d_1) para os códigos 0 a 3 do Caso 1.
		nDias_ = new int[] { 0, 1, 1, 2, 3 };

		// horarios[k]: ordem de verificação dos horários do dia (primeiro, último, segundo, penúltimo etc.), de acordo com Ma (4, 5 ou 6 aulas por dia).
		if (Ma == 4)
			horarios = new int[] { 0, 1, 4, 2, 3 };
		if (Ma == 5)
			horarios = new int[] { 0, 1, 5, 2, 4, 3 };
		if (Ma == 6)
			horarios = new int[] { 0, 1, 6, 2, 5, 3, 4 };
		// exec indica a fase: 1 = dias (0, 1, 2); 2 = dias (2, 3, 4).
		iMin = 0;
		exec = x0;
		if (exec == 2) {
			dias[1] = 2;
			dias[2] = 3;
			dias[3] = 4;
		} else {
			dias[1] = 0;
			dias[2] = 1;
			dias[3] = 2;
		}
		imax = 0;

		// amplitude[k] = soma de Mp sobre todos os Profs. no dia dias[k]; ordena2 ordena os três dias em ordem crescente dessa soma
		// (do mais difícil para o mais fácil), resultado em d2_[1..3].
		for (k = 1; k <= 3; k++) {
			amplitude[k] = 0;
			for (p = 1; p <= Qp; p++)
				amplitude[k] += Mp[p][dias[k]];
			d2_[k] = dias[k];
		}
		Module_2A.ordena2(1, 4);
		System.out.println("d2_: " + d2_[1] + " " + d2_[2] + " " + d2_[3]);

		// d_2 é o dia mais difícil, d_0 o intermediário e d_1 o mais fácil. O dia d_2 é o que recebe as aulas restantes do Caso 2 e o que é validado pelo Caso 11.
		d_0 = d2_[2];
		d_1 = d2_[3];
		d_2 = d2_[1];
		dias[1] = d_0;
		dias[2] = d_1;
		dias[3] = d_2;

		// dias2: dias cujos horários serão alocados pelo Módulo 5, na ordem de dificuldade. Na Fase 2, o dia 2 (quarta-feira) fica de fora.
		if (exec == 1) {
			k = 1;
			if (d2_[1] != 2) {
				dias2[k] = d2_[1];
				k++;
			}
			if (d2_[2] != 2) {
				dias2[k] = d2_[2];
				k++;
			}
			if (d2_[3] != 2)
				dias2[k] = d2_[3];
		} else {
			dias2[1] = d2_[1];
			dias2[2] = d2_[2];
			dias2[3] = d2_[3];
		}

		// Dias fora dos três considerados: dias 3 e 4 (Fase 2) ou dias 0 e 1 (Fase 3).
		aux2 = (exec == 1 ? 3 : 0);
		// Para cada Prof.: q_0[p][j] = aulas da j-ésima turma nos três dias (soma de dd); ns_p[p] = total; q_0[p][0] = número de
		// turmas com aulas; mantemP[p] = p tem aulas nos três dias; Wp1[p] = dias de trabalho de p fora dos três dias.
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;

			q_0[p][0] = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				q_0[p][j] = 0;
				for (d1 = 1; d1 <= 3; d1++)
					q_0[p][j] += dd[p][j][dias[d1]];
				ns_p[p] += q_0[p][j];
				if (q_0[p][j] > 0)
					q_0[p][0]++;
			}

			mantemP[p] = ns_p[p] > 0;
			Wp1[p] = 0;
			for (d1 = aux2; d1 <= aux2 + 1; d1++) {
				aux1 = 0;
				for (j = 1; j <= nTurmasP[p]; j++)
					aux1 += dd[p][j][d1];
				if (aux1 > 0)
					Wp1[p]++;
			}
			// Dp[p]: mínimo de dias de trabalho de p entre os três dias (maiores Mp primeiro, até cobrir ns_p[p]) e para que as aulas de cada turma caibam (1 ou 2 por dia).
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][dias[d + 1]];
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 2; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p[p])
					break;
			}
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (semGeminadasP[p])
					Dp[p] = Math.max(Dp[p], q_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q_0[p][j] + 1) / 2);
			}
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
		}

		// amplitude[p]: número de distribuições possíveis das aulas de p entre os três dias (determinaAmplitude) dividido pela
		// quantidade de aulas do Prof.; usada em ordena_profs2 (menos distribuições = Prof. mais restrito).
		for (p = 1; p <= Qp; p++) {
			d2_[p] = p;
			Wp1[0] = Wp1[p];
			for (i = 1; i <= 2; i++) {
				case_[i] = i;
				x_[i] = -1;
			}
			aux2 = determinaAmplitude();
			if (ns_p[p] == 0)
				amplitude[p] = 0.0f;
			else
				amplitude[p] = (float) aux2 / (float) ns_p[p];
		}
		ordena_profs2();

		// Montagem das unidades. Para cada Prof. com aulas, na ordem de d2_: uma unidade do Caso 1 e uma do Caso 2 para cada turma com aulas.
		// Wp1[p] passa a ser o máximo de dias de trabalho de p nos três dias (Wp0 menos os dias de trabalho fora deles, no máximo 3).
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			Wp1[p] = Wp0[p] - Wp1[p];
			if (Wp1[p] > 3)
				Wp1[p] = 3;
			k++;
			case_[k] = 1;
			Ap[k] = p;
			cont = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				nClasses = q_0[p][j];
				if (nClasses > 0) {
					a = lista[p][j];
					cont++;
					k++;
					Ap[k] = p;
					Aa[k] = a;
					j_[k] = j;
					case_[k] = 2;
					A[k] = nClasses;
				}
			}
		}

		// Unidade do Caso 11: validação dos horários do dia d_2.
		k++;
		case_[k] = 11;
		aux3 = k;
		Ap[k] = 0;
		// Unidades dos Casos 3 e 4 (um Caso 3 por Prof. seguido dos Casos 4 de suas turmas).
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			k++;
			case_[k] = 3;
			Ap[k] = p;
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (q_0[p][j] > 0) {
					k++;
					Ap[k] = p;
					Aa[k] = lista[p][j];
					j_[k] = j;
					case_[k] = 4;
				}
			}
		}

		// Unidade final (Caso 5): chama o Módulo 5 para alocar as aulas nos horários dos dias.
		k++;
		case_[k] = 5;
		n2 = k;

		// Ponteiros e valores iniciais das variáveis de controle (Sp2_, Sp3_, Sp0_, Sp1_, Sa_0, Sa_1, Sa_4, Sa_5), como no Módulo 3A.
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2 - 1; j++) {
			if (case_[j] == 4) {
				apontP[j] = aux2;
				aux2++;
				if (case_[j + 1] != 4) {
					Sp2_[aux2] = 0;
					Sp3_[aux2] = 0;
					aux2++;
				}
			}
		}
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2 - 1; j++) {
			if (case_[j] == 2) {
				apontP2[j] = aux2;
				aux2++;
				if (case_[j + 1] != 2) {
					Sp0_[aux2] = 0;
					Sp1_[aux2] = 0;
					aux2++;
				}
			}
		}

		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_0[auxA[j] - 1] = 0;
			Sa_1[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2 - 1; j++) {
			if (case_[j] == 2) {
				apontA[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}

		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_4[auxA[j] - 1] = 0;
			Sa_5[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2 - 1; j++) {
			if (case_[j] == 4) {
				apontA2[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		return n2;
	}

	// Conta o número de distribuições possíveis das aulas do Prof. p (variável global) entre os três dias: duas unidades,
	// uma para as aulas em d_0 e outra para as aulas em d_1; o que resta vai para d_2.
	public int determinaAmplitude() {
		int cont, n;
		cont = 0;
		n = 2;
		i = 1;
		do {
			if (!maximoXi_0()) {
				x_[i] = x0;
				atualiza02_();
				i++;
				if (i > n) {
					cont++;
					i = n;
				}
			} else {
				if (i == 1)
					return cont;
				x_[i] = -1;
				i--;
			}
		} while (true);
	}

	// Atualiza o número de dias de trabalho já contados.
	public void atualiza02_() {
		switch (case_[i]) {
		case 1: {
			nDiasWork1 = (x0 == 0 ? Wp1[0] : Wp1[0] + 1);
			break;
		}
		}
	}

	// Sucessor das duas unidades de determinaAmplitude: x0 = aulas em d_0 (case 1) e em d_1 (case 2). As aulas restantes
	// devem caber em d_2 e o número de dias de trabalho não pode exceder Wp0[p].
	public boolean maximoXi_0() {
		x0 = x_[i];
		switch (case_[i]) {
		case 1: {
			do {
				x0 = (x0 == -1 ? 0 : x0 + 1);
				teste1 = x0 > Mp[p][d_0] || x0 > ns_p[p] || (x0 > 0 && Wp1[0] + 1 > Wp0[p]);
				teste2 = !teste1 && x0 + Mp[p][d_1] + Mp[p][d_2] >= ns_p[p];
			} while (!teste1 && !teste2);
			return teste1;
		}

		case 2: {
			do {
				x0 = (x0 == -1 ? 0 : x0 + 1);
				nClasses = ns_p[p] - x_[i - 1] - x0;
				teste1 = x0 > Mp[p][d_1] || nClasses < 0 || (x0 > 0 && nDiasWork1 + 1 > Wp0[p]);
				teste2 = !teste1 && nClasses <= Mp[p][d_2];
				if (teste2) {
					aux0 = (x0 == 0 ? nDiasWork1 : nDiasWork1 + 1);
					if (nClasses > 0)
						aux0++;
					teste2 = aux0 <= Wp0[p];
				}
			} while (!teste1 && !teste2);
			return teste1;
		}
		}
		return true;
	}

	// Sucessor do Módulo 4 (Seção 4.5). Retorna false se encontrou o sucessor de x_[i] (valor em x0) e true se não existe.
	public boolean maximo() {
		int aux2, i0, maxX0_, minX0, d, x0_, k_;
		boolean teste, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 1 (Seção 4.5.1): número de dias de trabalho de p no par (d_0, d_1) (com códigos de 0 a 3, se complex_p0). Idêntico ao Caso 1 do Módulo 3A.
		case 1: {
			if (complex_p0[p]) {
				while (true) {
					aux2 = Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0);
					do {
						x0++;
						x0_ = nDias_[x0];
					} while (x0_ < aux2);
					if (x0 > 3 || x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
						return true;
					aux2 = (Wp1[p] - x0_ > 0 ? Mp[p][d_2] : 0);
					if (x0 > 0) {
						if (x0 == 1)
							aux2 += Mp[p][d_0];
						else {
							if (x0 == 2)
								aux2 += Mp[p][d_1];
							else
								aux2 += Mp[p][d_0] + Mp[p][d_1];
						}
					}
					if (ns_p[p] <= aux2)
						return false;
				}
			} else {
				while (true) {
					x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0));
					if (x0 > 2 || x0 > Wp1[p] || x0 > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
						return true;
					aux2 = (Wp1[p] - x0 > 0 ? Mp[p][d_2] : 0);
					if (x0 > 0) {
						if (x0 == 1)
							aux2 += Math.max(Mp[p][d_0], Mp[p][d_1]);
						else
							aux2 += Mp[p][d_0] + Mp[p][d_1];
					}
					if (ns_p[p] <= aux2)
						return false;
				}
			}
		}

		// Caso 3 (Seção 4.5.2): x0 = 1 se d_0 é dia de trabalho de p, 0 se não é. Idêntico ao Caso 3 do Módulo 3A.
		case 3: {
			while (true) {
				i0 = wp0[p];
				if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
					return true;
				if (Mp[p][d_2] == 0 || i0 == Wp1[p])
					aux2 = 0;
				else
					aux2 = 1;
				x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
				if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
					x0 = 1;
				if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
					return true;
				aux2 = (Wp1[p] - i0 > 0 ? Mp[p][d_2] : 0);
				teste = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
				teste = teste && ((!complex_p0[p]) || (i0 != 1) || (x_case_1[p] == 1 && x0 == 1)
						|| (x_case_1[p] == 2 && x0 == 0));
				if (teste) {
					if (x0 == 0 && i0 == 1)
						aux2 += Mp[p][d_1];
					else {
						if (x0 == 1)
							aux2 += (i0 == 1 ? Mp[p][d_0] : Mp[p][d_0] + Mp[p][d_1]);
					}
					teste = ns_p[p] <= aux2;
				}
				if (teste)
					return false;
			}
		}

		// Caso 2 (Seção 4.5.3): x0 = aulas do par (Prof., turma) no par (d_0, d_1); A[i] - x0 vão para d_2. Idêntico ao Caso 2 do Módulo 3A.
		case 2: {
			if (x0 == 2 * wp0[p] || (semGeminadasP[p] && x0 == wp0[p]))
				return true;
			case_Trivial = Np0[p] == 0 || Np1[p] == 0;
			n0 = A[i];
			initialized = x0 > -1;
			if (case_Trivial) {
				if (initialized)
					return true;
				x0 = (Np0[p] == 0 ? 0 : n0);
			} else
				x0++;
			if (initialized)
				return x0 > maxX0[i];
			a = Aa[i];
			Sp0 = Sp0_[apontP2[i] - 1];
			Sp2 = Sp1_[apontP2[i] - 1];
			Sa0 = Sa_0[apontA[i] - 1];
			Sa2 = Sa_1[apontA[i] - 1];

			aux0 = Math.min(Wp1[p] - wp0[p], 1);
			minX0 = (semGeminadasP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);
			aux0 = (semGeminadasP[p] ? Wp1[p] - 1 : 2 * Wp1[p] - 2);
			if (x0 > aux0) {
				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}
			maxX0_ = (semGeminadasP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Caso 4 (Seção 4.5.4): x0 = aulas do par (Prof., turma) em d_0; o resto das aulas do par no par de dias vai para d_1. Idêntico ao Caso 4 do Módulo 3A.
		case 4: {
			n0 = q2_[p][j_[i]];
			if (n0 == 0) {
				if (x0 == -1) {
					x0 = 0;
					return false;
				} else
					return true;
			}
			if (x0 == 2 || (semGeminadasP[p] && x0 == 1))
				return true;
			case_Trivial = Np2[p] == 0 || Np3[p] == 0;
			initialized = x0 > -1;
			if (case_Trivial) {
				if (initialized)
					return true;
				x0 = (Np2[p] == 0 ? 0 : n0);
			} else
				x0++;
			if (initialized)
				return x0 > maxX0[i];
			a = Aa[i];
			Sp0 = Sp2_[apontP[i] - 1];
			Sp2 = Sp3_[apontP[i] - 1];
			Sa0 = Sa_4[apontA2[i] - 1];
			Sa2 = Sa_5[apontA2[i] - 1];

			minX0 = (semGeminadasP[p] ? n0 - 1 : n0 - 2);
			minX0 = Math.max(minX0, Sp2 + n0 - Np3[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			maxX0_ = (semGeminadasP[p] ? 1 : 2);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np2[p] - Sp0);
			maxX0_ = Math.min(maxX0_, Ma - Sa0);
			x0 = Math.max(x0, minX0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Caso 11: validação das aulas destacadas para o dia d_2 (final da Seção 4.2.3): em cada horário k do dia, o número de Profs. que têm
		// aula em d_2 e estão disponíveis (não têm horário não disponível) deve ser pelo menos Qa, o número de turmas.
		case 11: {

			if (x0 < 0) {
				x0 = 0;
				// Na Fase 2, se d_2 é o dia 2 (cujos horários não serão alocados), nada é verificado.
				if (exec == 1 && d_2 == 2)
					return false;
				// presenteP[p]: p tem aulas em d_2 de acordo com as unidades do Caso 2 já definidas (A[i] - x_[i] > 0).
				for (p = 1; p <= Qp; p++)
					presenteP[p] = false;
				for (i0 = 1; i0 < i; i0++) {
					p = Ap[i0];
					presenteP[p] = presenteP[p] || (case_[i0] == 2 && A[i0] - x_[i0] > 0);
				}
				teste2 = true;
				// Conta, para cada horário, os Profs. presentes e disponíveis; se algum horário tem menos de Qa, a validação falha.
				for (k = 1; k <= Ma; k++) {
					k_ = horarios[k];
					aux0 = 0;
					for (p = 1; p <= Qp; p++) {
						if (presenteP[p] && !horariosND[p][d_2][k_])
							aux0++;
					}
					teste2 = aux0 >= Qa;
					if (!teste2)
						break;
				}
				return !teste2;
			}
			return true;
		}

		// Caso 5: a distribuição entre os três dias está completa. Grava-a em dd (atualiza4) e tenta alocar as aulas nos horários.
		case 5: {

			atualiza4();
			// Verificação prévia, para os dias d_0 e d_1 (d_2 já foi verificado no Caso 11): em cada horário, deve haver ao menos Qa Profs. disponíveis dentre os que têm aula no dia.
			d = 0;
			for (d1 = 1; d1 <= 2; d1++) {
				d = dias[d1];
				teste2 = true;
				if (exec == 1 && d == 2)
					continue;
				for (p = 1; p <= Qp; p++) {
					presenteP[p] = false;
					for (j = 1; j <= nTurmasP[p]; j++) {
						if (dd[p][j][d] > 0) {
							presenteP[p] = true;
							break;
						}
					}
				}
				for (k = 1; k <= Ma; k++) {
					k_ = horarios[k];
					aux0 = 0;
					for (p = 1; p <= Qp; p++) {
						if (presenteP[p] && !horariosND[p][d][k_])
							aux0++;
					}
					teste2 = aux0 >= Qa;
					if (!teste2)
						break;
				}
				if (!teste2)
					break;
			}

			// Se essa verificação falha, o recuo é para a unidade do Caso 3 mais próxima do fim.
			if (!teste2) {
				i1 = n2;
				do
					i1--;
				while (case_[i1] != 3);
				System.out.println("i1 = " + i1);
				return true;
			}

			// Para cada dia de dias2, executa o Módulo 5 (preliminares, determinaZ e principal). Se o Módulo 5 tem sucesso em todos os dias,
			// o Caso 5 é aprovado (false).
			teste2 = true;
			aux2 = (exec == 1 ? 2 : 3);
			for (d1 = 1; d1 <= aux2; d1++) {
				aux0 = g4_.preliminares(dias2[d1]);
				g4_.determinaZ(aux0);
				teste2 = g4_.principal(aux0);
				if (!teste2)
					break;
				else
					System.out.println("Fechou para d1 = " + d1);
			}
			// O Módulo 5 falhou: o recuo é para a unidade do Caso 2 (se o dia é d_2) ou do Caso 4 (se é d_0 ou d_1) do mesmo Prof. e da mesma turma em que o Módulo 5 parou.
			if (!teste2) {
				p = g4_.getP();
				a = g4_.getA();
				i1 = n2;

				if (dias2[d1] == d_2) {
					do
						i1--;
					while (case_[i1] != 11);
					do
						i1--;
					while (p != Ap[i1] || a != Aa[i1] || case_[i1] != 2);
				} else {
					do
						i1--;
					while (p != Ap[i1] || a != Aa[i1] || case_[i1] != 4);
				}
				System.out.println("i1 = " + i1);
				return true;
			}
			return false;
		}
		}
		return false;
	}

	// Atualização das variáveis de controle e de ligação depois de aceito x0 (Seções 4.5.1 a 4.5.4). Idêntica à do Módulo 3A.
	public void atualiza2_() {
		switch (case_[i]) {
		case 1: {
			p = Ap[i];
			if (complex_p0[p]) {
				switch (x0) {
				case 0: {
					Np0[p] = 0;
					break;
				}
				case 1: {
					Np0[p] = Mp[p][d_0];
					break;
				}
				case 2: {
					Np0[p] = Mp[p][d_1];
					break;
				}
				case 3:
					Np0[p] = Mp[p][d_0] + Mp[p][d_1];
				}
				Np1[p] = (Wp1[p] - nDias_[x0] == 0 ? 0 : Mp[p][d_2]);
				wp0[p] = nDias_[x0];
				x_case_1[p] = x0;
			} else {

				if (x0 == 0)
					Np0[p] = 0;
				else {
					if (x0 == 1)
						Np0[p] = Math.max(Mp[p][d_0], Mp[p][d_1]);
					else
						Np0[p] = Mp[p][d_0] + Mp[p][d_1];
				}
				Np1[p] = (Wp1[p] - x0 == 0 ? 0 : Mp[p][d_2]);
				wp0[p] = x0;
			}
			break;
		}

		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			break;
		}
		case 2: {
			p = Ap[i];
			nClasses = A[i] - x0;
			aux = apontP2[i];
			Sp0_[aux] = Sp0_[aux - 1] + x0;
			Sp1_[aux] = Sp1_[aux - 1] + nClasses;
			aux = apontA[i];
			Sa_0[aux] = Sa_0[aux - 1] + x0;
			Sa_1[aux] = Sa_1[aux - 1] + nClasses;
			q2_[p][j_[i]] = x0;
			break;
		}

		case 4: {
			p = Ap[i];
			nClasses = q2_[p][j_[i]] - x0;
			aux = apontP[i];
			Sp2_[aux] = Sp2_[aux - 1] + x0;
			Sp3_[aux] = Sp3_[aux - 1] + nClasses;
			aux = apontA2[i];
			Sa_4[aux] = Sa_4[aux - 1] + x0;
			Sa_5[aux] = Sa_5[aux - 1] + nClasses;
			break;
		}
		}
	}

	// Método 'dependencia' do Capítulo 3, conforme o listado na Seção 4.5.4 do livro.
	public boolean dependencia(int i, int j) {
		int p1, p2, a1, a2;
		if (j <= i || case_[j] == 1 || case_[i] == 11)
			return false;
		if (case_[j] == 11)
			return case_[i] == 2;
		p1 = Ap[i];
		a1 = Aa[i];
		p2 = Ap[j];
		a2 = Aa[j];
		if (complex_p0[p1] && case_[i] == 3)
			return false;
		if (case_[j] == 3)
			return case_[i] == 1 && p1 == p2;
		switch (case_[j]) {
		case (2): {
			switch (case_[i]) {
			case (1):
				return p1 == p2;
			case (2):
				return p1 == p2 || a1 == a2;
			case (3):
				return false;
			case (4):
				return false;
			}
			break;
		}
		case (4): {
			switch (case_[i]) {
			case (1):
				return false;
			case (2):
				return p1 == p2 && a1 == a2;
			case (3):
				return p1 == p2;
			case (4):
				return p1 == p2 || a1 == a2;
			}
		}
		}
		return true;
	}

	// determinaArranjo do Capítulo 3 (calcula z[i][l] e acumula o máximo ao longo de l).
	public void determinaZ(int n_) {
		int p, i, l;
		for (i = 2; i <= n_; i++) {
			for (l = 0; l <= n_ - i; l++) {
				p = i - 1;
				while (p > 0 && (!this.dependencia(p, i + l)))
					p--;
				z[i][l] = (short) p;
			}
		}
		for (l = 1; l <= n_ - 1; l++) {
			for (i = 2; i <= n_ - l; i++) {
				if (z[i][l - 1] > z[i][l])
					z[i][l] = z[i][l - 1];
			}
		}
	}

	// Algoritmo 2 do Capítulo 3. Retorna true se obtém uma distribuição para os três dias com horários alocados (Módulo 5) e false caso contrário.
	// Reaproveita, em y2, as soluções dos Casos 3 e 4 dos Profs. anteriores ao Prof. de iMin, como no Módulo 3A.
	public boolean principal2() {
		int k, p0, minimo, iMin, iMax4, i0, p, maxX0_;
		Object[] opcoes = { "Sim", "Não" };
		String entrada;
		iMin = 0;
		iMax4 = 0;
		minimo = -1;
		determinaZ(n2);
		// Na Fase 2, pede ao usuário o tempo máximo de execução e inicia o cronômetro (a Fase 3 continua com o mesmo cronômetro).
		if (exec == 1) {
			entrada = (String) JOptionPane.showInputDialog(null, "Informe tempo máximo de execução (min)", "Entrada",
					JOptionPane.QUESTION_MESSAGE, icone, null, null);
			tempoMaximo = Math.round(Float.parseFloat(entrada));
			tempoInicial = System.currentTimeMillis();
			nIteracoes = 0L;
		}
		imax = 1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		Ap[0] = 0;
		Ap[n2] = 0;
		do {
			nIteracoes++;
			// Controle de tempo: a cada 50000 iterações verifica se o tempo máximo foi excedido.
			if (nIteracoes >= 50000L) {
				nIteracoes = 0L;
				if ((System.currentTimeMillis() - tempoInicial) / 60000.0 > tempoMaximo) {
					JOptionPane.showMessageDialog(null, "Execução excedeu o tempo máximo", "Resultado",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			// Sucessor encontrado: se estamos na unidade do Caso 11 depois de um recuo (iMin > 0), repõe as soluções armazenadas em y2 para os Profs. anteriores ao de iMin;
			if (!maximo()) {
				if (case_[i] == 11 && iMin > 0) {
					p0 = i + 1;
					while (p0 != iMax4 && Ap[iMin] != Ap[p0])
						p0++;
					iMax4 = i + 1;
					x_[i] = 0;
					p = Ap[p0 - 1];
					for (i = i + 1; i < p0; i++) {
						n0 = q2_[Ap[i]][j_[i]];
						Sp0 = Sp2_[apontP[i] - 1];
						Sa0 = Sa_4[apontA2[i] - 1];
						maxX0_ = (semGeminadasP[Ap[i]] ? 1 : 2);
						maxX0_ = Math.min(maxX0_, n0);
						maxX0_ = Math.min(maxX0_, Np2[Ap[i]] - Sp0);
						maxX0_ = Math.min(maxX0_, Ma - Sa0);
						maxX0[i] = maxX0_;
						x0 = y2[p][i];
						x_[i] = x0;
						atualiza2_();
						iMaxI_[i] = p0;
					}
					i = p0;
				// senão guarda x0, atualiza as variáveis de controle e avança.
				} else {
					x_[i] = x0;
					atualiza2_();
					if (case_[i] == 11)
						iMax4 = i + 1;
					i++;
				}
				// Todas as unidades (inclusive o Caso 5) foram fechadas: sucesso.
				if (i > n2)
					return true;
				// Ao começar um novo Prof. nas unidades dos Casos 3 e 4, guarda em y2[p] as soluções até o Prof. anterior.
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux3 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = x_[i0];
				}
				if (i > imax) {
					imax = i;
					System.out.println(imax + "/" + n2);
				}
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Falha: recua para z[i][iMaxI_[i] - i] ou, se a falha foi na unidade do Caso 5, para a unidade indicada em i1.
			} else {
				if (i == 1) {
					System.out.println("Pesquisou todas as distribuições para 3 dias.");

					return false;
				}

				p0 = (case_[i] == 5 ? i1 : z[i][iMaxI_[i] - i]);
				// iMin registra a unidade dos Casos 1 ou 2 mais atrás atingida pelo recuo.
				if (case_[p0] <= 2 && case_[i] >= 3)
					iMin = p0;
				else {
					if (case_[p0] <= 2 && p0 < iMin)
						iMin = p0;
				}
				// Não deveria ocorrer: recuo além da primeira unidade.
				if (p0 == 0) {
					JOptionPane.showMessageDialog(null, "Contacte o desenvolvedor. Código do erro: 015", "",
							JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}

	// Grava em dd as aulas dos três dias: d_2 recebe A[i] - x_[i] (Caso 2); no Caso 4, d_0 recebe x_[i] e d_1 recebe o restante do par.
	public void atualiza4() {
		int p, j, i, p1;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nTurmasP[p]; j++) {
				for (d1 = 1; d1 <= 3; d1++)
					dd[p][j][dias[d1]] = 0;
			}
		}
		for (i = 1; i <= n2; i++) {
			p = Ap[i];
			j = j_[i];
			if (case_[i] == 2)
				dd[p][j][d_2] = A[i] - x_[i];
			else {
				if (case_[i] == 4) {
					dd[p][j][d_1] = q2_[p][j] - x_[i];
					dd[p][j][d_0] = x_[i];
				}
			}
		}
	}

	// Zera, para o dia d1, os quadros de horários dos Profs. (tabelaP) e das turmas (tabelaA).
	private void inicializaTabelas(int d1) {
		int p1, a1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tabelaP[p1][d1][k1] = 0;
			for (a1 = 1; a1 <= Qa; a1++)
				tabelaA[a1][d1][k1] = 0;
		}
	}

	// Obtém os quadros de horários finais: para cada dia de dias2, repete a execução do Módulo 5 e grava o resultado nos quadros.
	public void resultado() {
		for (d1 = 1; d1 <= (exec == 1 ? 2 : 3); d1++) {
			inicializaTabelas(dias2[d1]);
			aux0 = g4_.preliminares(dias2[d1]);
			g4_.determinaZ(aux0);
			g4_.principal(aux0);
			g4_.resultado(aux0, dias2[d1]);
		}
	}
}
