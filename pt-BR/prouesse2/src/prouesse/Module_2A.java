package prouesse;

import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.util.Random;
import java.util.BitSet;

// Classe Module_2A - implementa o Módulo 2A (Fase 1.1 do Algoritmo P2; Seções 5.2.1, 5.2.2, 5.2.3 e 5.3.1 do livro).
//
// Papel: é a primeira etapa da obtenção da distribuição de aulas. A semana é dividida em duas partes: a primeira com os
// dias d0 e d1 (índices 0 e 1) e a segunda com os dias d2, d3 e d4 (índices 2, 3 e 4). Este módulo decide, para cada Prof. p:
//   - maxDias0[p] e maxDias1[p]: quantidades (limitantes superiores) de dias de trabalho na 1ª e na 2ª parte da semana;
//   - ch0[p] e ch1[p] (Caso 10): carga horária de p na 1ª e na 2ª parte, para os Profs. com complex_p0 ou complex_p1 true;
//   - x0_[p][*] e x1_[p][*]: os dias de trabalho de p, para os Profs. com complex_p0 (1ª parte) ou complex_p1 (2ª parte) true.
// Com isso calcula os limitantes Np0[p] e Np1[p] (máximo de aulas de p em cada parte da semana) e reparte
// as aulas de cada par (Prof., turma) entre as duas partes (unidades do Caso 2), o que permite validar as decisões.
// Depois de fechadas todas as unidades, enumera as 'possibilidades' (bitSet_0, 1ª parte) e 'sub-possibilidades'
// (bitSet_1, 2ª parte), deriva de bitSet_1 as 'possibilidades' da 2ª parte (bitSet_2) e chama o Módulo 2B.
//
// A busca usa o Algoritmo 2 do Capítulo 3 (método principalV2): o 'gerador' do livro é a constante 'minimo' (= -1) e o
// 'sucessor' é o método maximo(), que devolve false quando encontra o sucessor (valor em x0) e true quando não existe.
// Casos (case_[i]) das unidades, nesta ordem: 6 (controle de graus_liberdade), 10 e 1 (por Prof.), 11 (validação), 2.
public class Module_2A extends FaseA1_Super {
	// Indicadores 'complexos' por Prof. (Seção 5.2.1): complex_p0[p] = Mp[p][0] e Mp[p][1] positivos e distintos;
	// complex_p1[p] = entre Mp[p][2], Mp[p][3] e Mp[p][4] há dois valores positivos e distintos;
	// complex_p2[p] = o mesmo que complex_p0, mas para os dias d2 e d3 (usado depois pelo Módulo 3B).
	static public boolean complex_p0[], complex_p1[], complex_p2[];
	// bitSet_0: possibilidades da 1ª parte (2 bits por Prof.: dias d0 e d1; os 2*Qp primeiros bits guardam os dias fixos dos
	// Profs. complex_p0); bitSet_1: sub-possibilidades da 2ª parte (3 bits por Prof.; os 3*Qp primeiros bits guardam os dias
	// fixos dos Profs. complex_p1); bitSet_2: possibilidades da 2ª parte (2 bits por Prof. formando a quantidade 0, 1 ou 2 de
	// dias de trabalho em d2 e d3). Ver Seção 5.2.2.
	static public BitSet bitSet_0, bitSet_1, bitSet_2;
	// nPossib[0] e nPossib[1]: quantidades de possibilidades (bitSet_0) e de sub-possibilidades (bitSet_1) armazenadas;
	// nPossib_2: quantidade de possibilidades da 2ª parte (bitSet_2); w: variável auxiliar; maxDias0[p] e maxDias1[p]:
	// limitantes para o número de dias de trabalho de p na 1ª e na 2ª parte da semana.
	static public int nPossib[], nPossib_2, w, maxDias0[], maxDias1[];
	// sumA_0[a] e sumA_1[a]: soma, para a turma a, das aulas que os Profs. podem oferecer na 1ª e na 2ª parte da semana
	// (usadas nas validações das demandas das turmas).
	static public int dias[], sumA_0[], sumA_1[];
	// x0_[p][k] e x1_[p][k]: dias de trabalho (1 ou 0) fixados para p na 1ª e na 2ª parte (resultado de detPossib_ com inicio true);
	// maximo0[p] = {menor, maior} entre Mp[p][0] e Mp[p][1]; maximo1[p] = Mp[p][2..4] em ordem crescente;
	// ch0[p] e ch1[p]: carga horária de p na 1ª e na 2ª parte; xx[p][j]: dia de trabalho em construção por detPossib_;
	// somaMax[p][j]: soma acumulada, até o Prof. p, dos máximos de aulas Mp no dia j; graus_liberdade e max_: controlam a
	// maximização do número de possibilidades (Seção 5.2.3); cont2: medida do número de possibilidades da solução
	// corrente dos Casos 10 e 1; somaMax0: soma de todos os Mp (apenas informativa).
	static private int x0_[][], x1_[][], maximo0[][], maximo1[][], ch0[], ch1[], arranjo2[], d, xx[][], somaMax[][],
			graus_liberdade, max_, cont2, somaMax0;

	// Construtor: aloca os arranjos e os BitSets usados pelo módulo.
	public Module_2A() {
		super();
		// Dimensões: um índice por Prof. (de 1 a Qp) e uma coluna por dia da parte da semana considerada.
		somaMax = new int[Qp + 1][3];
		xx = new int[Qp + 1][3];
		arranjo2 = new int[3];
		ch0 = new int[Qp + 1];
		ch1 = new int[Qp + 1];
		x0_ = new int[Qp + 1][2];
		x1_ = new int[Qp + 1][3];
		maximo0 = new int[Qp + 1][2];
		maximo1 = new int[Qp + 1][3];
		sumA_0 = new int[Qa + 1];
		sumA_1 = new int[Qa + 1];
		maxDias0 = new int[Qp + 1];
		maxDias1 = new int[Qp + 1];
		complex_p0 = new boolean[Qp + 1];
		complex_p1 = new boolean[Qp + 1];
		complex_p2 = new boolean[Qp + 1];
		// Os BitSets têm espaço para o limite de possibilidades armazenadas (ver detPossib_ e detPossib_2).
		bitSet_0 = new BitSet(1028 * Qp * 2);
		bitSet_1 = new BitSet(20000 * Qp * 3);
		bitSet_2 = new BitSet(5000 * Qp * 2);
		nPossib = new int[2];
	}

	// Método auxiliar: pede ao usuário um inteiro por caixa de diálogo e o devolve.
	private int leiaInt(String s) {
		String entrada;
		entrada = (String) JOptionPane.showInputDialog(null, s, "Entrada", JOptionPane.QUESTION_MESSAGE, icone, null,
				null);
		return Integer.parseInt(entrada);
	}

	// Ordena (por seleção, em ordem crescente de amplitude) os elementos de índice first a nElementos-1 do arranjo amplitude,
	// permutando em paralelo o arranjo d2_ (que guarda a identidade do dia ou do Prof. correspondente a cada elemento).
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

	// Devolve o índice do menor valor de amplitude no intervalo i..nElementos-1.
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

	// Prepara a execução do Módulo 2A (comandos da Fase 1.1, Seção 5.3.1) e devolve o número de unidades n2.
	// Etapas: (1) ordena os dias da semana; (2) classifica os Profs. (complex_p0, complex_p1, complex_p2) e calcula
	// maximo0/maximo1 e a carga semanal ns_p_; (3) ordena os Profs. pela quantidade de distribuições possíveis (Module_A);
	// (4) monta as unidades (arranjos case_, Ap, Aa, A, j_); (5) inicializa os ponteiros das variáveis de controle e o arranjo z.
	public int preliminares() {
		int aux0, aux1, p1, d1, d;
		float totalPossib_0;
		int arranjo[];
		// A execução começa na 1ª etapa (graus_liberdade = 1000), cujo objetivo é descobrir o valor max_ (Seção 5.2.3).
		graus_liberdade = 1000;
		max_ = -1;
		arranjo = new int[5];
		// Os arranjos Sp2_, Sp3_ e apontP de FaseA1_Super passam a ser os mesmos arranjos Sp0_, Sp1_ e apontP2 (aproveitamento de memória).
		FaseA1_Super.Sp2_ = Probl.Sp0_;
		FaseA1_Super.Sp3_ = Probl.Sp1_;
		FaseA1_Super.apontP = Probl.apontP2;
		// A linha 0 de somaMax é o ponto de partida das somas acumuladas em detPossib_.
		for (k = 0; k <= 2; k++)
			somaMax[0][k] = 0;
		// Aviso ao usuário: a preparação pode demorar.
		JOptionPane.showMessageDialog(null, "Esta ação poderá levar alguns minutos", "Resultado",
				JOptionPane.INFORMATION_MESSAGE);
		// Ordenação dos dias: para cada dia k (0 a 4) soma-se Mp[p][k] sobre todos os Profs.; amplitude[k] recebe o simétrico dessa
		// soma, de modo que a ordenação crescente coloque primeiro os dias com maior soma (os dias 'mais fáceis').
		somaMax0 = 0;
		for (k = 0; k <= 4; k++) {
			aux0 = 0;
			for (p1 = 1; p1 <= Qp; p1++) {
				aux0 += Mp[p1][k];
			}
			somaMax0 += aux0;
			d2_[k] = k;
			amplitude[k] = -aux0;
			System.out.println("Soma dos MaxP[*][] para " + (k + 2) + "a-feira: " + aux0);
		}
		// Ordena os 5 dias: d2_[0..4] fica com os dias originais do mais fácil ao mais difícil.
		ordena2(0, 5);
		// Troca as posições 0 e 2 de d2_: o dia mais fácil passa a ocupar a posição 2 (quarta-feira), isto é, apoia a 2ª parte da
		// semana (d2, d3 e d4), em que ficam os dois dias mais difíceis nas posições 3 e 4 (Seção 5.4.3, item c).
		aux0 = d2_[0];
		d2_[0] = d2_[2];
		d2_[2] = aux0;
		// destino0[d] = nova posição do dia original d.
		for (d1 = 0; d1 <= 4; d1++) {
			k = 0;
			while (d2_[k] != d1)
				k++;
			destino0[d1] = k;
		}
		// Permuta, para cada Prof., os valores de Mp[p][*] de acordo com a nova ordem dos dias. A permuta é desfeita mais adiante
		// (na classe executável), depois de obtida a distribuição.
		for (p1 = 1; p1 <= Qp; p1++) {
			for (d1 = 0; d1 <= 4; d1++)
				aux_[d1] = Mp[p1][d1];
			for (d1 = 0; d1 <= 4; d1++)
				Mp[p1][destino0[d1]] = aux_[d1];
		}
		// Mostra as somas de Mp por dia após a permutação.
		System.out.println("Soma dos MaxP depois das permutações entre os dias:");
		for (k = 0; k <= 4; k++) {
			aux0 = 0;
			for (p1 = 1; p1 <= Qp; p1++)
				aux0 += Mp[p1][k];

			System.out.println("Soma dos MaxP[*][] para " + (k + 2) + "a-feira: " + aux0);
		}

		// Classificação dos Profs. (Seção 5.2.1). complex_p0 e complex_p2 comparam apenas dois dias; complex_p1 compara os três dias da 2ª parte.
		for (p = 1; p <= Qp; p++) {
			complex_p0[p] = Mp[p][0] > 0 && Mp[p][1] > 0 && Mp[p][0] != Mp[p][1];
			complex_p2[p] = Mp[p][2] > 0 && Mp[p][3] > 0 && Mp[p][2] != Mp[p][3];
			// complex_p1: verdadeiro se, entre Mp[p][2], Mp[p][3] e Mp[p][4], há dois valores positivos distintos.
			complex_p1[p] = false;
			w = Mp[p][2];
			aux = Mp[p][3];
			if (w != 0 && aux != 0 && w != aux)
				complex_p1[p] = true;
			else {
				w = Math.max(w, aux);
				aux = Mp[p][4];
				if (w != 0 && aux != 0 && w != aux)
					complex_p1[p] = true;
			}
			// maximo0[p] guarda (menor, maior) de Mp[p][0] e Mp[p][1]; maximo1[p] guarda Mp[p][2], Mp[p][3] e Mp[p][4] em ordem crescente.
			maximo0[p][0] = Math.min(Mp[p][0], Mp[p][1]);
			maximo0[p][1] = Math.max(Mp[p][0], Mp[p][1]);
			for (d = 2; d <= 4; d++)
				arranjo2[d - 2] = Mp[p][d];
			Arrays.sort(arranjo2);
			for (k = 0; k <= 2; k++)
				maximo1[p][k] = arranjo2[k];

			// ns_p_[p] = carga horária semanal de p (soma das aulas em todas as suas turmas).
			ns_p_[p] = 0;
			for (j = 1; j <= nTurmasP[p]; j++)
				ns_p_[p] += ns[p][j];
		}
		// Ordenação dos Profs.: para cada Prof. a classe Module_A determina o número de distribuições possíveis de suas aulas
		// entre os dois pares de dias; os Profs. com menos possibilidades vão para o início (Seção 5.4.3, item a).
		totalPossib_0 = 0;
		for (p = 1; p <= Qp; p++) {
			Module_A.preliminares(p);
			aux = Module_A.principal_0();
			amplitude[p] = (float) aux;
			totalPossib_0 += aux;
			System.out.println("Nro de distribuições para p = " + p + ": " + aux);
		}
		System.out.println("Total de possib. para todos p (somente fase 1) = " + totalPossib_0);
		// d2_[1..Qp] passa a conter os Profs. em ordem crescente de número de distribuições.
		for (p = 1; p <= Qp; p++)
			d2_[p] = p;
		System.out.println("Ordenação dos Profs. em d2_:");
		ordena2(1, Qp + 1);
		for (p = 1; p <= Qp; p++)
			System.out.print(d2_[p] + " ");
		System.out.println();

		// Montagem das unidades (Seção 5.3.1). A unidade 1 é do Caso 6 (ajusta graus_liberdade).
		k = 1;
		case_[k] = 6;
		// Para cada Prof. (na ordem estabelecida): uma unidade do Caso 10 (somente se complex_p0 ou complex_p1) seguida de uma
		// unidade do Caso 1; A[k] guarda a carga horária semanal do Prof.
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (complex_p0[p] || complex_p1[p]) {
				k++;
				Ap[k] = p;
				A[k] = ns_p_[p];
				case_[k] = 10;
			}
			k++;
			Ap[k] = p;
			A[k] = ns_p_[p];
			case_[k] = 1;
		}
		// Unidade do Caso 11: validação das unidades dos Casos 10 e 1.
		k++;
		case_[k] = 11;
		// Unidades do Caso 2: uma para cada par (Prof. p, turma lista[p][j]); A[k] = ns[p][j] aulas a repartir; j_[k] = posição j da turma.
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
		}
		// n2 = número total de unidades.
		n2 = k;

		// Sentinela: marca o fim do arranjo Ap (usado para detectar a mudança de Prof.).
		Ap[n2 + 1] = 0;
		// Ponteiros das variáveis de controle Sp0_ e Sp1_ (aulas acumuladas do Prof. nas duas partes da semana): cada unidade do
		// Caso 2 recebe a posição apontP2[j]; ao mudar de Prof. é reservada uma posição com valor 0 para reiniciar as somas.
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
		// Variáveis de controle Sa_0 e Sa_1 (aulas acumuladas da turma nas duas partes): cada turma a dispõe de 16 posições
		// consecutivas a partir de auxA[a]; a posição anterior a primeira guarda o valor inicial 0.
		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_0[auxA[j] - 1] = 0;
			Sa_1[auxA[j] - 1] = 0;
		}
		// apontA[j] = posição de Sa_0 e Sa_1 correspondente à unidade j do Caso 2 (cada nova unidade da mesma turma usa a posição seguinte).
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				apontA[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		// Prepara o arranjo z, que indica para qual unidade a busca deve recuar (Capítulo 3).
		determinaZ(n2);
		return n2;
	}

	// Método 'sucessor' do Módulo 2A (Seções 5.2.2 e 5.2.3): para a unidade i procura o próximo valor x0 > x_[i] que cumpre as
	// restrições do caso de case_[i]. Retorna false se encontrou (valor em x0) e true se não há sucessor.
	public boolean maximo() {
		int i0, sumA0, sumA1, minX0, aux, soma_, maxX0_;
		boolean teste, initialized;
		// p é o Prof. da unidade; x0 parte do valor atual da variável de decisão (-1 = unidade ainda não tentada).
		p = Ap[i];
		x0 = x_[i];

		switch (case_[i]) {
		// Caso 6: unidade de controle da maximização de possibilidades (Seção 5.2.3). Na 1ª visita (x_[i] == -1) apenas
		// avança. Quando a busca retorna a esta unidade: se graus_liberdade < 1000 (2ª etapa), reduz-o de 1 em 1 (falha se já
		// chegou a 0); se ainda é 1000 (fim da 1ª etapa), passa a 2ª etapa com graus_liberdade = max_ (falha se max_ continua -1).
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
			return teste;
		}

		// Caso 10: x0 é a carga horária de p na 1ª parte da semana (ch0). Limites: no mínimo o que não cabe na 2ª parte
		// (ns_p_ menos a soma de Mp nos dias d2, d3 e d4) e no máximo o que cabe na 1ª parte (Mp[p][0] + Mp[p][1]).
		case 10: {
			if (x0 > -1) {
				x0++;
				return x0 > maxX0[i];
			}
			minX0 = Math.max(0, ns_p_[p] - Mp[p][2] - Mp[p][3] - Mp[p][4]);
			maxX0[i] = Math.min(ns_p_[p], Mp[p][0] + Mp[p][1]);
			x0 = Math.max(minX0, x0 + 1);
			return x0 > maxX0[i];
		}

		// Caso 1: x0 é o número de dias de trabalho de p na 1ª parte da semana (maxDias0). aux2 = número de dias da 2ª parte em que p está disponível.
		case 1: {
			aux2 = (Mp[p][2] > 0 ? 1 : 0) + (Mp[p][3] > 0 ? 1 : 0) + (Mp[p][4] > 0 ? 1 : 0);
			while (true) {
				x0++;
				// aux = dias que sobrariam para a 2ª parte (Wp0[p] - x0).
				aux = Wp0[p] - x0;
				// Nenhum valor maior que x0 serve se x0 passa de 2, de Wp0[p] ou do número de dias disponíveis de p na 1ª parte.
				if (x0 > 2 || x0 > Wp0[p])
					return true;
				if (x0 > (Mp[p][0] > 0 ? 1 : 0) + (Mp[p][1] > 0 ? 1 : 0))
					return true;
				// Na 2ª parte, p não pode trabalhar mais dias do que os disponíveis.
				if (aux > aux2)
					aux = aux2;
				// Verifica se a carga horária cabe. Para Profs. complex_p0/complex_p1, ch0 e ch1 já foram fixados pelo Caso 10: a soma dos
				// x0 maiores Mp da 1ª parte deve atingir ch0[p] e a dos aux maiores Mp da 2ª parte deve atingir ch1[p].
				if (complex_p0[p] || complex_p1[p]) {
					soma_ = 0;
					for (k = 0; k < x0; k++)
						soma_ += maximo0[p][1 - k];
					teste = soma_ >= ch0[p];
					if (teste) {
						soma_ = 0;
						for (k = 0; k < aux; k++)
							soma_ += maximo1[p][2 - k];
						teste = soma_ >= ch1[p];
					}
				// Para os demais Profs., basta que a capacidade máxima nas duas partes cubra a carga semanal ns_p_[p]. Se o teste e
				// satisfeito, x0 é o sucessor.
				} else
					teste = x0 * maximo0[p][1] + aux * maximo1[p][2] >= ns_p_[p];
				if (teste)
					return false;
			}
		}

		// Caso 110: variante do Caso 11 (valida detPossib_ antes de calcular os limitantes). Nenhuma unidade com este caso e
		// criada por preliminares(), logo este trecho não é executado.
		case 110: {
			if (x0 > -1)
				return true;
			cont2 = 0;
			for (p = 1; p <= Qp; p++) {
				if (maxDias0[p] == 1 && Mp[p][0] > 0 && Mp[p][1] > 0 && !complex_p0[p])
					cont2 += 1;
				if (complex_p1[p])
					continue;
				aux = 0;
				for (k = 2; k <= 4; k++) {
					if (Mp[p][k] > 0)
						aux++;
				}
				k = Wp0[p] - maxDias0[p];
				if (aux == 3 && (k == 1 || k == 2))
					cont2 += 3;
				if (aux == 2 && k == 1)
					cont2 += 2;
			}
			if (graus_liberdade < 1000 && cont2 != graus_liberdade)
				return true;

			teste = detPossib_(0, true);
			if (!teste)
				return true;
			for (p = 1; p <= Qp; p++) {
				if (complex_p0[p])
					Np0[p] = x0_[p][0] * Mp[p][0] + x0_[p][1] * Mp[p][1];
				else
					Np0[p] = maximo0[p][1] * maxDias0[p];
			}
			teste = detPossib_(1, true);
			if (!teste)
				return true;
			for (p = 1; p <= Qp; p++) {
				if (complex_p1[p])
					Np1[p] = x1_[p][0] * Mp[p][2] + x1_[p][1] * Mp[p][3] + x1_[p][2] * Mp[p][4];
				else
					Np1[p] = maximo1[p][2] * maxDias1[p];
			}

			sumA0 = 0;
			sumA1 = 0;
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				aux0 = (semGeminadasP[p] ? maxDias0[p] : 2 * maxDias0[p]);
				aux1 = (semGeminadasP[p] ? maxDias1[p] : 2 * maxDias1[p]);
				aux0 = Math.min(aux0, Np0[p]);
				aux1 = Math.min(aux1, Np1[p]);
				aux2 = nTurmasP[p];
				sumA0 += Np0[p];
				sumA1 += Np1[p];
				for (j = 1; j <= aux2; j++) {
					a = lista[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
					sumA_1[a] += Math.min(aux1, ns[p][j]);
				}
			}
			teste = sumA0 >= 2 * Ma * Qa && sumA1 >= 3 * Ma * Qa;
			if (teste) {
				for (a = 1; a <= Qa; a++) {
					teste = sumA_0[a] >= 2 * Ma && sumA_1[a] >= 3 * Ma;
					if (!teste)
						break;
				}
			}
			x0 = 0;
			if (graus_liberdade < 1000)
				return !teste;
			if (teste)
				max_ = Math.max(max_, cont2);
			return true;
		}

		// Caso 11: validação das decisões dos Casos 10 e 1 (Seção 5.2.2). Só tem efeito na ida; ao retornar a esta unidade
		// (x0 > -1) apenas continua o recuo.
		case 11: {
			if (x0 > -1)
				return true;
			// cont2 é uma medida da quantidade de possibilidades que a solução atual dos Casos 10 e 1 permite (Seção 5.2.3). Para cada Prof.
			// p, soma-se: 1, se p trabalha em um só dos dois dias da 1ª parte, ambos disponíveis, e não é complex_p0; e, se p não é complex_p1,
			// 3, se p tem os três dias da 2ª parte disponíveis e trabalha em 1 ou 2 deles, ou 2, se tem dois dias disponíveis e trabalha em 1.
			cont2 = 0;
			for (p = 1; p <= Qp; p++) {
				if (maxDias0[p] == 1 && Mp[p][0] > 0 && Mp[p][1] > 0 && !complex_p0[p])
					cont2 += 1;
				if (complex_p1[p])
					continue;
				aux = 0;
				for (k = 2; k <= 4; k++) {
					if (Mp[p][k] > 0)
						aux++;
				}
				k = Wp0[p] - maxDias0[p];
				if (aux == 3 && (k == 1 || k == 2))
					cont2 += 3;
				if (aux == 2 && k == 1)
					cont2 += 2;
			}
			// Na 2ª etapa (graus_liberdade < 1000) só se aceita a solução cujo cont2 é igual a graus_liberdade.
			if (graus_liberdade < 1000 && cont2 != graus_liberdade)
				return true;

			// Limitantes provisórios de aulas nas duas partes: para Profs. complexos, a carga horária ch0/ch1; para os demais,
			// o máximo de um dia vezes a quantidade de dias de trabalho.
			for (p = 1; p <= Qp; p++) {
				if (complex_p0[p])
					Np0[p] = ch0[p];
				else
					Np0[p] = maximo0[p][1] * maxDias0[p];
				if (complex_p1[p])
					Np1[p] = ch1[p];
				else
					Np1[p] = maximo1[p][2] * maxDias1[p];
			}

			// Verificação das demandas: sumA0 e sumA1 somam os limitantes Np0 e Np1 de todos os Profs.; sumA_0[a] e sumA_1[a] somam
			// a contribuição de cada Prof. à turma a (limitada pelo máximo de aulas da turma por dia e por ns[p][j]).
			sumA0 = 0;
			sumA1 = 0;
			for (a = 1; a <= Qa; a++) {
				sumA_0[a] = 0;
				sumA_1[a] = 0;
			}
			for (p = 1; p <= Qp; p++) {
				aux0 = (semGeminadasP[p] ? maxDias0[p] : 2 * maxDias0[p]);
				aux1 = (semGeminadasP[p] ? maxDias1[p] : 2 * maxDias1[p]);
				aux0 = Math.min(aux0, Np0[p]);
				aux1 = Math.min(aux1, Np1[p]);
				aux2 = nTurmasP[p];
				sumA0 += Np0[p];
				sumA1 += Np1[p];
				for (j = 1; j <= aux2; j++) {
					a = lista[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
					sumA_1[a] += Math.min(aux1, ns[p][j]);
				}
			}
			// As demandas totais (2*Ma*Qa na 1ª parte e 3*Ma*Qa na 2ª) e as de cada turma (2*Ma e 3*Ma) devem ser atendidas.
			teste = sumA0 >= 2 * Ma * Qa && sumA1 >= 3 * Ma * Qa;
			if (teste) {
				for (a = 1; a <= Qa; a++) {
					teste = sumA_0[a] >= 2 * Ma && sumA_1[a] >= 3 * Ma;
					if (!teste)
						break;
				}
			}
			// Procura (detPossib_ com inicio true) uma forma de escolher os dias de trabalho de todos os Profs.; as encontradas
			// ficam em x0_ (1ª parte) e x1_ (2ª parte).
			teste = teste && detPossib_(0, true);
			if (!teste)
				return true;
			teste = detPossib_(1, true);
			if (!teste)
				return true;
			// Com os dias de trabalho definidos, Np0 e Np1 dos Profs. complexos são recalculados com os máximos dos dias escolhidos.
			for (p = 1; p <= Qp; p++) {
				if (complex_p0[p])
					Np0[p] = x0_[p][0] * Mp[p][0] + x0_[p][1] * Mp[p][1];
				if (complex_p1[p])
					Np1[p] = x1_[p][0] * Mp[p][2] + x1_[p][1] * Mp[p][3] + x1_[p][2] * Mp[p][4];
			}
			// Valor da variável de decisão desta unidade (sem significado próprio).
			x0 = 0;
			// 2ª etapa: a unidade segue se a validação foi positiva. 1ª etapa: apenas registra em max_ o maior cont2 que passou na
			// validação e retorna true, para que a busca prossiga enumerando todas as soluções dos Casos 10 e 1.
			if (graus_liberdade < 1000)
				return !teste;
			if (teste)
				max_ = Math.max(max_, cont2);
			return true;
		}

		// Caso 2: x0 é o número de aulas do par (p, turma Aa[i]) na 1ª parte da semana; A[i] - x0 e o número na 2ª parte.
		// Na primeira visita calculam-se os limites minX0 e maxX0[i]; nas seguintes apenas incrementa-se x0.
		case 2: {
			// Não há sucessor se x0 já atingiu o máximo possível de aulas na 1ª parte (1 ou 2 por dia de trabalho).
			if (x0 == 2 * maxDias0[p] || (semGeminadasP[p] && x0 == maxDias0[p]))
				return true;
			// Caso trivial: um dos limitantes Np0[p] ou Np1[p] é zero, e as aulas ficam todas na outra parte (valor único).
			case_Trivial = Np0[p] == 0 || Np1[p] == 0;
			n0 = A[i];
			initialized = x0 > -1;
			if (case_Trivial) {
				if (x0 > -1)
					return true;
				x0 = (Np0[p] == 0 ? 0 : n0);
			} else
				x0++;
			if (initialized)
				return x0 > maxX0[i];
			// Sp0, Sp2: aulas já atribuídas ao Prof. (até a unidade anterior) na 1ª e na 2ª parte; Sa0, Sa2: o mesmo para a turma a.
			a = Aa[i];
			Sp0 = Sp0_[apontP2[i] - 1];
			Sp2 = Sp1_[apontP2[i] - 1];
			Sa0 = Sa_0[apontA[i] - 1];
			Sa2 = Sa_1[apontA[i] - 1];

			// Limite inferior: a 2ª parte não comporta mais que (maxDias1 ou 2*maxDias1) aulas do par, nem mais que Np1[p] aulas do Prof., nem mais que 3*Ma da turma.
			minX0 = (semGeminadasP[p] ? n0 - maxDias1[p] : n0 - 2 * maxDias1[p]);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - 3 * Ma);
			x0 = Math.max(x0, minX0);

			// Limite superior: a 1ª parte não comporta mais que (maxDias0 ou 2*maxDias0) aulas do par, nem mais que n0, nem mais
			// que o resto de Np0[p] para o Prof., nem mais que o resto de 2*Ma para a turma.
			maxX0_ = (semGeminadasP[p] ? maxDias0[p] : 2 * maxDias0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}
		}
		return true;
	}

	// Atualiza as variáveis de controle e de ligação depois que x0 foi aceito para a unidade i (Seção 5.2.2).
	public void atualiza2() {
		p = Ap[i];
		switch (case_[i]) {
		// Caso 10: registra as cargas horárias do Prof. nas duas partes da semana.
		case 10: {
			ch0[p] = x0;
			ch1[p] = ns_p_[p] - x0;
			break;
		}
		// Caso 1: define maxDias0 e maxDias1 (este limitado ao número de dias disponíveis na 2ª parte).
		case 1: {
			maxDias0[p] = x0;
			maxDias1[p] = Wp0[p] - x0;
			aux2 = (Mp[p][2] > 0 ? 1 : 0) + (Mp[p][3] > 0 ? 1 : 0) + (Mp[p][4] > 0 ? 1 : 0);
			if (maxDias1[p] > aux2)
				maxDias1[p] = aux2;
			break;
		}
		// Caso 2: acumula as aulas do Prof. (Sp0_, Sp1_) e da turma (Sa_0, Sa_1) em cada parte da semana.
		case 2: {
			nClasses = A[i] - x0;
			aux = apontP2[i];
			Sp0_[aux] = Sp0_[aux - 1] + x0;
			Sp1_[aux] = Sp1_[aux - 1] + nClasses;
			aux = apontA[i];
			Sa_0[aux] = Sa_0[aux - 1] + x0;
			Sa_1[aux] = Sa_1[aux - 1] + nClasses;
			break;
		}

		}
	}

	// Algoritmo 2 do Capítulo 3 (busca com recuo inteligente). Retorna true se o Módulo 2A (e também o 2B) obtém solução.
	public boolean principalV2() {
		int k, imax, p1, p0, minimo;
		boolean teste, minimo_;
		String entrada;
		Object[] opcoes = { "Sim", "Não" };

		// Inicialização: todas as unidades recebem o 'gerador' (minimo = -1) e a busca começa em i = 1.
		imax = 0;
		minimo = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		// Limite de tempo de execução pedido ao usuário (em minutos); verificado a cada 50000 iterações.
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
			// Se maximo() tem sucesso: guarda x0, atualiza as variáveis de controle e avança para a unidade seguinte.
			if (!maximo()) {
				x_[i] = x0;
				atualiza2();
				i++;
				if (i > imax) {
					imax = i;
					System.out.println(i + "/" + n2 + " p=" + Ap[i] + " case_ " + case_[i]);
				}
				// Todas as unidades fechadas: o Módulo 2A termina. Calcula as possibilidades (atualiza4), pergunta ao usuário como deve
				// ser o retorno ao Caso 2 e chama o Módulo 2B (preliminares e principal_V1).
				if (i > n2) {
					System.out.println();
					System.out.println("somaMax0 = " + somaMax0);
					atualiza4(n2);
					k = JOptionPane.showOptionDialog(null, "Retornar para o case_ 2 pelo minimo?", "",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);
					minimo_ = k == JOptionPane.YES_OPTION;
					g2_I_W_.preliminares();
					teste = g2_I_W_.principal_V1(minimo_);
					return teste;
				}
				// Atualiza iMaxI_ (unidade mais avançada atingida depois de cada unidade anterior), usado para determinar o recuo.
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Falha: recua para a unidade p0 = z[i][iMaxI_[i] - i], a menor que ainda pode influir na causa da falha.
			} else {
				if (i == 1)
					return false;
				p0 = z[i][iMaxI_[i] - i];
				if (p0 == 0)
					return false;
				// Redefine com o gerador as unidades abandonadas.
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}

	// Obtém as possibilidades: guarda em bitSet_0 e bitSet_1 os dias de trabalho fixos dos Profs. complexos e chama
	// detPossib_ (1ª parte, 2ª parte) e detPossib_2 (possibilidades da 2ª parte).
	public void atualiza4(int n) {
		int p, k;
		for (p = 1; p <= Qp; p++) {
			if (complex_p0[p]) {
				for (k = 0; k <= 1; k++)
					bitSet_0.set(2 * (p - 1) + k, x0_[p][k] != 0);
			}
			if (complex_p1[p]) {
				for (k = 0; k <= 2; k++)
					bitSet_1.set(3 * (p - 1) + k, x1_[p][k] != 0);
			}
		}

		// Armazena em bitSet_0 todas as possibilidades da 1ª parte.
		nPossib[0] = 0;
		detPossib_(0, false);
		// Armazena em bitSet_1 todas as sub-possibilidades da 2ª parte.
		nPossib[1] = 0;
		detPossib_(1, false);
		// Deriva de bitSet_1 as possibilidades da 2ª parte (bitSet_2).
		detPossib_2();
		System.out.println(" ");
		System.out.println("Número de possibilidades: " + nPossib[0] + " " + nPossib_2);
	}

	// Enumera formas de escolher os dias de trabalho de todos os Profs. (Seção 5.2.2). exec = 0: 1ª parte (dias 0 e 1);
	// exec = 1: 2ª parte (dias 2, 3 e 4). xx[p][j] = 1 (p trabalha no j-ésimo dia da parte), 0 ou -1 (ainda não definido).
	// Se inicio é true, para na primeira forma completa encontrada, copiando-a para x0_ ou x1_, e retorna true (false se não
	// existe nenhuma). Se inicio é false, percorre todas as formas, armazenando as válidas em bitSet_0 ou bitSet_1.
	private static boolean detPossib_(int exec, boolean inicio) {
		int dias[], maxCols, i, j, k, soma, p, x0, possib;
		boolean maximo, teste;

		if (exec == 0) {
			dias = new int[] { 0, 1 };
			maxCols = 1;
		} else {
			dias = new int[] { 2, 3, 4 };
			maxCols = 2;
		}

		// Estado inicial: nenhum dia definido; a busca percorre os Profs. (i) e os dias da parte (j) em ordem.
		for (p = 1; p <= Qp; p++) {
			for (j = 0; j <= maxCols; j++)
				xx[p][j] = -1;
		}
		i = 1;
		j = 0;
		while (true) {
			// Para Profs. complexos, com inicio false, os dias de trabalho são os já fixados em x0_ ou x1_ (valor único).
			x0 = xx[i][j];
			p = i;
			if (!inicio && ((exec == 0 && complex_p0[i]) || (exec == 1 && complex_p1[i]))) {
				maximo = x0 > -1;
				if (!maximo)
					x0 = (exec == 0 ? x0_[p][j] : x1_[p][j]);
			// Demais casos: tenta x0 = 0 e x0 = 1 (somente 0 se p não está disponível nesse dia).
			} else {
				if (x0 >= 1 || (x0 >= 0 && Mp[i][dias[j]] == 0))
					maximo = true;
				else {
					while (true) {
						x0++;
						if (x0 > 1 || (x0 > 0 && Mp[i][dias[j]] == 0)) {
							maximo = true;
							break;
						}
						// Poda: a soma dos máximos dos Profs. até i neste dia, mais o que os Profs. restantes poderiam oferecer (no máximo Ma
						// cada um), deve atingir a demanda do dia, Ma*Qa.
						teste = somaMax[i - 1][j] + x0 * Mp[i][dias[j]] + (Qp - i) * Ma >= Qa * Ma;
						// Ao definir o último dia da parte, verifica o conjunto dos dias de p: a quantidade de dias deve ser maxDias0[p] (1ª parte)
						// ou no máximo maxDias1[p] (2ª parte), e a capacidade deve atender a carga horária.
						if (teste && j == maxCols) {
							xx[i][j] = x0;
							soma = 0;
							for (k = 0; k <= maxCols; k++)
								soma += xx[i][k];
							if (exec == 0)
								teste = soma == maxDias0[i];
							else
								teste = soma <= maxDias1[i];
							if (teste) {
								// Prof. complexo: soma dos Mp dos dias escolhidos deve atingir a carga ch0 ou ch1.
								if (complex_p0[i] || complex_p1[i]) {
									soma = 0;
									for (k = 0; k <= maxCols; k++)
										soma += xx[i][k] * Mp[i][dias[k]];
									teste = soma >= (exec == 0 ? ch0[i] : ch1[i]);
								// Prof. não complexo: na 2ª parte, a capacidade das duas partes deve cobrir a carga semanal.
								} else {
									if (exec == 1) {
										soma = maxDias0[i] * maximo0[i][1];
										for (k = 0; k <= maxCols; k++)
											soma += xx[i][k] * Mp[i][dias[k]];
										teste = soma >= ns_p_[i];
									}
								}
							}
						}
						if (teste) {
							maximo = false;
							break;
						}
					}
				}
			}
			// Valor aceito: registra-o, acumula a soma dos máximos do dia e avança para o próximo dia ou Prof.
			if (!maximo) {
				xx[i][j] = x0;
				somaMax[i][j] = somaMax[i - 1][j] + x0 * Mp[i][dias[j]];
				j++;
				if (j > maxCols) {
					i++;
					j = 0;
					// Passou do último Prof.: forma completa de escolher os dias de trabalho.
					if (i > Qp) {
						// inicio true: guarda a forma encontrada em x0_ ou x1_ e retorna true.
						if (inicio) {
							for (p = 1; p <= Qp; p++) {
								for (j = 0; j <= maxCols; j++) {
									if (exec == 0)
										x0_[p][j] = xx[p][j];
									else
										x1_[p][j] = xx[p][j];
								}
							}
							return true;
						}

						// inicio false: armazena a forma em bitSet_0 (possib * Qp * 2 + ...) ou bitSet_1 (possib * Qp * 3 + ...); para Profs.
						// complexos copia os bits fixos do início do BitSet. Só é contada (nPossib) se passar em validacao().
						System.out.print(".");
						possib = nPossib[exec] + 1;
						// Limite de espaço: encerra se atingiu o número máximo de possibilidades (1026) ou sub-possibilidades (19998) armazenáveis.
						if ((exec == 0 && possib == 1026) || (exec == 1 && possib == 19998))
							return true;
						if (exec == 0) {
							for (p = 1; p <= Qp; p++) {
								if (complex_p0[p]) {
									for (k = 0; k <= maxCols; k++)
										bitSet_0.set(possib * Qp * 2 + 2 * (p - 1) + k, bitSet_0.get(2 * (p - 1) + k));
								} else {
									for (k = 0; k <= maxCols; k++)
										bitSet_0.set(possib * Qp * 2 + 2 * (p - 1) + k, xx[p][k] != 0);
								}
							}
						} else {
							for (p = 1; p <= Qp; p++) {
								if (complex_p1[p]) {
									for (k = 0; k <= maxCols; k++)
										bitSet_1.set(possib * Qp * 3 + 3 * (p - 1) + k, bitSet_1.get(3 * (p - 1) + k));
								} else {
									for (k = 0; k <= maxCols; k++)
										bitSet_1.set(possib * Qp * 3 + 3 * (p - 1) + k, xx[p][k] != 0);
								}
							}
						}
						// Se não passar na validação, o espaço será reutilizado pela próxima forma.
						if (validacao(exec, possib))
							nPossib[exec]++;
						// Posiciona a busca no último elemento para continuar procurando outras formas (recuo).
						i = Qp;
						j = maxCols;
					}
				}
			// Sem sucessor: reinicia a posição e recua. A enumeração termina (retornando false) quando recua além do 1º Prof.
			} else {
				xx[i][j] = -1;
				j--;
				if (j < 0) {
					j = maxCols;
					i--;
					if (i < 1)
						return false;
				}
			}
		}
	}

	// Valida uma possibilidade (exec = 0) ou sub-possibilidade (exec = 1) armazenada em possib: (1) em cada dia, a soma dos
	// Mp dos Profs. que trabalham deve atingir Ma*Qa; (2) para cada turma, em cada dia, os Profs. que trabalham devem poder
	// oferecer pelo menos Ma aulas (cada Prof. oferece no máximo 1 aula por dia na turma se semGeminadasP, senão 2; e no
	// máximo Mp[p][dia] e ns[p][j]).
	private static boolean validacao(int exec, int possib) {
		int k, sumA0, aux0;
		boolean x0;
		if (exec == 0)
			dias = new int[] { 0, 1 };
		else
			dias = new int[] { 2, 3, 4 };
		for (k = 0; k < dias.length; k++) {
			sumA0 = 0;
			for (p = 1; p <= Qp; p++) {
				if (exec == 0)
					x0 = bitSet_0.get(possib * Qp * 2 + 2 * (p - 1) + k);
				else
					x0 = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1) + k);
				if (x0)
					sumA0 += Mp[p][dias[k]];
			}
			if (sumA0 < Ma * Qa)
				return false;
		}
		for (k = 0; k < dias.length; k++) {
			for (a = 1; a <= Qa; a++)
				sumA_0[a] = 0;
			for (p = 1; p <= Qp; p++) {
				if (exec == 0)
					x0 = bitSet_0.get(possib * Qp * 2 + 2 * (p - 1) + k);
				else
					x0 = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1) + k);
				if (!x0)
					continue;
				aux0 = (semGeminadasP[p] ? 1 : 2);
				aux0 = Math.min(aux0, Mp[p][dias[k]]);
				aux2 = nTurmasP[p];
				for (j = 1; j <= aux2; j++) {
					a = lista[p][j];
					sumA_0[a] += Math.min(aux0, ns[p][j]);
				}
			}
			for (a = 1; a <= Qa; a++) {
				if (sumA_0[a] < Ma)
					return false;
			}
		}
		return true;
	}

	// Obtém as possibilidades da 2ª parte (bitSet_2) a partir das sub-possibilidades (bitSet_1): para cada Prof. conta quantos
	// dos dias d2 e d3 são de trabalho (0, 1 ou 2, em 2 bits) e descarta as repetidas.
	private static void detPossib_2() {
		int possib, k, p, m;
		boolean teste, teste2;

		nPossib_2 = 0;
		// Para cada sub-possibilidade, monta a possibilidade candidata na posição k.
		for (possib = 1; possib <= nPossib[1]; possib++) {
			k = nPossib_2 + 1;
			for (p = 1; p <= Qp; p++) {
				teste = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1));
				teste2 = bitSet_1.get(possib * Qp * 3 + 3 * (p - 1) + 1);
				if (teste && teste2) {
					bitSet_2.set(k * Qp * 2 + 2 * (p - 1), true);
					bitSet_2.set(k * Qp * 2 + 2 * (p - 1) + 1, false);
				} else {
					if (teste != teste2) {
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1), false);
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1) + 1, true);
					} else {
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1), false);
						bitSet_2.set(k * Qp * 2 + 2 * (p - 1) + 1, false);
					}
				}
			}
			// A primeira é sempre aceita.
			if (possib == 1) {
				nPossib_2++;
				continue;
			}
			// Compara com as já armazenadas (posições 1 a k-1); se igual a alguma, não é contada (a posição k será reutilizada).
			teste = true;
			for (m = 1; m < k; m++) {
				teste2 = true;
				for (p = 1; p <= Qp; p++) {
					if (bitSet_2.get(m * Qp * 2 + 2 * (p - 1)) != bitSet_2.get(k * Qp * 2 + 2 * (p - 1)) || bitSet_2
							.get(m * Qp * 2 + 2 * (p - 1) + 1) != bitSet_2.get(k * Qp * 2 + 2 * (p - 1) + 1)) {
						teste2 = false;
						break;
					}
				}
				if (teste2) {
					teste = false;
					break;
				}
			}
			// Possibilidade nova: contada (limite de 4998 armazenadas).
			if (teste) {
				nPossib_2++;
				if (nPossib_2 == 4998)
					return;
			}
		}
	}

	// Dependência: não utilizada pela versão eficiente (determinaZ).
	public boolean dependencia0(int i, int j) {
		return true;
	}

	// Método 'dependencia' (Capítulo 3): indica se a unidade j depende da unidade i (i < j). As unidades dos Casos 1, 10 e 11
	// dependem de todas as anteriores; no Caso 2 a dependência é com o Caso 1 do mesmo Prof. e com unidades do Caso 2 do mesmo Prof. ou da mesma turma.
	public boolean dependencia(int i, int j) {
		if (j <= i)
			return false;
		if (case_[j] == 1 || case_[j] == 10)
			return true;
		// Observação: as duas condições seguintes nunca são alcançadas, pois o teste anterior já retorna true para os Casos 1 e 10.
		if (case_[j] == 10)
			return case_[i] == 6;
		if (case_[j] == 1)
			return case_[i] == 6 || (case_[i] == 10 && Ap[i] == Ap[j]);
		if (case_[j] == 11)
			return true;

		if (case_[j] == 2) {
			if (case_[i] == 1)
				return Ap[i] == Ap[j];
			if (case_[i] == 2)
				return Ap[i] == Ap[j] || Aa[i] == Aa[j];
			return false;
		}

		return true;
	}

	// determinaArranjo do Capítulo 3: preenche z[i][l] com o maior p < i de que a unidade i + l depende; depois acumula o
	// máximo ao longo de l, de modo que z[i][q - i] indique para onde recuar.
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
}
