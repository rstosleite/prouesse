package prouesse;

// Classe Module_A - fornece subsídios para a ordenação dos Profs. (Seção 4.6.2 do livro): conta, para um Prof., o número de
// possibilidades de distribuição das suas aulas restantes entre os pares de dias (d0,d1) e (d2,d3). É usada por Module_2A e Module_2B.
//
// Funciona como Module_A_, mas aplicado depois do Módulo 1: só as aulas dos quatro dias restantes (dias 1 a 4) são consideradas,
// isto e, ns - dd[..][0]. O dia 0 já está decidido e é fixado como 'sem unidade' (x_[1] = 0); a busca é feita sobre os dias 2 a 5
// (índices 2 a 5 de x_, que correspondem aos dias 1 a 4). Unidades: casos 10 (dias de trabalho), 100 (aulas no 1o par), 11 (validação)
//   e 2.
public class Module_A extends FaseA1_Super {
	// Campos privados, com o mesmo significado de Module_A_ (ch0/ch1 passam a ser as aulas do 1o e do 2o par de dias; Wp01 = máximo de
	//   dias de trabalho nos 4 dias).
	static private int d1, n2, p, maxDias0, maxDias1, Sp2_[], Sp3_[], apontP[], nhorarios_P0, nhorarios_P1, ch0, ch1,
			ns_p_, nPossibP, possibP[][], Wp01;
	static private boolean teste2;

	public Module_A() {
	}

	// Prepara a contagem para o Prof. p0, com Wp0_ = W_1p (máximo de dias de trabalho nos 4 dias). A carga horária considerada e
	// a do período dos 4 dias (ns menos as aulas do dia 0); as unidades são as mesmas de Module_A_.
	static public void preliminares(int p0, int Wp0_) {
		Sp2_ = new int[42];
		Sp3_ = new int[42];
		apontP = new int[42];
		possibP = new int[45][2];
		p = p0;
		Wp01 = Wp0_;
		ns_p_ = 0;
		for (j = 1; j <= nTurmasP[p]; j++)
			// Carga horária restante (semanal menos a do dia 0, decidida pelo Módulo 1).
			ns_p_ += ns[p][j] - dd[p][j][0];
		nPossibP = 0;
		k = 0;
		for (d1 = 0; d1 <= 4; d1++) {
			k++;
			case_[k] = 10;
		}
		k++;
		case_[k] = 100;
		k++;
		case_[k] = 11;
		for (j = 1; j <= nTurmasP[p]; j++) {
			k++;
			Aa[k] = lista[p][j];
			j_[k] = j;
			// Aulas da turma nos 4 dias (A) = carga horária da turma menos as do dia 0.
			A[k] = ns[p][j] - dd[p][j][0];
			case_[k] = 2;
		}
		n2 = k;

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

	// Equivale ao método 'sucessor' (retorna false se há sucessor, em x0; true caso contrário).
	static public boolean maximo_() {
		int aux0, aux10, aux11, minX0, maxX0, nDias, soma0;
		boolean case_Trivial, teste;
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 10 (uma unidade por dia): x0 = 0 ou 1 (o Prof. não trabalha / trabalha no dia correspondente), respeitando Wp01 e a capacidade
		//   para a carga horária.
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
				if (x0 > 1 || nDias + x0 > Wp01)
					return true;
				teste = soma0 + (x0 == 0 ? 0 : Mp[p][i - 1]) >= ns_p_;
				if (teste)
					return false;
			}
		}

		// Caso 100: x0 = ch0 = total de aulas no 1o par de dias (d0,d1) = dias 1 e 2; nhorarios_P1 = capacidade do 2o par (dias 3 e 4).
		// Mínimo: o que não cabe no 2o par; máximo: o que cabe no 1o par.
		case 100: {
			nhorarios_P0 = x_[2] * Mp[p][1] + x_[3] * Mp[p][2];
			nhorarios_P1 = x_[4] * Mp[p][3] + x_[5] * Mp[p][4];
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

		// Caso 11: validação e eliminação de duplicatas. maxDias0 = dias de trabalho no 1o par (x_[2] + x_[3]); maxDias1 = restante.
		// Descarta pares (ch0, maxDias0) já contados; verifica se as aulas das turmas, limitadas por 1 ou 2 por dia, cobrem a carga horária.
		case 11: {
			if (x0 > -1)
				return true;
			maxDias0 = x_[2] + x_[3];
			maxDias1 = Wp01 - maxDias0;
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
				aux10 += Math.min((semGeminadasP[p] ? maxDias0 : 2 * maxDias0), ns[p][j] - dd[p][j][0]);
				aux11 += Math.min((semGeminadasP[p] ? maxDias1 : 2 * maxDias1), ns[p][j] - dd[p][j][0]);
			}
			x0 = 0;
			return Math.min(aux10, ch0) + Math.min(aux11, ch1) < ns_p_;
		}

		// Caso 2: x0 = aulas da turma no 1o par de dias; mesmas restrições do Caso 2 do Módulo 2 (Seção 4.3.5), usando ch0 e ch1 como limites
		//   de aulas por par.
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

	// Atualiza os totais acumulados do Prof. nos dois pares de dias depois de uma unidade do caso 2.
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

	// Conta as possibilidades pela busca com recuo, sem parar na primeira (como em Module_A_). Retorna 0 se o Prof. não tem aulas nos 4
	//   dias.
	// x_[1] = 0 fixa o dia 0 como já decidido; a busca começa na unidade 2.
	static public int principal_0() {
		int minimo, cont_0;
		if (ns_p_ == 0)
			return 0;
		cont_0 = 0;
		minimo = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		x_[1] = 0;
		i = 2;
		do {
			if (!maximo_()) {
				x_[i] = x0;
				atualiza2();
				i++;
				if (i > n2) {
					i = n2;
					cont_0++;
				}
			} else {
				if (i == 2)
					return cont_0;
				x_[i] = minimo;
				i--;
			}
		} while (true);
	}

}
