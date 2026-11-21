package prouesse;

// Classe Resultado4 - insere aulas geminadas nos resultados finais (Seções 4.6.3 e 4.7.2 do livro).
//
// Depois que as Fases 2 e 3 completaram os quadros de horários, para cada Prof. p com comGeminadasP[p] == true tenta-se criar, em cada
// turma a de p que ainda não tenha aulas geminadas, um dia com duas aulas consecutivas de p nessa turma. Para isso é feita uma troca
//   que
// envolve um segundo Prof. pL (que também atende a turma a), uma segunda turma aL (atendida por p e por pL) e um segundo dia d3:
//                     turma a         turma aL
//    Prof. p          d3-   d+        d-    d3+
//    Prof. pL         d-    d3+       d3-   d+
// ('+' acrescenta e '-' retira uma aula no dia indicado; d é o dia em que p passa a ter duas aulas em a.) Cada Prof. e cada turma
// mantém o mesmo total de aulas em d e em d3, de modo que a distribuição continua válida. Depois da troca, as aulas dos dias d e d3
//   são
// realocadas nos horários pelo Módulo 5; se isso falhar em algum dos dois dias, a troca é desfeita.
public class Resultado4 extends Probl {
	// distr_pa, distr_paL, distr_pLa, distr_pLaL = cópias das linhas de dd (aulas nos 5 dias) de (p,a), (p,aL), (pL,a) e (pL,aL);
	// j, j1, j2, jL = posições das turmas a, aL (de p) e a, aL (de pL) nas listas dos Profs.;  d, d3 = dias da troca;
	// delta_* = variação da situação de geminadas de cada par (ver delta);  contTentativas e contSucessos = estatística das tentativas;
	// distr0, tabelaP0 = cópias de segurança das aulas e dos horários dos dias d e d3, para desfazer a troca;  g40_ = objeto do Módulo 5.
	private static int distr_pa[], distr_paL[], distr_pLa[], distr_pLaL[], j1, j2, p, d, k, a, h, d3, aL, l, j, jL, pL,
			delta_paL, delta_pLa, delta_pLaL, soma, contTentativas, contSucessos, p1, j0, distr0[][][], tabelaP0[][],
			aux0;
	private static boolean teste, teste2;
	static public Module_5 g40_;

	// Construtor: cria o objeto do Módulo 5 e aloca as cópias de segurança (distr0 por Prof. e turma, e tabelaP0 por Prof. e horário).
	public Resultado4() {
		g40_ = new Module_5();
		distr0 = new int[Qp + 1][][];
		for (p1 = 1; p1 <= Qp; p1++)
			distr0[p1] = new int[nTurmasP[p1] + 1][2];
		tabelaP0 = new int[Qp + 1][Ma + 1];
	}

	// Zera o quadro de horários dos Profs. no dia d1 (antes de o Módulo 5 reescreve-lo).
	private static void inicializaTabelas(int d1) {
		int p1, k1;
		for (k1 = 1; k1 <= Ma; k1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				tabelaP[p1][d1][k1] = 0;
		}
	}

	// Avalia a situação de geminadas de uma linha de distribuição (aulas nos 5 dias). teste = true se não há nenhum dia com 2 aulas.
	// Com semGem == false (ver chamadas): retorna 1 se não há geminada e 0 se há. Com semGem == true: retorna 0 se não há geminada e -1 se
	//   há.
	// Assim, chamada antes e depois da troca (usando como semGem o resultado anterior == 1), dá +1 se o par perdeu a geminada, -1 se
	//   ganhou e 0 se não mudou.
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

	// Tenta criar uma aula geminada para (p, a) pela troca descrita no cabeçalho da classe. Percorre, em ordem: o dia d em que p tem aulas
	//   na turma a;
	// o Prof. pL, diferente de p, que atende a turma a e tem aula em d; a turma aL, diferente de a, atendida por p e por pL, com aula de p
	//   em d;
	// o dia d3 diferente de d em que (p,a) e (pL,aL) tem aulas. Retorna true na primeira troca bem-sucedida e false se nenhuma funcionar.
	private static boolean insereGem(int p, int a) {
		boolean teste;
		// Dia d: só interessa um dia em que p tem aula na turma a.
		for (d = 0; d <= 4; d++) {
			if (distr_pa[d] == 0)
				continue;
			// Procura outro Prof. pL que atenda a turma a (j2 = posição de a na lista de pL) e tenha aula em d.
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
				// Procura outra turma aL de p, com aula de p em d, que também seja atendida por pL (jL = posição de aL na lista de pL).
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

					// Segundo dia d3 (diferente de d) em que p tem aula em a e pL tem aula em aL.
					for (d3 = 0; d3 <= 4; d3++) {
						if (d3 == d || dd[p][j][d3] < 1 || dd[pL][jL][d3] < 1)
							continue;
						for (k = 0; k <= 4; k++) {
							// Copia as linhas de distribuição dos quatro pares envolvidos e guarda a situação anterior de geminadas dos três pares afetados (além
							//   de (p,a)).
							distr_pa[k] = dd[p][j][k];
							distr_paL[k] = dd[p][j1][k];
							distr_pLa[k] = dd[pL][j2][k];
							distr_pLaL[k] = dd[pL][jL][k];
						}
						delta_paL = delta(distr_paL, false);
						delta_pLa = delta(distr_pLa, false);
						delta_pLaL = delta(distr_pLaL, false);
						// Aplica a troca nas cópias: (p,aL) e (pL,a) perdem uma aula em d e ganham em d3; (p,a) e (pL,aL) perdem em d3 e ganham em d.
						teste2 = true;
						distr_paL[d]--;
						distr_pLa[d]--;
						distr_pa[d3]--;
						distr_pLaL[d3]--;
						distr_pLaL[d]++;
						distr_pa[d]++;
						distr_paL[d3]++;
						distr_pLa[d3]++;
						// Rejeita se algum par passaria do máximo de aulas por dia e turma (1 ou 2 conforme semGeminadasP; (p,a) e (p,aL) até 2).
						if (distr_pLaL[d] > (semGeminadasP[pL] ? 1 : 2) || distr_pa[d] > 2 || distr_paL[d3] > 2
								|| distr_pLa[d3] > (semGeminadasP[pL] ? 1 : 2))
							teste2 = false;
						// Avalia o efeito sobre as geminadas: soma as variações de (p,aL) e, se pL deseja geminadas, de (pL,a) e (pL,aL); soma >= 2 e
						//   rejeitada.
						// Se (p,aL) perderia a geminada, exige aL > a; e se um par de pL com geminadas desejadas perderia, exige pL > p (critério de ordem
						// que parece servir para que trocas posteriores não desfaçam as anteriores).
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

						if (teste2) {
							for (p1 = 1; p1 <= Qp; p1++) {
								for (j0 = 1; j0 <= nTurmasP[p1]; j0++) {
									// Troca aceita nas cópias: guarda cópia de segurança (distr0, tabelaP0) das aulas e dos horários dos dias d e d3 ...
									distr0[p1][j0][0] = dd[p1][j0][d];
									distr0[p1][j0][1] = dd[p1][j0][d3];
								}
								for (j0 = 1; j0 <= Ma; j0++)
									tabelaP0[p1][j0] = tabelaP[p1][d][j0];
							}
							// ... e aplica a troca em dd.
							dd[p][j1][d] = distr_paL[d];
							dd[pL][j2][d] = distr_pLa[d];
							dd[p][j][d3] = distr_pa[d3];
							dd[pL][jL][d3] = distr_pLaL[d3];
							dd[pL][jL][d] = distr_pLaL[d];
							dd[p][j][d] = distr_pa[d];
							dd[p][j1][d3] = distr_paL[d3];
							dd[pL][j2][d3] = distr_pLa[d3];
							teste = true;
							// Realoca nos horários, pelo Módulo 5, as aulas do dia d e, em seguida, as do dia d3 (preliminares, determinaZ, principal e
							//   resultado).
							// Se os dois dias derem certo, a troca vale e a função retorna true.
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
							// Se os dois dias foram realocados com sucesso (teste), a troca vale e retorna true; senão, restaura dd e os quadros dos Profs. a
							//   partir das cópias de segurança e tenta a próxima combinação.
							if (teste)
								return true;
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
		return false;
	}

	// Percorre os Profs. com comGeminadasP == true e, para cada turma deles sem nenhum dia com 2 aulas, tenta inserir uma geminada
	//   (insereGem).
	// Ao final reconstroi os quadros das turmas (tabelaA) a partir dos quadros dos Profs. (tabelaP) e retorna a fração de sucessos
	//   (contSucessos / contTentativas).
	static public float insereGeminadas() {

		// Aloca as linhas de trabalho e inicializa d2_ (ordem natural dos Profs., usada pelo Módulo 5).
		distr_pa = new int[5];
		distr_paL = new int[5];
		distr_pLa = new int[5];
		distr_pLaL = new int[5];
		for (p = 1; p <= Qp; p++)
			d2_[p] = p;
		contTentativas = 0;
		contSucessos = 0;
		for (p = 1; p <= Qp; p++) {
			// Só interessam os Profs. que desejam aulas geminadas.
			if (!comGeminadasP[p])
				continue;
			for (a = 1; a <= Qa; a++) {
				// Para cada turma a: j = posição de a na lista do Prof. (0 se a turma não é do Prof.).
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
				for (k = 0; k <= 4; k++) {
					distr_pa[k] = dd[p][j][k];
					teste = teste || distr_pa[k] > 1;
				}
				// Se a turma ainda não tem geminadas (nenhum dia com mais de 1 aula), conta a tentativa e tenta inserir.
				if (!teste) {
					contTentativas++;
					if (insereGem(p, a))
						contSucessos++;
				}
			}
		}

		for (d = 0; d <= 4; d++) {
			for (k = 1; k <= Ma; k++) {
				for (a = 1; a <= Qa; a++)
					// Reconstroi os quadros das turmas: zera tabelaA e preenche a partir de tabelaP (a turma tabelaP[p][d][k] e atendida pelo Prof. p
					//   nesse horário).
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
		// Imprime a estatística e devolve a taxa de sucesso.
		System.out.println("contSucessos = " + contSucessos + " contTentativas = " + contTentativas);
		return (float) contSucessos / (float) contTentativas;
	}
}
