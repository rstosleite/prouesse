package prouesse;

// Classe Module_3 - implementa o Módulo 3 (Fase 1.2; Seções 4.1.3 e 4.4 do livro).
//
// Finalidade: o Módulo 2 distribui as aulas de cada (Prof., turma) entre dois pares de dias, (d_0,d_1) = (1,2) e (d_2,d_3) = (3,4).
//   Este módulo
// completa a distribuição dentro de um par: para cada unidade (Prof. p, turma a) decide x = número de aulas (0, 1 ou 2) no 1o dia do
// par; no 2o dia ficam y = q2_[p][j] - x aulas. É chamado uma vez por par de dias e por possibilidade de dias de trabalho (possibP).
// Todas as unidades são do mesmo tipo (Caso 4 da Seção 4.4.1). Restrições (Seção 4.4.1): o total de aulas de cada Prof. nos dois dias
//   não
// ultrapassa N_2p (1o dia) e N_3p (2o dia); o total de cada turma em cada dia não ultrapassa Ma; no máximo 1 aula por dia e por turma
// para Profs. sem geminadas, e 2 caso contrário. A busca é o Algoritmo 2 do Capítulo 3 (principal2); a dependência entre unidades e:
// x[j] depende de x[i] quando as unidades têm o mesmo Prof. ou a mesma turma (ver determinaZ).
// Variáveis de controle: Sp2_, Sp3_ (totais do Prof. em cada dia do par) e Sa_4, Sa_5 (totais da turma em cada dia do par).
// Os arranjos de unidades (Ap, Aa, j_, x_, maxX0, z, iMaxI_) são os da superclasse Probl, diferentes dos usados pelo Módulo 2
//   (FaseB2_Super).
public class Module_3 extends FaseB2_Super {

	// Cópias privadas (aliases) dos arranjos de unidades de Probl: Ap, Aa, j_, x_, maxX0, z, iMaxI_;  n2 = número de unidades;  x0 =
	//   candidato a sucessor;
	// q2_ = aulas de cada (Prof., turma) no par de dias em tratamento (q2_ ou q2_0 de Module_2).
	private int Ap[], Aa[], j_[], x_[], maxX0[], i, n2, x0, q2_[][], iMaxI_[];
	private short z[][];

	// Construtor vazio; a preparação é feita por preliminares2().
	public Module_3() {
	}

	// Prepara o módulo (chamado uma vez, ao final de preliminares do Módulo 2): liga os arranjos privados aos de Probl, monta as unidades
	// e zera a distribuição dd dos 4 dias.
	public void preliminares2() {
		int k;
		// Aliases: este módulo usa os arranjos de unidades de Probl (os mesmos do Módulo 1, já concluído); o Módulo 2 usa os de FaseB2_Super.
		z = Probl.z;
		iMaxI_ = Probl.iMaxI_;
		Ap = Probl.Ap;
		Aa = Probl.Aa;
		j_ = Probl.j_;
		x_ = Probl.x_;
		maxX0 = Probl.maxX0;

		// Unidades: para cada Prof. (na mesma ordem d2_ do Módulo 2) com aulas e para cada turma dele com aulas nos 4 dias (q_0 > 0),
		// uma unidade (Ap = Prof., Aa = turma, j_ = posição da turma na lista do Prof.). Mesma ordem das unidades do caso 2 do Módulo 2.
		k = 0;
		for (i1 = 1; i1 <= Qp; i1++) {
			p = d2_[i1];
			if (ns_p[p] <= 0)
				continue;
			for (j = 1; j <= nTurmasP[p]; j++) {
				if (q_0[p][j] > 0) {
					k++;
					Ap[k] = p;
					Aa[k] = lista[p][j];
					j_[k] = j;
				}
			}
		}
		n2 = k;
		Ap[k + 1] = 0;

		// Calcula o arranjo z e zera a distribuição dd dos dias tratados (que sera preenchida por atualiza4).
		determinaZ(n2);
		for (p = 1; p <= Qp; p++) {
			for (j = 1; j <= nTurmasP[p]; j++) {
				dd[p][j][d_0] = 0;
				dd[p][j][d_1] = 0;
				dd[p][j][d_2] = 0;
				dd[p][j][d_3] = 0;
			}
		}
	}

	// getP e getA: devolvem o Prof. e a turma da unidade imax, para o Módulo 2 saber onde este módulo falhou (e para qual unidade do caso
	//   2 recuar).
	public int getP(int imax) {
		return Ap[imax];
	}

	public int getA(int imax) {
		return Aa[imax];
	}

	// Caso 4 (Seção 4.4.1): sucessor de x[i], número de aulas de (p, turma) no 1o dia do par. Retorna false se há sucessor (em x0), true
	//   caso contrário.
	public boolean maximo_4() {
		int maxX0_, minX0, p;
		boolean initialized;
		p = Ap[i];
		x0 = x_[i];
		// n0 = aulas de (p, turma) no par. Se n0 == 0, o único valor possível e 0 (aceito na 1a visita).
		n0 = q2_[p][j_[i]];
		if (n0 == 0) {
			if (x0 == -1) {
				x0 = 0;
				return false;
			} else
				return true;
		}
		// Não há sucessor se x0 já é o máximo por dia e turma (2, ou 1 para Profs. sem geminadas).
		if (x0 == 2 || (semGeminadasP[p] && x0 == 1))
			return true;
		// Caso trivial: o Prof. só trabalha em um dos dois dias do par (N_2p ou N_3p igual a 0); há um único valor (0 ou n0), tentado uma vez.
		case_Trivial = Np2[p] == 0 || Np3[p] == 0;
		initialized = x0 > -1;
		if (case_Trivial) {
			// Se a unidade já foi visitada, os limites não mudaram: reaproveita maxX0[i].
			if (initialized)
				return true;
			x0 = (Np2[p] == 0 ? 0 : n0);
		} else
			x0++;
		if (initialized)
			return x0 > maxX0[i];
		// Valores já acumulados antes da unidade: Sp0/Sp2 = aulas do Prof. no 1o/2o dia (Sp2_, Sp3_); Sa0/Sa2 = aulas da turma no 1o/2o dia
		//   (Sa_4, Sa_5).
		a = Aa[i];
		Sp0 = Sp2_[apontP[i] - 1];
		Sp2 = Sp3_[apontP[i] - 1];
		Sa0 = Sa_4[apontA2[i] - 1];
		Sa2 = Sa_5[apontA2[i] - 1];

		// Limite inferior: o que não cabe no 2o dia (max. por dia, capacidade restante N_3p - Sp2 do Prof. e Ma - Sa2 da turma).
		minX0 = (semGeminadasP[p] ? n0 - 1 : n0 - 2);
		minX0 = Math.max(minX0, Sp2 + n0 - Np3[p]);
		minX0 = Math.max(minX0, Sa2 + n0 - Ma);
		// Limite superior: max. por dia (1 ou 2), n0, capacidade restante do Prof. no 1o dia (N_2p - Sp0) e da turma (Ma - Sa0).
		maxX0_ = (semGeminadasP[p] ? 1 : 2);
		maxX0_ = Math.min(maxX0_, n0);
		maxX0_ = Math.min(maxX0_, Np2[p] - Sp0);
		maxX0_ = Math.min(maxX0_, Ma - Sa0);
		x0 = Math.max(x0, minX0);
		maxX0[i] = maxX0_;
		return x0 > maxX0_;
	}

	// Atualiza as variáveis de controle após a unidade i receber o valor x0: totais acumulados do Prof. (Sp2_, Sp3_) e da turma (Sa_4,
	//   Sa_5)
	// no 1o e no 2o dia do par (o 2o dia recebe nClasses = q2_ - x0).
	public void atualiza_4_() {
		p = Ap[i];
		nClasses = q2_[p][j_[i]] - x0;
		aux = apontP[i];
		Sp2_[aux] = Sp2_[aux - 1] + x0;
		Sp3_[aux] = Sp3_[aux - 1] + nClasses;
		aux = apontA2[i];
		Sa_4[aux] = Sa_4[aux - 1] + x0;
		Sa_5[aux] = Sa_5[aux - 1] + nClasses;
	}

	// Grava em dd a distribuição obtida: para exec == 0 nos dias d_0 e d_1 (x e q2_ - x); para exec == 1 nos dias d_2 e d_3.
	public void atualiza4(int exec) {
		int i;
		for (i = 1; i <= n2; i++) {
			p = Ap[i];
			j = j_[i];
			if (exec == 0) {
				dd[p][j][d_1] = q2_[p][j] - x_[i];
				dd[p][j][d_0] = x_[i];
			} else {
				dd[p][j][d_3] = q2_[p][j] - x_[i];
				dd[p][j][d_2] = x_[i];
			}
		}
	}

	// Busca do Módulo 3 (Algoritmo 2 do Capítulo 3; gerador = minimo = -1) para o par de dias exec e a possibilidade de dias de trabalho
	//   possib,
	// com Aq2_ = aulas de cada (Prof., turma) no par. Retorna 0 se todas as unidades foram fechadas (sucesso); caso contrário, retorna
	// imax, a unidade mais avancada atingida, para o Módulo 2 saber onde recuar.
	public int principal2(int[][] Aq2_, int exec, int possib) {
		int p0, minimo, k, imax;
		q2_ = Aq2_;
		for (p = 1; p <= Qp; p++) {
			if (ns_p[p] <= 0)
				continue;
			// Para cada Prof. com aulas: x0 = 1 se, na possibilidade em teste, ele trabalha no 1o dia do par. Define N_2p (Np2: máximo de aulas no
			//   1o dia,
			// 0 se não trabalha) e N_3p (Np3: máximo no 2o dia, 0 se não trabalha nele), conforme wp0 (par 1) ou wp1 (par 2) e os dias do par.
			x0 = (possibP[exec][possib][p] ? 1 : 0);
			if (exec == 0) {
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_0]);
				Np3[p] = (wp0[p] - x0 == 0 ? 0 : Mp[p][d_1]);
			} else {
				Np2[p] = (x0 == 0 ? 0 : Mp[p][d_2]);
				Np3[p] = (wp1[p] - x0 == 0 ? 0 : Mp[p][d_3]);
			}
		}

		// Ponteiros apontP (para Sp2_/Sp3_, com posição zerada entre Profs.) e apontA2 (para Sa_4/Sa_5, bloco de 16 posições por turma).
		Sp2_[0] = 0;
		Sp3_[0] = 0;
		aux2 = 1;
		for (j = 1; j <= n2; j++) {
			apontP[j] = aux2;
			aux2++;
			if (Ap[j + 1] != Ap[j]) {
				Sp2_[aux2] = 0;
				Sp3_[aux2] = 0;
				aux2++;
			}
		}

		for (j = 1; j <= Qa; j++) {
			auxA[j] = 1 + 16 * (j - 1);
			Sa_4[auxA[j] - 1] = 0;
			Sa_5[auxA[j] - 1] = 0;
		}
		for (j = 1; j <= n2; j++) {
			apontA2[j] = auxA[Aa[j]];
			auxA[Aa[j]]++;
		}

		// Inicialização da busca: todas as variáveis de decisão com o gerador.
		imax = 0;
		minimo = -1;
		for (i = 1; i <= n2; i++)
			x_[i] = minimo;
		i = 1;
		iMaxI_[1] = 1;
		do {
			nIteracoes++;
			// Unidades sem aulas no par (q2_ == 0) não têm escolha: na ida recebem x = 0 (repassando as variáveis de controle) e são saltadas;
			// no recuo, as que estão imediatamente antes da unidade corrente (do mesmo Prof.) são desfeitas e saltadas.
			if (q2_[Ap[i]][j_[i]] == 0) {
				if (x_[i] == -1) {
					do {
						x_[i] = 0;
						aux = apontP[i];
						Sp2_[aux] = Sp2_[aux - 1];
						Sp3_[aux] = Sp3_[aux - 1];
						aux = apontA2[i];
						Sa_4[aux] = Sa_4[aux - 1];
						Sa_5[aux] = Sa_5[aux - 1];
						i++;
						iMaxI_[i] = i;
					} while (x_[i] == -1 && Ap[i] == Ap[i - 1] && q2_[Ap[i]][j_[i]] == 0);
					if (i > imax)
						imax = i;
					if (i > n2)
						return 0;
					iMaxI_[i] = i;
					for (k = 1; k < i; k++) {
						if (i > iMaxI_[k])
							iMaxI_[k] = i;
					}
				} else {
					while (x_[i] == 0 && Ap[i] == Ap[i - 1] && q2_[Ap[i]][j_[i]] == 0) {
						x_[i] = minimo;
						i--;
					}
				}
			}
			// Com sucessor: guarda x0, atualiza o controle, avanca e atualiza iMaxI_ (unidade mais avancada já atingida). Se i > n2, todas as
			//   unidades foram fechadas.
			if (!maximo_4()) {
				x_[i] = x0;
				atualiza_4_();
				i++;
				if (i > imax)
					imax = i;
				if (i > n2)
					return 0;
				iMaxI_[i] = i;
				for (k = 1; k < i; k++) {
					if (i > iMaxI_[k])
						iMaxI_[k] = i;
				}
			} else {
				// Sem sucessor: recuo do Algoritmo 2 para p0 = z[i][iMaxI_[i] - i]; as unidades após p0 voltam ao gerador. Se não há para onde recuar,
				//   retorna imax (falha).
				if (i == 1)
					return imax;
				p0 = z[i][iMaxI_[i] - i];
				if (p0 == 0)
					return imax;
				for (i = i; i > p0; i--)
					x_[i] = minimo;
				i = p0;
			}
		} while (true);
	}

	// Calcula o arranjo z (método determinaArranjo do Capítulo 3) para a dependência do Módulo 3: z[i][l] = maior unidade anterior a i com
	//   o
	// mesmo Prof. ou a mesma turma que alguma unidade de i a i+l, tornado cumulativo em l.
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
