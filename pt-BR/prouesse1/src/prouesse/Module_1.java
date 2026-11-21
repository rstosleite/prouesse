package prouesse;

import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

// Classe Module_1 - implementa o Módulo 1 (Fase 1.1; Seções 4.1.1 e 4.7.1 do livro), na versão descrita no livro.
// Foi substituida por Module_1B, mais eficiente (esta classe permanece como referência).
//
// Finalidade: decidir quantas aulas de cada Prof. e turma ocorrerão no primeiro dia (dia 0, o dia mais difícil depois da
// ordenação dos dias, Seção 4.6.1). Resultado em dd[p][j][0]. Unidades do Módulo 1 (Seção 4.7.1):
//   caso 10: 5 unidades por Prof. (uma por dia); x = 1 se o dia e dia de trabalho do Prof. e 0 caso contrário;
//   caso 11: unidade única após as do caso 10; atualiza as variáveis de ligação (maxDias0/1, nHorarios_P0/1) de todos os Profs.;
//   caso 1 : uma por Prof.; validação dos valores de unidades anteriores;
//   caso 2 : uma por (Prof., turma); número de aulas da turma no dia 0 (as demais ficam nos outros 4 dias);
//   caso 5 : uma por Prof., após as suas unidades do caso 2; apenas atualiza variáveis de controle.
// Os Profs. são tratados em ordem crescente de Mp[p][0] (máximo de aulas no dia 0).
// Atenção: esta classe usa variáveis (somaMax, somaMax2, Sa_2, Sa_3, Sp2_, Sp3_) que não aparecem declaradas nem nela própria
// nem nas superclasses disponibilizadas (FaseA1_Super e Probl); aparentemente ela não compila isoladamente com estes arquivos.
public class Module_1 extends FaseA1_Super {
	// Campo declarado mas não utilizado.
	private boolean conjTurmas[];

	// Construtor: apenas chama o construtor da superclasse FaseA1_Super.
	public Module_1() {
		super();
	}

	// Ordenação por seleção, em ordem crescente, de amplitude[] (posições first a nElementos-1), permutando junto d2_[].
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

	// Índice do menor elemento de amplitude[] entre as posições i e nElementos-1.
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

	// Prepara o Módulo 1: ordena os dias, ordena os Profs. e define as unidades (case_, Ap, Aa, j_, A) e ponteiros. Retorna o número de
	//   unidades.
	public int preliminares() {
		int aux0, aux1, p1, d1, d;
		float totalPossib_0;
		int arranjo[];
		arranjo = new int[5];
		// min0[p] = número mínimo de dias de trabalho de p (calculado abaixo). Aviso ao usuário sobre a demora da execução.
		min0 = new int[Qp + 1];
		JOptionPane.showMessageDialog(null, "Esta ação poderá levar alguns minutos", "Resultado",
				JOptionPane.INFORMATION_MESSAGE);
		// Zera registradores de progresso e as somas de oferta por dia (somaMax) e por dia e turma (somaMax2).
		pUltimo1 = 0;
		pUltimo2 = 0;
		pUltimo3 = 0;
		for (k = 0; k <= 4; k++) {
			somaMax[k] = 0;
			for (a = 1; a <= Qa; a++)
				somaMax2[k][a] = 0;
		}
		for (p = 1; p <= Qp; p++)
			// Todos os Profs. são mantidos nesta fase.
			mantemP[p] = true;
		for (k = 0; k <= 4; k++) {
			aux0 = 0;
			for (p1 = 1; p1 <= Qp; p1++) {
				if (mantemP[p1])
					aux0 += Mp[p1][k];
			}
			d2_[k] = k;
			// 1) Soma, por dia, dos máximos de aulas dos Profs.;  2) ordena os dias do menor para o maior (dias mais difíceis primeiro) ...
			amplitude[k] = aux0;
			System.out.println("Soma dos MaxP[*][] para " + (k + 2) + "a-feira: " + aux0);
		}
		ordena2(0, 5);
		for (d1 = 0; d1 <= 4; d1++) {
			k = 0;
			while (d2_[k] != d1)
				k++;
			// ... e guarda em destino0 a nova posição de cada dia.
			destino0[d1] = k;
		}
		for (p1 = 1; p1 <= Qp; p1++) {
			for (d1 = 0; d1 <= 4; d1++)
				aux_[d1] = Mp[p1][d1];
			for (d1 = 0; d1 <= 4; d1++)
				// 3) Permuta as colunas de Mp dos Profs. conforme a nova ordem dos dias.
				Mp[p1][destino0[d1]] = aux_[d1];
		}

		for (p = 1; p <= Qp; p++) {
			// 4) Por Prof.: carga horária semanal ns_p_ e número mínimo de dias de trabalho min0 (somando os maiores máximos diários até cobrir a
			//   carga).
			ns_p_[p] = 0;
			for (j = 1; j <= nTurmasP[p]; j++)
				ns_p_[p] += ns[p][j];

			for (d = 0; d <= 4; d++)
				arranjo[d] = Mp[p][d];
			Arrays.sort(arranjo);
			aux0 = 0;
			min0[p] = 0;
			for (d = 4; d >= 0; d--) {
				aux0 += arranjo[d];
				min0[p]++;
				if (aux0 >= ns_p_[p])
					break;
			}
		}
		for (p = 1; p <= Qp; p++) {
			d2_[p] = p;
			// 5) Ordena os Profs. em ordem crescente do máximo de aulas no dia 0 (d2_ = ordem de tratamento).
			amplitude[p] = (float) Mp[p][0];
		}
		ordena2(1, Qp + 1);
		System.out.println();
		i1 = 1;
		// Zera as aulas já alocadas por turma (Sa_2, Sa_3) para o primeiro Prof. da ordem.
		while (!mantemP[d2_[i1]])
			i1++;
		for (a = 1; a <= Qa; a++) {
			Sa_2[d2_[i1]][a] = 0;
			Sa_3[d2_[i1]][a] = 0;
		}
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			for (d1 = 0; d1 <= 4; d1++) {
				k++;
				j_[k] = d1;
				Ap[k] = p;
				A[k] = i1;
				// 6) Unidades do caso 10: 5 por Prof. (j_ = dia, A = posição do Prof. na ordem).
				case_[k] = 10;
			}
		}
		k++;
		// Unidade do caso 11.
		case_[k] = 11;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			k++;
			Ap[k] = p;
			A[k] = ns_p_[p];
			// Por Prof.: uma unidade do caso 1, ...
			case_[k] = 1;
			for (j = 1; j <= nTurmasP[p]; j++) {
				k++;
				Ap[k] = p;
				Aa[k] = lista[p][j];
				j_[k] = j;
				A[k] = ns[p][j];
				// ... uma do caso 2 por turma (A = carga horária na turma) ...
				case_[k] = 2;
			}
			k++;
			Ap[k] = p;
			// ... e uma do caso 5.
			case_[k] = 5;
		}
		// 7) Ponteiros apontP para as unidades do caso 2 nos arranjos de totais acumulados Sp2_/Sp3_ (uma posição zerada entre Profs.).
		n2 = k;
		Ap[0] = 0;

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

	// Equivale ao 'sucessor': procura o menor x0 > x_[i] que cumpre as restrições do caso; retorna false se foi bem-sucedido, true caso
	//   contrário.
	public boolean maximo() {
		int aux0, case_0, aux2, i0, aux10, aux11, minX0, maxX0, nDias, soma, soma0;
		boolean case_Trivial, teste;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 10: x0 = 1 se o dia j é de trabalho do Prof. e 0 se não. Dia sem disponibilidade (Mp == 0) só admite x0 = 0.
		// Se os dias ainda disponíveis mal bastam para o mínimo min0, o dia atual deve ser de trabalho (começa em x0 = 0 para forçar 1).
		// Calcula: nDias = dias de trabalho já escolhidos, soma0 = capacidade (escolhidos + ainda não decididos), aux = oferta máxima dos
		//   Profs. seguintes no dia.
		case 10: {
			if (x0 >= 1)
				return true;
			j = j_[i];
			if (Mp[p][j] == 0) {
				if (x0 == 0)
					return true;
				x0 = 0;
				return false;
			}
			if (x0 == -1) {
				soma0 = 0;
				for (k = i - j; k <= i - 1; k++)
					soma0 += x_[k];
				if (soma0 + 4 - j < min0[p])
					x0 = 0;
			}
			nDias = 0;
			soma0 = 0;
			for (k = 0; k <= j - 1; k++) {
				aux0 = x_[i - j + k];
				nDias += aux0;
				soma0 += (aux0 == 0 ? 0 : Mp[p][k]);
			}
			for (k = j + 1; k <= 4; k++)
				soma0 += Mp[p][k];
			i0 = A[i];
			if (i0 > 0.75 * Qp) {
				aux = 0;
				for (k = i0 + 1; k <= Qp; k++)
					aux += Mp[d2_[k]][j];
			} else
				aux = (Qp - i0) * Ma;
			// Para cada candidato: respeita Wp0, a capacidade total cobre a carga horária, a oferta acumulada do dia (somaMax) mais o que este e
			//   os
			// demais Profs. podem ofertar cobre Ma*Qa aulas, e, para cada turma do Prof., a oferta acumulada somada cobre Ma.
			while (true) {
				x0++;
				if (x0 > 1 || nDias + x0 > Wp0[p])
					return true;
				teste = soma0 + (x0 == 0 ? 0 : Mp[p][j]) >= ns_p_[p];
				if (teste) {
					aux2 = (semGeminadasP[p] ? x0 : 2 * x0) + (Qp - i0) * 2;
					teste = somaMax[(i0 - 1) * 5 + j] + x0 * Mp[p][j] + aux >= Ma * Qa;
					for (k = 1; k <= nTurmasP[p]; k++) {
						teste = teste && somaMax2[(i0 - 1) * 5 + j][lista[p][k]] + aux2 >= Ma;
						if (!teste)
							break;
					}
				}
				if (teste)
					return false;
			}
		}

		// Caso 11: sem restrições; só garante uma única execução (nova chamada retorna true).
		case 11: {
			if (x0 > -1)
				return true;
			x0 = 0;
			return false;
		}

		// Caso 1: validação do Prof.: o máximo de aulas que ele pode dar no dia 0 e nos dias 1 a 4 (limitado pelo espaço livre das turmas)
		//   deve cobrir a carga horária.
		case 1: {
			if (x0 > -1)
				return true;
			x0 = 0;
			aux10 = 0;
			aux11 = 0;
			aux0 = nTurmasP[p];
			for (j = 1; j <= aux0; j++) {
				aux10 += Math.min(Math.min(Ma - Sa_2[p][lista[p][j]], (semGeminadasP[p] ? 1 : 2)), ns[p][j]);
				aux11 += Math.min(Math.min(4 * Ma - Sa_3[p][lista[p][j]], (semGeminadasP[p] ? 4 : 8)), ns[p][j]);
			}
			return Math.min(aux10, nHorarios_P0[p]) + Math.min(aux11, nHorarios_P1[p]) < ns_p_[p];
		}

		// Caso 2: x0 = número de aulas da turma no dia 0, com as restrições de máximo/mínimo explicadas para o Caso 2 do Módulo 2 (Seção
		//   4.3.5),
		// usando nHorarios_P0/nHorarios_P1 como limites de aulas do Prof. no dia 0 e nos dias 1 a 4 e a capacidade das turmas (Ma e 4*Ma).
		case 2: {
			if (nHorarios_P0[p] == x0)
				return true;
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
			maxX0 = (semGeminadasP[p] ? maxDias0[p] : 2 * maxDias0[p]);
			if (x0 > maxX0)
				return true;
			maxX0 = Math.min(maxX0, n0);
			maxX0 = Math.min(maxX0, nHorarios_P0[p] - Sp0);
			maxX0 = Math.min(maxX0, Ma - Sa0);

			minX0 = (semGeminadasP[p] ? n0 - maxDias1[p] : n0 - 2 * maxDias1[p]);
			minX0 = Math.max(minX0, Sp2 + n0 - nHorarios_P1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - 4 * Ma);
			x0 = Math.max(x0, minX0);
			return x0 > maxX0;
		}

		// Caso 5: primeira passagem sem efeito; numa nova chamada retorna true (recuo).
		case 5: {
			if (x0 < 0) {
				x0 = 0;
				return false;
			}
			i1 = i - 1;
			return true;
		}

		// Caso 3 sem implementação.
		case 3: {
		}
		}
		return true;
	}

	// Atualiza as variáveis de controle e de ligação após uma unidade receber o valor x0.
	public void atualiza2_() {
		int i0, p2, aux, x1;
		switch (case_[i]) {
		case 10: {
			// Caso 10, ao fechar o 5o dia do Prof.: acumula a oferta (somaMax, somaMax2) incluindo este Prof.; somaMax2 conta aulas por turma (1
			//   ou 2 por dia de trabalho).
			if (j_[i] == 4) {
				p = Ap[i];
				i0 = A[i];
				for (j = 0; j <= 4; j++) {
					x0 = x_[i - 4 + j];
					aux = (semGeminadasP[p] ? x0 : 2 * x0);
					somaMax[i0 * 5 + j] = somaMax[(i0 - 1) * 5 + j] + (x0 == 0 ? 0 : Mp[p][j]);
					for (a = 1; a <= Qa; a++)
						somaMax2[i0 * 5 + j][a] = somaMax2[(i0 - 1) * 5 + j][a];
					if (aux > 0) {
						for (k = 1; k <= nTurmasP[p]; k++)
							somaMax2[i0 * 5 + j][lista[p][k]] += aux;
					}
				}
			}
			break;
		}

		case 11: {
			// Caso 11: para todos os Profs., calcula dias de trabalho (maxDias0/1) e máximos de aulas (nHorarios_P0/1) no dia 0 e nos demais,
			//   conforme os dias de trabalho decididos.
			for (i0 = 5; i0 <= 5 * Qp; i0 += 5) {
				p = Ap[i0];
				maxDias0[p] = x_[i0 - 4];
				maxDias1[p] = x_[i0 - 3] + x_[i0 - 2] + x_[i0 - 1] + x_[i0];
				nHorarios_P0[p] = x_[i0 - 4] * Mp[p][0];
				aux = 0;
				for (j = 1; j <= 4; j++)
					aux += (x_[i0 - 4 + j] == 0 ? 0 : Mp[p][j]);
				nHorarios_P1[p] = aux;
			}
			break;
		}

		case 2: {
			p = Ap[i];
			a = Aa[i];
			nClasses = A[i] - x0;
			aux = apontP[i];
			// Caso 2: acumula as aulas do Prof. no dia 0 (Sp2_) e nos dias 1 a 4 (Sp3_).
			Sp2_[aux] = Sp2_[aux - 1] + x0;
			Sp3_[aux] = Sp3_[aux - 1] + nClasses;
			break;
		}

		case 5: {
			// Caso 5: copia para o próximo Prof. as aulas já alocadas em cada turma (Sa_2/Sa_3) e soma as aulas do Prof. atual.
			if (i + 1 < n2) {
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

	// Busca do Módulo 1 (Algoritmo 1 do Capítulo 3, com recuo adaptado; gerador = minimo = -1). Retorna true se também o Módulo 2 tiver
	//   sucesso.
	public boolean principal1(int minimo, int n) {
		int k, imax, p1, p0, iMaxI_;
		boolean teste;
		String entrada;
		Object[] opcoes = { "Sim", "Não" };
		iMaxI_ = 1;
		imax = 1;
		// Inicializa as variáveis de decisão; solicita o tempo máximo de execução (minutos), verificado a cada 50000 iterações.
		for (i = 1; i <= n; i++)
			x_[i] = minimo;
		i = 1;
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
			// Sucesso na unidade i: guarda x0 e atualiza controle (após o 5o dia do caso 10, no caso 11 e nos casos 2 e 5).
			if (!maximo()) {
				x_[i] = x0;
				if ((case_[i] != 10 && case_[i] != 1) || (case_[i] == 10 && j_[i] == 4))
					atualiza2_();
				i++;
				if (i > n) {
					// Ao fechar a última unidade: grava dd[..][0] e chama o Módulo 2 (preliminares e principal_V2); o resultado é devolvido.
					atualiza4(n);
					g2_I_W_3.preliminares();
					teste = g2_I_W_3.principal_V2();
					return teste;
				}

				if (i > imax) {
					imax = i;
					if (case_[i] != 3)
						pUltimo1 = Ap[i];
					System.out.println(i + "/" + n + " case_ " + case_[i]);
				}
				// iMaxI_ registra a unidade mais avancada alcançada (caso 11 ou casos menores que 10), usada para decidir o ponto de recuo.
				if (case_[i] == 11)
					iMaxI_ = i;
				if (case_[i] < 10 && iMaxI_ < i)
					iMaxI_ = i;
			} else {
				// Sem sucessor: unidades do caso 10 recuam uma posição; nos demais casos, recua-se para a unidade anterior (ou, no caso 11,
				// para a última unidade do Prof. da unidade mais avancada alcançada), restaurando x_ com o gerador até la.
				if (i == 1)
					return false;
				if (case_[i] != 10) {
					if (case_[i] == 11 && case_[iMaxI_] != 11) {
						p0 = i - 1;
						while (Ap[p0] != Ap[iMaxI_])
							p0--;
					} else
						p0 = i - 1;

					if (case_[p0] == 5)
						p0--;
					for (k = i; k > p0; k--)
						x_[k] = minimo;
					i = p0;
				} else {
					x_[i] = minimo;
					i--;
					if (i == 0)
						return false;
				}
			}
		} while (true);
	}

	// Grava em dd[p][j][0] o número de aulas do dia 0 das unidades do caso 2 (zera antes todas as posições).
	public void atualiza4(int n) {
		int i, p1, j1;
		for (p1 = 1; p1 <= Qp; p1++) {
			for (j1 = 1; j1 <= nTurmasP[p1]; j1++)
				dd[p1][j1][0] = 0;
		}
		for (i = 5 * Qp; i <= n; i++) {
			if (case_[i] == 2)
				dd[Ap[i]][j_[i]][0] = x_[i];
		}
	}

}
