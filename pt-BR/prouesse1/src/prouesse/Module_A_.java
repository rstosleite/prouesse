package prouesse;

// Classe Module_A_ - conta, para um Prof., o número de possibilidades de distribuição das suas aulas entre o dia 0 e os demais dias
// (Seções 4.6.2 e 4.7 do livro, recurso de ordenação dos Profs.). É utilizada por Module_1B.
//
// Método: o mesmo esquema de busca dos outros módulos (Capítulo 3, Algoritmo 1, gerador = -1), mas sem parar na primeira solução:
// cada vez que todas as unidades são fechadas a solução é contada e a busca continua (ver principal_0), de modo que o
// resultado é o número total de possibilidades. Todos os membros são estáticos; as unidades do Prof. p são:
//   casos 10 (5 unidades): o Prof. trabalha (1) ou não (0) em cada um dos 5 dias;
//   caso 100: ch0 = total de aulas do Prof. no dia 0 (o restante, ch1, ocorre nos dias 1 a 4);
//   caso 11 : validação, que também elimina repetições do par (ch0, maxDias0);
//   caso 2  : para cada turma do Prof., quantas das suas aulas ocorrem no dia 0.
// Obs.: o contador usa os arranjos estáticos de unidades (case_, Aa, A, x_ ...) compartilhados; Module_1B só reconstroi as suas
// próprias unidades depois de contar as possibilidades de todos os Profs.
public class Module_A_ extends FaseA1_Super {
	// Campos privados: p = Prof. em análise; n2 = número de unidades; maxDias0/maxDias1 = dias de trabalho no dia 0 e nos dias 1 a 4;
	// nhorarios_P0/nhorarios_P1 = máximo de aulas no dia 0 e nos dias 1 a 4; ch0/ch1 = aulas do Prof. no dia 0 e nos dias 1 a 4;
	// ns_p_ = carga horária semanal do Prof.; Sp2_/Sp3_/apontP = totais acumulados de aulas nos dois grupos de dias e ponteiros;
	// possibP/nPossibP = pares (ch0, maxDias0) já contados (para não contar duas vezes); teste2 = indicador temporário.
	static private int d1, n2, p, maxDias0, maxDias1, Sp2_[], Sp3_[], apontP[], nhorarios_P0, nhorarios_P1, ch0, ch1,
			ns_p_, nPossibP, possibP[][];
	static private boolean teste2;

	public Module_A_() {
	}

	// Prepara a contagem para o Prof. p0: calcula a carga horária semanal e monta as unidades (5 do caso 10, uma do caso 100,
	// uma do caso 11 e uma do caso 2 por turma) e os ponteiros das variáveis de controle.
	static public void preliminares(int p0) {
		// Arranjos pequenos, suficientes para um único Prof. (as dimensões admitem até 30 turmas).
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
		// Unidades do caso 10: uma para cada dia da semana (índices 1 a 5 correspondem aos dias 0 a 4).
		for (d1 = 0; d1 <= 4; d1++) {
			k++;
			case_[k] = 10;
		}
		k++;
		// Unidade do caso 100 (aulas do Prof. no dia 0), seguida da unidade do caso 11 (validação).
		case_[k] = 100;
		k++;
		case_[k] = 11;
		for (j = 1; j <= nTurmasP[p]; j++) {
			k++;
			Aa[k] = lista[p][j];
			j_[k] = j;
			A[k] = ns[p][j];
			// Unidades do caso 2: uma por turma do Prof. (A = carga horária semanal na turma).
			case_[k] = 2;
		}
		n2 = k;

		// Ponteiros apontP das unidades do caso 2 nos arranjos Sp2_/Sp3_ (a posição 0 vale 0: totais acumulados antes da 1a turma).
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

	// Equivale ao método 'sucessor': procura o menor x0 > x_[i] que cumpre as restrições da unidade i.
	// Retorna false se foi bem-sucedido (valor em x0) e true se não há sucessor.
	static public boolean maximo_() {
		int aux0, aux10, aux11, minX0, maxX0, nDias, soma0;
		boolean case_Trivial, teste;
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 10 (dia i-1): x0 = 0 ou 1 (não trabalha / trabalha). Calcula nDias = dias de trabalho já decididos e soma0 = soma dos
		// máximos de aulas dos dias já escolhidos como de trabalho mais os de todos os dias ainda por decidir.
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
				// Rejeita x0 > 1 e o excesso de dias de trabalho; aceita o valor se a capacidade soma0 (mais o dia i-1, se trabalhado) cobre a carga
				//   horária.
				if (x0 > 1 || nDias + x0 > Wp0[p])
					return true;
				teste = soma0 + (x0 == 0 ? 0 : Mp[p][i - 1]) >= ns_p_;
				if (teste)
					return false;
			}
		}

		// Caso 100: x0 = ch0 = total de aulas no dia 0. Capacidade do dia 0 (nhorarios_P0) e dos dias 1 a 4 (nhorarios_P1) dadas pelos dias de
		// trabalho escolhidos. Mínimo: o que não cabe nos dias 1 a 4; máximo: o que cabe no dia 0 (e a carga horária).
		case 100: {
			nhorarios_P0 = x_[1] * Mp[p][0];
			nhorarios_P1 = x_[2] * Mp[p][1] + x_[3] * Mp[p][2] + x_[4] * Mp[p][3] + x_[5] * Mp[p][4];
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

		// Caso 11: validação executada uma vez por combinação dos casos anteriores. maxDias0 = o Prof. trabalha no dia 0?; maxDias1 = restante
		//   dos dias.
		// Se o par (ch0, maxDias0) já foi considerado, retorna true (não conta duplicata); senão o registra em possibP.
		case 11: {
			if (x0 > -1)
				return true;
			maxDias0 = x_[1];
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
			// Estimativa do máximo de aulas no dia 0 e nos dias 1 a 4 (1 ou 2 aulas por dia e turma); falha se, limitado por ch0 e ch1, não cobre
			//   a carga horária.
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

		// Caso 2: x0 = aulas da turma no dia 0. Limites: superior = min(2*dias de trabalho no dia 0 (ou 1), aulas da turma, ch0 - total já
		//   atribuído ao Prof. no dia 0);
		// inferior = o que não cabe nos dias 1 a 4 (análogo ao Caso 2 do Módulo 2, Seção 4.3.5, com 'ch0' e 'ch1' no lugar de N_0p e N_1p).
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

	// Atualiza os totais acumulados (Sp2_ no dia 0 e Sp3_ nos demais dias) depois de uma unidade do caso 2.
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

	// Percorre todas as possibilidades pela busca com recuo (Algoritmo 1) e retorna quantas foram encontradas (cont_0).
	// Ao fechar a última unidade a solução é contada e a busca simplesmente continua a partir dela (i = n2).
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
				if (i > n2) {
					i = n2;
					cont_0++;
				}
			} else {
				if (i == 1)
					return cont_0;
				x_[i] = minimo;
				i--;
			}
		} while (true);
	}

}
