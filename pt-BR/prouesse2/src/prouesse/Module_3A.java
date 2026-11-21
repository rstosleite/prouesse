package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Classe Module_3A - implementa o Módulo 3A (Seção 5.3.2 e Seção 5.4.4 do livro).
//
// Papel: finaliza a distribuição de aulas entre três dias (d_0, d_1 e d_2), a partir de uma pré-distribuição já feita. É
// uma classe muito semelhante ao Módulo 4 do Algoritmo P1 (Seções 4.1.4 e 4.5), exceto que não chama o Módulo 5. É chamada
// pelo Módulo 2B de duas formas: (a) preliminares(q2_, maxDias1), para os dias (d2, d3, d4) (usada em case_5, quando o
// Módulo 3B não é utilizado); (b) preliminares0(), para os dias (d0, d1, d2), depois que os dias d3 e d4 já têm sua
// distribuição em dd (é a execução que repete o dia d2 para dar mais flexibilidade).
//
// Casos das unidades (case_[i]), nesta ordem: 1 e 2 (por Prof.: um Caso 1 seguido dos Casos 2 de suas turmas), 11 (unidade de
// verificação, sem restrição própria), 3 e 4 (por Prof.: um Caso 3 seguido dos Casos 4 de suas turmas):
//   - Caso 1: x[k] = quantidade de dias de trabalho do Prof. no par (d_0, d_1) (com codificação de 0 a 3 para Profs. complexos);
//   - Caso 2: x[k] = aulas do par (Prof., turma) no par (d_0, d_1); a diferença para o total vai para o dia d_2;
//   - Caso 3: x[k] = 1 se d_0 é dia de trabalho do Prof., 0 caso contrário;
//   - Caso 4: x[k] = aulas do par (Prof., turma) em d_0; o resto do par vai para d_1.
// O 'gerador' do livro é minimo = -1 e o 'sucessor' é o método maximo(), que devolve false se encontra o sucessor (valor em
// x0) e true se não existe sucessor.
public class Module_3A extends Classes_acessorias {

	// d_0, d_1 e d_2 (de Probl) indicam os três dias da execução; Dp[p]: mínimo de dias de trabalho de p entre os três dias
	// para cumprir sua carga e as aulas de cada turma; arranjo: auxiliar para ordenar os máximos Mp.
	static protected int d1, nDiasWork1, aux3, Dp[], arranjo[];
	// complex_p0[p]: p tem Mp[p][d_0] e Mp[p][d_1] positivos e distintos (Seção 4.1.4).
	static protected boolean teste1, teste2, case_Trivial, complex_p0[];
	// nDias_[x]: quantidade de dias de trabalho do par (d_0, d_1) correspondente ao código x do Caso 1;
	// x_case_1[p]: código (0 a 3) escolhido no Caso 1 para um Prof. complexo; imax: unidade mais avançada atingida.
	static protected int nDias_[], x_case_1[], imax;

	// Construtor: aloca os arranjos próprios desta classe.
	public Module_3A() {
		Dp = new int[Qp + 1];
		arranjo = new int[3];
		complex_p0 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
	}

	// Prepara a execução para os dias (d0, d1, d2) = (0, 1, 2), depois que os dias 3 e 4 já foram distribuídos em dd.
	// Retorna o número de unidades n2.
	public int preliminares0() {
		int d;
		// nDias_[código] = número de dias de trabalho do Prof. no par (d_0, d_1) para os códigos 0, 1, 2, 3 do Caso 1.
		nDias_ = new int[] { 0, 1, 1, 2, 3 };
		d_0 = 0;
		d_1 = 1;
		d_2 = 2;
		imax = 0;
		// Para cada Prof.: q_0[p][j] = aulas da j-ésima turma ainda sem dia (ns menos as aulas dos dias 3 e 4); ns_p[p] = total dessas
		// aulas; Wp1[p] = número de dias de trabalho de p que já ocorrem nos dias 3 e 4.
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;
			Wp1[p] = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				q_0[p][j] = ns[p][j];
				ns_p[p] += ns[p][j];
			}

			// Subtrai as aulas já distribuídas nos dias 3 e 4 (em dd) e conta os dias de trabalho de p nesses dias.
			for (d1 = 3; d1 <= 4; d1++) {
				aux1 = 0;
				for (j = 1; j <= nTurmasP[p]; j++) {
					aux2 = dd[p][j][d1];
					q_0[p][j] -= aux2;
					ns_p[p] -= aux2;
					aux1 += aux2;
				}
				if (aux1 > 0)
					Wp1[p]++;
			}
			// Dp[p]: número mínimo de dias de trabalho de p nos dias 0, 1 e 2: usa os maiores Mp primeiro, até cobrir ns_p[p]...
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][d];
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 2; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p[p])
					break;
			}
			// ... e considera que, em cada turma, p tem 1 aula por dia (se semGeminadasP) ou até 2.
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (semGeminadasP[p])
					Dp[p] = Math.max(Dp[p], q_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q_0[p][j] + 1) / 2);
			}
			// complex_p0: Prof. cujos máximos nos dois primeiros dias são positivos e distintos.
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
		}

		// Montagem das unidades. Para cada Prof. com aulas a distribuir (na ordem d2_ estabelecida no Módulo 2A): uma unidade do
		// Caso 1 seguida de uma unidade do Caso 2 para cada turma com aulas.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			// Wp1[p] passa a ser o máximo de dias de trabalho de p nos dias 0, 1 e 2: Wp0 menos os dias de trabalho em 3 e 4 (no máximo 3).
			Wp1[p] = Wp0[p] - Wp1[p];
			if (Wp1[p] > 3)
				Wp1[p] = 3;
			k++;
			case_[k] = 1;
			Ap[k] = p;
			Aa[k] = 0;
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

		// Unidade do Caso 11: não impõe restrição (o sucessor apenas aceita a primeira visita).
		k++;
		case_[k] = 11;
		Aa[k] = 0;
		aux3 = k;
		Ap[k] = 0;
		// Depois do Caso 11: para cada Prof., uma unidade do Caso 3 seguida de uma unidade do Caso 4 para cada turma com aulas.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			case_[k] = 3;
			Ap[k] = p;
			Aa[k] = 0;
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
		// n2 = número total de unidades.
		n2 = k;

		// Variáveis de controle Sp2_ e Sp3_ (aulas acumuladas do Prof. em d_0 e d_1), indexadas pelas unidades do Caso 4 (apontP);
		// ao final das unidades de um Prof. reserva-se uma posição zerada.
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
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

		// Variáveis de controle Sp0_ e Sp1_ (aulas acumuladas do Prof. no par (d_0, d_1) e em d_2), indexadas pelas unidades do Caso 2.
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
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

		// Variáveis de controle Sa_0 e Sa_1 (aulas acumuladas da turma no par (d_0, d_1) e em d_2); cada turma dispõe de 16 posições.
		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_0[auxA[j] - 1] = 0;
			Sa_1[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				apontA[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}

		// Variáveis de controle Sa_4 e Sa_5 (aulas acumuladas da turma em d_0 e em d_1), para as unidades do Caso 4.
		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_4[auxA[j] - 1] = 0;
			Sa_5[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 4) {
				apontA2[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		return n2;
	}

	// Prepara a execução para os dias (d2, d3, d4) = (2, 3, 4), com as aulas de cada par (Prof., turma) nesses dias em q2_
	// (arranjo ch2 do Módulo 2B) e maxDias1[p] como limite de dias de trabalho de p. Estrutura análoga à de preliminares0().
	public int preliminares(int[][] q2_, int[] maxDias1) {
		int d;
		// d_0, d_1 e d_2 passam a ser os dias 2, 3 e 4.
		nDias_ = new int[] { 0, 1, 1, 2, 3 };
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		imax = 0;
		// Aqui q_0[p][j] = q2_[p][j] (aulas do par na 2ª parte da semana) e ns_p[p] é a soma dessas aulas.
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				ns_p[p] += q2_[p][j];
				q_0[p][j] = q2_[p][j];
			}
			for (d = 2; d <= 4; d++)
				arranjo[d - 2] = Mp[p][d];
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

		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			// O limite de dias de trabalho é o de toda a 2ª parte da semana (maxDias1), no máximo 3.
			Wp1[p] = maxDias1[p];
			if (Wp1[p] > 3)
				Wp1[p] = 3;
			k++;
			case_[k] = 1;
			Ap[k] = p;
			Aa[k] = 0;
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

		k++;
		case_[k] = 11;
		Aa[k] = 0;
		aux3 = k;
		Ap[k] = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			case_[k] = 3;
			Ap[k] = p;
			Aa[k] = 0;
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
		n2 = k;

		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
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
		for (j = 1; j <= n2; j++) {
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
		for (j = 1; j <= n2; j++) {
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
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 4) {
				apontA2[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		return n2;
	}

	// Prof. da unidade mais avançada atingida pela busca (usado pelo Módulo 2B para decidir o recuo).
	public int get_P() {
		return Ap[imax];
	}

	// Turma da unidade mais avançada atingida (0 se a unidade é de Caso 1, 3 ou 11).
	public int get_A() {
		return Aa[imax];
	}

	// Sucessor do Módulo 3A. Retorna false se encontrou o sucessor de x_[i] (valor em x0) e true se não existe.
	public boolean maximo() {
		int aux2, i0, maxX0_, minX0, d, x0_, k_;
		boolean teste, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 1 (Seções 4.1.4 e 4.5.1): número de dias de trabalho de p em (d_0, d_1).
		case 1: {
			// Prof. complexo: x0 de 0 a 3 (0 = nenhum dia, 1 = só d_0, 2 = só d_1, 3 = ambos); nDias_[x0] é a quantidade de dias.
			if (complex_p0[p]) {
				while (true) {

					// A quantidade mínima de dias em (d_0, d_1) é Dp[p] menos o dia d_2, se p pode trabalhar em d_2.
					aux2 = Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0);
					do {
						x0++;
						x0_ = nDias_[x0];
					} while (x0_ < aux2);
					// Sem sucessor se x0 passa de 3, se excede os dias que sobram para o Prof. (Wp1) ou os dias disponíveis em (d_0, d_1).
					if (x0 > 3 || x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
						return true;
					// A capacidade total (máximo de aulas) com essa escolha deve cobrir a carga ns_p[p]: máximos de d_2 (se p ainda pode
					// trabalhar nele) e dos dias escolhidos de (d_0, d_1).
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
			// Prof. não complexo: x0 = 0, 1 ou 2 dias de trabalho, e a capacidade usa o maior Mp quando há um só dia.
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

		// Caso 3 (Seção 4.5.2): x0 = 1 se d_0 é dia de trabalho de p, 0 se não é. i0 = wp0[p] = quantidade de dias de trabalho no par
		// (d_0, d_1), definida pelo Caso 1.
		case 3: {
			while (true) {
				i0 = wp0[p];
				// Sem sucessor se x0 > 1, x0 > i0 ou se p não está disponível em d_0 e x0 > 0.
				if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
					return true;
				// Valor mínimo de x0: o necessário para atingir Dp[p] dias, considerando d_1 e d_2.
				if (Mp[p][d_2] == 0 || i0 == Wp1[p])
					aux2 = 0;
				else
					aux2 = 1;
				x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
				// Se p trabalha nos dois dias, ou só pode trabalhar em d_0, d_0 é dia de trabalho.
				if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
					x0 = 1;
				if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
					return true;
				aux2 = (Wp1[p] - i0 > 0 ? Mp[p][d_2] : 0);

				// Para Prof. complexo com apenas um dia de trabalho no par, o valor deve ser consistente com o código escolhido no Caso 1.
				teste = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
				teste = teste && ((!complex_p0[p]) || (i0 != 1) || (x_case_1[p] == 1 && x0 == 1)
						|| (x_case_1[p] == 2 && x0 == 0));
				// Confere se a capacidade com esta escolha cobre a carga ns_p[p].
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

		// Caso 2 (Seção 4.5.3): x0 = aulas do par (Prof., turma Aa[i]) em (d_0, d_1); A[i] - x0 em d_2.
		case 2: {
			// Não há sucessor se x0 já está no máximo possível no par de dias.
			if (x0 == 2 * wp0[p] || (semGeminadasP[p] && x0 == wp0[p]))
				return true;
			// Caso trivial: Np0[p] (máximo em (d_0, d_1)) ou Np1[p] (máximo em d_2) é zero; as aulas ficam todas em um só lugar.
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
			// Sp0, Sp2, Sa0 e Sa2: aulas já destinadas, pelas unidades anteriores, ao Prof. e à turma em (d_0, d_1) e em d_2.
			Sp0 = Sp0_[apontP2[i] - 1];
			Sp2 = Sp1_[apontP2[i] - 1];
			Sa0 = Sa_0[apontA[i] - 1];
			Sa2 = Sa_1[apontA[i] - 1];

			// Limite inferior de x0: o que não cabe em d_2 (no máximo 1 ou 2 aulas do par, se d_2 for dia de trabalho; Np1[p] para o Prof.; Ma para a turma).
			aux0 = Math.min(Wp1[p] - wp0[p], 1);
			minX0 = (semGeminadasP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);

			// Se x0 excede o que caberia com um dia de trabalho a menos, o Prof. não pode ter aulas em d_2: se já tem (Sp2 > 0) não há
			// sucessor; senão, todas as aulas do par ficam em (d_0, d_1).
			aux0 = (semGeminadasP[p] ? Wp1[p] - 1 : 2 * Wp1[p] - 2);
			if (x0 > aux0) {

				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}

			// Limite superior de x0: o que cabe em (d_0, d_1) (por dias de trabalho, por n0, pelo resto de Np0[p] e pelo resto de 2*Ma da turma).
			maxX0_ = (semGeminadasP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Caso 4 (Seção 4.5.4): x0 = aulas do par (Prof., turma) em d_0; o restante das aulas do par em (d_0, d_1), n0, vai para d_1.
		case 4: {
			// n0 = aulas do par no par de dias, definidas no Caso 2 (q2_).
			n0 = q2_[p][j_[i]];
			// Sem aulas: o único valor é 0.
			if (n0 == 0) {
				if (x0 == -1) {
					x0 = 0;
					return false;
				} else
					return true;
			}
			// Sem sucessor se x0 já é 2 (ou 1, havendo no máximo uma aula por dia).
			if (x0 == 2 || (semGeminadasP[p] && x0 == 1))
				return true;
			// Np2[p] e Np3[p]: máximos de aulas de p em d_0 e em d_1 (definidos no Caso 3).
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
			// Sp0, Sp2, Sa0 e Sa2: aulas já destinadas ao Prof. e à turma em d_0 e em d_1 pelas unidades anteriores do Caso 4.
			Sp0 = Sp2_[apontP[i] - 1];
			Sp2 = Sp3_[apontP[i] - 1];
			Sa0 = Sa_4[apontA2[i] - 1];
			Sa2 = Sa_5[apontA2[i] - 1];

			// Limites de x0: o que não cabe em d_1, e o que cabe em d_0 (por aulas por dia, n0, Np2[p] e Ma da turma).
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

		// Caso 11: aceita na primeira visita (x0 passa a 0); ao retornar a esta unidade, o recuo continua.
		case 11: {
			if (x0 < 0) {
				x0 = 0;
				return false;
			}
			return true;
		}
		}
		return false;
	}

	// Atualização das variáveis de controle e de ligação depois de aceito x0 (Seções 4.5.1 a 4.5.4).
	public void atualiza2_() {
		switch (case_[i]) {
		// Caso 1: define Np0 (máximo de aulas em (d_0, d_1)), Np1 (máximo em d_2), wp0 (dias de trabalho em (d_0, d_1)) e,
		// para Profs. complexos, guarda o código x0 em x_case_1.
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
				Np0[p] = x0 * Math.max(Mp[p][d_0], Mp[p][d_1]);
				Np1[p] = (Wp1[p] - x0 == 0 ? 0 : Mp[p][d_2]);
				wp0[p] = x0;
			}
			break;
		}

		// Caso 3: define Np2 (máximo de aulas em d_0) e Np3 (máximo em d_1).
		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			break;
		}
		// Caso 2: acumula as aulas do Prof. e da turma no par de dias e em d_2, e registra em q2_ as aulas do par (Prof., turma) em (d_0, d_1).
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

		// Caso 4: acumula as aulas do Prof. e da turma em d_0 e em d_1.
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

	// Método 'dependencia' do Capítulo 3 (ver Seção 4.5.4): indica se a unidade j depende da unidade i (i < j). O Caso 1 e as
	// unidades de Caso 11 não dependem de nenhuma unidade anterior; o Caso 2 depende do Caso 1 do Prof. e de unidades do Caso
	// 2 do mesmo Prof. ou da mesma turma; o Caso 3 do Caso 1 do mesmo Prof.; o Caso 4 depende do Caso 2 do mesmo par, do
	// Caso 3 do mesmo Prof. e do Caso 4 do mesmo Prof. ou da mesma turma.
	public boolean dependencia(int i, int j) {
		int p1, p2, a1, a2;
		if (j <= i || case_[j] == 1 || case_[i] == 11 || case_[j] == 11)
			return false;
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

	// determinaArranjo do Capítulo 3: calcula z[i][l] (maior unidade anterior a i da qual a unidade i + l depende) e depois
	// acumula o máximo ao longo de l, de modo que z[i][q - i] indique para onde recuar.
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

	// Algoritmo 2 do Capítulo 3. Retorna true se obtém a distribuição entre os três dias e false se esgota as possibilidades.
	// Além do Algoritmo 2, aproveita as soluções já encontradas para as unidades dos Casos 3 e 4 dos Profs. que
	// precedem o Prof. da unidade (iMin) a que a busca recuou, guardadas em y2: elas continuam válidas.
	public boolean principal2() {
		int k, p0, minimo, iMin, iMax4, i0, p, maxX0_;
		Object[] opcoes = { "Sim", "Não" };
		iMin = 0;
		iMax4 = 0;
		minimo = -1;
		// Prepara z; todas as unidades começam com o gerador minimo.
		determinaZ(n2);
		imax = 1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		Ap[0] = 0;
		Ap[n2 + 1] = 0;
		do {
			nIteracoes++;
			// Se o sucessor foi encontrado:
			if (!maximo()) {
				// ao chegar na unidade do Caso 11 depois de um recuo (iMin > 0), repõe diretamente as soluções armazenadas em y2
				// para as unidades dos Casos 3 e 4 dos Profs. anteriores ao Prof. de iMin, em vez de recalculá-las...
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
				// ...senão, simplesmente guarda x0, atualiza as variáveis de controle e avança.
				} else {
					x_[i] = x0;
					atualiza2_();
					if (case_[i] == 11)
						iMax4 = i + 1;
					i++;
				}
				// Todas as unidades fechadas: distribuição obtida.
				if (i > n2)
					return true;

				// Quando começam as unidades de um novo Prof., guarda em y2[p] a solução das unidades dos Casos 3 e 4 até o Prof. p anterior.
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux3 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = (short) x_[i0];
				}
				// imax: unidade mais avançada atingida (consultada por get_P e get_A).
				if (i > imax) {
					imax = i;
				}
				// iMaxI_[k]: unidade mais avançada atingida desde a última visita a k (determina o recuo).
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Falha: se é a primeira unidade, esgotaram-se todas as distribuições.
			} else {
				if (i == 1) {
					System.out.println("Pesquisou todas as distribuições para 3 dias.");
					return false;
				}
				// Recua para p0 = z[i][iMaxI_[i] - i]; iMin registra a unidade dos Casos 1 ou 2 mais atrás atingida, para o reaproveitamento descrito acima.
				p0 = z[i][iMaxI_[i] - i];

				if (case_[p0] <= 2 && case_[i] >= 3)
					iMin = p0;
				else {
					if (case_[p0] <= 2 && p0 < iMin)
						iMin = p0;
				}
				if (p0 == 0) {

					return false;
				}
				// Redefine com o gerador as unidades abandonadas.
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}

	// Grava em dd as aulas por dia: d_2 recebe A[i] - x_[i] (Caso 2); no Caso 4, d_0 recebe x_[i] e d_1 recebe o restante do par (q2_).
	public void atualiza4() {
		int p, j, i, p1;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nTurmasP[p]; j++) {
				dd[p][j][d_0] = 0;
				dd[p][j][d_1] = 0;
				dd[p][j][d_2] = 0;
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
}
