package prouesse;

// Classe Resultado4 - insere aulas geminadas nos resultados finais (Seções 4.6.3, 4.7.2 e 5.4.3, item b, do livro).
//
// Papel: depois que os quadros de horários de todos os dias estão prontos (Fases 2 e 3), tenta melhorar o resultado para os
// Profs. p com comGeminadasP[p] verdadeiro: para cada turma a de p que ainda não tem nenhum dia com duas aulas de p (aulas
// geminadas), procura trocas de aulas com outro Prof. pL e outra turma aL, envolvendo dois dias d e d3, que façam aparecer
// duas aulas de (p, a) no dia d. As trocas feitas na distribuição dd são:
//                  turma a            turma aL
//        Prof. p   d3-  d+            d-   d3+
//        Prof. pL  d-   d3+           d3-  d+
// (o sinal + indica acréscimo de uma aula no dia e o sinal - indica retirada de uma aula). Depois da troca, os horários dos
// dias d e d3 são alocados de novo com o Módulo 5; se isso falha em algum dos dias, a troca é desfeita.
public class Resultado4 extends Probl {

	// distr_pa, distr_paL, distr_pLa e distr_pLaL: aulas por dia dos pares (p, a), (p, aL), (pL, a) e (pL, aL); j, j1, j2 e jL: posições
	// das turmas a, aL (para p) e a, aL (para pL) nos arranjos lista; delta_*: variação de aulas geminadas de cada par com a troca;
	// contTentativas e contSucessos: quantidade de turmas tentadas e das que receberam geminadas; distr0 e tabelaP0: cópias
	// de segurança da distribuição e dos horários dos Profs., usadas para desfazer uma troca que falhou.
	private static int distr_pa[], distr_paL[], distr_pLa[], distr_pLaL[], j1, j2, p, d, k, a, h, d3, aL, l, j, jL, pL,
			delta_paL, delta_pLa, delta_pLaL, soma, contTentativas, contSucessos, p1, j0, distr0[][][], tabelaP0[][],
			aux0;
	private static boolean teste, teste2;
	// g40_: objeto do Módulo 5, usado para realocar os horários dos dias alterados.
	static public Module_5 g40_;

	// Construtor: cria o Módulo 5 e aloca os arranjos de cópia de segurança.
	public Resultado4() {
		g40_ = new Module_5();
		distr0 = new int[Qp + 1][][];
		for (p1 = 1; p1 <= Qp; p1++)
			distr0[p1] = new int[nTurmasP[p1] + 1][2];
		tabelaP0 = new int[Qp + 1][Ma + 1];
	}

	// Zera os horários dos Profs. (tabelaP) do dia d1, antes de gravar o novo resultado.
	private static void inicializaTabelas(int d1) {
		int p1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tabelaP[p1][d1][k1] = 0;
		}
	}

	// Mede a situação de geminadas de uma distribuição por dia (distr[0..4]). Primeiro cálculo (semGem = false): retorna 1 se a
	// distribuição não tem nenhum dia com mais de uma aula (nenhuma geminada) e 0 se tem. Segundo cálculo, feito depois da troca com
	// semGem = true se antes não havia geminada: retorna 0 se continua sem geminada e -1 se passou a ter (ganho); com
	// semGem = false: retorna 0 se continua com geminada e 1 se perdeu (perda).
	private static int delta(int[] distr, boolean semGem) {
		boolean teste;
		teste = false;
		for (k = 0; k <= 4; k++)
			teste = teste || distr[k] > 1;
		teste = !teste;
		if (semGem) {
			if (teste)
				return 0;
			else
				return -1;
		} else {
			if (teste)
				return 1;
			else
				return 0;
		}
	}

	// Tenta inserir aulas geminadas de (p, a) por uma das trocas descritas no cabeçalho. Retorna true se alguma troca teve sucesso.
	private static boolean insereGem(int p, int a) {
		boolean teste;
		// d: dia em que p tem aula na turma a (será o dia da futura geminada).
		for (d = 0; d <= 4; d++) {
			if (distr_pa[d] == 0)
				continue;
			// pL: outro Prof. que também leciona na turma a (posição j2) e tem aula nela em d.
			for (pL = 1; pL <= Qp; pL++) {
				if (pL == p)
					continue;
				j2 = 0;
				for (l = 1; l <= nTurmasP[pL]; l++) {
					if (lista[pL][l] == a) {
						j2 = l;
						break;
					}
				}
				if (j2 == 0 || dd[pL][j2][d] < 1)
					continue;
				// aL: outra turma de p com aula em d e que também é turma de pL (posição jL na lista de pL).
				for (j1 = 1; j1 <= nTurmasP[p]; j1++) {
					aL = lista[p][j1];
					if (aL == a || dd[p][j1][d] < 1)
						continue;
					jL = 0;
					for (l = 1; l <= nTurmasP[pL]; l++) {
						if (lista[pL][l] == aL) {
							jL = l;
							break;
						}
					}
					if (jL == 0)
						continue;

					// d3: segundo dia, em que p tem aula em a e pL tem aula em aL.
					for (d3 = 0; d3 <= 4; d3++) {
						if (d3 == d || dd[p][j][d3] < 1 || dd[pL][jL][d3] < 1)
							continue;

						// Copia as aulas dos quatro pares nos cinco dias e registra a situação de geminadas antes da troca.
						for (k = 0; k <= 4; k++) {
							distr_pa[k] = dd[p][j][k];
							distr_paL[k] = dd[p][j1][k];
							distr_pLa[k] = dd[pL][j2][k];
							distr_pLaL[k] = dd[pL][jL][k];
						}
						delta_paL = delta(distr_paL, false);
						delta_pLa = delta(distr_pLa, false);
						delta_pLaL = delta(distr_pLaL, false);
						teste2 = true;

						// Aplica a troca da tabela do cabeçalho.
						distr_paL[d]--;
						distr_pLa[d]--;
						distr_pa[d3]--;
						distr_pLaL[d3]--;
						distr_pLaL[d]++;
						distr_pa[d]++;
						distr_paL[d3]++;
						distr_pLa[d3]++;
						// Rejeita a troca se alguma turma ultrapassar o máximo de aulas por dia (1 se o Prof. não admite geminadas, senão 2).
						if (distr_pLaL[d] > (semGeminadasP[pL] ? 1 : 2) || distr_pa[d] > 2 || distr_paL[d3] > 2
								|| distr_pLa[d3] > (semGeminadasP[pL] ? 1 : 2))
							teste2 = false;
						// Avalia o efeito sobre as geminadas: a troca não pode causar perda líquida de duas ou mais geminadas; uma perda em (p, aL) só é
						// aceita se aL > a, e uma perda de Prof. pL com preferência por geminadas só é aceita se pL > p (evita trocas cíclicas).
						if (teste2) {

							delta_paL = delta(distr_paL, delta_paL == 1);
							delta_pLa = delta(distr_pLa, delta_pLa == 1);
							delta_pLaL = delta(distr_pLaL, delta_pLaL == 1);
							soma = delta_paL;
							if (comGeminadasP[pL]) {
								soma += delta_pLa;
								soma += delta_pLaL;
							}
							if (soma >= 2)
								teste2 = false;
							else {
								if (delta_paL == 1)
									teste2 = aL > a;
								if (teste2 && comGeminadasP[pL] && (delta_pLa == 1 || delta_pLaL == 1) && pL <= p)
									teste2 = false;
							}
						}

						// Troca aceita: guarda cópia de segurança das aulas e dos horários do dia d, grava a nova distribuição e tenta realocar os
						// horários dos dias d e d3 com o Módulo 5.
						if (teste2) {

							// Cópia de segurança (aulas dos dias d e d3 de todos os pares e horários dos Profs. no dia d).
							for (p1 = 1; p1 <= Qp; p1++) {
								for (j0 = 1; j0 <= nTurmasP[p1]; j0++) {
									distr0[p1][j0][0] = dd[p1][j0][d];
									distr0[p1][j0][1] = dd[p1][j0][d3];
								}
								for (j0 = 1; j0 <= Ma; j0++)
									tabelaP0[p1][j0] = tabelaP[p1][d][j0];
							}
							// Grava a nova distribuição em dd.
							dd[p][j1][d] = distr_paL[d];
							dd[pL][j2][d] = distr_pLa[d];
							dd[p][j][d3] = distr_pa[d3];
							dd[pL][jL][d3] = distr_pLaL[d3];
							dd[pL][jL][d] = distr_pLaL[d];
							dd[p][j][d] = distr_pa[d];
							dd[p][j1][d3] = distr_paL[d3];
							dd[pL][j2][d3] = distr_pLa[d3];
							// Executa o Módulo 5 para o dia d; se tem sucesso, grava os horários e repete para o dia d3.
							teste = true;
							aux0 = g40_.preliminares(d);
							if (aux0 > 0)
								g40_.determinaZ(aux0);
							if (aux0 > 0 && g40_.principal(aux0)) {
								inicializaTabelas(d);
								g40_.resultado(aux0, d);
								aux0 = g40_.preliminares(d3);
								if (aux0 > 0)
									g40_.determinaZ(aux0);
								if (aux0 > 0 && g40_.principal(aux0)) {
									inicializaTabelas(d3);
									g40_.resultado(aux0, d3);

								} else
									teste = false;
							} else
								teste = false;
							// Sucesso nos dois dias: a troca é mantida.
							if (teste)
								return true;
							// Falha: restaura a distribuição e os horários anteriores.
							else {

								for (p1 = 1; p1 <= Qp; p1++) {
									for (j0 = 1; j0 <= nTurmasP[p1]; j0++) {
										dd[p1][j0][d] = distr0[p1][j0][0];
										dd[p1][j0][d3] = distr0[p1][j0][1];
									}
									for (j0 = 1; j0 <= Ma; j0++)
										tabelaP[p1][d][j0] = tabelaP0[p1][j0];

								}
							}
						}
					}
				}
			}
		}
		// Nenhuma troca possível para (p, a).
		return false;
	}

	// Percorre os Profs. com comGeminadasP e as turmas deles sem aulas geminadas e chama insereGem. No fim, reconstrói os horários das
	// turmas (tabelaA) a partir dos horários dos Profs. (tabelaP) e retorna a fração de tentativas bem-sucedidas.
	static public float insereGeminadas() {

		distr_pa = new int[5];
		distr_paL = new int[5];
		distr_pLa = new int[5];
		distr_pLaL = new int[5];

		// d2_ define a ordem natural dos Profs. para o Módulo 5.
		for (p = 1; p <= Qp; p++)
			d2_[p] = p;
		contTentativas = 0;
		contSucessos = 0;
		// Para cada Prof. que prefere geminadas e cada turma a dele...
		for (p = 1; p <= Qp; p++) {
			if (!comGeminadasP[p])
				continue;
			for (a = 1; a <= Qa; a++) {
				j = 0;
				for (l = 1; l <= nTurmasP[p]; l++) {
					if (lista[p][l] == a) {
						j = l;
						break;
					}
				}
				if (j == 0)
					continue;
				teste = false;
				// ...se ainda não há nenhum dia com mais de uma aula de (p, a), tenta inserir uma geminada.
				for (k = 0; k <= 4; k++) {
					distr_pa[k] = dd[p][j][k];
					teste = teste || distr_pa[k] > 1;
				}

				if (!teste) {
					contTentativas++;
					if (insereGem(p, a))
						contSucessos++;
				}
			}
		}

		// Reconstrói tabelaA: zera e depois preenche com o Prof. de cada turma em cada horário.
		for (d = 0; d <= 4; d++) {
			for (k = 1; k <= Ma; k++) {
				for (a = 1; a <= Qa; a++)
					tabelaA[a][d][k] = 0;
			}
		}

		for (d = 0; d <= 4; d++) {
			for (k = 1; k <= Ma; k++) {
				for (p = 1; p <= Qp; p++) {
					a = tabelaP[p][d][k];
					tabelaA[a][d][k] = p;
				}
			}
		}
		System.out.println("contSucessos = " + contSucessos + " contTentativas = " + contTentativas);
		return (float) contSucessos / (float) contTentativas;
	}
}
