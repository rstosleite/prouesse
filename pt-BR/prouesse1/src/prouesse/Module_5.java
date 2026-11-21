package prouesse;

// Classe Module_5 - implementa o Módulo 5 (Seção 4.1.5 do livro): aloca nos horários de um dia as aulas desse dia.
//
// Entrada: a distribuição completa em dd (número de aulas de cada Prof. e turma no dia d). Para cada dia, o módulo decide em que
//   horários
// (1 a Ma) cada aula ocorre. Cada unidade i é um par (Prof. Ap[i], turma Aa[i]) com A[i] aulas no dia (1 ou 2); x[i] é o horário da
// (primeira) aula; se A[i] == 2 as duas aulas são geminadas e ocupam os horários x[i] e x[i]+1.
// Restrições: um Prof. não pode dar duas aulas no mesmo horário, nem uma turma assistir duas aulas no mesmo horário; um Prof. não pode
// ser alocado em horários não disponíveis (horariosND). A busca é o Algoritmo 2 do Capítulo 3 (principal), com dependência entre
// unidades do mesmo Prof. ou da mesma turma (determinaZ). Se a busca falha, getP/getA informam o Prof. e a turma onde o módulo
// não conseguiu avançar, para o Módulo 4 recuar até a unidade correspondente. Resultados: tabelaP e tabelaA (resultado).
// Usa os arranjos de unidades de Probl (Ap, Aa, A, x_, z, iMaxI_).
public class Module_5 extends Probl {
	// imax = unidade mais avancada alcançada;  p, a, i = Prof., turma e unidade correntes;  x0 = candidato a horário da unidade i.
	// baliP2[p][h] = true se o Prof. p está ocupado ou indisponível no horário h;  baliA2[a][h] = true se a turma a está ocupada no
	//   horário h.
	// baliP1[h], baliA1[h] = cópias de trabalho para o Prof. e a turma da unidade corrente;  presenteP[p] = o Prof. tem aulas no dia.
	private int imax, p, a, i, a1, p1, h1, j1, xi, aux1, x0;
	private boolean aulaGeminada, baliP2[][], baliA2[][], teste, baliP1[], baliA1[], presenteP[];

	// Construtor: aloca os marcadores de ocupação (por Prof., por turma e por horário).
	public Module_5() {
		presenteP = new boolean[Qp + 1];
		baliP1 = new boolean[Ma + 1];
		baliA1 = new boolean[Ma + 1];
		baliP2 = new boolean[Qp + 1][Ma + 1];
		baliA2 = new boolean[Qa + 1][Ma + 1];
	}

	// Prepara a alocação do dia d. Retorna o número n de unidades (pares Prof./turma com aulas no dia) ou 0 se já se sabe que é
	//   impossível.
	public int preliminares(int d) {
		int n, i1, aux0, j, k;
		for (p = 1; p <= Qp; p++) {
			// 1) Marca os Profs. que têm aulas no dia d.
			presenteP[p] = false;
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (dd[p][j][d] > 0) {
					presenteP[p] = true;
					break;
				}
			}
		}
		// 2) Verificação prévia: em cada horário do dia deve haver pelo menos Qa Profs. presentes e disponíveis (um por turma); se não,
		//   retorna 0.
		for (k = 1; k <= Ma; k++) {
			aux0 = 0;
			for (p = 1; p <= Qp; p++) {
				if (presenteP[p] && !horariosND[p][d][k])
					aux0++;
			}
			if (aux0 < Qa)
				return 0;
		}

		for (h1 = 1; h1 <= Ma; h1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				// 3) Estado inicial: os horários não disponíveis do Prof. contam como ocupados; as turmas começam livres.
				baliP2[p1][h1] = horariosND[p1][d][h1];
			for (a1 = 1; a1 <= Qa; a1++)
				baliA2[a1][h1] = false;
		}

		// 4) Monta as unidades, Prof. por Prof. (na ordem d2_) e, para cada um, suas turmas com aulas no dia (A = número de aulas).
		n = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p1 = d2_[i1];
			for (j1 = 1; j1 <= nTurmasP[p1]; j1++) {
				if (dd[p1][j1][d] > 0) {
					n++;
					Ap[n] = p1;
					Aa[n] = lista[p1][j1];
					A[n] = dd[p1][j1][d];
				}
			}
		}
		return n;
	}

	// Equivale ao 'sucessor': procura o menor horário x0 > x_[i] em que a unidade i pode ser alocada. Retorna false se encontrou (em x0)
	// e true caso contrário. aulaGeminada = a unidade tem 2 aulas, que exigem dois horários consecutivos livres.
	public boolean maximoXi_() {
		p = Ap[i];
		a = Aa[i];
		aulaGeminada = A[i] > 1;
		x0 = x_[i];
		// Copia, para os horários candidatos, a ocupação do Prof. e da turma.
		for (h1 = x0 + 1; h1 <= Ma; h1++) {
			baliP1[h1] = baliP2[p][h1];
			baliA1[h1] = baliA2[a][h1];
		}
		// Se a unidade já estava alocada em x0, o horário x0+1 ocupado por ela mesma é liberado na copia (o horário x0 não e candidato).
		if (x0 != 0 && aulaGeminada) {
			baliP1[x0 + 1] = false;
			baliA1[x0 + 1] = false;
		}
		// Último horário possível de início: Ma (aula simples) ou Ma-1 (geminada, que usa também o horário seguinte).
		aux1 = (aulaGeminada ? Ma - 1 : Ma);
		teste = true;
		// Avanca até achar um horário livre para o Prof. e a turma (e também o seguinte, se geminada); teste continua true se não houver.
		while (teste && x0 < aux1) {
			x0++;
			teste = baliP1[x0] || baliA1[x0] || (aulaGeminada && (baliP1[x0 + 1] || baliA1[x0 + 1]));
		}
		return teste;
	}

	// getP e getA: Prof. e turma da unidade mais avancada alcançada (imax), onde a alocação falhou.
	public int getP() {
		return (Ap[imax]);
	}

	public int getA() {
		return (Aa[imax]);
	}

	// Libera os horários ocupados pela unidade i (desfaz a marcação de atualiza2_).
	public void atualiza3_(int i) {
		xi = x_[i];
		if (xi != 0) {
			baliP2[Ap[i]][xi] = false;
			baliA2[Aa[i]][xi] = false;
			if (A[i] > 1) {
				baliP2[Ap[i]][xi + 1] = false;
				baliA2[Aa[i]][xi + 1] = false;
			}
		}
	}

	// Marca como ocupados, para o Prof. e para a turma, o horário x0 escolhido (e o seguinte, se geminada).
	public void atualiza2_(int i) {
		baliP2[Ap[i]][x0] = true;
		baliA2[Aa[i]][x0] = true;
		if (A[i] > 1) {
			baliP2[Ap[i]][x0 + 1] = true;
			baliA2[Aa[i]][x0 + 1] = true;
		}

	}

	// Grava os resultados do dia d: tabelaP[p][d][h] = turma e tabelaA[a][d][h] = Prof. para cada horário h ocupado pelas aulas das n
	//   unidades.
	public void resultado(int n, int d) {
		for (i = 1; i <= n; i++) {
			p = Ap[i];
			a = Aa[i];
			tabelaP[p][d][x_[i]] = a;
			tabelaA[a][d][x_[i]] = p;
			if (A[i] > 1) {
				tabelaP[p][d][x_[i] + 1] = a;
				tabelaA[a][d][x_[i] + 1] = p;
			}
		}
	}

	// Busca do Módulo 5 (Algoritmo 2 do Capítulo 3). x_[i] = 0 significa 'ainda não alocada' (gerador = minimo = 0). Retorna true se as n2
	//   unidades
	// foram alocadas nos horários do dia; false caso contrário.
	public boolean principal(int n2) {
		int p0, minimo, k;
		imax = 0;
		minimo = 0;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		do {
			// Com horário livre: libera a marcação anterior da unidade, guarda x0, marca o novo horário e avanca (atualizando iMaxI_).
			if (!this.maximoXi_()) {
				this.atualiza3_(i);
				x_[i] = x0;
				this.atualiza2_(i);
				i++;
				if (i > imax)
					imax = i;
				if (i > n2)
					return true;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				if (i == 1)
					return false;
				// Sem horário: recua para a unidade p0 indicada por z, liberando os horários das unidades após p0; se não há para onde recuar, falha.
				p0 = z[i][iMaxI_[i] - i];
				if (p0 == 0)
					return false;
				for (i = i; i > p0; i--) {
					if (x_[i] != minimo) {
						this.atualiza3_(i);
						x_[i] = minimo;
					}
				}
				i = p0;
			}
		} while (true);
	}

	// Calcula o arranjo z (Capítulo 3) para a dependência do Módulo 5: z[i][l] = maior unidade anterior a i com o mesmo Prof. ou a mesma
	// turma que alguma unidade de i a i+l, tornado cumulativo em l.
	public void determinaZ(int n_) {
		int p, i, l;
		for (i = 2; i <= n_; i++) {
			for (l = 0; l <= n_ - i; l++) {
				p = i - 1;
				while (p > 0 && Ap[p] != Ap[i + l] && Aa[p] != Aa[i + l])
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
