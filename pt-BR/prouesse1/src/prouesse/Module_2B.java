package prouesse;

import java.util.Arrays;

import javax.swing.JOptionPane;

// Classe Module_2B - implementa o Módulo 2 (Fase 1.2; Seções 4.1.2 e 4.3 do livro).
//
// Finalidade: depois que o Módulo 1 fixou as aulas do dia 0, distribuir as aulas restantes de cada Prof., em cada turma, entre
// os 4 dias seguintes (dias 1 a 4 na numeração permutada pelo Módulo 1), agrupados em dois pares: (d_0,d_1) = (1,2) e (d_2,d_3) =
//   (3,4).
// O Módulo 2 decide quantas aulas de cada (Prof., turma) vão para o 1o par (x) e quantas para o 2o par (y = A - x), além de
// determinar os dias de trabalho dos Profs.; depois chama o Módulo 3 (Module_3) para repartir as aulas dentro de cada par.
//
// Método: busca do Capítulo 3 com recuo inteligente (Algoritmo 2, método principal_V2; z calculado por determinaZ a partir de
// dependência). Cada unidade tem um tipo, dado por case_[i]; a ordem das unidades é a seguinte (ver preliminares):
//   6   : (uma unidade, a primeira) controla graus_liberdade (maximização das possibilidades de dias de trabalho, Seção 4.7);
//   1, 10: para cada Prof., o número de dias de trabalho em (d_0,d_1) e em (d_2,d_3) (para complex = true, quais dias; Seções 4.3.1 e
//     4.3.2);
//   105  : validação das unidades 1 e 10;
//   3    : para cada Prof., se trabalha no dia d_0 (Seção 4.3.3);   35 : validação dos casos 3;
//   30   : idem para o dia d_2 (Seção 4.3.4);                       305: validação dos casos 30;
//   2    : para cada (Prof., turma), quantas aulas ficam no 1o par de dias (Seção 4.3.5);
//   5    : chama o Módulo 3 para cada par de dias, com todas as possibilidades de dias de trabalho memorizadas.
// Após a unidade 5 (índice > n2) são incluídas cópias das unidades dos casos 3, 35, 30 e 305; elas não fazem parte da busca principal:
// são usadas apenas por detPossibP para enumerar é memorizar todas as determinações possíveis dos dias de trabalho (possibP).
public class Module_2B extends FaseB2_Super {
	// graus_liberdade = valor de controle da maximização (Seção 4.7): 1000 na 1a etapa (descobrir max_) e, na 2a, começa em max_ e diminui
	//   de 1 em 1;
	// max_ = maior número de graus de liberdade (cont2) encontrado;  cont2 = número de (Prof., par de dias) cujo único dia de trabalho
	//   pode ser qualquer dos dois;
	// Ep[p] = E_p: número mínimo de aulas que o Prof. p precisa dar em um dia de trabalho (Seção 4.3.1).
	private int graus_liberdade, max_, cont2, Ep[];

	// Construtor: aloca os arranjos próprios do Módulo 2 (dimensionados por Qp e Qa) e cria o objeto do Módulo 3.
	public Module_2B() {
		Ep = new int[Qp + 1];
		Dp = new int[Qp + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		arranjo = new int[4];
		complex_p0 = new boolean[Qp + 1];
		complex_p1 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
		x_case_10 = new int[Qp + 1];
		possibP = new boolean[2][1026][Qp + 1];
		nPossibP = new int[3];
		g2_ = new Module_3();
	}

	// Prepara a execução: calcula as variáveis auxiliares de cada Prof., ordena os Profs., define as unidades (case_, Ap, Aa, j_, A),
	// os ponteiros das variáveis de controle e o arranjo z. Retorna n2, o número de unidades da busca.
	public int preliminares() {
		int d;
		// A maximização começa na 1a etapa (graus_liberdade = 1000, max_ ainda indefinido).
		graus_liberdade = 1000;
		max_ = -1;
		// nDias_[x] = número de dias de trabalho representado pelo valor x das unidades dos casos 1 e 10 (0, 1, 1, 2, 3); para complex = true,
		//   x=1 e x=2 são
		// os dois modos de trabalhar um único dia. d_0..d_3 = dias 1 a 4 tratados pelo módulo (Fase 1.2).
		nDias_ = new int[] { 0, 1, 1, 2, 3 };
		d_0 = 1;
		d_1 = 2;
		d_2 = 3;
		d_3 = 4;
		dias = new int[] { 0, 1, 2, 3, 4 };
		aux4 = 0;
		aux5 = 0;
		aux41 = 0;
		aux51 = 0;
		pMin = 0;
		for (p = 1; p <= Qp; p++) {
			// 1) Variáveis auxiliares por Prof. p: q_0[p][j] = aulas da j-ésima turma nos 4 dias (carga horária menos as do dia 0);
			// ns_p[p] = total de aulas do Prof. nesses 4 dias.
			ns_p[p] = 0;
			aux = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				q_0[p][j] = ns[p][j] - dd[p][j][0];
				ns_p[p] += q_0[p][j];
				aux += dd[p][j][0];
			}
			// Wp1[p] = W_1p: máximo de dias de trabalho nos 4 dias = Wp0 menos 1 se o Prof. já trabalha no dia 0 (limitado a 4).
			Wp1[p] = Wp0[p];
			if (aux > 0)
				Wp1[p]--;
			if (Wp1[p] > 4)
				Wp1[p] = 4;
			for (d = 0; d <= 3; d++)
				arranjo[d] = Mp[p][dias[d + 1]];
			// Dp[p] = D_p: número mínimo de dias de trabalho em 1..4. Somam-se os maiores máximos de aulas diários até cobrir ns_p[p];
			// depois, Dp é elevado ao mínimo exigido por cada turma (q_0 dias se o Prof. não pode ter geminadas; senão, metade arredondada para
			//   cima).
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 3; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p[p])
					break;
			}
			aux0 = 0;
			d = 3;
			for (k = 1; k <= Wp1[p] - 1; k++) {
				aux0 += arranjo[d];
				d--;
			}
			// Ep[p] = E_p: o que resta da carga horária depois de somar os Wp1-1 maiores máximos diários, isto e, o mínimo de aulas que o Prof.
			//   tem de dar em algum dia de trabalho.
			aux0 = ns_p[p] - aux0;
			Ep[p] = Math.max(aux0, 0);
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (semGeminadasP[p])
					Dp[p] = Math.max(Dp[p], q_0[p][j]);
				else
					Dp[p] = Math.max(Dp[p], (q_0[p][j] + 1) / 2);
			}
			// complex_0p e complex_1p: o par de dias tem dois dias disponíveis com máximos de aulas diferentes (os dias não são intercambiáveis).
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
			complex_p1[p] = Mp[p][d_2] > 0 && Mp[p][d_3] > 0 && Mp[p][d_2] != Mp[p][d_3];
		}

		for (p = 1; p <= Qp; p++) {
			d2_[p] = p;
			if (ns_p[p] == 0)
				amplitude[p] = 0.0f;
			else {
				// 2) Ordenação dos Profs. (Seção 4.6.2): amplitude[p] = quantidade de possibilidades de distribuição do Prof. (Module_A);
				// Profs. sem aulas nos 4 dias recebem 0. Em seguida os Profs. são ordenados em ordem crescente (ordena2) e d2_ guarda a ordem.
				Module_A.preliminares(p, Wp1[p]);
				amplitude[p] = (float) Module_A.principal_0();
			}
		}
		// d2_[1..Qp] = Profs. em ordem crescente de número de possibilidades. Profs. com ns_p <= 0 não geram unidades.
		Module_1B.ordena2(1, Qp + 1);

		k = 1;
		// 3) Montagem das unidades. A primeira unidade (índice 1) é do caso 6.
		case_[k] = 6;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// Para cada Prof. (na ordem d2_): uma unidade do caso 1 seguida de uma do caso 10.
			case_[k] = 1;
			Ap[k] = p;
			k++;
			case_[k] = 10;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		// Unidade do caso 105: valida as decisões dos casos 1 e 10.
		case_[k] = 105;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// Unidades do caso 3 (uma por Prof.; aux4 = índice da primeira) e, a seguir, a unidade de validação do caso 35.
			case_[k] = 3;
			if (aux4 == 0)
				aux4 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		// Unidade do caso 35.
		case_[k] = 35;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// Unidades do caso 30 (uma por Prof.; aux5 = índice da primeira) e unidade de validação do caso 305.
			case_[k] = 30;
			if (aux5 == 0)
				aux5 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		// Unidade do caso 305.
		case_[k] = 305;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			for (j = 1; j <= nTurmasP[p]; j++) {
				// Unidades do caso 2: uma para cada (Prof., turma) que tem aulas nos 4 dias (A = número dessas aulas).
				nClasses = q_0[p][j];
				if (nClasses > 0) {
					a = lista[p][j];
					k++;
					Ap[k] = p;
					Aa[k] = a;
					j_[k] = j;
					case_[k] = 2;
					A[k] = nClasses;
				}
			}
		}
		k += 1;
		n2 = k;
		// Unidade do caso 5 (n2 = índice dela = número de unidades da busca): chama o Módulo 3.
		case_[k] = 5;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			// Cópias das unidades 3, 35, 30 e 305 após n2 (aux41 e aux51 = índices das primeiras cópias de 3 e 30): usadas apenas por detPossibP.
			case_[k] = 3;
			if (aux41 == 0)
				aux41 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		case_[k] = 35;

		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			k++;
			case_[k] = 30;
			if (aux51 == 0)
				aux51 = k;
			Ap[k] = p;
		}
		k++;
		Ap[k] = 0;
		case_[k] = 305;

		// 4) Ponteiros apontP2 das unidades do caso 2 para os arranjos Sp0_/Sp1_ (totais acumulados do Prof. nos dois pares de dias); entre os
		// blocos de Profs. diferentes é inserida uma posição zerada, para que o 'valor anterior' da 1a turma de cada Prof. seja 0.
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				apontP2[j] = aux2;
				aux2++;
				if (case_[j + 1] != 2 || Ap[j + 1] != Ap[j]) {
					Sp0_[aux2] = 0;
					Sp1_[aux2] = 0;
					aux2++;
				}
			}
		}

		for (j = 1; j <= Qa; j++) {
			// 5) Ponteiros apontA para os arranjos Sa_0/Sa_1 (totais acumulados por turma): cada turma ocupa um bloco de 16 posições;
			// a posição anterior a de cada unidade guarda o total acumulado antes dela.
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
		// 6) Aloca iMaxI_, calcula z e prepara o Módulo 3 (preliminares2). Retorna n2.
		iMaxI_ = new int[n2 + 2];
		determinaZ(n2);
		g2_.preliminares2();
		return n2;
	}

	// Validações dos casos 105, 35 e 305 (Seções 4.3.2 a 4.3.4). Cada uma é executada uma única vez por visita (a nova chamada, depois de
	// um recuo, retorna true). Retorna false se a validação foi bem-sucedida e true caso contrário (o algoritmo recua).
	public boolean maximo_especial() {
		int sumA0, sumA1, aux1, aux2;
		boolean teste;
		x0 = x_[i];
		// Unidade de validação: só admite uma execução (x_[i] = -1 na primeira vez).
		if (x0 > -1)
			return true;
		x0 = 0;
		sumA0 = 0;
		sumA1 = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
		}

		// Caso 105: com as decisões dos casos 1 e 10 (wp0, wp1, Np0, Np1), a oferta máxima de aulas dos Profs. em cada par de dias deve cobrir
		//   a demanda:
		// soma dos máximos >= 2*Ma*Qa em cada par e, para cada turma, soma >= 2*Ma, onde cada Prof. contribui com min(1 ou 2 aulas por dia x
		//   dias, N, aulas da turma).
		if (case_[i] == 105) {
			// cont2 = graus de liberdade da solução: número de (Prof., par) com um único dia de trabalho e dois dias disponíveis de mesmo máximo
			//   (o dia pode ser qualquer um dos dois).
			cont2 = 0;
			for (p = 1; p <= Qp; p++) {
				if (ns_p[p] <= 0)
					continue;
				if (wp0[p] == 1 && Mp[p][1] > 0 && Mp[p][2] > 0 && !complex_p0[p])
					cont2++;
				if (wp1[p] == 1 && Mp[p][3] > 0 && Mp[p][4] > 0 && !complex_p1[p])
					cont2++;
			}
			// Na 2a etapa, só se aceitam soluções dos casos 1 e 10 com exatamente graus_liberdade graus de liberdade.
			if (graus_liberdade < 1000 && cont2 != graus_liberdade)
				return true;
			for (p = 1; p <= Qp; p++) {
				if (ns_p[p] <= 0)
					continue;
				aux0 = (semGeminadasP[p] ? wp0[p] : 2 * wp0[p]);
				aux1 = (semGeminadasP[p] ? wp1[p] : 2 * wp1[p]);
				aux0 = Math.min(aux0, Np0[p]);
				aux1 = Math.min(aux1, Np1[p]);
				aux2 = nTurmasP[p];
				sumA0 += Np0[p];
				for (j = 1; j <= aux2; j++)
					sumA_0[lista[p][j]] += Math.min(aux0, q_0[p][j]);
				sumA1 += Np1[p];
				for (j = 1; j <= aux2; j++)
					sumA_1[lista[p][j]] += Math.min(aux1, q_0[p][j]);
			}
			teste = sumA0 >= 2 * Ma * Qa && sumA1 >= 2 * Ma * Qa;
			if (teste) {
				for (a = 1; a <= Qa; a++) {
					teste = sumA_0[a] >= 2 * Ma && sumA_1[a] >= 2 * Ma;
					if (!teste)
						break;
				}
			}
			x0 = 0;
			return !teste;
		}

		// Caso 35: percorre as unidades do caso 3 (a partir de aux4, ou de aux41 se estiver nas cópias após n2) e, com Np2/Np3 = máximo de
		//   aulas do Prof. no
		// 1o e no 2o dia do par, verifica se a oferta cobre Ma*Qa aulas em cada dia e Ma aulas por turma em cada dia.
		if (case_[i] == 35) {
			i1 = (i <= n2 ? aux4 : aux41);
			while (case_[i1] == 3) {
				p = Ap[i1];
				aux0 = (semGeminadasP[p] ? x_[i1] : 2 * x_[i1]);
				aux1 = wp0[p] - x_[i1];
				if (!semGeminadasP[p])
					aux1 *= 2;
				x0 = x_[i1];
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
				Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
				aux0 = Math.min(aux0, Np2[p]);
				aux1 = Math.min(aux1, Np3[p]);
				aux2 = nTurmasP[p];
				sumA0 += Np2[p];
				sumA1 += Np3[p];
				for (j = 1; j <= aux2; j++) {
					if (q_0[p][j] <= 0)
						continue;
					a = lista[p][j];
					sumA_0[a] += Math.min(aux0, q_0[p][j]);
					sumA_1[a] += Math.min(aux1, q_0[p][j]);
				}
				i1++;
			}
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

		// Caso 305: o mesmo para o par (d_2,d_3), a partir das unidades do caso 30 (aux5 ou aux51), com Np2_0 e Np3_0.
		if (case_[i] == 305) {
			i1 = (i <= n2 ? aux5 : aux51);
			while (case_[i1] == 30) {
				p = Ap[i1];
				aux0 = (semGeminadasP[p] ? x_[i1] : 2 * x_[i1]);
				aux1 = wp1[p] - x_[i1];
				if (!semGeminadasP[p])
					aux1 *= 2;
				x0 = x_[i1];
				Np2_0[p] = (x0 == 0 ? 0 : Mp[p][d_2]);
				Np3_0[p] = (wp1[p] - x0 == 0 ? 0 : Mp[p][d_3]);
				aux0 = Math.min(aux0, Np2_0[p]);
				aux1 = Math.min(aux1, Np3_0[p]);
				aux2 = nTurmasP[p];
				sumA0 += Np2_0[p];
				sumA1 += Np3_0[p];
				for (j = 1; j <= aux2; j++) {
					if (q_0[p][j] <= 0)
						continue;
					a = lista[p][j];
					sumA_0[a] += Math.min(aux0, q_0[p][j]);
					sumA_1[a] += Math.min(aux1, q_0[p][j]);
				}
				i1++;
			}
			teste = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
			if (teste) {
				for (a = 1; a <= Qa; a++) {
					teste = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
					if (!teste)
						break;
				}
			}
			x0 = 0;
			// Na 1a etapa (graus_liberdade == 1000) e na busca principal (i <= n2), se a validação passar apenas se registra max_ = max(max_,
			//   cont2)
			// e a unidade retorna false; principal_V2 então forca o recuo para continuar enumerando todas as soluções dos casos 1 e 10.
			if (i > n2 || graus_liberdade < 1000)
				return !teste;
			if (teste) {
				System.out.println("max_ = " + max_);
				max_ = Math.max(max_, cont2);
				return false;
			}
			return true;
		}
		x0 = 0;
		return false;
	}

	// Caso 1 (Seção 4.3.1): sucessor de x[i] para o Prof. p = Ap[i]; x = número de dias de trabalho em (d_0,d_1) (ou, se complex_p0 =
	//   true,
	// 0 = nenhum, 1 = só d_0, 2 = só d_1, 3 = ambos). Retorna false se há sucessor (em x0).
	public boolean maximo_1() {
		int x0_;
		// Poda: a soma dos produtos wp0 x Ep dos Profs. anteriores (unidades de índice par) não pode ultrapassar 2*Ma*Qa, o total de aulas que
		//   a escola pode ter em (d_0,d_1).
		if (x_[i] == -1) {
			aux0 = 0;
			for (k = 2; k < i; k += 2)
				aux0 += wp0[Ap[k]] * Ep[Ap[k]];
			if (aux0 > 2 * Ma * Qa)
				return true;
		}
		p = Ap[i];
		x0 = x_[i];
		// aux2 = minimo de dias de trabalho em (d_0,d_1): Dp menos os dias de (d_2,d_3) em que o Prof. tem disponibilidade.
		aux2 = Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0) - (Mp[p][d_3] > 0 ? 1 : 0);
		// complex_p0: candidatos x0 = 0, 1, 2, 3 com número de dias nDias_[x0] >= aux2; rejeita x0 > 3, mais dias que Wp1 ou que os dias
		//   disponíveis em (d_0,d_1).
		if (complex_p0[p]) {
			do {
				x0++;
				x0_ = nDias_[x0];
			} while (x0_ < aux2);
			return (x0 > 3 || x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0));
		// Senão: x0 = número de dias, no mínimo aux2; rejeita x0 > 2, x0 > Wp1[p] ou mais dias que os disponíveis.
		} else {
			x0 = Math.max(x0 + 1, aux2);
			return (x0 > 2 || x0 > Wp1[p] || x0 > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0));
		}
	}

	// Caso 10 (Seção 4.3.2): análogo ao caso 1, para o par (d_2,d_3) (os parâmetros d_0 e d_1 recebem aqui os dias d_2 e d_3).
	// aux20 = minimo de dias de trabalho nesse par (Dp - wp0); aux2 = máximo de aulas do Prof. em (d_0,d_1) conforme o definido no caso 1.
	// Cada candidato x0 deve respeitar Wp1 - wp0, a disponibilidade e permitir cumprir a carga horária ns_p[p] (aux2 + oferta do par
	//   atual).
	public boolean maximo_10(int d_0, int d_1) {
		int x0_, aux20, aux;
		if (x_[i] == -1) {
			aux0 = 0;
			for (k = 3; k < i; k += 2)
				aux0 += wp1[Ap[k]] * Ep[Ap[k]];
			if (aux0 > 2 * Ma * Qa)
				return true;
		}
		p = Ap[i];
		x0 = x_[i];
		aux20 = Dp[p] - wp0[p];
		aux2 = 0;
		if (complex_p0[p]) {
			aux = x_case_1[p];
			if (aux > 0) {
				if (aux == 1)
					aux2 = Mp[p][1];
				else {
					if (aux == 2)
						aux2 = Mp[p][2];
					else
						aux2 = Mp[p][1] + Mp[p][2];
				}
			}
		} else {
			if (wp0[p] > 0) {
				if (wp0[p] == 1)
					aux2 = Math.max(Mp[p][1], Mp[p][2]);
				else
					aux2 = Mp[p][1] + Mp[p][2];
			}
		}
		if (complex_p1[p]) {
			while (true) {
				do {
					x0++;
					x0_ = nDias_[x0];
				} while (x0_ < aux20);
				if (x0 > 3 || wp0[p] + x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
					return true;
				aux = aux2;
				if (x0 > 0) {
					if (x0 == 1)
						aux += Mp[p][d_0];
					else {
						if (x0 == 2)
							aux += Mp[p][d_1];
						else
							aux += Mp[p][d_0] + Mp[p][d_1];
					}
				}
				if (ns_p[p] <= aux)
					return false;
			}
		} else {
			while (true) {
				x0 = Math.max(x0 + 1, aux20);
				if (x0 > 2 || wp0[p] + x0 > Wp1[p] || x0 > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
					return true;
				aux = aux2;
				if (x0 > 0) {
					if (x0 == 1)
						aux += Math.max(Mp[p][d_0], Mp[p][d_1]);
					else
						aux += Mp[p][d_0] + Mp[p][d_1];
				}
				if (ns_p[p] <= aux)
					return false;
			}
		}
	}

	// Caso 3 (Seção 4.3.3): x = 1 se o Prof. trabalha no dia d_0 e 0 se não. Restrições: x <= wp0; x = 0 se Mp[p][d_0] == 0;
	// x > 0 se necessário para atingir os dias mínimos Dp (considerando d_1 e wp1) e o número de dias wp0 do par; se complex_p0 e wp0 ==
	//   1, o
	// valor e consequência de x_case_1 (1: trabalha em d_0; 2: trabalha em d_1). Retorna false se há sucessor.
	public boolean maximo_3() {
		int i0, x0_;
		boolean teste;
		p = Ap[i];
		x0 = x_[i];
		while (true) {
			i0 = wp0[p];
			if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
				return true;
			x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - wp1[p]);
			if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
				x0 = 1;
			if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
				return true;
			teste = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
			teste = teste && ((!complex_p0[p]) || (i0 != 1) || (x_case_1[p] == 1 && x0 == 1)
					|| (x_case_1[p] == 2 && x0 == 0));
			if (teste)
				return false;
		}
	}

	// Caso 30 (Seção 4.3.4): análogo ao caso 3, para o dia d_2 do par (d_2,d_3), usando wp1, x_case_10 e Dp - (disponibilidade em d_3) -
	//   wp0.
	public boolean maximo_30(int d_0, int d_1) {
		int i0, x0_;
		boolean teste;
		p = Ap[i];
		x0 = x_[i];
		while (true) {
			i0 = wp1[p];
			if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
				return true;
			x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][4] > 0 ? 1 : 0) - wp0[p]);
			if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
				x0 = 1;
			if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
				return true;
			teste = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
			teste = teste && ((!complex_p1[p]) || (i0 != 1) || (x_case_10[p] == 1 && x0 == 1)
					|| (x_case_10[p] == 2 && x0 == 0));
			if (teste)
				return false;
		}
	}

	// Caso 2 (Seção 4.3.5): x0 = número de aulas de (p, turma Aa[i]) no 1o par de dias; as demais (A[i] - x0) ficam no 2o par.
	// Restrições: máximo de aulas por dia e por turma (1 ou 2) x dias de trabalho de cada par (wp0, wp1); a soma do Prof. não ultrapassa
	// Np0 (1o par) e Np1 (2o par); a soma na turma não ultrapassa 2*Ma em cada par. Usa as variáveis de controle Sp0_, Sp1_, Sa_0 e Sa_1.
	// Retorna false se há sucessor.
	public boolean maximo_2() {
		int maxX0_, minX0;
		boolean initialized;
		p = Ap[i];
		x0 = x_[i];
		if (x0 == 2 * wp0[p] || (semGeminadasP[p] && x0 == wp0[p]))
			return true;
		// Caso trivial: se o Prof. não tem aulas possíveis em um dos pares, só há um valor (0 ou todas as aulas), tentado uma única vez.
		case_Trivial = Np0[p] == 0 || Np1[p] == 0;
		n0 = A[i];
		// Quando a unidade já foi inicializada, os limites não mudaram desde a última visita: reaproveita maxX0[i] (guardado na primeira vez).
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
		// Totais já atribuídos antes desta unidade: Sp0/Sp2 = aulas do Prof. no 1o/2o par (Sp0_, Sp1_); Sa0/Sa2 = aulas da turma no 1o/2o par
		//   (Sa_0, Sa_1).
		Sp0 = Sp0_[apontP2[i] - 1];
		Sp2 = Sp1_[apontP2[i] - 1];
		Sa0 = Sa_0[apontA[i] - 1];
		Sa2 = Sa_1[apontA[i] - 1];

		// Limite inferior: o que não cabe no 2o par (por dias de trabalho wp1, por Np1 - Sp2 e por 2*Ma na turma).
		minX0 = (semGeminadasP[p] ? n0 - wp1[p] : n0 - 2 * wp1[p]);
		minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
		minX0 = Math.max(minX0, Sa2 + n0 - 2 * Ma);
		x0 = Math.max(x0, minX0);
		// Limite superior: dias de trabalho do 1o par, A[i], Np0 - Sp0 (capacidade restante do Prof.) e 2*Ma - Sa0 (capacidade restante da
		//   turma).
		maxX0_ = (semGeminadasP[p] ? wp0[p] : 2 * wp0[p]);
		maxX0_ = Math.min(maxX0_, n0);
		maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
		maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
		maxX0[i] = maxX0_;
		return x0 > maxX0_;
	}

	// Atualiza as variáveis de ligação após uma unidade do caso 1 (com Np0, wp0, d_0, d_1, x_case_1, complex_p0) ou do caso 10 (com Np1,
	//   wp1,
	// d_2, d_3, x_case_10 e complex do 2o par): Np = máximo de aulas do Prof. no par; wp = número de dias de trabalho; x_case = valor de
	//   x.
	public void atualiza_1(int Np0[], int wp0[], int d_0, int d_1, int[] x_case_1, boolean[] complex_p0) {
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
			wp0[p] = x0;
		}
	}

	// Atualiza as variáveis de controle e de ligação após uma unidade do caso 2: totais acumulados do Prof. (Sp0_, Sp1_) e da turma (Sa_0,
	//   Sa_1)
	// nos dois pares de dias, e q2_ / q2_0 = aulas de (p, turma) no 1o e no 2o par (entradas do Módulo 3).
	public void atualiza_2() {
		p = Ap[i];
		nClasses = A[i] - x0;
		aux = apontP2[i];
		Sp0_[aux] = Sp0_[aux - 1] + x0;
		Sp1_[aux] = Sp1_[aux - 1] + nClasses;
		aux = apontA[i];
		Sa_0[aux] = Sa_0[aux - 1] + x0;
		Sa_1[aux] = Sa_1[aux - 1] + nClasses;
		q2_[p][j_[i]] = x0;
		q2_0[p][j_[i]] = nClasses;
	}

	// Enumera e memoriza em possibP todas as determinações dos dias de trabalho para o par exec (0: (d_0,d_1); 1: (d_2,d_3)) compatíveis
	//   com as
	// decisões dos casos 1 e 10 (Seção 4.2.2, observação 4). Usa as cópias das unidades 3/35 (ou 30/305) após n2 com um recuo simples: ao
	//   fechar a
	// unidade de validação, a possibilidade é gravada (possibP[exec][k][p] = true se o Prof. trabalha no 1o dia do par) e a busca continua
	// até esgotar todas as possibilidades.
	public void detPossibP(int exec) {
		int minimo, i0, imax, j, k;
		boolean teste;
		j = 0;
		imax = 0;
		minimo = -1;
		for (i = aux41; i <= x_.length - 1; i++) {
			x_[i] = minimo;
			if ((exec == 0 && case_[i] == 35) || (exec == 1 && case_[i] == 305))
				j = i;
		}

		i0 = (exec == 0 ? aux41 : aux51);
		i = i0;
		do {
			if (exec == 0) {
				if (case_[i] == 3)
					teste = maximo_3();
				else
					teste = maximo_especial();
			} else {
				if (case_[i] == 30)
					teste = maximo_30(d_2, d_3);
				else
					teste = maximo_especial();
			}
			if (!teste) {
				x_[i] = x0;
				if (i == j) {
					k = nPossibP[exec];
					if (k >= 1026) {
						System.out.println("ERRO: ESGOTOU O ESPAÇO PARA ARMAZENAR AS POSSIBILIDADES.");
					} else {
						for (i = i0; i <= j - 1; i++)
							possibP[exec][k][Ap[i]] = x_[i] != 0;
						nPossibP[exec]++;
						i = j;
					}
				} else {
					i++;
					if (i > imax) {
						imax = i;
						System.out.println("i=" + imax + " Caso[i]=" + case_[i]);
					}
				}
			} else {
				if (i == i0) {
					System.out.println("Número de possibilidades para exec = " + exec + ": " + nPossibP[exec]);
					return;
				}
				x_[i] = minimo;
				i--;
			}
		} while (true);

	}

	// Busca principal do Módulo 2: Algoritmo 2 do Capítulo 3 (gerador = minimo = -1; iMaxI_ = iMax; z = arranjo z).
	// Retorna true se todas as unidades foram fechadas (distribuição dos 4 dias completa) e false se o problema não tem solução.
	public boolean principal_V2() {
		int p0, minimo, k, i0, salvaI, imax_;
		boolean teste;
		Object[] opcoes = { "Sim", "Não" };
		imax_ = 0;
		minimo = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		do {
			// Controle de tempo: a cada 50000 iterações confere o tempo decorrido; se exceder o limite informado, o programa é encerrado.
			nIteracoes++;
			if (nIteracoes >= 50000L) {
				nIteracoes = 0L;
				if ((System.currentTimeMillis() - tempoInicial) / 60000.0 > tempoMaximo) {
					JOptionPane.showMessageDialog(null, "Execução excedeu o tempo máximo", "Resultado",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			teste = true;
			// 1) Obtenção do sucessor de x[i]: cada tipo de unidade chama o método correspondente (teste = true significa 'sem sucessor / falha').
			switch (case_[i]) {
			// Caso 6 (primeira unidade): ajusta graus_liberdade (cada novo valor é um recomeço da busca). Na 1a visita nada muda. Ao retornar a
			//   ela:
			// se graus_liberdade < 1000, falha quando chega a 0 e senão diminui 1; se ainda e 1000, passa a valer max_ (falha se max_ == -1, isto
			//   e, nenhuma solução).
			case 6: {
				x0 = 0;
				teste = false;
				if (x_[i] > -1) {
					if (graus_liberdade < 1000) {
						if (graus_liberdade == 0)
							teste = true;
						else
							graus_liberdade--;
					} else {
						graus_liberdade = max_;
						System.out.println("graus_liberdade = " + graus_liberdade);
						if (max_ == -1)
							teste = true;
					}
				}
				break;
			}
			case 105: {
				teste = maximo_especial();
				break;
			}
			case 35: {
				teste = maximo_especial();
				break;
			}
			// Caso 305: além de validar, na 1a etapa (graus_liberdade == 1000) a unidade, mesmo bem-sucedida, forca um recuo até a unidade do caso
			//   105
			// (i1_), de modo a enumerar todas as soluções dos casos 1 e 10 e descobrir max_; se falhar, o recuo é de uma unidade.
			case 305: {
				teste = maximo_especial();
				if (!teste) {
					if (graus_liberdade == 1000) {
						i1_ = i;
						do
							i1_--;
						while (case_[i1_] != 105);
						teste = true;
					}
				} else
					i1_ = i - 1;
				break;
			}
			case 1: {
				teste = maximo_1();
				break;
			}
			case 10: {
				teste = maximo_10(d_2, d_3);
				break;
			}
			case 3: {
				teste = maximo_3();
				break;
			}
			case 30: {
				teste = maximo_30(d_2, d_3);
				break;
			}
			// Caso 2: x0 = aulas de (p, turma) no 1o par.
			case 2: {
				teste = maximo_2();
				break;
			}
			// Caso 5: chama o Módulo 3 para cada par de dias, tentando uma a uma as possibilidades de dias de trabalho memorizadas (possibP):
			// primeiro exec = 0 com q2_ (aulas no 1o par); se fechar, g2_.atualiza4(0) grava dd e repete com exec = 1 e q2_0 (aulas no 2o par).
			// Se nenhuma possibilidade fecha, g2_.principal2 devolveu em imax a unidade do Módulo 3 mais avancada atingida; dela obtém-se o Prof.
			//   (p) e a
			// turma (a) onde o Módulo 3 falhou e o recuo vai para a unidade do caso 2 correspondente (i1_). Aqui imax é o menor índice alcançado
			//   entre as possibilidades (Math.min).
			case 5: {
				exec = 0;
				sucesso = false;
				cont = nPossibP[exec];
				imax = 50000;
				for (possib = 0; possib < cont; possib++) {
					aux = g2_.principal2(q2_, exec, possib);
					if (aux == 0) {
						sucesso = true;
						break;
					}
					imax = Math.min(imax, aux);
				}
				if (sucesso) {
					g2_.atualiza4(0);
					System.out.println();
					System.out.println("Fechou para exec = 0");
					exec = 1;
					sucesso = false;
					cont = nPossibP[exec];
					imax = 50000;
					for (possib = 0; possib < cont; possib++) {
						aux = g2_.principal2(q2_0, exec, possib);
						if (aux == 0) {
							sucesso = true;
							g2_.atualiza4(1);
							break;
						}
						imax = Math.min(imax, aux);
					}
				}
				if (sucesso)
					x0 = 0;
				else {
					p = g2_.getP(imax);
					a = g2_.getA(imax);
					i1_ = n2;
					do
						i1_--;
					while (Ap[i1_] != p || Aa[i1_] != a);
					System.out.print("i1_=" + i1_ + " ");
				}
				teste = !sucesso;
				break;
			}
			}

			// 2) Houve sucesso: atualiza as variáveis de ligação/controle (casos 1, 10 e 2), guarda x0, e, ao passar pelo caso 305, memoriza todas
			//   as
			// possibilidades de dias de trabalho (detPossibP, para os dois pares). Depois avanca e atualiza iMaxI_ (unidade mais avancada já
			//   atingida).
			if (!teste) {
				switch (case_[i]) {
				case 1: {
					atualiza_1(Np0, wp0, d_0, d_1, x_case_1, complex_p0);
					break;
				}
				case 10: {
					atualiza_1(Np1, wp1, d_2, d_3, x_case_10, complex_p1);
					break;
				}
				case 2: {
					atualiza_2();
					break;
				}
				}
				x_[i] = x0;
				// Passou o caso 305: as decisões dos casos 1, 10, 3 e 30 estão validadas, então enumeram-se as possibilidades de dias de trabalho.
				if (case_[i] == 305) {
					salvaI = i;
					nPossibP[0] = 0;
					detPossibP(0);
					nPossibP[1] = 0;
					detPossibP(1);
					i = salvaI;
				}
				i++;
				if (i > imax_) {
					imax_ = i;
					System.out.println("V2 " + i + "/" + n2 + " case_ " + case_[i]);
				}
				// Todas as unidades fechadas (incluindo a chamada ao Módulo 3): distribuição completa dos 4 dias obtida.
				if (i > n2) {
					System.out.println((float) nIteracoes / 1000000.0f);
					return true;
				}
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				// 3) Sem sucessor: recuo do Algoritmo 2. p0 = z[i][iMaxI_[i] - i] é a unidade para a qual recuar; para os casos 5 e 305, p0 = i1_
				//   (definido acima).
				// Se i == 1 ou p0 == 0 o problema não tem solução. Em seguida todas as unidades após p0 voltam ao gerador e a busca continua em p0.
				if (case_[i] == 5 || case_[i] == 305)
					p0 = i1_;
				else
					p0 = z[i][iMaxI_[i] - i];
				// Fim da pesquisa: todas as possibilidades foram esgotadas.
				if (i == 1 || p0 == 0) {
					System.out.println("Pesquisou todas as possibilidades.");
					return false;
				}
				// Recuo a partir da primeira unidade do caso 2: salta para a unidade do caso 105 (o recuo prossegue automaticamente a partir dela).
				if (case_[i] == 2 && case_[i - 1] != 2) {
					p0 = i - 1;
					System.out.println("ADVERTÊNCIA: Recuando a partir do case_ 2.");
					while (case_[p0] != 105)
						p0--;
				}
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}

	// dependencia(i,j) (Capítulo 3 e Seção 4.3.5): true se a variável de decisão x[j] depende de x[i]. Não há dependência se j <= i;
	// os casos 3/35 não influenciam os casos 30/305 (e vice-versa); duas unidades do caso 2 só dependem uma da outra se tiverem o mesmo
	//   Prof. ou a mesma turma;
	// nos demais casos considera-se que existe dependência.
	public boolean dependencia(int i, int j) {
		if (j <= i)
			return false;
		if ((case_[i] == 3 || case_[i] == 35) && (case_[j] == 30 || case_[j] == 305))
			return false;
		if (case_[i] == 2 && case_[j] == 2)
			return Ap[i] == Ap[j] || Aa[i] == Aa[j];
		else
			return true;
	}

	// Calcula o arranjo z do Algoritmo 2 (método determinaArranjo do Capítulo 3): z[i][l] = maior unidade p < i da qual alguma unidade de
	// i até i+l depende (segundo dependência), tornado cumulativo em l. Ele diz para onde recuar quando a busca não avanca além de i+l.
	public void determinaZ(int n_) {
		int p, i, l;
		z = new short[n_ + 1][n_ + 1];
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
}
