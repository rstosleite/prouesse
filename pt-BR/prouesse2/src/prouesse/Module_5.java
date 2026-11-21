package prouesse;

// Classe Module_5 - implementa o Módulo 5 (Seção 4.1.5 do livro).
//
// Papel: dada uma distribuição completa em dd, aloca as aulas de um dia da semana nos horários do dia (1 a Ma). Para
// cada par (Prof. p, turma a) com aulas no dia (1 ou 2 aulas), cada unidade i decide x[i], o horário da (primeira) aula;
// se são duas aulas, elas ocupam os horários consecutivos x[i] e x[i] + 1 (aulas geminadas). Restrições: um Prof. não pode
// ter duas aulas no mesmo horário, nem aulas em horários não disponíveis (horariosND), e uma turma não pode ter duas aulas no mesmo horário.
// É chamado pelo Módulo 4 (e também por Resultado4) para finalizar os horários dos dias. Usa o Algoritmo 2 do Capítulo 3
// (método principal) com 'gerador' igual a 0 (os horários são numerados a partir de 1).
public class Module_5 extends Probl {

	// imax: unidade mais avançada atingida pela busca; xi, aux1 e x0: auxiliares dos horários.
	private int imax, p, a, i, a1, p1, h1, j1, xi, aux1, x0;
	// baliP2[p][h] e baliA2[a][h]: o horário h está ocupado (ou não disponível) para o Prof. p e para a turma a, de acordo com as
	// unidades já fechadas; baliP1 e baliA1: cópias temporárias usadas ao procurar o próximo horário; presenteP[p]: p tem aulas no dia.
	private boolean aulaGeminada, baliP2[][], baliA2[][], teste, baliP1[], baliA1[], presenteP[];

	// Construtor: aloca os arranjos de marcação de horários ocupados.
	public Module_5() {
		presenteP = new boolean[Qp + 1];
		baliP1 = new boolean[Ma + 1];
		baliA1 = new boolean[Ma + 1];
		baliP2 = new boolean[Qp + 1][Ma + 1];
		baliA2 = new boolean[Qa + 1][Ma + 1];
	}

	// Prepara a alocação do dia d. Retorna o número de unidades n (um para cada par (Prof., turma) com aulas no dia) ou 0 se a
	// alocação é impossível (falta de Profs. disponíveis em algum horário).
	public int preliminares(int d) {
		int n, i1, aux0, j, k;
		// presenteP[p]: p tem pelo menos uma aula no dia d.
		for (p = 1; p <= Qp; p++) {
			presenteP[p] = false;
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (dd[p][j][d] > 0) {
					presenteP[p] = true;
					break;
				}
			}
		}
		// Verificação prévia: em cada horário k, o número de Profs. presentes e disponíveis deve ser pelo menos Qa (uma aula por turma).
		for (k = 1; k <= Ma; k++) {
			aux0 = 0;
			for (p = 1; p <= Qp; p++) {
				if (presenteP[p] && !horariosND[p][d][k])
					aux0++;
			}
			if (aux0 < Qa)
				return 0;
		}

		// Estado inicial dos horários: para cada Prof., ocupados os horários não disponíveis no dia (horariosND); nenhum horário de turma ocupado.
		for (h1 = 1; h1 <= Ma; h1++) {
			for (p1 = 1; p1 <= Qp; p1++)
				baliP2[p1][h1] = horariosND[p1][d][h1];
			for (a1 = 1; a1 <= Qa; a1++)
				baliA2[a1][h1] = false;
		}

		// Monta as unidades, na ordem dos Profs. d2_: Ap, Aa e A[n] = quantidade de aulas do par no dia (1 ou 2).
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

	// Sucessor da unidade i: procura o menor horário x0 > x_[i] em que as aulas do par podem ser alocadas. Retorna false se
	// encontrou (valor em x0) e true se não existe.
	public boolean maximoXi_() {
		p = Ap[i];
		a = Aa[i];
		// Se o par tem duas aulas, elas precisam de dois horários consecutivos.
		aulaGeminada = A[i] > 1;
		x0 = x_[i];
		// Copia, para os horários posteriores a x0, a ocupação atual do Prof. e da turma.
		for (h1 = x0 + 1; h1 <= Ma; h1++) {
			baliP1[h1] = baliP2[p][h1];
			baliA1[h1] = baliA2[a][h1];
		}
		// Libera o horário x0 + 1, que pertence à própria alocação anterior desta unidade (par geminado).
		if (x0 != 0 && aulaGeminada) {
			baliP1[x0 + 1] = false;
			baliA1[x0 + 1] = false;
		}
		// Último horário em que a aula pode começar: Ma - 1 se são duas aulas, senão Ma.
		aux1 = (aulaGeminada ? Ma - 1 : Ma);
		teste = true;
		// Avança x0 até um horário livre para o Prof. e para a turma (e, para duas aulas, também no horário seguinte).
		while (teste && x0 < aux1) {
			x0++;
			teste = baliP1[x0] || baliA1[x0] || (aulaGeminada && (baliP1[x0 + 1] || baliA1[x0 + 1]));
		}
		return teste;
	}

	// Prof. da unidade mais avançada atingida pela busca (para o Módulo 4 decidir o recuo).
	public int getP() {
		return (Ap[imax]);
	}

	// Turma da unidade mais avançada atingida.
	public int getA() {
		return (Aa[imax]);
	}

	// Libera os horários que a unidade i ocupava (antes de ser alterada ou abandonada).
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

	// Marca como ocupados, para o Prof. e para a turma, o horário x0 (e x0 + 1 se são duas aulas).
	public void atualiza2_(int i) {
		baliP2[Ap[i]][x0] = true;
		baliA2[Aa[i]][x0] = true;
		if (A[i] > 1) {
			baliP2[Ap[i]][x0 + 1] = true;
			baliA2[Aa[i]][x0 + 1] = true;
		}

	}

	// Grava o resultado do dia d nos quadros de horários: tabelaP[p][d][h] = turma e tabelaA[a][d][h] = Prof.
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

	// Algoritmo 2 do Capítulo 3. Retorna true se todas as aulas do dia foram alocadas e false se não é possível.
	public boolean principal(int n2) {
		int p0, minimo, k;
		imax = 0;
		// O 'gerador' é 0 (nenhum horário escolhido).
		minimo = 0;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		do {
			// Sucesso na unidade: libera o horário anterior, registra x0, marca o novo horário e avança.
			if (!this.maximoXi_()) {
				this.atualiza3_(i);
				x_[i] = x0;
				this.atualiza2_(i);
				i++;
				if (i > imax)
					imax = i;
				// Todas as unidades fechadas.
				if (i > n2)
					return true;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			// Falha: recua para z[i][iMaxI_[i] - i], liberando os horários das unidades abandonadas.
			} else {
				if (i == 1)
					return false;
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

	// Calcula z[i][l] (Capítulo 3): a unidade j depende da unidade i se têm o mesmo Prof. ou a mesma turma; depois acumula o máximo ao longo de l.
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
