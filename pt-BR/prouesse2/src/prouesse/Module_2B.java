package prouesse;

import java.awt.datatransfer.SystemFlavorMap;
import java.util.Arrays;

import javax.swing.JOptionPane;

// Classe Module_2B - implementa o Módulo 2B (Fase 1.2 do Algoritmo P2; Seções 5.2.4, 5.3.2, 5.4 e 5.4.3 do livro).
//
// Papel: com os resultados do Módulo 2A (maxDias0, maxDias1, dias de trabalho dos Profs. complexos, possibilidades em
// bitSet_0 e bitSet_2), distribui as aulas de cada par (Prof. p, turma a) entre as duas partes da semana: x[k] aulas na
// 1ª parte (dias d0 e d1) e A[k] - x[k] na 2ª parte (dias d2, d3 e d4). Só há dois tipos de unidade:
//   - Caso 2: uma unidade por par (Prof., turma); a variável de decisão é x[k] (ver maximo_2);
//   - Caso 10: uma unidade por Prof., posterior à última unidade do Caso 2 do Prof.; é uma unidade de validação (maximo_10) que
//     confronta o que já foi decidido nas unidades do Caso 2 com as possibilidades e sub-possibilidades do Módulo 2A.
// Há ainda uma unidade final, do Caso 5, que chama os Módulos 3B e 3A (case_50) para finalizar a distribuição entre os
// cinco dias; se eles falharem, a busca retorna a uma unidade do Caso 2 (principal_V1).
public class Module_2B extends Probl {

	// Np0[p] e Np1[p]: máximo de aulas de p na 1ª e na 2ª parte; wp0[p]: número de dias de trabalho de p em d2 e d3 (na
	// possibilidade da 2ª parte em teste); x_case__3[p]: 1 se o primeiro dia da parte da semana em validação é dia de trabalho
	// de p (conforme a possibilidade ou sub-possibilidade em teste), 0 se não é; ns_p0[p] e ns_p1[p]: carga horária de p na 1ª e
	// na 2ª parte, conhecida depois que as unidades do Caso 2 de p foram executadas; sumP0 e sumP1: somas acumuladas
	// dos mínimos de aulas dos Profs. em d2 e d3; Ep[p]: mínimo de aulas de p em um dia da 2ª parte; ch2[p][j]: melhor limitante
	// superior disponível para as aulas do par (p, j-ésima turma) na parte da semana em validação (a variável ns2 do livro,
	// Seção 5.4); somaMax: soma dos máximos de aulas dos dias de trabalho de p na 2ª parte; case_10: índice da primeira
	// unidade do Caso 10 a partir da qual se aplica o teste das somas dos mínimos de aulas por dia (Seção 5.4.2);
	// x2 e Ap2: arranjos da busca auxiliar sobre os Profs. de maximo_10.
	static public int Np0[], Np1[], contTentativas, contSucessos2, wp0[], x_case__3[], ns_p0[], ns_p1[], sumP0[],
			sumP1[], Ep[], ch2[][], somaMax[], case_10, x2[], Ap2[];
	// complex_p0 e complex_p1: indicadores definidos no Módulo 2A (Seção 5.2.1); diasTrabalhoP[p][d]: p trabalha no dia d
	// (validação de três dias); primeiraPossibilidade: true enquanto se testa a primeira possibilidade (para Profs. não
	// complexos o resultado do teste não depende da possibilidade, então só é testado uma vez); teste3: passa a false quando uma
	// demanda de Prof. não é atendida por uma possibilidade, o que torna inútil testar as demais.
	static public boolean complex_p0[], complex_p1[], diasTrabalhoP[][], primeiraPossibilidade, teste3;

	// Construtor: aloca os arranjos usados pelo módulo.
	public Module_2B() {
		Dp = new int[Qp + 1];
		somaMax = new int[Qp + 1];
		diasTrabalhoP = new boolean[Qp + 1][5];
		arranjo = new int[3];
		wp0 = new int[Qp + 1];
		x_case__3 = new int[Qp + 1];
		ns_p0 = new int[Qp + 1];
		ns_p1 = new int[Qp + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		sumA_1_ = new int[Qa + 1];
		sumP0 = new int[Qp + 1];
		sumP1 = new int[Qp + 1];
		x2 = new int[Qp + 3];
		Ap2 = new int[Qp + 3];
		Ep = new int[Qp + 1];
		ch2 = new int[Qp + 1][];
		for (p = 1; p <= Qp; p++)
			ch2[p] = new int[nTurmasP[p] + 1];
	}

	// Prepara a execução do Módulo 2B (Seção 5.3.2): obtém os dados do Módulo 2A, calcula Np0 e Np1, monta as unidades
	// (Caso 2 e Caso 10 por Prof., e uma unidade final do Caso 5) e as variáveis de controle. Retorna o número de unidades (n2 + 1).
	public int preliminares() {
		int aux;
		sumP0[0] = 0;
		sumP1[0] = 0;
		// Objetos dos Módulos 3B e 3A que serão executados depois do Caso 2.
		g2_0 = new Module_3B();
		g2_ = new Module_3A();
		// Arranjos maxDias0 e maxDias1 determinados pelo Módulo 2A.
		maxDias0 = Module_2A.maxDias0;
		maxDias1 = Module_2A.maxDias1;
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		complex_p0 = Module_2A.complex_p0;
		complex_p1 = Module_2A.complex_p1;
		// Np0[p] e Np1[p] (Seção 5.2.1): para Profs. complexos, soma dos Mp dos dias de trabalho fixados em bitSet_0 e bitSet_1;
		// para os demais, quantidade de dias de trabalho vezes o maior Mp da parte da semana.
		for (p = 1; p <= Qp; p++) {
			if (complex_p0[p]) {
				Np0[p] = 0;
				for (k = 0; k <= 1; k++) {
					if (Module_2A.bitSet_0.get(2 * (p - 1) + k))
						Np0[p] += Mp[p][k];
				}
			} else
				Np0[p] = maxDias0[p] * Math.max(Mp[p][0], Mp[p][1]);
			if (complex_p1[p]) {
				Np1[p] = 0;
				for (k = 0; k <= 2; k++) {
					if (Module_2A.bitSet_1.get(3 * (p - 1) + k))
						Np1[p] += Mp[p][k + 2];
				}
			} else {
				aux = Math.max(Mp[p][2], Mp[p][3]);
				aux = Math.max(Mp[p][4], aux);
				Np1[p] = maxDias1[p] * aux;
			}
			System.out.println(p + " " + Np0[p] + " " + Np1[p] + " " + maxDias0[p] + " " + maxDias1[p]);
		}
		// Montagem das unidades, na ordem dos Profs. estabelecida pelo Módulo 2A (d2_): primeiro as unidades do Caso 2 de cada
		// turma do Prof. e, em seguida, a unidade do Caso 10 do Prof. (Seção 5.3.2).
		k = 0;
		case_10 = 10000;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			for (j = 1; j <= nTurmasP[p]; j++) {
				k++;
				Ap[k] = p;
				Aa[k] = lista[p][j];
				j_[k] = j;
				A[k] = ns[p][j];
				case_[k] = 2;
			}
			k++;
			Ap[k] = p;
			case_[k] = 10;
			// Guarda o índice da unidade do Caso 10 do (Qa + 1)-ésimo Prof.; até lá o teste das somas dos mínimos de aulas por dia não é aplicado.
			if (i1 == Qa + 1)
				case_10 = k;
		}
		// n2 é o número de unidades dos Casos 2 e 10; a unidade seguinte é do Caso 5 (chamada dos Módulos 3B e 3A).
		n2 = k;
		case_[n2 + 1] = 5;
		Ap[n2 + 1] = 0;

		// Variáveis de controle: ponteiros e acumuladores de aulas do Prof. e da turma nas duas partes da semana (como no Módulo 2A).
		Sp0_[0] = 0;
		Sp1_[0] = 0;
		aux2 = 1;
		// apontP2[j]: posição em Sp0_ e Sp1_ da unidade j do Caso 2; ao final das unidades de um Prof. é reservada uma posição zerada.
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

		// Sa_0 e Sa_1: aulas acumuladas por turma; cada turma dispõe de 16 posições consecutivas, a partir de auxA[a].
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
		return n2 + 1;
	}

	// Validação de dois dias (Seções 5.4, 5.4.1 e 5.4.2), para a possibilidade da 1ª parte cujos dias de trabalho estão em
	// x_case__3 (dia d0) e maxDias0 - x_case__3 (dia d1). Considera as unidades até iFinal. Retorna true se as demandas dos
	// Profs. são atendidas. Pode alterar teste3 para false, indicando que nenhuma outra possibilidade serviria.
	private boolean validacao_dois_dias(int iFinal) {
		int k, sumP_0, sumP_1, i, p, aux3, aux4, d_0, d_1, soma, d;
		boolean teste;

		// Teste das somas dos mínimos (Seção 5.4.2): em cada dia, o mínimo de aulas de cada Prof. é a carga na 1ª parte menos o que
		// o Prof. pode ministrar no outro dia; a soma desses mínimos não pode ultrapassar Ma*Qa, o total de aulas do dia.
		if (iFinal >= case_10) {
			for (d = 0; d <= 1; d++) {
				soma = 0;
				k = 0;
				while (true) {
					k++;
					p = d2_[k];
					if (d == 0)
						aux = ns_p0[p] - (maxDias0[p] - x_case__3[p] > 0 ? Mp[p][1] : 0);
					else
						aux = ns_p0[p] - (x_case__3[p] > 0 ? Mp[p][0] : 0);
					if (aux > 0)
						soma += aux;
					if (p == Ap[iFinal])
						break;
				}
				if (soma > Ma * Qa)
					return false;
			}
		}
		// Demanda dos Profs. (Seção 5.4.1): para cada Prof. (percorrendo as unidades do Caso 2 e do Caso 10), calcula quantas
		// aulas ele pode ministrar em d0 e em d1 e compara com a carga ns_p0[p].
		d_0 = 0;
		d_1 = 1;
		i = 1;
		teste = false;
		while (true) {
			p = Ap[i];
			// Profs. sem dia de trabalho na 1ª parte, ou não complexos (depois da primeira possibilidade), são pulados: o resultado seria sempre o mesmo.
			if (maxDias0[p] == 0 || (!complex_p0[p] && !primeiraPossibilidade)) {
				do
					i++;
				while (case_[i] == 2);
				teste = true;
				i++;
				if (i > iFinal)
					break;
				continue;
			}
			// aux0 e aux1: dias de trabalho em d0 e d1 (0 ou 1); aux3 e aux4: capacidade máxima de aulas do Prof. em d0 e d1;
			// se são permitidas aulas geminadas, o Prof. pode ter 2 aulas por dia em cada turma.
			sumP_0 = 0;
			sumP_1 = 0;
			aux0 = x_case__3[p];
			aux1 = maxDias0[p] - aux0;
			aux3 = aux0 * Mp[p][d_0];
			aux4 = aux1 * Mp[p][d_1];
			if (!semGeminadasP[p]) {
				aux0 *= 2;
				aux1 *= 2;
			}
			// Para cada turma do Prof., soma o que ela pode contribuir em d0 e em d1 (no máximo as aulas x_[i] já destinadas à 1ª parte).
			do {
				k = x_[i];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
				i++;
			} while (case_[i] == 2);
			i++;
			// Aulas que o Prof. pode ministrar nos dois dias: limitadas pela capacidade de cada dia e pelas contribuições das turmas.
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			// A demanda do Prof. é atendida se a carga ns_p0[p] cabe nessa capacidade.
			teste = ns_p0[p] <= aux0;
			// Falha de um Prof. não complexo ou da primeira possibilidade invalida todas as possibilidades.
			if (!teste)
				teste3 = false;
			if (!teste || i > iFinal)
				break;
		}
		primeiraPossibilidade = false;
		return teste;
	}

	// Validação de três dias (Seções 5.4, 5.4.1 e 5.4.2), análoga à de dois dias, para a sub-possibilidade da 2ª parte: x_case__3
	// indica se d2 é dia de trabalho; wp0 - x_case__3, se d3 é; maxDias1 - wp0, se d4 é. Usa as aulas A[i] - x_[i] da 2ª parte.
	private boolean validacao_tres_dias(int iFinal) {
		int d, k, sumP_0, sumP_1, sumP_1_, i, p, aux3, aux4, aux4_, aux1_, d_0, d_1, d_2, soma;
		boolean teste;

		// Teste das somas dos mínimos em d2, d3 e d4 (Seção 5.4.2): mínimo de aulas de cada Prof. no dia = carga ns_p1 menos a
		// capacidade dos outros dias de trabalho; a soma não pode ultrapassar Ma*Qa.
		if (iFinal >= case_10) {
			k = 0;
			while (true) {
				k++;
				p = d2_[k];
				aux0 = x_case__3[p];
				aux1 = wp0[p] - aux0;
				aux1_ = maxDias1[p] - wp0[p];
				diasTrabalhoP[p][2] = aux0 > 0;
				diasTrabalhoP[p][3] = aux1 > 0;
				diasTrabalhoP[p][4] = aux1_ > 0;
				somaMax[p] = 0;
				for (d = 2; d <= 4; d++)
					somaMax[p] += (diasTrabalhoP[p][d] ? Mp[p][d] : 0);
				if (p == Ap[iFinal])
					break;
			}
			for (d = 2; d <= 4; d++) {
				soma = 0;
				k = 0;
				while (true) {
					k++;
					p = d2_[k];
					aux = ns_p1[p] - somaMax[p] + (diasTrabalhoP[p][d] ? Mp[p][d] : 0);
					if (aux > 0)
						soma += aux;
					if (p == Ap[iFinal])
						break;
				}
				if (soma > Ma * Qa)
					return false;
			}
		}
		// Demanda dos Profs. em d2, d3 e d4 (fórmula da Seção 5.4.1), comparando a capacidade com ns_p1[p].
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		i = 1;
		teste = false;
		while (true) {
			p = Ap[i];
			if (maxDias1[p] == 0 || (!complex_p1[p] && !primeiraPossibilidade)) {
				do
					i++;
				while (case_[i] == 2);
				teste = true;
				i++;
				if (i > iFinal)
					break;
				continue;
			}
			sumP_0 = 0;
			sumP_1 = 0;
			sumP_1_ = 0;
			aux0 = x_case__3[p];
			aux1 = wp0[p] - aux0;
			aux1_ = maxDias1[p] - wp0[p];
			aux3 = aux0 * Mp[p][d_0];
			aux4 = aux1 * Mp[p][d_1];
			aux4_ = aux1_ * Mp[p][d_2];
			if (!semGeminadasP[p]) {
				aux0 *= 2;
				aux1 *= 2;
				aux1_ *= 2;
			}
			do {
				k = A[i] - x_[i];
				sumP_0 += Math.min(aux0, k);
				sumP_1 += Math.min(aux1, k);
				sumP_1_ += Math.min(aux1_, k);
				i++;
			} while (case_[i] == 2);
			i++;
			aux0 = Math.min(aux3, sumP_0);
			aux0 += Math.min(aux4, sumP_1);
			aux0 += Math.min(aux4_, sumP_1_);
			teste = ns_p1[p] <= aux0;
			if (!teste)
				teste3 = false;
			if (!teste || i > iFinal)
				break;
		}
		primeiraPossibilidade = false;
		return teste;
	}

	// Sucessor do Caso 10 (validação, Seção 5.4). Retorna false (aprovado) ou true (reprovado, inicia o recuo).
	// Etapas: (1) validação de dois dias: procura uma possibilidade da 1ª parte (bitSet_0) compatível com as unidades do
	// Caso 2 já definidas; (2) validação de três dias: procura uma possibilidade da 2ª parte (bitSet_2) e, entre as suas
	// sub-possibilidades, uma compatível. Se alguma das etapas não encontra nenhuma, a unidade falha.
	public boolean maximo_10(int iFinal) {
		boolean sucesso, maximo_;
		int cont, possib, p, minimo, i0, n2_, i, d_0, d_1, d_2, d, salvaI, soma;

		// Ao retornar a esta unidade, apenas continua o recuo.
		if (x_[iFinal] > -1)
			return true;
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		// Cargas horárias do Prof. nas duas partes: acumuladas nas variáveis de controle até a unidade anterior.
		p = Ap[iFinal];
		ns_p0[p] = Sp0_[apontP2[iFinal - 1]];
		ns_p1[p] = Sp1_[apontP2[iFinal - 1]];

		sucesso = false;
		// cont = número de possibilidades da 1ª parte.
		cont = Module_2A.nPossib[0];
		if (cont == 0) {
			System.out.print("erro1");
			System.exit(0);
		}
		// ch2 (1ª parte): aulas já decididas (x_[i]) para as unidades do Caso 2 anteriores; para as ainda não executadas, a carga semanal A[i] (melhor limitante disponível).
		for (i = 1; i <= iFinal; i++) {
			if (case_[i] == 2)
				ch2[Ap[i]][j_[i]] = x_[i];
		}
		for (i = iFinal + 1; i <= n2; i++) {
			if (case_[i] == 2)
				ch2[Ap[i]][j_[i]] = A[i];
		}

		teste3 = true;
		primeiraPossibilidade = true;
		// Etapa 1 - validação de dois dias: testa cada possibilidade da 1ª parte, colocando em x_case__3 se d0 é dia de trabalho.
		for (possib = 1; possib <= cont; possib++) {
			for (p = 1; p <= Qp; p++)
				x_case__3[p] = (Module_2A.bitSet_0.get(possib * Qp * 2 + 2 * (p - 1)) ? 1 : 0);
			// Atende às demandas dos Profs.?
			sucesso = validacao_dois_dias(iFinal);
			// Se uma demanda de Prof. falhou, nenhuma possibilidade servirá: a unidade é reprovada.
			if (!teste3)
				return true;
			// Atende também às demandas das turmas? (case__350_) Se sim, basta uma possibilidade.
			if (sucesso) {
				sucesso = case__350_();
				if (sucesso)
					break;
			}
		}
		if (!sucesso)
			return (true);
		// Preparação da etapa 2: ch2 passa a representar as aulas de cada par na 2ª parte (A[i] - x_[i]).
		for (i = 1; i <= iFinal; i++) {
			if (case_[i] == 2)
				ch2[Ap[i]][j_[i]] = A[i] - x_[i];
		}
		// Dp[p]: número mínimo de dias de trabalho de p em d2, d3 e d4 para cumprir ns_p1[p] (usa os maiores Mp primeiro) e para
		// que cada turma de p tenha suas aulas na 2ª parte (1 por dia, ou 2 se permitido). Ep[p]: mínimo de aulas de p em um dia, supondo
		// os outros dias de trabalho na capacidade máxima. Só são necessários para Profs. que não são complex_p2.
		p = Ap[iFinal];
		if (!Module_2A.complex_p2[p]) {
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][d + 2];
			Arrays.sort(arranjo);
			aux0 = 0;
			Dp[p] = 0;
			for (d = 2; d >= 0; d--) {
				aux0 += arranjo[d];
				Dp[p]++;
				if (aux0 >= ns_p1[p])
					break;
			}
			i = iFinal - 1;
			case_[0] = 0;
			while (case_[i] == 2) {
				if (semGeminadasP[p])
					Dp[p] = Math.max(Dp[p], A[i] - x_[i]);
				else
					Dp[p] = Math.max(Dp[p], (A[i] - x_[i] + 1) / 2);
				i--;
			}
			aux0 = 0;
			d = 2;
			for (k = 1; k <= maxDias1[p] - 1; k++) {
				aux0 += arranjo[d];
				d--;
			}
			Ep[p] = Math.max(ns_p1[p] - aux0, 0);
		}

		// Ap2 lista os Profs. (na ordem de d2_) que não são complex_p2: estes têm os dias de d2 e d3 ainda por decidir. salvaI é a posição
		// do Prof. da unidade atual (iFinal) nessa lista; n2_ conta uma unidade a mais, de verificação das turmas.
		k = 0;
		salvaI = -1;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (p == Ap[iFinal])
				salvaI = k + 1;
			if (Module_2A.complex_p2[p]) {
				if (p == Ap[iFinal])
					salvaI = k;
				continue;
			}
			k++;
			Ap2[k] = p;
		}
		k++;
		n2_ = k;

		// Etapa 2 - validação de três dias: para cada possibilidade da 2ª parte (bitSet_2), wp0[p] = quantidade (0, 1 ou 2) de
		// dias de trabalho de p em d2 e d3. Para Profs. complex_p2, x_case__3 é fixo (dia de trabalho conhecido desde o Módulo 2A).
		cont = Module_2A.nPossib_2;
		if (cont == 0) {
			System.out.println("erro2_");
			System.exit(0);
		}
		primeiraPossibilidade = true;
		teste3 = true;
		for (possib = 1; possib <= cont; possib++) {
			for (p = 1; p <= Qp; p++) {
				teste = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1));
				teste2 = Module_2A.bitSet_2.get(possib * Qp * 2 + 2 * (p - 1) + 1);
				wp0[p] = 2 * (teste ? 1 : 0) + (teste2 ? 1 : 0);
				if (Module_2A.complex_p2[p]) {
					teste = Module_2A.bitSet_1.get(3 * (p - 1));
					x_case__3[p] = (teste ? 1 : 0);
				}
			}
			// Busca com recuo sobre os Profs. de Ap2: x2[i] = 1 se d2 é dia de trabalho do Prof. Ap2[i], 0 caso contrário
			// (sub-possibilidades da possibilidade em teste). Se todas as unidades fecham, a validação tem sucesso.
			minimo = -1;
			for (i = 1; i <= n2_; i++)
				x2[i] = minimo;
			i = 1;
			sucesso = false;
			do {
				if (i < n2_) {
					p = Ap2[i];
					x0 = x2[i];
					i0 = wp0[p];
					// Valores inválidos: mais que 1, mais que wp0[p], ou 1 quando p não está disponível em d2.
					if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
						maximo_ = true;
					else {
						// Até o Prof. da unidade (salvaI), usa-se o mínimo de dias de trabalho Dp para limitar x0 inferiormente.
						if (i <= salvaI) {
							if (Mp[p][d_2] == 0 || i0 == maxDias1[p])
								aux2 = 0;
							else
								aux2 = 1;
							x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
						} else
							x0++;
						// Se o Prof. trabalha em dois dias de d2 e d3, ou só pode trabalhar em d2, o valor é forçado a 1.
						if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
							x0 = 1;
						maximo_ = (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0));
						// Poda: as somas acumuladas dos mínimos de aulas em d2 e d3 não podem ultrapassar Ma*Qa.
						if (!maximo_ && i <= salvaI) {
							maximo_ = (sumP0[i - 1] + (x0 != 0 ? Ep[p] : 0) <= Ma * Qa)
									&& (sumP1[i - 1] + (i0 - x0 != 0 ? Ep[p] : 0) <= Ma * Qa);
							maximo_ = !maximo_;
						}
						// No Prof. da unidade, executa a validação de três dias com a sub-possibilidade parcial definida até aqui.
						if ((i == salvaI || (i == 1 && salvaI == 0)) && !maximo_) {
							x_case__3[Ap2[i]] = x0;
							maximo_ = !validacao_tres_dias(iFinal);
							if (!teste3)
								return true;
						}
					}
				// Unidade final desta busca: verifica as demandas das turmas nos três dias (case__350).
				} else
					maximo_ = case__350(i);
				// Valor aceito: atualiza x_case__3 e as somas acumuladas dos mínimos de aulas e avança.
				if (!maximo_) {
					x2[i] = x0;
					if (i < n2_) {
						p = Ap2[i];
						x_case__3[p] = x0;
						if (i <= salvaI) {
							sumP0[i] = sumP0[i - 1] + (x0 != 0 ? Ep[p] : 0);
							sumP1[i] = sumP1[i - 1] + (wp0[p] - x0 != 0 ? Ep[p] : 0);
						}
					}
					i++;
					if (i > n2_) {
						sucesso = true;
						break;
					}
				// Sem sucessor: recua (a busca termina sem sucesso se recuar além da primeira unidade).
				} else {
					if (i == 1)
						break;
					x2[i] = minimo;
					i--;
				}
			} while (true);
			if (sucesso)
				break;
		}
		// Valor da variável de decisão desta unidade (sem significado). Retorna true (falha) se nenhuma possibilidade foi aprovada.
		x0 = 0;
		return (!sucesso);
	}

	// Verifica se as demandas das turmas em d2, d3 e d4 (Ma aulas por turma e por dia) podem ser atendidas pela sub-possibilidade
	// em teste (x_case__3, wp0, maxDias1). Retorna true se NÃO podem (falha), false se podem.
	public boolean case__350(int i) {
		int aux2, aux3, aux4, aux4_, aux1_, x0, d_0, d_1, d_2, sumA0, sumA1, sumA1_;
		boolean teste;
		d_0 = 2;
		d_1 = 3;
		d_2 = 4;
		sumA0 = 0;
		sumA1 = 0;
		sumA1_ = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
			sumA_1_[a] = 0;
		}
		teste = false;
		// Para cada Prof. com dias de trabalho na 2ª parte: aulas que ele pode ministrar em cada turma em d2, d3 e d4 (limitadas por
		// 1 ou 2 aulas por dia, pela capacidade Mp do dia e por ch2[p][j]); acumula a capacidade total e a de cada turma.
		for (p = 1; p <= Qp; p++) {
			if (maxDias1[p] == 0)
				continue;
			x0 = x_case__3[p];
			aux0 = (semGeminadasP[p] ? x0 : 2 * x0);
			aux1 = wp0[p] - x0;
			aux1_ = maxDias1[p] - wp0[p];
			if (!semGeminadasP[p]) {
				aux1 *= 2;
				aux1_ *= 2;
			}
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux4_ = (maxDias1[p] - wp0[p] == 0 ? 0 : Mp[p][d_2]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux1_ = Math.min(aux1_, aux4_);
			aux2 = nTurmasP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			sumA1_ += aux4_;
			for (j = 1; j <= aux2; j++) {
				a = lista[p][j];
				sumA_0[a] += Math.min(aux0, ch2[p][j]);
				sumA_1[a] += Math.min(aux1, ch2[p][j]);
				sumA_1_[a] += Math.min(aux1_, ch2[p][j]);
			}
		}
		// Verificação de consistência: a capacidade de d4 deveria sempre atender Ma*Qa (garantido pelo Módulo 2A).
		if (sumA1_ < Ma * Qa) {
			System.out.println("ERRO 86");
			System.exit(0);
		}
		// As capacidades totais de d2 e d3 devem atingir Ma*Qa; depois cada turma deve ter pelo menos Ma aulas possíveis em cada dia.
		teste = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
		if (!teste)
			return true;
		for (a = 1; a <= Qa; a++) {
			teste = sumA_0[a] >= Ma && sumA_1[a] >= Ma && sumA_1_[a] >= Ma;
			if (!teste)
				break;
		}
		x0 = 0;
		return !teste;
	}

	// Verifica se as demandas das turmas em d0 e d1 podem ser atendidas pela possibilidade da 1ª parte em teste (x_case__3,
	// maxDias0). Análogo a case__350, mas retorna true quando podem ser atendidas.
	public boolean case__350_() {
		int d_0, d_1, sumA0, sumA1, aux3;
		d_0 = 0;
		d_1 = 1;
		sumA0 = 0;
		sumA1 = 0;
		for (a = 1; a <= Qa; a++) {
			sumA_0[a] = 0;
			sumA_1[a] = 0;
		}
		for (p = 1; p <= Qp; p++) {
			if (maxDias0[p] == 0)
				continue;
			x0 = x_case__3[p];
			aux0 = (semGeminadasP[p] ? x0 : 2 * x0);
			aux1 = maxDias0[p] - x0;
			if (!semGeminadasP[p])
				aux1 *= 2;
			aux3 = (x0 == 0 ? 0 : Mp[p][d_0]);
			aux4 = (maxDias0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			aux0 = Math.min(aux0, aux3);
			aux1 = Math.min(aux1, aux4);
			aux2 = nTurmasP[p];
			sumA0 += aux3;
			sumA1 += aux4;
			for (j = 1; j <= aux2; j++) {
				a = lista[p][j];
				sumA_0[a] += Math.min(aux0, ch2[p][j]);
				sumA_1[a] += Math.min(aux1, ch2[p][j]);
			}
		}
		teste = sumA0 >= Ma * Qa && sumA1 >= Ma * Qa;
		if (teste) {
			for (a = 1; a <= Qa; a++) {
				teste = sumA_0[a] >= Ma && sumA_1[a] >= Ma;
				if (!teste)
					break;
			}
		}
		return teste;
	}

	// Sucessor do Caso 2 (Seção 5.3.2, mesmo cálculo do Caso 2 do Módulo 2A): x0 = número de aulas do par (Ap[i], Aa[i]) na 1ª
	// parte da semana; A[i] - x0 na 2ª parte. Na primeira visita calcula os limites minX0 e maxX0[i]; nas seguintes incrementa x0.
	public boolean maximo_2() {
		int maxX0_, minX0;
		boolean initialized;
		p = Ap[i];
		x0 = x_[i];
		// Não há sucessor se x0 já está no máximo possível de aulas do par na 1ª parte.
		if (x0 == 2 * maxDias0[p] || (semGeminadasP[p] && x0 == maxDias0[p]))
			return true;
		// Caso trivial: Np0[p] ou Np1[p] é zero e as aulas ficam todas em uma só parte.
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
		// Sp0, Sp2, Sa0 e Sa2: aulas já destinadas ao Prof. e à turma, em cada parte, pelas unidades anteriores.
		Sp0 = Sp0_[apontP2[i] - 1];
		Sp2 = Sp1_[apontP2[i] - 1];
		Sa0 = Sa_0[apontA[i] - 1];
		Sa2 = Sa_1[apontA[i] - 1];

		// Limite inferior: o que não cabe na 2ª parte (por dias de trabalho, por Np1[p] e por 3*Ma da turma).
		minX0 = (semGeminadasP[p] ? n0 - maxDias1[p] : n0 - 2 * maxDias1[p]);
		minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
		minX0 = Math.max(minX0, Sa2 + n0 - 3 * Ma);
		x0 = Math.max(x0, minX0);

		// Limite superior: o que cabe na 1ª parte (por dias de trabalho, por n0, pelo resto de Np0[p] e pelo resto de 2*Ma da turma).
		maxX0_ = (semGeminadasP[p] ? maxDias0[p] : 2 * maxDias0[p]);
		maxX0_ = Math.min(maxX0_, n0);
		maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
		maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
		maxX0[i] = maxX0_;
		return x0 > maxX0_;
	}

	// Atualiza as variáveis de controle (aulas acumuladas do Prof. e da turma nas duas partes) depois de aceito x0.
	public void atualiza_2() {
		p = Ap[i];
		nClasses = A[i] - x0;
		aux = apontP2[i];
		Sp0_[aux] = Sp0_[aux - 1] + x0;
		Sp1_[aux] = Sp1_[aux - 1] + nClasses;
		aux = apontA[i];
		Sa_0[aux] = Sa_0[aux - 1] + x0;
		Sa_1[aux] = Sa_1[aux - 1] + nClasses;
	}

	// Alternativa à unidade do Caso 5 (case_50) sem o Módulo 3B: executa apenas o Módulo 3A, primeiro para os dias (d2, d3, d4) e
	// depois para (d0, d1, d2). Em caso de falha, i1_ recebe o índice da unidade do Caso 2 a que a busca deve recuar. Retorna
	// false se obtém a distribuição completa e true se falha.
	static private boolean case_5(boolean minimo) {

		// Primeira execução do Módulo 3A (dias d2, d3 e d4), com as aulas da 2ª parte em ch2.
		g2_.preliminares(ch2, maxDias1);
		teste = g2_.principal2();
		if (teste) {
			System.out.println("Fechou para 3 dias");
			g2_.atualiza4();
		} else {
			// Falha: obtém o Prof. e a turma em que o Módulo 3A parou e localiza a última unidade do Caso 2 correspondente.
			p = g2_.get_P();
			a = g2_.get_A();
			i1_ = n2;
			if (a != 0) {
				while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2)
					i1_--;
			} else {
				while (Ap[i1_] != p || case_[i1_] != 2)
					i1_--;
			}
			System.out.println("i0_=" + i1_ + " ");
			return true;
		}

		// Segunda execução do Módulo 3A (dias d0, d1 e d2).
		g2_.preliminares0();
		teste = g2_.principal2();
		if (teste) {
			g2_.atualiza4();
			x0 = 0;
			return false;
		}
		p = g2_.get_P();
		a = g2_.get_A();
		i1_ = n2;
		if (a != 0) {
			while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2)
				i1_--;
		} else {
			while (Ap[i1_] != p || case_[i1_] != 2)
				i1_--;
		}
		System.out.println("i1_=" + i1_ + " ");
		return true;
	}

	// Unidade do Caso 5: finaliza a distribuição pelos Módulos 3B e 3A (Seção 5.3.2). O Módulo 3B é tentado para cada
	// possibilidade da 2ª parte (bitSet_2), até que uma dê sucesso; depois, o Módulo 3A distribui as aulas entre d0, d1 e d2.
	// Retorna false se ambos têm sucesso; senão retorna true e i1_ indica a unidade do Caso 2 a que o algoritmo deve recuar.
	static private boolean case_50(boolean minimo) {
		int min_, max_, media, contSucessos;
		cont = Module_2A.nPossib_2;
		if (cont == 0) {
			System.out.print("erro24");
			System.exit(0);
		}
		min_ = 10000;
		max_ = -1;
		media = 0;
		contSucessos = 0;
		// preliminares1 verifica se a possibilidade é viável (tem sub-possibilidade viável); preliminares2 prepara a execução do Módulo 3B.
		for (possib = 1; possib <= cont; possib++) {
			teste = g2_0.preliminares1(ch2, possib);
			if (!teste)
				continue;
			g2_0.preliminares2(ch2, possib);
			contSucessos++;
			// Executa o Módulo 3B para esta possibilidade.
			sucesso = g2_0.principal_V3();
			if (sucesso) {
				System.out.println();
				System.out.println("Fechou para 3 dias.");
				g2_0.atualiza4();
				break;
			}
			// Falha do Módulo 3B: registra a unidade do Caso 2 (do Prof. e turma em que ele parou) para decidir o recuo.
			p = g2_0.getP();
			a = g2_0.getA();
			if (a != 0) {
				i1_ = n2 + 1;
				do
					i1_--;
				while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2);
				min_ = Math.min(min_, i1_);
				max_ = Math.max(max_, i1_);
				media += i1_;
			} else {
				System.out.println("Erro 023. Foi até o case_: " + Module_3B.case_[Module_3B.imax]);
				System.exit(0);
			}
		}
		if (contSucessos == 0) {
			System.out.println("contSucessos == 0");
			System.exit(0);
		}
		// O Módulo 3B falhou para todas as possibilidades. O recuo é para a unidade de menor índice registrada (se foram até 10
		// possibilidades testadas), para a unidade de índice médio (se foram mais) ou, se o usuário optou por não retornar
		// pelo mínimo, para a de maior índice.
		if (!sucesso) {
			if (contSucessos <= 10)
				i1_ = min_;
			else
				i1_ = Math.floorDiv(media, contSucessos);
			if (!minimo)
				i1_ = max_;
			System.out.print("i0_=" + i1_ + " ");
			return true;
		}
		// O Módulo 3B teve sucesso: executa o Módulo 3A para os dias (d0, d1, d2).
		g2_.preliminares0();
		teste = g2_.principal2();
		if (teste) {
			g2_.atualiza4();
			x0 = 0;
			return false;
		}
		p = g2_.get_P();
		a = g2_.get_A();
		i1_ = n2;
		if (a != 0) {
			while (Ap[i1_] != p || Aa[i1_] != a || case_[i1_] != 2)
				i1_--;
		} else {
			while (Ap[i1_] != p || case_[i1_] != 2)
				i1_--;
		}
		System.out.println("i1_=" + i1_ + " ");
		return true;
	}

	// Algoritmo 2 do Capítulo 3 simplificado, para o Módulo 2B. minimo_1 é a escolha do usuário sobre o recuo da unidade do Caso 5.
	// Retorna true se a distribuição foi obtida e false se o Módulo 2B não encontra solução.
	public boolean principal_V1(boolean minimo_1) {
		int k, p0, minimo, imax;
		boolean teste;
		long tempoInicial;
		Object[] opcoes = { "Sim", "Não" };
		imax = 0;
		// O 'gerador' do livro (minimo = -1).
		minimo = -1;
		for (i = 1; i <= n2 + 1; i++)
			x_[i] = minimo;
		i = 1;
		tempoInicial = System.currentTimeMillis();
		do {
			nIteracoes++;
			// Controle do tempo máximo de execução.
			if (nIteracoes >= 50000L) {
				nIteracoes = 0L;
				if ((System.currentTimeMillis() - tempoInicial) / 60000.0 > tempoMaximo) {
					JOptionPane.showMessageDialog(null, "Execução excedeu o tempo máximo", "Resultado",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			// Seleção do sucessor de acordo com o caso da unidade: 5, 2 ou 10.
			if (case_[i] == 5)
				teste = case_50(minimo_1);
			else {
				if (case_[i] == 2)
					teste = maximo_2();
				else
					teste = maximo_10(i);
			}
			// Sucesso: guarda x0, atualiza as variáveis de controle (Caso 2) e avança. Se passou da última unidade, a distribuição está completa.
			if (!teste) {
				x_[i] = x0;
				if (case_[i] == 2)
					atualiza_2();
				i++;
				if (i > imax) {
					imax = i;
					System.out.println(i + "/" + (n2 + 1) + " case_ = " + case_[i] + " p=" + Ap[i]);
				}
				if (i > n2 + 1) {
					System.out
							.println("Tempo decorrido (min): " + (System.currentTimeMillis() - tempoInicial) / 60000.0);
					return true;
				}
			// Falha: se for a primeira unidade, retorna false (não há recuo a partir da primeira unidade do Caso 2). Se a falha foi
			// na unidade do Caso 5, volta para a unidade indicada em i1_; senão, recua para a unidade anterior, saltando as unidades do Caso 10.
			} else {
				if (i == 1)
					return false;
				if (case_[i] == 5)
					p0 = i1_;
				else
					p0 = i - 1;
				if (case_[p0] == 10)
					p0--;
				if (p0 == 0)
					return false;
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}
}
