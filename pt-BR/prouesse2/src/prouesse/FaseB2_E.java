package prouesse;

// Classe FaseB2_E - inicializa variáveis de sua superclasse Probl.
//
// Papel: o construtor aloca os arranjos compartilhados pelos módulos de distribuição (2A, 2B, 3A e 3B): variáveis de ligação
// (Np0 a Np3, wp0, wp1, Wp1), arranjos das unidades da busca (Ap, Aa, j_, A, x_, maxX0, case_, iMaxI_, z, y2) e variáveis de
// controle (Sp0_ a Sp3_, Sa_0, Sa_1, Sa_4, Sa_5, com seus ponteiros). É criada por FaseA1_Super.
public class FaseB2_E extends Probl {

	// Construtor: aloca os arranjos.
	public FaseB2_E() {
		int p1;

		// q2_[p][j]: aulas do par (p, j-ésima turma) em uma parte da semana (um dos conjuntos de dias da distribuição).
		q2_ = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++)
			q2_[p1] = new int[nTurmasP[p1] + 1];

		// Arranjo de dias considerados.
		dias = new int[4];

		// wp0 e wp1: dias de trabalho de cada Prof. em partes da semana.
		wp0 = new int[Qp + 1];
		wp1 = new int[Qp + 1];

		// Variáveis de ligação Np0 a Np3 (e variantes com sufixo _ e _0): máximos de aulas de cada Prof. em partes da semana ou em dias.
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		Np2 = new int[Qp + 1];
		Np3 = new int[Qp + 1];
		Np2_ = new int[Qp + 1];
		Np3_ = new int[Qp + 1];
		Np2_0 = new int[Qp + 1];
		Np3_0 = new int[Qp + 1];

		// Wp1[p]: máximo de dias de trabalho de p entre os dias do módulo; auxA[a]: ponteiro auxiliar por turma.
		Wp1 = new int[Qp + 1];

		auxA = new int[Qa + 1];

		// Número de posições para as unidades dos Módulos 2A e 2B: 2 * Qp + (total de turmas dos Profs.) + 7. Os arranjos Ap, Aa, j_,
		// A, x_, maxX0, case_ e iMaxI_ guardam os dados das unidades; z é a matriz de recuo; apontP2 e apontA são ponteiros das
		// variáveis de controle; Sp0_ e Sp1_ guardam as aulas acumuladas dos Profs.
		aux = 0;
		for (p = 1; p <= Qp; p++)
			aux += nTurmasP[p];
		aux = 2 * Qp + aux + 7;
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		iMaxI_ = new int[aux];
		z = new short[aux][aux];
		apontP2 = new int[aux];
		apontA = new int[aux];
		Sp0_ = new int[aux];
		Sp1_ = new int[aux];

		// Número de posições para as unidades dos Módulos 3A, 3B e 4: 2 * Qp + 2 * (total de turmas) + 7. y2[p][i] guarda soluções dos
		// Casos 3 e 4 (reaproveitamento após recuos); apontP e apontA2 são ponteiros; Sp2_ e Sp3_ guardam as aulas acumuladas dos Profs. em dias.
		aux = aux - 2 * Qp - 7;
		aux = 2 * Qp + 2 * aux + 7;
		y2 = new short[Qp + 1][aux];
		apontP = new int[aux];
		apontA2 = new int[aux];
		Sp2_ = new int[aux];
		Sp3_ = new int[aux];

		// Sa_0, Sa_1, Sa_4 e Sa_5: aulas acumuladas por turma; cada turma dispõe de 16 posições.
		aux = 16 * Qa + 2;
		Sa_0 = new int[aux];
		Sa_1 = new int[aux];
		Sa_4 = new int[aux];
		Sa_5 = new int[aux];
	}
}
