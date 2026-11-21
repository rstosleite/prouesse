package prouesse;

// Classe Module_A - subsídio para a ordenação dos Profs. (Seção 5.4.3, item a, e Seção 4.6.2 do livro).
//
// Papel: para um Prof. p, conta de quantas maneiras as aulas de p podem ser distribuídas entre as duas partes da semana
// (dias d0 e d1 contra d2, d3 e d4), considerando todas as suas turmas. O Módulo 2A usa essa contagem (método principal_0)
// para ordenar os Profs. em ordem crescente de possibilidades. A contagem é feita por uma busca exaustiva com o mesmo
// algoritmo de busca do Capítulo 3, sem recuo inteligente: ao fechar todas as unidades conta uma solução e continua.
// Unidades (case_[i]): cinco do Caso 10 (x = 1 se p trabalha no dia i - 1, 0 se não), uma do Caso 100 (carga horária de p
// na 1ª parte: ch0; o resto é ch1), uma do Caso 11 (validação) e uma do Caso 2 por turma de p (aulas da turma na 1ª parte).
public class Module_A extends FaseA1_Super {

	// Sp2_, Sp3_ e apontP: acumuladores de aulas do Prof. nas duas partes (como no Módulo 2A); ns_p_: carga semanal de p;
	// ch0 e ch1: aulas de p na 1ª e na 2ª parte; maxDias0 e maxDias1: máximos de dias de trabalho de p nas duas partes;
	// possibP[][]: pares (ch0, maxDias0) já contados (evita contar duas vezes a mesma combinação); nPossibP: quantidade.
	static private int d1, n2, p, maxDias0, maxDias1, Sp2_[], Sp3_[], apontP[], nhorarios_P0, nhorarios_P1, ch0, ch1,
			ns_p_, nPossibP, possibP[][];
	static private boolean teste2;

	// Construtor vazio: a classe só tem métodos estáticos.
	public Module_A() {
	}

	// Prepara a contagem para o Prof. p0: calcula a carga semanal e monta as unidades.
	static public void preliminares(int p0) {
		Sp2_ = new int[42];
		Sp3_ = new int[42];
		apontP = new int[42];
		possibP = new int[45][2];
		p = p0;
		ns_p_ = 0;
		for (j = 1; j <= nTurmasP[p]; j++)
			ns_p_ += ns[p][j];
		nPossibP = 0;
		k = 0;
		// Unidades do Caso 10: uma para cada dia da semana (dias de trabalho de p).
		for (d1 = 0; d1 <= 4; d1++) {
			k++;
			case_[k] = 10;
		}
		// Unidade do Caso 100: divide a carga horária de p entre a 1ª e a 2ª parte.
		k++;
		case_[k] = 100;
		// Unidade do Caso 11: valida as escolhas anteriores e evita combinações repetidas.
		k++;
		case_[k] = 11;
		// Unidades do Caso 2: uma para cada turma de p, com A[k] = ns[p][j] aulas a repartir entre as duas partes.
		for (j = 1; j <= nTurmasP[p]; j++) {
			k++;
			Aa[k] = lista[p][j];
			j_[k] = j;
			A[k] = ns[p][j];
			case_[k] = 2;
		}
		n2 = k;

		// Ponteiros das somas acumuladas de aulas de p nas duas partes (apenas para as unidades do Caso 2).
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			if (case_[j] == 2) {
				apontP[j] = aux2;
				aux2++;
			}
		}
	}

	// Sucessor da unidade i. Retorna false se encontrou o sucessor de x_[i] (valor em x0) e true se não existe.
	static public boolean maximo_() {
		int aux0, aux10, aux11, minX0, maxX0, nDias, soma0;
		boolean case_Trivial, teste;
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 10: x0 = 1 se p trabalha no dia i - 1. O número de dias de trabalho não pode exceder Wp0[p] e a capacidade dos
		// dias já escolhidos mais a de todos os dias seguintes deve cobrir a carga ns_p_.
		case 10: {
			if (x0 >= 1)
				return true;
			nDias = 0;
			soma0 = 0;
			for (k = 1; k <= i - 1; k++) {
				nDias += x_[k];
				soma0 += (x_[k] == 0 ? 0 : Mp[p][k - 1]);
			}
			for (k = i + 1; k <= 5; k++)
				soma0 += Mp[p][k - 1];
			while (true) {
				x0++;
				if (x0 > 1 || nDias + x0 > Wp0[p])
					return true;
				teste = soma0 + (x0 == 0 ? 0 : Mp[p][i - 1]) >= ns_p_;
				if (teste)
					return false;
			}
		}

		// Caso 100: x0 = ch0, aulas de p na 1ª parte. Mínimo: o que não cabe na 2ª parte; máximo: o que cabe na 1ª parte,
		// de acordo com os dias de trabalho escolhidos nas unidades do Caso 10.
		case 100: {
			nhorarios_P0 = x_[1] * Mp[p][0] + x_[2] * Mp[p][1];
			nhorarios_P1 = x_[3] * Mp[p][2] + x_[4] * Mp[p][3] + x_[5] * Mp[p][4];
			minX0 = Math.max(0, ns_p_ - nhorarios_P1);
			minX0 = Math.max(x0 + 1, minX0);
			maxX0 = Math.min(ns_p_, nhorarios_P0);
			if (maxX0 < minX0)
				return true;
			x0 = minX0;
			ch0 = x0;
			ch1 = ns_p_ - x0;
			return false;

		}

		// Caso 11: define maxDias0 (dias de trabalho na 1ª parte) e maxDias1 (resto de Wp0[p]). Se a combinação (ch0, maxDias0) já
		// foi contada, a unidade falha (para não contar de novo). Também falha se as aulas das turmas não podem ser repartidas
		// segundo ch0, ch1, maxDias0 e maxDias1.
		case 11: {
			if (x0 > -1)
				return true;
			maxDias0 = x_[1] + x_[2];
			maxDias1 = Wp0[p] - maxDias0;
			teste2 = false;

			if (nPossibP > 0) {
				for (k = 0; k <= nPossibP - 1; k++) {
					teste2 = ch0 == possibP[k][0] && maxDias0 == possibP[k][1];
					if (teste2)
						break;
				}
			}
			if (!teste2) {
				k = nPossibP;
				nPossibP++;
				possibP[k][0] = ch0;
				possibP[k][1] = maxDias0;
			} else
				return true;

			aux10 = 0;
			aux11 = 0;
			aux0 = nTurmasP[p];
			for (j = 1; j <= aux0; j++) {
				aux10 += Math.min((semGeminadasP[p] ? maxDias0 : 2 * maxDias0), ns[p][j]);
				aux11 += Math.min((semGeminadasP[p] ? maxDias1 : 2 * maxDias1), ns[p][j]);
			}
			x0 = 0;
			return Math.min(aux10, ch0) + Math.min(aux11, ch1) < ns_p_;
		}

		// Caso 2: x0 = aulas da turma na 1ª parte, com A[i] - x0 na 2ª parte, respeitando ch0, ch1 e a quantidade de aulas por dia
		// (1 ou 2). Caso trivial: ch0 ou ch1 é zero e as aulas ficam todas em uma só parte.
		case 2: {
			if (ch0 == x0)
				return true;
			case_Trivial = ch0 == 0 || ch1 == 0;
			if (case_Trivial && x0 > -1)
				return true;
			a = Aa[i];
			n0 = A[i];
			Sp0 = Sp2_[apontP[i] - 1];
			Sp2 = Sp3_[apontP[i] - 1];
			if (case_Trivial)
				x0 = (ch0 == 0 ? 0 : n0);
			else
				x0++;
			maxX0 = (semGeminadasP[p] ? maxDias0 : 2 * maxDias0);
			if (x0 > maxX0)
				return true;
			maxX0 = Math.min(maxX0, n0);
			maxX0 = Math.min(maxX0, ch0 - Sp0);

			minX0 = (semGeminadasP[p] ? n0 - maxDias1 : n0 - 2 * maxDias1);
			minX0 = Math.max(minX0, Sp2 + n0 - ch1);
			x0 = Math.max(x0, minX0);
			return x0 > maxX0;
		}
		}
		return true;
	}

	// Atualiza as aulas acumuladas do Prof. nas duas partes depois de aceito x0 (Caso 2).
	static public void atualiza2() {
		int aux;
		if (case_[i] == 2) {
			a = Aa[i];
			nClasses = A[i] - x0;
			aux = apontP[i];
			Sp2_[aux] = Sp2_[aux - 1] + x0;
			Sp3_[aux] = Sp3_[aux - 1] + nClasses;
		}
	}

	// Conta, por busca exaustiva, as distribuições possíveis do Prof. p (cada solução completa conta uma) e devolve a quantidade.
	static public int principal_0() {
		int minimo, cont_0;
		cont_0 = 0;
		minimo = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		do {
			if (!maximo_()) {
				x_[i] = x0;
				atualiza2();
				i++;
				// Solução completa: conta e continua a procura (volta para a última unidade).
				if (i > n2) {
					i = n2;
					cont_0++;
				}
			} else {
				// Fim da busca: não há mais soluções.
				if (i == 1)
					return cont_0;
				x_[i] = minimo;
				i--;
			}
		} while (true);
	}

}
