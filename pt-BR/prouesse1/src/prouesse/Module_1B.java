package prouesse;

import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

// Classe Module_1B - implementa o Módulo 1 (Fase 1.1; Seções 4.1.1 e 4.7.1 do livro). É uma versão aperfeiçoada de Module_1.
//
// Finalidade: decidir quais aulas (de quais Profs. e turmas) ocorrerão no primeiro dia e quantas (0, 1 ou 2 por Prof. e turma).
// Os resultados vão para dd[p][j][0]. Como os dias são reordenados antes (Seção 4.6.1), o 'dia 0' é o dia mais difícil
// (menor soma dos máximos de aulas Mp), e não necessariamente a segunda-feira; Prouesse5D_X restaura a ordem original depois.
//
// Método: busca do Capítulo 3 (Algoritmo 1: recuo simples, gerador = minimo = -1), com as unidades abaixo. Diferentemente de Module_1,
// que decide dia a dia o comparecimento de cada Prof. (casos 10 e 11 do livro), aqui basta decidir, por Prof.:
//   caso 510: ch0 = total de aulas do Prof. no dia 0 (só existe para Profs. com complex_p01 == true);
//   caso 501: se o Prof. trabalha (1) ou não (0) no dia 0;
//   caso 511: unidade única, executada após todas as 501/510; valida a oferta total e calcula nHorarios_P0/P1;
//   caso 1  : validação do Prof. (uma por Prof.), antes das suas unidades do caso 2;
//   caso 2  : número de aulas da turma Aa[i] do Prof. no dia 0 (restante nos outros 4 dias);
//   caso 5  : unidade após as do caso 2 de cada Prof.; apenas propaga as variáveis de controle para o próximo Prof.
// Os Profs. são tratados na ordem crescente do número de possibilidades de distribuição (Seção 4.6.2), calculado por Module_A_.
// Quando todas as unidades são fechadas, o Módulo 2 (Module_2B) é chamado para completar a distribuição.
public class Module_1B extends FaseA1_Super {
	// complex_p01[p] = true se os máximos de aulas Mp[p][d] positivos dos dias 1 a 4 não são todos iguais (análogo a complex_0p);
	// arranjo2/maximo1[p][] = Mp[p][1..4] ordenados em ordem crescente (maximo1[p][3] é o maior);
	// ch0[p], ch1[p] = total de aulas do Prof. p no dia 0 e nos dias 1 a 4 (definidos pelo caso 510);
	// Sa_2[p][a], Sa_3[p][a] = aulas já alocadas a turma a, no dia 0 e nos dias 1 a 4, pelos Profs. anteriores a p na ordem (variáveis de
	//   controle);
	// Sp2_, Sp3_, apontP = totais acumulados de aulas do Prof. nos dois grupos de dias e ponteiros das unidades neles;
	// sumA_0, sumA_1 = somas por turma (usadas pelo caso 511);  g0 = objeto de Module_A.
	private boolean complex_p01[];
	private int arranjo2[], maximo1[][], ch0[], ch1[], apontP[], Sa_2[][], Sa_3[][], Sp2_[], Sp3_[], sumA_0[], sumA_1[];
	private Module_A g0;

	// Construtor: chama o construtor de FaseA1_Super (que prepara os módulos seguintes) e aloca os arranjos próprios.
	public Module_1B() {
		super();
		// Arranjos de controle por Prof. e por turma (Sa_2, Sa_3), somas por turma e dados por Prof. (complex_p01, maximo1, ch0, ch1).
		g0 = new Module_A();
		Sa_2 = new int[Qp + 1][Qa + 1];
		Sa_3 = new int[Qp + 1][Qa + 1];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		complex_p01 = new boolean[Qp + 1];
		arranjo2 = new int[4];
		maximo1 = new int[Qp + 1][4];
		ch0 = new int[Qp + 1];
		ch1 = new int[Qp + 1];
		aux = 0;
		for (p = 1; p <= Qp; p++)
			aux += nTurmasP[p];
		// Tamanho dos arranjos Sp2_, Sp3_ e apontP: número de unidades do Módulo 1 (no máximo 4 por Prof. + uma por turma + folga).
		aux = 4 * Qp + aux + 7;
		Sp2_ = new int[aux];
		Sp3_ = new int[aux];
		apontP = new int[aux];
	}

	// Ordenação por seleção, em ordem crescente, das posições first a nElementos-1 do arranjo amplitude[], permutando junto d2_[].
	// Ao final, d2_[i] é o elemento (dia ou Prof.) que ocupa a i-ésima posição da ordenação. Também é usado pelos Módulos 2 e 4.
	static public void ordena2(int first, int nElementos) {
		int i, j, t0;
		float t;
		for (i = first; i < nElementos - 1; i++) {
			j = indMenor2(i, nElementos);
			t = amplitude[i];
			amplitude[i] = amplitude[j];
			amplitude[j] = t;
			t0 = d2_[i];
			d2_[i] = d2_[j];
			d2_[j] = t0;
		}
	}

	// Devolve o índice do menor elemento de amplitude[] entre as posições i e nElementos-1.
	static private int indMenor2(int i, int nElementos) {
		int m = i, j;
		float menor;
		menor = amplitude[m];
		for (j = i + 1; j <= nElementos - 1; j++)
			if (amplitude[j] < menor) {
				m = j;
				menor = amplitude[m];
			}
		return m;
	}

	// Prepara a execução do Módulo 1: ordena os dias (Seção 4.6.1), ordena os Profs. (Seção 4.6.2), define o tipo (case_) e os dados
	// (Ap, Aa, j_, A) de cada unidade e os ponteiros das variáveis de controle. Retorna n2, o número de unidades.
	public int preliminares() {
		int aux0, p1, d1, d;
		// Aviso ao usuário: a preparação (contagem de distribuições) e a busca podem demorar.
		JOptionPane.showMessageDialog(null, "Esta ação poderá levar alguns minutos", "Resultado",
				JOptionPane.INFORMATION_MESSAGE);
		// 1) Para cada dia k, amplitude[k] = soma, sobre os Profs., dos máximos de aulas Mp[p][k]; d2_[k] = k.
		for (k = 0; k <= 4; k++) {
			aux0 = 0;
			for (p1 = 1; p1 <= Qp; p1++)
				aux0 += Mp[p1][k];
			d2_[k] = k;
			amplitude[k] = aux0;
			System.out.println("Soma dos MaxP[*][] para " + (k + 2) + "a-feira: " + aux0);
		}
		// 2) Ordena os dias em ordem crescente da soma dos máximos: os dias com menor oferta (mais 'difíceis') ficam nas primeiras posições.
		ordena2(0, 5);
		for (d1 = 0; d1 <= 4; d1++) {
			k = 0;
			while (d2_[k] != d1)
				k++;
			// destino0[d1] = nova posição do dia original d1 na ordenação.
			destino0[d1] = k;
		}
		for (p1 = 1; p1 <= Qp; p1++) {
			for (d1 = 0; d1 <= 4; d1++)
				// 3) Permuta as colunas de Mp de cada Prof., de modo que Mp[p][0] passe a ser o máximo do dia mais difícil, e assim por diante.
				aux_[d1] = Mp[p1][d1];
			for (d1 = 0; d1 <= 4; d1++)
				Mp[p1][destino0[d1]] = aux_[d1];
		}

		for (p = 1; p <= Qp; p++) {
			// 4) Para cada Prof. p: ns_p_[p] = carga horária semanal total.
			ns_p_[p] = 0;
			for (j = 1; j <= nTurmasP[p]; j++)
				ns_p_[p] += ns[p][j];
			// complex_p01[p] = true se, entre os dias 1 a 4, há dois máximos de aulas positivos e diferentes (os dias de trabalho não são
			//   intercambiáveis).
			complex_p01[p] = false;
			aux = -1;
			for (d1 = 1; d1 <= 4; d1++) {
				if (Mp[p][d1] > 0 && Mp[p][d1] != aux && aux != -1) {
					complex_p01[p] = true;
					break;
				}
				if (Mp[p][d1] > 0 && aux == -1)
					aux = Mp[p][d1];
			}
			for (d = 1; d <= 4; d++)
				// maximo1[p][] = máximos dos dias 1 a 4 em ordem crescente.
				arranjo2[d - 1] = Mp[p][d];
			Arrays.sort(arranjo2);
			for (k = 0; k <= 3; k++)
				maximo1[p][k] = arranjo2[k];
		}
		for (p = 1; p <= Qp; p++) {
			// 5) Ordenação dos Profs. (Seção 4.6.2): para cada Prof., Module_A_ conta quantas possibilidades de distribuição ele tem
			// entre o dia 0 e os demais dias. Profs. com complex_p01 recebem -10000 e vão para o início; os demais ficam em ordem crescente do
			//   número de possibilidades.
			Module_A_.preliminares(p);
			aux = Module_A_.principal_0();
			System.out.println("Nro de distribuições para p = " + p + ": " + aux);
			if (complex_p01[p])
				aux -= 10000;
			amplitude[p] = (float) aux;
			d2_[p] = p;
		}
		// d2_[1..Qp] passa a conter os Profs. na ordem em que serão tratados.
		ordena2(1, Qp + 1);
		System.out.println();
		// 6) Monta as unidades, Prof. por Prof. (na ordem d2_): se complex_p01, uma unidade do caso 510 (aulas do Prof. no dia 0)...
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (complex_p01[p]) {
				k++;
				Ap[k] = p;
				A[k] = ns_p_[p];
				case_[k] = 510;
			}
			k++;
			Ap[k] = p;
			A[k] = ns_p_[p];
			// ... e uma unidade do caso 501 (o Prof. trabalha ou não no dia 0).
			case_[k] = 501;
		}
		k++;
		// Uma única unidade do caso 511, depois das unidades 510/501 de todos os Profs.: validação conjunta da oferta de aulas.
		case_[k] = 511;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			k++;
			Ap[k] = p;
			// Para cada Prof., na ordem: uma unidade do caso 1 (validação), ...
			case_[k] = 1;
			for (j = 1; j <= nTurmasP[p]; j++) {
				k++;
				Ap[k] = p;
				Aa[k] = lista[p][j];
				j_[k] = j;
				A[k] = ns[p][j];
				// ... uma unidade do caso 2 por turma do Prof. (A[k] = carga horária semanal na turma) ...
				case_[k] = 2;
			}
			k++;
			Ap[k] = p;
			// ... e uma unidade do caso 5 (atualiza o estado para o próximo Prof.).
			case_[k] = 5;
		}
		// n2 = número de unidades. Ap[n2+1] = 0 funciona como sentinela.
		n2 = k;
		Ap[n2 + 1] = 0;
		// 7) Ponteiros apontP das unidades do caso 2 para os arranjos Sp2_/Sp3_ (totais acumulados do Prof. no dia 0 e nos dias 1 a 4).
		// Entre um Prof. e o seguinte é reservada uma posição zerada, de modo que a posição anterior a da primeira turma de cada Prof. valha
		//   0.
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				apontP[j] = aux2;
				aux2++;
				if (j + 2 < n2 && case_[j + 1] != 2) {
					Sp2_[aux2] = 0;
					Sp3_[aux2] = 0;
					aux2++;
				}
			}
		}
		return n2;
	}

	// Equivale ao método 'sucessor' do livro: tenta obter o menor x0 > x_[i] que cumpre as restrições do caso case_[i].
	// Retorna false se foi bem-sucedido (o valor está em x0) e true caso contrário (sem sucessor).
	// Nos casos de validação (511, 1 e 5) a unidade só admite uma execução (x_ = -1); uma nova chamada, após um recuo, retorna true.
	public boolean maximo() {
		int aux0, aux2, aux10, aux11, minX0, maxX0, soma_, d, p2, sumA0, sumA1;
		boolean case_Trivial, teste;

		// Prof. da unidade corrente e valor atual de x[i] (x_[i] = -1 antes da primeira tentativa).
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 510: x0 = total de aulas do Prof. no dia 0.
		// Mínimo: o que não cabe nos dias 1 a 4 (carga horária menos a soma de Mp nesses dias), pelo menos x0+1;
		// máximo: o menor entre a carga horária e o máximo de aulas do dia 0.
		case 510: {
			minX0 = ns_p_[p];
			for (d = 1; d <= 4; d++)
				minX0 -= Mp[p][d];
			if (minX0 < 0)
				minX0 = 0;
			minX0 = Math.max(x0 + 1, minX0);
			maxX0 = Math.min(ns_p_[p], Mp[p][0]);
			if (maxX0 < minX0)
				return true;
			x0 = minX0;
			return false;
		}
		// Caso 501: x0 = 1 se o Prof. trabalha no dia 0 e 0 caso contrário. Candidatos testados em ordem crescente (0, 1).
		// aux2 = número de dias, entre 1 e 4, em que o Prof. tem disponibilidade;  aux = máximo de dias de trabalho restantes para os dias 1 a
		//   4.
		case 501: {
			aux2 = 0;
			for (d = 1; d <= 4; d++)
				aux2 += (Mp[p][d] > 0 ? 1 : 0);
			while (true) {
				x0++;
				aux = Wp0[p] - x0;
				// Descarta x0 > 1, x0 > Wp0[p] (máximo de dias de trabalho) e trabalhar no dia 0 quando Mp[p][0] == 0 (indisponível).
				if (x0 > 1 || x0 > Wp0[p])
					return true;
				if (x0 > (Mp[p][0] > 0 ? 1 : 0))
					return true;
				if (aux > aux2)
					aux = aux2;
				// Teste de suficiência da escolha: se complex_p01, a capacidade do dia 0 (x0*Mp[p][0]) deve cobrir ch0 e a soma dos 'aux' maiores
				// máximos dos dias 1 a 4 deve cobrir ch1; caso contrário (todos os máximos positivos iguais), a capacidade total deve cobrir a carga
				//   horária.
				if (complex_p01[p]) {
					soma_ = x0 * Mp[p][0];
					teste = soma_ >= ch0[p];
					if (teste) {
						soma_ = 0;
						for (k = 0; k < aux; k++)
							soma_ += maximo1[p][3 - k];
						teste = soma_ >= ch1[p];
					}
				} else
					teste = x0 * Mp[p][0] + aux * maximo1[p][3] >= ns_p_[p];
				if (teste)
					return false;
			}
		}
		// Caso 511 (validação conjunta, executada uma única vez): com os dias de trabalho (maxDias0, maxDias1) e as aulas
		// no dia 0 (ch0, ch1) já decididos para todos os Profs., calcula o máximo de aulas de cada Prof. no dia 0 (nHorarios_P0)
		// e nos dias 1 a 4 (nHorarios_P1) e verifica a oferta de aulas.
		case 511: {
			if (x0 > -1)
				return true;
			for (p = 1; p <= Qp; p++) {
				// nHorarios_P0 = máximo de aulas no dia 0; nHorarios_P1 = ch1 (se complex_p01) ou o maior máximo diário vezes o número de dias de
				//   trabalho nos dias 1 a 4.
				nHorarios_P0[p] = Mp[p][0] * maxDias0[p];
				if (complex_p01[p])
					nHorarios_P1[p] = ch1[p];
				else
					nHorarios_P1[p] = maximo1[p][3] * maxDias1[p];
			}
			// Oferta total: sumA0/sumA1 somam nHorarios_P0/P1 dos Profs. e sumA_0[a]/sumA_1[a] somam, por turma, o que os Profs. podem oferecer a
			//   ela (limitado por 1 ou 2 aulas por dia e por ns).
			sumA0 = 0;
			sumA1 = 0;
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				aux0 = (semGeminadasP[p] ? maxDias0[p] : 2 * maxDias0[p]);
				aux1 = (semGeminadasP[p] ? maxDias1[p] : 2 * maxDias1[p]);
				aux0 = Math.min(aux0, nHorarios_P0[p]);
				aux1 = Math.min(aux1, nHorarios_P1[p]);
				aux2 = nTurmasP[p];
				sumA0 += nHorarios_P0[p];
				sumA1 += nHorarios_P1[p];
				for (j = 1; j <= aux2; j++) {
					a = lista[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
					sumA_1[a] += Math.min(aux1, ns[p][j]);
				}
			}
			// A oferta deve cobrir a demanda: Ma*Qa aulas no dia 0 e 4*Ma*Qa nos demais dias, no total e, também, para cada turma (Ma e 4*Ma).
			teste = sumA0 >= Ma * Qa && sumA1 >= 4 * Ma * Qa;
			if (teste) {
				for (a = 1; a <= Qa; a++) {
					teste = sumA_0[a] >= Ma && sumA_1[a] >= 4 * Ma;
					if (!teste)
						break;
				}
			}
			x0 = 0;
			// Falha (true) se a oferta for insuficiente.
			return !teste;
		}

		// Caso 1 (validação do Prof., antes das suas unidades do caso 2): estima o máximo de aulas que o Prof. consegue dar no dia 0 (aux10)
		// e nos dias 1 a 4 (aux11), considerando o espaço ainda livre em cada uma das suas turmas (Sa_2, Sa_3); falha se, somados,
		// esses máximos (limitados por nHorarios_P0 e nHorarios_P1) não cobrem a carga horária.
		case 1: {
			if (x0 > -1)
				return true;
			x0 = 0;
			aux10 = 0;
			aux11 = 0;
			aux0 = nTurmasP[p];
			for (j = 1; j <= aux0; j++) {
				aux10 += Math.min(Math.min(Ma - Sa_2[p][lista[p][j]], maxDias0[p] * (semGeminadasP[p] ? 1 : 2)),
						ns[p][j]);
				aux11 += Math.min(Math.min(4 * Ma - Sa_3[p][lista[p][j]], maxDias1[p] * (semGeminadasP[p] ? 1 : 2)),
						ns[p][j]);
			}
			return Math.min(aux10, nHorarios_P0[p]) + Math.min(aux11, nHorarios_P1[p]) < ns_p_[p];
		}
		// Caso 2: x0 = número de aulas da turma Aa[i] do Prof. no dia 0; o restante (A[i] - x0) fica nos dias 1 a 4.
		// Sp0/Sp2 = totais já atribuídos ao Prof. (nas demais turmas) no dia 0 e nos dias 1 a 4; Sa0/Sa2 = totais já alocados a turma.
		case 2: {
			// Se x0 já atingiu o máximo de aulas do Prof. no dia 0, não há sucessor.
			if (nHorarios_P0[p] == x0)
				return true;
			// Caso trivial: se o Prof. não pode dar aula no dia 0 (ou nos outros dias), só há um valor possível (0 ou todas as aulas), tentado uma
			//   única vez.
			case_Trivial = nHorarios_P0[p] == 0 || nHorarios_P1[p] == 0;
			if (case_Trivial && x0 > -1)
				return true;
			a = Aa[i];
			n0 = A[i];
			Sp0 = Sp2_[apontP[i] - 1];
			Sp2 = Sp3_[apontP[i] - 1];
			Sa0 = Sa_2[p][a];
			Sa2 = Sa_3[p][a];
			if (case_Trivial)
				x0 = (nHorarios_P0[p] == 0 ? 0 : n0);
			else
				x0++;
			// Limite superior de x0: no máximo 1 (ou 2) aulas por dia de trabalho, A[i] aulas, o que resta de nHorarios_P0 e o que resta na turma
			//   (Ma - Sa0).
			maxX0 = (semGeminadasP[p] ? maxDias0[p] : 2 * maxDias0[p]);
			if (x0 > maxX0)
				return true;
			maxX0 = Math.min(maxX0, n0);
			maxX0 = Math.min(maxX0, nHorarios_P0[p] - Sp0);
			maxX0 = Math.min(maxX0, Ma - Sa0);

			// Limite inferior: o que não cabe nos dias 1 a 4, considerando dias de trabalho, o que resta de nHorarios_P1 e a capacidade da turma
			//   (4*Ma) nesses dias.
			minX0 = (semGeminadasP[p] ? n0 - maxDias1[p] : n0 - 2 * maxDias1[p]);
			minX0 = Math.max(minX0, Sp2 + n0 - nHorarios_P1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - 4 * Ma);
			x0 = Math.max(x0, minX0);
			return x0 > maxX0;
		}

		// Caso 5: valida na primeira passagem (x0 passa de -1 a 0); numa nova chamada (após recuo) retorna true, e o recuo prossegue.
		case 5: {
			if (x0 < 0) {
				x0 = 0;
				return false;
			}
			return true;
		}

		}
		return false;
	}

	// Atualiza as variáveis de controle e de ligação depois que a unidade i recebeu o valor x0 (chamado por principalV1 para todos os
	//   casos, exceto 511).
	public void atualiza2_() {
		int i0, p2, aux, d;
		p = Ap[i];
		switch (case_[i]) {
		case 510: {
			// Caso 510: registra as aulas do Prof. no dia 0 (ch0) e nos demais (ch1).
			ch0[p] = x0;
			ch1[p] = ns_p_[p] - x0;
			break;
		}
		case 501: {
			// Caso 501: maxDias0 = dias de trabalho no dia 0 (0 ou 1); maxDias1 = dias de trabalho restantes, limitado ao número de dias
			//   disponíveis entre 1 e 4.
			maxDias0[p] = x0;
			maxDias1[p] = Wp0[p] - x0;
			aux2 = 0;
			for (d = 1; d <= 4; d++)
				aux2 += (Mp[p][d] > 0 ? 1 : 0);
			if (maxDias1[p] > aux2)
				maxDias1[p] = aux2;
			break;
		}

		case 2: {
			p = Ap[i];
			a = Aa[i];
			nClasses = A[i] - x0;
			aux = apontP[i];
			// Caso 2: acumula as aulas do Prof. no dia 0 (Sp2_) e nos dias 1 a 4 (Sp3_), considerando as turmas já tratadas.
			Sp2_[aux] = Sp2_[aux - 1] + x0;
			Sp3_[aux] = Sp3_[aux - 1] + nClasses;
			break;
		}

		case 5: {
			// Caso 5: passa para o próximo Prof. (p2) as aulas já alocadas por todos os Profs. anteriores em cada turma:
			// copia Sa_2/Sa_3 de p para p2 e soma as aulas do próprio p (unidades do caso 2 imediatamente anteriores).
			if (i < n2) {
				p = Ap[i];
				p2 = Ap[i + 1];
				for (a = 1; a <= Qa; a++) {
					Sa_2[p2][a] = Sa_2[p][a];
					Sa_3[p2][a] = Sa_3[p][a];
				}
				i0 = i - 1;
				do {
					x0 = x_[i0];
					a = Aa[i0];
					Sa_2[p2][a] += x0;
					Sa_3[p2][a] += A[i0] - x0;
					i0--;
				} while (case_[i0] == 2);
			}
		}
		}
	}

	// Executa a busca do Módulo 1 (Algoritmo 1 do Capítulo 3, com recuo simples; 'minimo' é o gerador, -1).
	// n = número de unidades. Retorna true se o Módulo 1 e o Módulo 2 (chamado ao final) tiveram sucesso.
	public boolean principalV1(int minimo, int n) {
		int imax;
		String entrada;
		boolean teste;
		imax = 1;
		// Inicializa todas as variáveis de decisão com o gerador.
		for (i = 1; i <= n; i++)
			x_[i] = minimo;
		i = 1;
		// Solicita ao usuário o tempo máximo de execução (em minutos); a cada 50000 iterações o tempo decorrido é conferido e o programa é
		//   encerrado se o limite for excedido.
		entrada = (String) JOptionPane.showInputDialog(null, "Informe tempo máximo de execução (min)", "Entrada",
				JOptionPane.QUESTION_MESSAGE, icone, null, null);
		tempoMaximo = Math.round(Float.parseFloat(entrada));
		nIteracoes = 0L;
		tempoInicial = System.currentTimeMillis();
		do {
			nIteracoes++;
			if (nIteracoes >= 50000L) {
				nIteracoes = 0L;
				if ((System.currentTimeMillis() - tempoInicial) / 60000.0 > tempoMaximo) {
					JOptionPane.showMessageDialog(null, "Execução excedeu o tempo máximo", "Resultado",
							JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
			}
			// Obtido o sucessor: guarda x0 em x_[i], atualiza as variáveis de controle (exceto no caso 511) e avanca para a unidade seguinte.
			if (!maximo()) {
				x_[i] = x0;
				if (case_[i] != 511)
					atualiza2_();
				i++;
				if (i > n) {
					// Todas as unidades fechadas: grava a distribuição do dia 0 em dd e chama o Módulo 2 (preliminares + principal_V2), cujo resultado é o
					//   resultado da Fase 1.
					atualiza4(n);
					g2_I_W_3.preliminares();
					teste = g2_I_W_3.principal_V2();
					return teste;
				}
				// Registro de progresso (maior unidade atingida).
				if (i > imax) {
					imax = i;
					System.out.println(i + "/" + n + " case_ " + case_[i]);
				}
			} else {
				// Sem sucessor: se estava na primeira unidade, o problema não tem solução. Senão, recua uma unidade;
				// unidades dos casos 1, 5 e 511 não têm escolha própria, então o recuo continua mais uma unidade.
				if (i == 1)
					return false;
				x_[i] = minimo;
				i--;
				if (case_[i] == 1 || case_[i] == 5 || case_[i] == 511) {
					x_[i] = minimo;
					i--;
				}
			}
		} while (true);
	}

	// Grava em dd[p][j][0] o número de aulas do dia 0 determinado pelas unidades do caso 2 (zera antes todas as posições).
	public void atualiza4(int n) {
		int i, p1, j1;
		for (p1 = 1; p1 <= Qp; p1++) {
			for (j1 = 1; j1 <= nTurmasP[p1]; j1++)
				dd[p1][j1][0] = 0;
		}
		for (i = 1; i <= n; i++) {
			if (case_[i] == 2)
				dd[Ap[i]][j_[i]][0] = x_[i];
		}
	}
}
