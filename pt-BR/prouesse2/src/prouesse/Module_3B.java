package prouesse;

import java.util.Arrays;
import javax.swing.JOptionPane;

// Classe Module_3B - implementa o Módulo 3B (Seções 5.2.5 e 5.4.4 do livro).
//
// Papel: finaliza a distribuição de aulas entre os três dias d2, d3 e d4 (2ª parte da semana), a partir da pré-distribuição
// feita pelo Módulo 2B (aulas de cada par (Prof., turma) na 2ª parte, recebidas em q2_0 = ch2). É executado uma vez para cada
// 'possibilidade' da 2ª parte (índice possib em bitSet_2, do Módulo 2A), que já informa a quantidade wp0[p] de dias de trabalho
// de cada Prof. entre d2 e d3. O Módulo 2B chama, nesta ordem: preliminares1 (verifica se a possibilidade é viável),
// preliminares2 (monta as unidades) e principal_V3 (busca); se a busca tem sucesso, atualiza4 grava o resultado em dd.
//
// É semelhante ao Módulo 4 do Algoritmo P1, mas as unidades do Caso 3 e do Caso 4 são ordenadas de outra forma e não
// existem unidades do Caso 1. Casos (case_[i]), nesta ordem: 2 (aulas do par (Prof., turma) em d2 e d3; o resto vai para d4),
// 3 (1 se d2 é dia de trabalho do Prof., 0 se não; só para Profs. que não são complex_p2), 35 (validação dos Casos 3) e 4 (aulas
// do par em d2; o resto das aulas de d2 e d3 vai para d3). O 'gerador' é minimo = -1 e o 'sucessor' é o método maximo(),
// que devolve false se encontra o sucessor (em x0) e true se não existe.
public class Module_3B extends Probl {

	// Cópias privadas das referências para os arranjos de Classes_acessorias (compartilhados com o Módulo 3A): unidades
	// (Ap, Aa, j_, A, case_), valores (x_), controle da busca (iMaxI_, z, y2). q2_0[p][j]: aulas do par (p, j-ésima turma) na 2ª parte.
	private int Ap[], Aa[], j_[], A[], x_[], i, n2, x0, iMaxI_[], d2_[], q2_0[][], maxDias1[];
	// ns_p[p]: aulas de p na 2ª parte; wp0[p]: dias de trabalho de p em d2 e d3 (pela possibilidade); Np0, Np1, Np2 e Np3:
	// máximos de aulas de p em (d2, d3), em d4, em d2 e em d3; x_case__3[p]: 1 se d2 é dia de trabalho de p, 0 se não é;
	// sumA_0, sumA_1 e sumA_1_: capacidade acumulada por turma em d2, d3 e d4; Dp[p]: mínimo de dias de trabalho de p em d2, d3 e d4.
	private int k, i1, p, a, j, nClasses, aux1, p1, ns_p[], wp0[], Np2[], Np3[], Np0[], Np1[], sumA_0[], sumA_1[],
			sumA_1_[], x_case__3[], Dp[];
	private int Sp0_[], Sp1_[], Sa_0[], Sa_1[], apontP2[], apontA[], arranjo[], d, aux35, aux, aux35_;
	// p2_[k]: posição, na ordem de Profs. d2_, do Prof. da unidade k; usado para reaproveitar soluções após o recuo (principal_V3).
	static public int case_[], imax, p2_[];
	private short z[][], y2[][];

	// Construtor: obtém as referências dos arranjos compartilhados e aloca os arranjos próprios.
	public Module_3B() {
		this.Ap = Classes_acessorias.Ap;
		this.Aa = Classes_acessorias.Aa;
		this.j_ = Classes_acessorias.j_;
		this.A = Classes_acessorias.A;
		this.x_ = Classes_acessorias.x_;
		this.iMaxI_ = Classes_acessorias.iMaxI_;
		this.case_ = Classes_acessorias.case_;
		this.z = Classes_acessorias.z;
		this.y2 = Classes_acessorias.y2;
		p2_ = new int[x_.length];
		arranjo = new int[3];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		sumA_1_ = new int[Qa + 1];
		ns_p = new int[Qp + 1];
		wp0 = new int[Qp + 1];
		Np2 = new int[Qp + 1];
		Np3 = new int[Qp + 1];
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		x_case__3 = new int[Qp + 1];
		Dp = new int[Qp + 1];
		q_0 = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++)
			q_0[p1] = new int[nTurmasP[p1] + 1];
		Sp0_ = Classes_acessorias.Sp0_;
		Sp1_ = Classes_acessorias.Sp1_;
		Sa_0 = Classes_acessorias.Sa_0;
		Sa_1 = Classes_acessorias.Sa_1;
		apontP2 = Classes_acessorias.apontP2;
		apontA = Classes_acessorias.apontA;
	}

	// Verifica se a possibilidade possib (da 2ª parte) é viável: calcula, para cada Prof. com aulas na 2ª parte, wp0 e Dp, e
	// procura (detPossibP) uma sub-possibilidade que atenda às demandas das turmas e dos Profs.; retorna true se existe alguma
	// (Seção 5.4.4). Prepara as unidades do Caso 3 e a unidade do Caso 35 usadas por detPossibP.
	public boolean preliminares1(int[][] q2_0, int possib) {
		boolean teste, teste2;
		this.q2_0 = q2_0;
		d2_ = Module_2A.d2_;
		maxDias1 = Module_2A.maxDias1;
		// Dias da execução: d2, d3 e d4 (valores 2, 3 e 4).
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		dias[1] = d_0;
		dias[2] = d_1;
		dias[3] = d_2;
		// Para cada Prof.: ns_p = aulas na 2ª parte; wp0 vem dos dois bits de bitSet_2 (quantidade de dias de trabalho entre d2 e d3).
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;
			for (j = 1; j <= nTurmasP[p]; j++)
				ns_p[p] += q2_0[p][j];
			if (ns_p[p] == 0)
				continue;
			teste = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1));
			teste2 = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1) + 1);
			wp0[p] = 2 * (teste ? 1 : 0) + (teste2 ? 1 : 0);
			// Prof. complex_p2: o dia d2 já é de trabalho (ou não) de forma definitiva, conforme bitSet_1; confere a carga com a do Módulo 2B.
			if (Module_2A.complex_p2[p]) {
				teste = Module_2A.bitSet_1.get(3 * (p - 1));
				x_case__3[p] = (teste ? 1 : 0);
				if (ns_p[p] != Module_2B.ns_p1[p]) {
					System.out.println("ERRO 401.");
					System.exit(0);
				}
			}
			// Dp[p]: dias de trabalho mínimos de p em d2, d3 e d4 (maiores Mp primeiro, até cobrir ns_p[p]) e para que cada turma de p
			// tenha suas aulas (1 por dia se semGeminadasP, senão até 2).
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
					Dp[p] = Math.max(Dp[p], q2_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q2_0[p][j] + 1) / 2);
			}
		}
		// Unidades de preparação: uma do Caso 3 para cada Prof. com aulas que não seja complex_p2, seguidas de uma do Caso 35.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			if (Module_2A.complex_p2[p]) {
				x0 = x_case__3[p];
				continue;
			}
			k++;
			case_[k] = 3;
			Ap[k] = p;
		}
		// Unidade do Caso 35 (validação dos Casos 3).
		k++;
		case_[k] = 35;
		aux35_ = k;
		// Procura uma forma de definir x_case__3 para todos os Profs. que atenda às demandas.
		return detPossibP();
	}

	// Prepara a execução do Módulo 3B para a possibilidade possib, depois da aprovação em preliminares1: calcula Np0 e Np1 e
	// monta as unidades na ordem: Caso 2 (todos os pares), Caso 3 (Profs. que não são complex_p2), Caso 35 e Caso 4 (todos os pares).
	public void preliminares2(int[][] q2_0, int possib) {
		// Np0[p]: máximo de aulas em (d2, d3); Np1[p]: máximo em d4. Para Prof. complexo, usa os dias de trabalho fixados em bitSet_1.
		for (p = 1; p <= Qp; p++) {
			x0 = wp0[p];
			if (Module_2A.complex_p2[p]) {
				Np0[p] = 0;
				for (k = 0; k <= 1; k++) {
					teste = Module_2A.bitSet_1.get(3 * (p - 1) + k);
					Np0[p] += (teste ? Mp[p][k + 2] : 0);
				}
				teste = Module_2A.bitSet_1.get(3 * (p - 1) + 2);
				Np1[p] = (teste ? Mp[p][d_2] : 0);
			} else {
				Np0[p] = x0 * Math.max(Mp[p][d_0], Mp[p][d_1]);
				Np1[p] = (maxDias1[p] - x0 == 0 ? 0 : Mp[p][d_2]);
			}
		}

		// Unidades do Caso 2 (apenas pares com aulas na 2ª parte): A[k] = aulas do par na 2ª parte; p2_[k] = posição do Prof. em d2_.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			for (j = 1; j <= nTurmasP[p]; j++) {
				nClasses = q2_0[p][j];
				if (nClasses > 0) {
					a = lista[p][j];
					k++;
					Ap[k] = p;
					p2_[k] = i1;
					Aa[k] = a;
					j_[k] = j;
					case_[k] = 2;
					A[k] = nClasses;
				}
			}
		}
		// Unidades do Caso 3 (Profs. não complex_p2). Para complex_p2, Np2 e Np3 já são definidos diretamente.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			if (Module_2A.complex_p2[p]) {
				x0 = x_case__3[p];
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
				Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
				continue;
			}
			k++;
			case_[k] = 3;
			Ap[k] = p;
			p2_[k] = i1;
			Aa[k] = 0;
		}
		// Unidade do Caso 35: validação dos resultados dos Casos 2 e 3.
		k++;
		case_[k] = 35;
		aux35 = k;
		Aa[k] = 0;
		// Unidades do Caso 4: distribuem as aulas entre d2 e d3.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] == 0)
				continue;
			for (j = 1; j <= nTurmasP[p]; j++) {
				nClasses = q2_0[p][j];
				if (nClasses > 0) {
					k++;
					Ap[k] = p;
					p2_[k] = i1;
					Aa[k] = lista[p][j];
					j_[k] = j;
					case_[k] = 4;
				}
			}
		}
		// n2 = número de unidades.
		n2 = k;

		// Variáveis de controle e ponteiros (aulas acumuladas por Prof. e por turma), como nos Módulos 2A e 3A.
		Ap[n2 + 1] = 0;
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 4) {
				apontP[j] = aux2;
				aux2++;
				if (Ap[j + 1] != Ap[j]) {
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
				if (Ap[j + 1] != Ap[j]) {
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
		// Prepara o arranjo z (recuo da busca).
		determinaZ(n2);
	}

	// Prof. da unidade mais avançada atingida (para o Módulo 2B decidir o recuo).
	public int getP() {
		return (Ap[imax]);
	}

	// Turma da unidade mais avançada atingida.
	public int getA() {
		return (Aa[imax]);
	}

	// Validação das demandas das turmas (equivalente ao Caso 35): retorna true se, com os dias de trabalho em x_case__3 e
	// wp0, cada turma pode receber Ma aulas em cada um dos dias d2, d3 e d4, e as capacidades totais atingem Ma*Qa.
	public boolean case__35() {
		int aux2, aux3, aux4, sumA0, sumA1, sumA1_, aux4_, aux1_, x0;
		boolean teste;
		sumA0 = 0;
		sumA1 = 0;
		sumA1_ = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
			sumA_1_[a] = 0;
		}
		for (p = 1; p <= Qp; p++) {
			if (ns_p[p] <= 0)
				continue;
			x0 = x_case__3[p];
			aux0 = (semGeminadasP[p] ? x0 : 2 * x0);
			aux1 = wp0[p] - x0;
			aux1_ = Module_2A.maxDias1[p] - wp0[p];
			if (!semGeminadasP[p]) {
				aux1 *= 2;
				aux1_ *= 2;
			}
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux4_ = (Module_2A.maxDias1[p] - wp0[p] == 0 ? 0 : Mp[p][d_2]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux1_ = Math.min(aux1_, aux4_);
			aux2 = nTurmasP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			sumA1_ += aux4_;
			for (j = 1; j <= aux2; j++) {
				a = lista[p][j];
				sumA_0[a] += Math.min(aux0, q2_0[p][j]);
				sumA_1[a] += Math.min(aux1, q2_0[p][j]);
				sumA_1_[a] += Math.min(aux1_, q2_0[p][j]);
			}
		}
		if (sumA1_ < Ma * Qa) {
			System.out.println("ERRO 049");
			System.exit(0);
		}
		teste = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa && sumA1_ >= Ma * Qa;
		if (!teste)
			return false;
		for (a = 1; a <= Qa; a++) {
			teste = sumA_0[a] >= Ma && sumA_1[a] >= Ma && sumA_1_[a] >= Ma;
			if (!teste)
				return false;
		}
		return true;
	}

	// Validação das demandas dos Profs.: para cada Prof. com aulas, a capacidade em d2, d3 e d4 (limitada por Mp, por 1 ou 2
	// aulas por turma e dia, e pelas aulas de cada turma) deve cobrir ns_p[p]. Retorna false se alguma demanda não é atendida.
	private boolean case__35_() {
		int k, sumP_0, sumP_1, sumP_1_, p, aux3, aux4, aux4_, aux1_, d_0, d_1, d_2;
		boolean teste;

		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		for (p = 1; p <= Qp; p++) {
			if (ns_p[p] == 0) {
				teste = true;
				continue;
			}
			sumP_0 = 0;
			sumP_1 = 0;
			sumP_1_ = 0;
			aux0 = x_case__3[p];
			aux1 = wp0[p] - aux0;
			aux1_ = maxDias1[p] - wp0[p];
			aux3 = (aux0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (aux1 == 0 ? 0 : Mp[p][d_1]);
			aux4_ = (aux1_ == 0 ? 0 : Mp[p][d_2]);
			if (!semGeminadasP[p]) {
				aux0 *= 2;
				aux1 *= 2;
				aux1_ *= 2;
			}
			aux2 = nTurmasP[p];
			for (j = 1; j <= aux2; j++) {
				k = q2_0[p][j];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
				sumP_1_ += Math.min(aux1_, k);
			}
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			aux0 += Math.min(aux4_, sumP_1_);
			teste = ns_p[p] <= aux0;
			if (!teste)
				return false;
		}
		return true;
	}

	// Sucessor do Módulo 3B. Retorna false se encontrou o sucessor de x_[i] (valor em x0) e true se não existe.
	public boolean maximo() {
		int aux2, i0, maxX0_, minX0, aux3, aux4, sumA0, sumA1;
		boolean teste, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {

		// Caso 3: x0 = 1 se d2 é dia de trabalho de p, 0 se não é (com wp0[p] dias de trabalho entre d2 e d3). O menor valor
		// respeita a quantidade mínima de dias Dp[p], a disponibilidade de p em d2 e d3 e wp0[p].
		case 3: {
			i0 = wp0[p];
			if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
				return true;
			if (Mp[p][d_2] == 0 || i0 == maxDias1[p])
				aux2 = 0;
			else
				aux2 = 1;
			x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
			if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
				x0 = 1;
			if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
				return true;
			return false;
		}

		// Caso 2: x0 = aulas do par (Prof., turma) em (d2, d3); A[i] - x0 vão para d4. Análogo ao Caso 2 do Módulo 3A, com maxDias1[p] como
		// limite de dias de trabalho de p entre d2, d3 e d4 e wp0[p] como dias de trabalho entre d2 e d3.
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

			// Limite inferior de x0: o que não cabe em d4 (por dia de trabalho de p em d4, Np1[p] e Ma da turma).
			aux0 = Math.min(maxDias1[p] - wp0[p], 1);
			minX0 = (semGeminadasP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);
			// Se x0 excede o que caberia com um dia de trabalho a menos, p não pode ter aulas em d4.
			aux0 = (semGeminadasP[p] ? maxDias1[p] - 1 : 2 * maxDias1[p] - 2);
			if (x0 > aux0) {
				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}

			// Limite superior de x0: o que cabe em (d2, d3).
			maxX0_ = (semGeminadasP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Caso 4: x0 = aulas do par (Prof., turma) em d2; o resto das aulas q_0 do par em (d2, d3) vai para d3.
		case 4: {
			// n0 = aulas do par em (d2, d3), determinadas no Caso 2.
			n0 = q_0[p][j_[i]];
			if (n0 == 0) {
				if (x0 == -1) {
					x0 = 0;
					return false;
				} else
					return true;
			}
			if (x0 == 2 || (semGeminadasP[p] && x0 == 1))
				return true;
			// Np2[p] e Np3[p]: máximos de aulas de p em d2 e d3 (definidos pelo Caso 3 ou diretamente, para Profs. complex_p2).
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
			// Sp0, Sp2, Sa0 e Sa2: aulas já destinadas ao Prof. e à turma em d2 e em d3 pelas unidades anteriores do Caso 4.
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
		// Caso 35: validação dos resultados dos Casos 2 e 3. Só tem efeito na ida; ao retornar a esta unidade o recuo continua.
		case 35: {
			if (x0 == 0)
				return true;
			sumA0 = 0;
			sumA1 = 0;
			// Para cada turma, soma a contribuição possível de cada Prof. em d2 e em d3, limitada por 1 ou 2 aulas por dia, pela
			// capacidade Mp do dia e pelas aulas q2_0 do par.
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				if (ns_p[p] <= 0)
					continue;
				x0 = x_case__3[p];
				aux0 = (semGeminadasP[p] ? x0 : 2 * x0);
				aux1 = wp0[p] - x0;
				if (!semGeminadasP[p])
					aux1 *= 2;
				aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
				aux4 = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
				aux0 = Math.min(aux0, aux3);
				aux1 = Math.min(aux1, aux4);
				aux2 = nTurmasP[p];
				sumA0 += aux3;
				sumA1 += aux4;
				for (j = 1; j <= aux2; j++) {
					a = lista[p][j];
					sumA_0[a] += Math.min(aux0, q2_0[p][j]);
					sumA_1[a] += Math.min(aux1, q2_0[p][j]);
				}
			}
			// Exige capacidade total de Ma*Qa em cada dia e de Ma por turma e dia.
			teste = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
			if (teste) {
				for (a = 1; a <= Qa; a++) {
					teste = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
					if (!teste)
						break;
				}
			}
			x0 = 0;
			return !teste;
		}
		}
		return true;
	}

	// Atualização das variáveis de controle e de ligação depois de aceito x0.
	public void atualiza2_() {
		switch (case_[i]) {

		// Caso 3: define Np2, Np3 e x_case__3.
		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			x_case__3[p] = x0;
			break;
		}
		// Caso 2: acumula aulas do Prof. e da turma em (d2, d3) e em d4; q_0 recebe as aulas do par em (d2, d3).
		case 2: {
			p = Ap[i];
			nClasses = A[i] - x0;
			aux = apontP2[i];
			Sp0_[aux] = Sp0_[aux - 1] + x0;
			Sp1_[aux] = Sp1_[aux - 1] + nClasses;
			aux = apontA[i];
			Sa_0[aux] = Sa_0[aux - 1] + x0;
			Sa_1[aux] = Sa_1[aux - 1] + nClasses;
			q_0[p][j_[i]] = x0;
			break;
		}

		// Caso 4: acumula aulas do Prof. e da turma em d2 e em d3.
		case 4: {
			p = Ap[i];
			nClasses = q_0[p][j_[i]] - x0;
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

	// Algoritmo 2 do Capítulo 3. Retorna true se obtém a distribuição entre d2, d3 e d4 para a possibilidade e false se não obtém.
	// Aproveita as soluções dos Casos 4 de Profs. anteriores ao Prof. de pMin (guardadas em y2) após um recuo.
	public boolean principal_V3() {
		int k, p0, minimo, iMax4, i0, pMin, p, maxX0_;
		pMin = 0;
		iMax4 = 0;
		minimo = -1;
		// imax: unidade mais avançada atingida (usada por getP e getA).
		imax = 1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		// Sentinelas dos arranjos Ap.
		Ap[aux35] = 0;
		Ap[n2 + 1] = 0;
		do {
			nIteracoes++;
			if (!maximo()) {
				// Ao passar pela unidade do Caso 35 depois de um recuo, repõe de y2 as soluções do Caso 4 dos Profs. anteriores ao Prof. de pMin, sem recalculá-las...
				if (case_[i] == 35 && pMin > 0) {
					p0 = i + 1;
					while (p0 != iMax4 && pMin != p2_[p0])
						p0++;
					iMax4 = i + 1;
					x_[i] = 0;
					iMaxI_[i] = p0;
					p = d2_[p2_[p0 - 1]];
					for (i = i + 1; i < p0; i++) {
						n0 = q_0[Ap[i]][j_[i]];
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
					pMin = 0;
				// ...senão guarda x0, atualiza as variáveis de controle e avança.
				} else {
					x_[i] = x0;
					atualiza2_();
					if (case_[i] == 35)
						iMax4 = i + 1;
					i++;
				}
				// Todas as unidades fechadas: distribuição obtida.
				if (i > n2) {
					System.out.println();
					return true;
				}
				// Ao começar as unidades do Caso 4 de um novo Prof., guarda em y2[p] as soluções do Caso 4 até o Prof. anterior.
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux35 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = (short) x_[i0];
				}
				if (i > imax)
					imax = i;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Falha: recua para p0 = z[i][iMaxI_[i] - i]; pMin registra o menor Prof. atingido pelo recuo, quando este parte do Caso 4 e chega a um Caso 2 ou 3.
			} else {
				if (i == 1)
					return false;
				p0 = z[i][iMaxI_[i] - i];
				if (case_[p0] <= 3 && case_[i] == 4) {
					pMin = p2_[p0];
					if (pMin == 0) {
						System.out.println(case_[p0] + " (teste9) ");
						System.exit(0);
					}
				} else {
					if (case_[p0] != 4 && p2_[p0] < pMin) {
						pMin = p2_[p0];
						if (pMin == 0) {
							System.out.println(case_[p0] + " (teste10) ");
							System.exit(0);
						}
					}
				}
				if (p0 == 0)
					return false;
				// Redefine com o gerador as unidades abandonadas.
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}

	// Método 'dependencia' do Capítulo 3: indica se a unidade j depende da unidade i (i < j). Caso 2: depende de unidades do
	// Caso 2 do mesmo Prof. ou da mesma turma; Casos 3 e 35: dependem das unidades do Caso 3; Caso 4: do Caso 2 do mesmo par,
	// do Caso 3 do mesmo Prof. e do Caso 4 do mesmo Prof. ou da mesma turma.
	public boolean dependencia(int i, int j) {
		int p1, p2, a1, a2;
		if (j <= i)
			return false;
		p1 = Ap[i];
		a1 = Aa[i];
		p2 = Ap[j];
		a2 = Aa[j];
		if (case_[j] == 2)
			return p1 == p2 || a1 == a2;
		if (case_[j] == 3 || case_[j] == 35)
			return case_[i] == 3;
		if (case_[j] == 4) {
			if (case_[i] == 2)
				return p1 == p2 && a1 == a2;
			if (case_[i] == 4)
				return p1 == p2 || a1 == a2;
			if (case_[i] == 3)
				return p1 == p2;
			if (case_[i] == 35)
				return false;
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

	// Grava em dd as aulas por dia nos dias 2, 3 e 4: d4 recebe A[i] - x_[i] (Caso 2); no Caso 4, d2 recebe x_[i] e d3 recebe o resto do par.
	// Observação: o Módulo 3A recalcula depois a distribuição de d2 (lê somente d3 e d4 de dd).
	public void atualiza4() {
		int i;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nTurmasP[p]; j++) {
				dd[p][j][2] = 0;
				dd[p][j][3] = 0;
				dd[p][j][4] = 0;
			}
		}
		for (i = 1; i <= n2; i++) {
			p = Ap[i];
			j = j_[i];
			if (case_[i] == 2)
				dd[p][j][4] = A[i] - x_[i];
			if (case_[i] == 4) {
				dd[p][j][3] = q_0[p][j] - x_[i];
				dd[p][j][2] = x_[i];
			}
		}
	}

	// Procura, entre os valores do Caso 3 (busca com recuo), uma definição de x_case__3 que passe nos testes de demanda das turmas
	// (case__35) e dos Profs. (case__35_). Retorna true se encontra e false se esgota as possibilidades.
	public boolean detPossibP() {
		int minimo;
		boolean teste;
		minimo = -1;
		for (i = 1; i <= aux35_; i++)
			x_[i] = minimo;
		i = 1;
		do {
			// Unidades do Caso 3: usa maximo(). Na unidade final (Caso 35) aplica os testes de demanda.
			if (case_[i] == 3)
				teste = maximo();
			else {
				teste = case__35();
				if (teste)
					teste = case__35_();
				if (teste)
					return true;
				// Se os testes falham, força o recuo (teste = true) para continuar a procura.
				teste = true;
			}
			if (!teste) {
				x_[i] = x0;
				if (case_[i] == 3)
					x_case__3[Ap[i]] = x0;
				i++;
				if (i > aux35_) {
					System.out.println("ERRO 047");
					System.exit(0);
				}
			} else {
				if (i == 1)
					return false;
				x_[i] = minimo;
				i--;
			}
		} while (true);
	}
}
