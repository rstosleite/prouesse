package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Classe Module_4 - implementa o Módulo 4 (Fases 2 e 3; Seções 4.1.4 e 4.5 do livro).
//
// Ponto de partida: uma distribuição completa das aulas em dd (obtida pelos Módulos 1 e 2). Este módulo considera três dias (d_0, d_1,
//   d_2),
// redistribui as aulas de cada Prof. e turma entre eles (sem mudar os totais da semana nem os dias fora do conjunto) e, para cada nova
// distribuição, tenta alocar as aulas nos horários, um dia por vez, pelo Módulo 5. Se o Módulo 5 falha, a busca recua até a unidade
//   que
// pode corrigir o problema. Fase 2: dias 0, 1 e 2 (preliminares(1)), com alocação em horários apenas dos dias que não são o dia 2;
// Fase 3: dias 2, 3 e 4 (preliminares(2)), com alocação dos três dias. (Dias na ordem original; ver Prouesse5D_X.)
//
// Dos três dias, d_2 é o de menor soma de máximos de aulas (o mais difícil) e (d_0,d_1) forma o par restante. Unidades (ver
//   preliminares):
//   caso 1 : por Prof., número de dias de trabalho em (d_0,d_1) (se complex_p0 = true: 0, 1, 2 ou 3 conforme os dias; Seção 4.5.1);
//   caso 2 : por (Prof., turma), x = aulas no par (d_0,d_1); y = A - x aulas em d_2 (Seção 4.5.3);
//   caso 11: validação: com as aulas destacadas para d_2, deve haver pelo menos Qa Profs. disponíveis em cada horário de d_2;
//   caso 3 : por Prof., se trabalha no dia d_0 (Seção 4.5.2);
//   caso 4 : por (Prof., turma), x = aulas em d_0 e q2_ - x aulas em d_1 (idêntico ao caso 4 do Módulo 3, Seção 4.4.1);
//   caso 5 : última unidade; verifica a disponibilidade em cada horário dos dias d_0 e d_1 e chama o Módulo 5 para os dias a alocar.
// A busca é o Algoritmo 2 do Capítulo 3 (principal2); a dependência entre unidades está em dependencia() (Seção 4.5.4).
public class Module_4 extends FaseB2_Super {
	// exec = 1 (Fase 2) ou 2 (Fase 3);  nDiasWork1 = dias de trabalho do Prof. no par (auxiliar de determinaAmplitude);
	// aux3 = índice da unidade do caso 11;  Dp, arranjo, nDias_, complex_p0, x_case_1 = variáveis dos casos 1 e 3 (D_p, complex_p,
	//   x_caso1);
	// g4_ = objeto do Módulo 5;  presenteP[p] = o Prof. tem aulas no dia em teste;  dias2[] = dias a alocar em horários (pelo Módulo 5);
	// horarios[] = ordem em que os horários do dia são examinados nas validações (primeiro, último, segundo, penúltimo ...);  y2[p][i] =
	//   cópias de x_ (ver principal2).
	static protected int exec, d1, nDiasWork1, aux3, Dp[], arranjo[];
	static public Module_5 g4_;
	static protected boolean teste1, teste2, case_Trivial, presenteP[], complex_p0[];
	static protected int iMin, dias2[], nDias_[], horarios[], x_case_1[];
	static protected short y2[][];
	ImageIcon icone;

	// Construtor: aloca os arranjos próprios do Módulo 4 e cria o objeto do Módulo 5.
	public Module_4() {
		presenteP = new boolean[Qp + 1];
		g4_ = new Module_5();
		dias2 = new int[4];
		y2 = new short[Qp + 1][x_.length];
		Dp = new int[Qp + 1];
		arranjo = new int[3];
		complex_p0 = new boolean[Qp + 1];
		x_case_1 = new int[Qp + 1];
	}

	// Ordena os Profs. (resultado em d2_) que serão tratados pelas unidades do Módulo 4. Profs. sem aulas nos três dias ficam no fim.
	// Os demais são escolhidos um a um, de forma gulosa: em cada posição fica o Prof. de menor valor de um critério que combina (a) o seu
	// número de possibilidades de distribuição (amplitude, calculada em preliminares), e (b) quanto ele compartilha turmas com os Profs.
	//   já
	// escolhidos (aux2, ponderado pelo número de turmas com aulas, q_0[p][0]).
	static public void ordena_profs2() {
		float aux1, aux2[], aux3, min1, max0, max0_, delta;
		boolean lista_[], teste;
		int i, indMin, k, p, p1, j, j1, a, cont;
		lista_ = new boolean[Qp + 1];
		aux2 = new float[Qp + 1];
		cont = 0;
		k = 0;
		for (i = 1; i <= Qp; i++) {
			if (q_0[i][0] > 0)
				cont++;
			else {
				d2_[Qp - k] = i;
				k++;
				lista_[i] = true;
			}
		}
		for (i = 1; i <= cont; i++) {
			if (i == cont) {
				p = 1;
				while (lista_[p])
					p++;
				d2_[i] = p;
				lista_[p] = true;
				continue;
			}
			max0 = (float) -10000.0;
			max0_ = (float) -10000.0;
			if (i > 1) {
				for (p = 1; p <= Qp; p++) {
					if (lista_[p] || q_0[p][0] <= 0)
						continue;
					aux2[p] = 0.0f;
					aux3 = 0.0f;
					for (k = 1; k <= i - 1; k++) {
						p1 = d2_[k];
						aux3 += 0.5 * (float) q_0[p1][0];
						delta = aux3 / (float) q_0[p1][0];
						aux3 += 0.5 * (float) q_0[p1][0];
						for (j = nTurmasP[p1]; j >= 1; j--) {
							a = lista[p1][j];
							teste = false;
							for (j1 = nTurmasP[p]; j1 >= 1; j1--) {
								if (a == lista[p][j1] && q_0[p][j1] > 0) {
									teste = true;
									break;
								}
							}
							if (teste)
								aux2[p] += delta;
						}
					}
					if (aux2[p] > 0.00001) {
						aux2[p] = (float) q_0[p][0] / aux2[p];
						if (aux2[p] > max0)
							max0 = aux2[p];
					} else
						aux2[p] = -1.0f;
					if (amplitude[p] > max0_)
						max0_ = amplitude[p];
				}
			}
			indMin = 0;
			min1 = 1000000000.0f;
			for (p = 1; p <= Qp; p++) {
				if (lista_[p] || q_0[p][0] <= 0)
					continue;
				if (i > 1) {
					if (aux2[p] < -0.001)
						aux1 = 1.0f + amplitude[p] / max0_;
					else
						aux1 = aux2[p] / max0 + amplitude[p] / max0_;
				} else
					aux1 = amplitude[p];
				if (aux1 < min1) {
					indMin = p;
					min1 = aux1;
				}
			}
			p = indMin;
			if (p == 0) {
				System.out.println("ERRO 049");
				System.exit(0);
			}
			d2_[i] = p;
			lista_[p] = true;
		}
	}

	// Prepara o Módulo 4 para a Fase 2 (x0 = 1) ou para a Fase 3 (x0 = 2): define os dias, as variáveis auxiliares de cada Prof., a ordem
	// dos Profs. e as unidades. Retorna n2, o número de unidades.
	public int preliminares(int x0) {
		int d;
		JOptionPane.showMessageDialog(null, "Esta ação poderá levar alguns minutos", "Resultado",
				JOptionPane.INFORMATION_MESSAGE);
		// nDias_ = número de dias de trabalho associado a cada valor de x do caso 1 (complex). Em horarios[], a ordem de exame dos horários
		//   para cada Ma (4, 5 ou 6).
		nDias_ = new int[] { 0, 1, 1, 2, 3 };
		if (Ma == 4)
			horarios = new int[] { 0, 1, 4, 2, 3 };
		if (Ma == 5)
			horarios = new int[] { 0, 1, 5, 2, 4, 3 };
		if (Ma == 6)
			horarios = new int[] { 0, 1, 6, 2, 5, 3, 4 };
		iMin = 0;
		exec = x0;
		// 1) Dias do conjunto: Fase 3 -> dias 2, 3 e 4; Fase 2 -> dias 0, 1 e 2 (dias[1..3]).
		if (exec == 2) {
			dias[1] = 2;
			dias[2] = 3;
			dias[3] = 4;
		} else {
			dias[1] = 0;
			dias[2] = 1;
			dias[3] = 2;
		}
		imax = 0;

		for (k = 1; k <= 3; k++) {
			// 2) Ordena os três dias pela soma dos máximos de aulas (ordena2): d_2 = dia de menor soma (mais difícil); d_0 e d_1 = os outros dois.
			//   dias[1..3] = d_0, d_1, d_2.
			amplitude[k] = 0;
			for (p = 1; p <= Qp; p++)
				amplitude[k] += Mp[p][dias[k]];
			d2_[k] = dias[k];
		}
		Module_1.ordena2(1, 4);
		System.out.println("d2_: " + d2_[1] + " " + d2_[2] + " " + d2_[3]);
		d_0 = d2_[2];
		d_1 = d2_[3];
		d_2 = d2_[1];
		dias[1] = d_0;
		dias[2] = d_1;
		dias[3] = d_2;
		// 3) dias2[] = dias cujas aulas serão alocadas em horários: Fase 2 -> os dias do conjunto exceto o dia 2; Fase 3 -> d_0, d_1 e, por
		//   último, d_2.
		if (exec == 1) {
			k = 1;
			if (d2_[1] != 2) {
				dias2[k] = d2_[1];
				k++;
			}
			if (d2_[2] != 2) {
				dias2[k] = d2_[2];
				k++;
			}
			if (d2_[3] != 2)
				dias2[k] = d2_[3];
		} else {
			k = 1;
			if (d2_[1] != d_2) {
				dias2[k] = d2_[1];
				k++;
			}
			if (d2_[2] != d_2) {
				dias2[k] = d2_[2];
				k++;
			}
			if (d2_[3] != d_2)
				dias2[k] = d2_[3];
			dias2[3] = d_2;
		}

		// 4) Variáveis auxiliares por Prof. p: q_0[p][j] = aulas da turma j nos três dias; q_0[p][0] = número de turmas com aulas; ns_p[p] =
		//   total de aulas
		// nos três dias; mantemP[p] = tem aulas nos três dias.
		aux2 = (exec == 1 ? 3 : 0);
		for (p = 1; p <= Qp; p++) {
			ns_p[p] = 0;

			q_0[p][0] = 0;
			for (j = 1; j <= nTurmasP[p]; j++) {
				q_0[p][j] = 0;
				for (d1 = 1; d1 <= 3; d1++)
					q_0[p][j] += dd[p][j][dias[d1]];
				ns_p[p] += q_0[p][j];
				if (q_0[p][j] > 0)
					q_0[p][0]++;
			}

			mantemP[p] = ns_p[p] > 0;
			// Wp1[p] inicialmente = número de dias de trabalho do Prof. fora do conjunto (nos dois dias restantes da semana, conforme a
			//   distribuição atual).
			Wp1[p] = 0;
			for (d1 = aux2; d1 <= aux2 + 1; d1++) {
				aux1 = 0;
				for (j = 1; j <= nTurmasP[p]; j++)
					aux1 += dd[p][j][d1];
				if (aux1 > 0)
					Wp1[p]++;
			}
			for (d = 0; d <= 2; d++)
				arranjo[d] = Mp[p][dias[d + 1]];
			// Dp[p] = D_p: mínimo de dias de trabalho nos três dias (soma dos maiores máximos até cobrir ns_p), elevado ao mínimo exigido por
			//   turma.
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
			// complex_p0: d_0 e d_1 são dias disponíveis com máximos de aulas diferentes.
			complex_p0[p] = Mp[p][d_0] > 0 && Mp[p][d_1] > 0 && Mp[p][d_0] != Mp[p][d_1];
		}

		for (p = 1; p <= Qp; p++) {
			// 5) amplitude[p] = (número de formas de dividir as aulas do Prof. entre os três dias, por determinaAmplitude) / ns_p[p]; depois
			//   ordena_profs2 define a ordem dos Profs.
			d2_[p] = p;
			Wp1[0] = Wp1[p];
			for (i = 1; i <= 2; i++) {
				case_[i] = i;
				x_[i] = -1;
			}
			aux2 = determinaAmplitude();
			if (ns_p[p] == 0)
				amplitude[p] = 0.0f;
			else
				amplitude[p] = (float) aux2 / (float) ns_p[p];
		}
		ordena_profs2();

		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			// 6) Monta as unidades na ordem d2_: por Prof., uma do caso 1, seguida de uma do caso 2 por turma (A = aulas da turma nos 3 dias); Wp1
			//   = W_1p = Wp0 menos os dias de trabalho fora do conjunto (no máximo 3).
			Wp1[p] = Wp0[p] - Wp1[p];
			if (Wp1[p] > 3)
				Wp1[p] = 3;
			k++;
			case_[k] = 1;
			Ap[k] = p;
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
		// Unidade do caso 11 (aux3 = seu índice).
		case_[k] = 11;
		aux3 = k;
		Ap[k] = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (!mantemP[p])
				continue;
			k++;
			// Por Prof., uma unidade do caso 3, seguida de uma do caso 4 por turma com aulas.
			case_[k] = 3;
			Ap[k] = p;
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
		k++;
		// Última unidade, do caso 5 (n2 = k).
		case_[k] = 5;
		n2 = k;

		// 7) Ponteiros das unidades do caso 4 para os totais acumulados Sp2_/Sp3_ (apontP) e das unidades do caso 2 para Sp0_/Sp1_ (apontP2),
		//   com posição zerada entre Profs.
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2 - 1; j++) {
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
		for (j = 1; j <= n2 - 1; j++) {
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
			// Ponteiros por turma (blocos de 16 posições) para as unidades do caso 2 (apontA, em Sa_0/Sa_1) e, depois, do caso 4 (apontA2, em
			//   Sa_4/Sa_5).
			Sa_0[auxA[j] - 1] = 0;
			Sa_1[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2 - 1; j++) {
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
		for (j = 1; j <= n2 - 1; j++) {
			if (case_[j] == 4) {
				apontA2[j] = auxA[Aa[j]];
				auxA[Aa[j]]++;
			}
		}
		return n2;
	}

	// Conta de quantas formas as aulas do Prof. p (ns_p[p]) podem ser divididas entre os três dias, respeitando os máximos diários e Wp0
	//   (busca com
	// duas unidades: x_[1] = aulas em d_0 e x_[2] = aulas em d_1; o restante vai para d_2). O resultado é usado como 'amplitude' do Prof.
	public int determinaAmplitude() {
		int cont, n;
		cont = 0;
		n = 2;
		i = 1;
		do {
			if (!maximoXi_0()) {
				x_[i] = x0;
				atualiza02_();
				i++;
				if (i > n) {
					cont++;
					i = n;
				}
			} else {
				if (i == 1)
					return cont;
				x_[i] = -1;
				i--;
			}
		} while (true);
	}

	// Após fechar a unidade 1, atualiza nDiasWork1 = dias de trabalho já ocupados incluindo d_0.
	public void atualiza02_() {
		switch (case_[i]) {
		case 1: {
			nDiasWork1 = (x0 == 0 ? Wp1[0] : Wp1[0] + 1);
			break;
		}
		}
	}

	// Sucessor das unidades de determinaAmplitude: caso 1, aulas em d_0; caso 2, aulas em d_1 (as restantes vão para d_2). Retorna true se
	//   não há sucessor.
	public boolean maximoXi_0() {
		x0 = x_[i];
		switch (case_[i]) {
		case 1: {
			do {
				x0 = (x0 == -1 ? 0 : x0 + 1);
				teste1 = x0 > Mp[p][d_0] || x0 > ns_p[p] || (x0 > 0 && Wp1[0] + 1 > Wp0[p]);
				teste2 = !teste1 && x0 + Mp[p][d_1] + Mp[p][d_2] >= ns_p[p];
			} while (!teste1 && !teste2);
			return teste1;
		}

		case 2: {
			do {
				x0 = (x0 == -1 ? 0 : x0 + 1);
				nClasses = ns_p[p] - x_[i - 1] - x0;
				teste1 = x0 > Mp[p][d_1] || nClasses < 0 || (x0 > 0 && nDiasWork1 + 1 > Wp0[p]);
				teste2 = !teste1 && nClasses <= Mp[p][d_2];
				if (teste2) {
					aux0 = (x0 == 0 ? nDiasWork1 : nDiasWork1 + 1);
					if (nClasses > 0)
						aux0++;
					teste2 = aux0 <= Wp0[p];
				}
			} while (!teste1 && !teste2);
			return teste1;
		}
		}
		return true;
	}

	// Equivale ao método 'sucessor': procura o menor x0 > x_[i] que cumpre as restrições do caso da unidade i (Seção 4.5).
	// Retorna false se foi bem-sucedido (valor em x0) e true caso contrário.
	public boolean maximo() {
		int aux2, i0, maxX0_, minX0, d, x0_, k_;
		boolean teste, initialized;
		p = Ap[i];
		x0 = x_[i];
		switch (case_[i]) {
		// Caso 1 (Seção 4.5.1): x0 = número de dias de trabalho em (d_0,d_1) (ou, se complex_p0, a codificação 0, 1, 2, 3).
		// Mínimo: Dp menos o dia d_2 (se disponível). Máximo: 2 (ou 3 na codificação), Wp1 e os dias disponíveis em (d_0,d_1).
		// Verificação adicional: a capacidade em d_2 (se sobrar dia de trabalho) mais a dos dias escolhidos deve cobrir a carga horária ns_p.
		case 1: {
			if (complex_p0[p]) {
				while (true) {
					aux2 = Dp[p] - (Mp[p][d_2] > 0 ? 1 : 0);
					do {
						x0++;
						x0_ = nDias_[x0];
					} while (x0_ < aux2);
					if (x0 > 3 || x0_ > Wp1[p] || x0_ > (Mp[p][d_0] > 0 ? 1 : 0) + (Mp[p][d_1] > 0 ? 1 : 0))
						return true;
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

		// Caso 3 (Seção 4.5.2): x0 = 1 se o Prof. trabalha em d_0 e 0 se não. Restrições: x0 <= wp0; 0 se Mp[p][d_0] == 0; mínimo para atingir
		//   os dias Dp
		// e wp0; com complex_p0 e um único dia de trabalho, o valor e consequência de x_case_1. Por fim, a carga horária ns_p deve ser
		//   cumprida
		// com as capacidades de d_0, d_1 e d_2.
		case 3: {
			while (true) {
				i0 = wp0[p];
				if (x0 > 1 || x0 > i0 || (x0 > 0 && Mp[p][d_0] == 0))
					return true;
				if (Mp[p][d_2] == 0 || i0 == Wp1[p])
					aux2 = 0;
				else
					aux2 = 1;
				x0 = Math.max(x0 + 1, Dp[p] - (Mp[p][d_1] > 0 ? 1 : 0) - aux2);
				if (x0 == 0 && (i0 == 2 || (i0 == 1 && Mp[p][d_1] == 0)))
					x0 = 1;
				if (x0 > 1 || x0 > i0 || (x0 == 1 && Mp[p][d_0] == 0))
					return true;
				aux2 = (Wp1[p] - i0 > 0 ? Mp[p][d_2] : 0);
				teste = !(x0 == 0 && i0 == 1 && Mp[p][d_1] == 0);
				teste = teste && ((!complex_p0[p]) || (i0 != 1) || (x_case_1[p] == 1 && x0 == 1)
						|| (x_case_1[p] == 2 && x0 == 0));
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

		// Caso 2 (Seção 4.5.3): x0 = aulas de (p, turma) no par (d_0,d_1); y = A[i] - x0 ficam em d_2. Restrições: a soma das aulas do Prof.
		//   no par não
		// ultrapassa Np0, e em d_2 não ultrapassa Np1; na turma, 2*Ma no par e Ma em d_2; no máximo (1 ou 2) aulas por dia e turma vezes os
		//   dias de trabalho.
		case 2: {
			// Não há sucessor se x0 já é o máximo possível.
			if (x0 == 2 * wp0[p] || (semGeminadasP[p] && x0 == wp0[p]))
				return true;
			// Caso trivial: o Prof. só tem capacidade no par ou só em d_2; há um único valor (0 ou todas as aulas), tentado uma vez. Se já
			//   inicializada, reaproveita maxX0[i].
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

			// Limite inferior: o que não cabe em d_2 (conforme dias de trabalho restantes, Np1 e Ma de capacidade da turma em d_2).
			aux0 = Math.min(Wp1[p] - wp0[p], 1);
			minX0 = (semGeminadasP[p] ? n0 - aux0 : n0 - 2 * aux0);
			minX0 = Math.max(minX0, Sp2 + n0 - Np1[p]);
			minX0 = Math.max(minX0, Sa2 + n0 - Ma);
			x0 = Math.max(x0, minX0);
			// Se x0 excede o que é possível sem usar um dos dias de trabalho do Prof. em d_2: se o Prof. já tem aulas em d_2 (Sp2 > 0) não há
			//   sucessor;
			// senão só resta colocar todas as aulas da turma no par (x0 = n0).
			aux0 = (semGeminadasP[p] ? Wp1[p] - 1 : 2 * Wp1[p] - 2);
			if (x0 > aux0) {
				if (Sp2 > 0)
					return true;
				if (x0 < n0)
					x0 = n0;
			}
			// Limite superior: dias de trabalho no par, A[i], capacidade restante do Prof. no par (Np0 - Sp0) e da turma (2*Ma - Sa0).
			maxX0_ = (semGeminadasP[p] ? wp0[p] : 2 * wp0[p]);
			maxX0_ = Math.min(maxX0_, n0);
			maxX0_ = Math.min(maxX0_, Np0[p] - Sp0);
			maxX0_ = Math.min(maxX0_, 2 * Ma - Sa0);
			maxX0[i] = maxX0_;
			return x0 > maxX0_;
		}

		// Caso 4 (Seção 4.5.4, idem Caso 4 do Módulo 3): x0 = aulas de (p, turma) em d_0; o restante de q2_ vai para d_1; mesmas restrições do
		//   Módulo 3 (com N_2p = Np2, N_3p = Np3, e Ma por dia).
		case 4: {
			n0 = q2_[p][j_[i]];
			if (n0 == 0) {
				if (x0 == -1) {
					x0 = 0;
					return false;
				} else
					return true;
			}
			if (x0 == 2 || (semGeminadasP[p] && x0 == 1))
				return true;
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

		// Caso 11 (validação, uma única execução): com as aulas destacadas para d_2 pelas unidades do caso 2, marca os Profs. com aulas em d_2
		// (presenteP) e verifica, em cada horário de d_2, se há pelo menos Qa Profs. presentes e disponíveis. Na Fase 2, se d_2 é o dia 2, a
		//   validação é dispensada.
		case 11: {
			if (x0 < 0) {
				x0 = 0;
				if (exec == 1 && d_2 == 2)
					return false;
				for (p = 1; p <= Qp; p++)
					presenteP[p] = false;
				for (i0 = 1; i0 < i; i0++) {
					p = Ap[i0];
					presenteP[p] = presenteP[p] || (case_[i0] == 2 && A[i0] - x_[i0] > 0);
				}
				teste2 = true;
				for (k = 1; k <= Ma; k++) {
					k_ = horarios[k];
					aux0 = 0;
					for (p = 1; p <= Qp; p++) {
						if (presenteP[p] && !horariosND[p][d_2][k_])
							aux0++;
					}
					teste2 = aux0 >= Qa;
					if (!teste2)
						break;
				}
				return !teste2;
			}
			return true;
		}

		// Caso 5 (última unidade): a distribuição dos três dias está completa. atualiza4() grava em dd.
		// 1) Validação: em cada horário dos dias d_0 e d_1 deve haver Qa Profs. presentes e disponíveis; se não, recua para a última unidade
		//   do caso 3.
		case 5: {
			atualiza4();
			d = 0;
			for (d1 = 1; d1 <= 2; d1++) {
				d = dias[d1];
				teste2 = true;
				if (exec == 1 && d == 2)
					continue;
				for (p = 1; p <= Qp; p++) {
					presenteP[p] = false;
					for (j = 1; j <= nTurmasP[p]; j++) {
						if (dd[p][j][d] > 0) {
							presenteP[p] = true;
							break;
						}
					}
				}
				for (k = 1; k <= Ma; k++) {
					k_ = horarios[k];
					aux0 = 0;
					for (p = 1; p <= Qp; p++) {
						if (presenteP[p] && !horariosND[p][d][k_])
							aux0++;
					}
					teste2 = aux0 >= Qa;
					if (!teste2)
						break;
				}
				if (!teste2)
					break;
			}
			if (!teste2) {
				i1 = n2;
				do
					i1--;
				while (case_[i1] != 3);
				System.out.println("i1 = " + i1);
				return true;
			}

			teste2 = true;
			// 2) Executa o Módulo 5 para cada dia a alocar (dias2): preliminares, determinaZ e principal. Se todos os dias forem alocados, o caso
			//   5 e
			// bem-sucedido (retorna false). Se um dia falha, g4_.getP/getA indicam o Prof. e a turma; o recuo vai para a unidade do caso 2 (se o
			//   dia e d_2)
			// ou do caso 4 (se for outro dia) correspondente (i1) e retorna true.
			aux2 = (exec == 1 ? 2 : 3);
			for (d1 = 1; d1 <= aux2; d1++) {
				aux0 = g4_.preliminares(dias2[d1]);
				g4_.determinaZ(aux0);
				teste2 = g4_.principal(aux0);
				if (!teste2)
					break;
				else
					System.out.println("Fechou para d1 = " + d1);
			}
			if (!teste2) {
				p = g4_.getP();
				a = g4_.getA();
				i1 = n2;
				if (dias2[d1] == d_2) {
					do
						i1--;
					while (case_[i1] != 11);
					do
						i1--;
					while (p != Ap[i1] || a != Aa[i1] || case_[i1] != 2);
				} else {
					do
						i1--;
					while (p != Ap[i1] || a != Aa[i1] || case_[i1] != 4);
				}
				System.out.println("i1 = " + i1);
				return true;
			}
			return false;
		}
		}
		return false;
	}

	// Atualiza as variáveis de ligação e de controle após a unidade i receber o valor x0 (Seção 4.5).
	public void atualiza2_() {
		switch (case_[i]) {
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
				// Caso 1: define Np0 (máximo de aulas do Prof. no par), Np1 (máximo em d_2: 0 se não sobra dia de trabalho), wp0 (dias no par) e
				//   x_case_1. A versão para complex_p0 usa os máximos de cada dia.
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

		// Caso 3: Np2 = máximo do Prof. em d_0 (0 se não trabalha) e Np3 = máximo em d_1 (0 se não trabalha).
		case 3: {
			p = Ap[i];
			Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
			Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			break;
		}
		case 2: {
			p = Ap[i];
			// Caso 2: acumula as aulas do Prof. e da turma no par (Sp0_, Sa_0) e em d_2 (Sp1_, Sa_1); q2_[p][j] = x0, aulas da turma no par.
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

		case 4: {
			p = Ap[i];
			// Caso 4: acumula as aulas do Prof. e da turma em d_0 (Sp2_, Sa_4) e em d_1 (Sp3_, Sa_5).
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

	// dependencia(i,j) (Seção 4.5.4): true se x[j] depende de x[i]. As unidades do caso 1 não dependem de unidades anteriores; o caso 11
	//   só depende
	// das unidades do caso 2; as do caso 3 só do caso 1 do mesmo Prof. (e nem isso se complex_p0); as do caso 2 dependem do caso 1 do
	//   mesmo Prof. e de
	// unidades do caso 2 de mesmo Prof. ou turma; as do caso 4 dependem de unidades do caso 2 de mesmo Prof. e turma, do caso 3 do mesmo
	//   Prof. e de unidades
	// do caso 4 de mesmo Prof. ou turma. Nos demais casos considera-se que há dependência.
	public boolean dependencia(int i, int j) {
		int p1, p2, a1, a2;
		if (j <= i || case_[j] == 1 || case_[i] == 11)
			return false;
		if (case_[j] == 11)
			return case_[i] == 2;
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

	// Calcula o arranjo z do Algoritmo 2 (Capítulo 3, determinaArranjo) a partir de dependencia(): z[i][l] = maior unidade p < i da qual
	//   alguma
	// unidade de i a i+l depende, tornado cumulativo em l.
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

	// Busca principal do Módulo 4 (Algoritmo 2 do Capítulo 3; gerador = minimo = -1). Retorna true se todas as unidades (inclusive a de
	// alocação em horários, caso 5) foram fechadas; false se todas as distribuições para os três dias foram pesquisadas sem sucesso.
	public boolean principal2() {
		int k, p0, minimo, iMin, iMax4, i0, p, maxX0_;
		String entrada;
		iMin = 0;
		iMax4 = 0;
		minimo = -1;
		determinaZ(n2);
		// Na Fase 2 pede ao usuário o tempo máximo de execução e inicia a contagem (a Fase 3 continua contando a partir do mesmo início).
		if (exec == 1) {
			entrada = (String) JOptionPane.showInputDialog(null, "Informe tempo máximo de execução (min)", "Entrada",
					JOptionPane.QUESTION_MESSAGE, icone, null, null);
			tempoMaximo = Math.round(Float.parseFloat(entrada));
			tempoInicial = System.currentTimeMillis();
			nIteracoes = 0L;
		}
		imax = 1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		Ap[0] = 0;
		Ap[n2] = 0;
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
			// Com sucessor: guarda x0 e atualiza o controle e avanca.
			// Otimização: depois de um recuo que só alterou as unidades a partir de iMin (do Prof. Ap[iMin]), ao chegar de novo ao caso 11 as
			//   unidades dos casos 3 e 4
			// dos Profs. anteriores são restauradas a partir de cópias (y2) das soluções já encontradas, sem recálculo, e a busca prossegue no
			//   Prof. Ap[iMin].
			if (!maximo()) {
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
				} else {
					x_[i] = x0;
					atualiza2_();
					if (case_[i] == 11)
						iMax4 = i + 1;
					i++;
				}
				if (i > n2)
					return true;
				// Ao concluir as unidades de um Prof. nos casos 3/4, guarda em y2[p] uma cópia de x_ das unidades após o caso 11 (para a otimização
				//   acima).
				if (i > iMax4 && Ap[i] != Ap[i - 1]) {
					iMax4 = i;
					p = Ap[i - 1];
					for (i0 = aux3 + 1; i0 <= i - 1; i0++)
						y2[p][i0] = (short) x_[i0];
				}
				if (i > imax) {
					imax = i;
					System.out.println(imax + "/" + n2);
				}
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				// Sem sucessor: recuo do Algoritmo 2. p0 = z[i][iMaxI_[i] - i], ou, para o caso 5, i1 (unidade calculada em máximo para o Prof. e a
				//   turma
				// onde o Módulo 5 falhou). iMin registra a menor unidade dos casos 1/2 para a qual a busca recuou.
				if (i == 1) {
					System.out.println("Pesquisou todas as distribuições para 3 dias.");
					return false;
				}
				p0 = (case_[i] == 5 ? i1 : z[i][iMaxI_[i] - i]);
				if (case_[p0] <= 2 && case_[i] >= 3)
					iMin = p0;
				else {
					if (case_[p0] <= 2 && p0 < iMin)
						iMin = p0;
				}
				if (p0 == 0) {
					JOptionPane.showMessageDialog(null, "Contacte o desenvolvedor. Código do erro: 015", "",
							JOptionPane.INFORMATION_MESSAGE);
					return false;
				}
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}

	// Grava em dd a distribuição dos três dias: zera dd nos dias de dias[1..3] e preenche com o resultado dos casos 2 (d_2 = A - x) e 4
	//   (d_0 = x e d_1 = q2_ - x).
	public void atualiza4() {
		int p, j, i, p1;
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nTurmasP[p]; j++) {
				for (d1 = 1; d1 <= 3; d1++)
					dd[p][j][dias[d1]] = 0;
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

	// Zera os quadros de horários (de Profs. e de turmas) do dia d1.
	private void inicializaTabelas(int d1) {
		int p1, a1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tabelaP[p1][d1][k1] = 0;
			for (a1 = 1; a1 <= Qa; a1++)
				tabelaA[a1][d1][k1] = 0;
		}
	}

	// Chamado por Prouesse5D_X quando a fase foi bem-sucedida: refaz, pelo Módulo 5, a alocação em horários de cada dia de dias2
	// (preliminares, determinaZ, principal) e grava os horários nos quadros tabelaP e tabelaA (g4_.resultado).
	public void resultado() {
		for (d1 = 1; d1 <= (exec == 1 ? 2 : 3); d1++) {
			inicializaTabelas(dias2[d1]);
			aux0 = g4_.preliminares(dias2[d1]);
			g4_.determinaZ(aux0);
			g4_.principal(aux0);
			g4_.resultado(aux0, dias2[d1]);
		}
	}
}
