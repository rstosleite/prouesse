package prouesse;

// Classe Classes_acessorias - encapsula e inicializa variáveis de suas subclasses (Module_3A e Module_4).
//
// Papel: declara e aloca os arranjos que guardam as unidades da busca e as variáveis de controle usadas pelos Módulos 3A e 4
// (que estendem esta classe). Eles têm as mesmas funções dos de mesmo nome em Probl (ver comentários lá), mas são campos
// próprios desta classe, com o tamanho adequado às unidades desses módulos.
public class Classes_acessorias extends Probl {

	// Unidades da busca: Ap, Aa, j_, A, case_ e x_ (Prof., turma, posição da turma, aulas, caso e valor de cada unidade); maxX0 (limite
	// superior de x_); iMaxI_ (arranjo iMax do Algoritmo 2); i, n2 e x0 (índice, número de unidades e sucessor).
	protected static int Ap[], Aa[], j_[], A[], case_[], x_[], maxX0[], i, n2, x0, iMaxI_[];

	// Variáveis de controle e ponteiros: Sp0_ e Sp1_ (aulas acumuladas do Prof. no par de dias e no terceiro dia), Sa_0 e Sa_1 (idem
	// para turmas), com os ponteiros apontP2 e apontA.
	protected static int Sp0_[], Sp1_[], Sa_0[], Sa_1[], apontP2[], apontA[];

	// Arranjos z (recuo da busca) e y2 (soluções reaproveitáveis dos Casos 3 e 4).
	protected static short z[][], y2[][];

	// Construtor: aloca os arranjos, cada um com aux posições.
	public Classes_acessorias() {
		aux = 0;
		for (p = 1; p <= Qp; p++)
			aux += nTurmasP[p];
		// aux = 2 * (total de turmas dos Profs.) + 2 * Qp + 7: comporta as unidades de todos os casos (um Caso 1 e um Caso 3 por Prof., um
		// Caso 2 e um Caso 4 por par (Prof., turma) e as unidades de validação).
		aux = 2 * aux + 2 * Qp + 7;
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		iMaxI_ = new int[aux];
		z = new short[aux][aux];
		// y2 reaproveita o arranjo já criado em Probl (por FaseB2_E).
		y2 = Probl.y2;

		apontP2 = new int[aux];
		apontA = new int[aux];
		Sp0_ = new int[aux];
		Sp1_ = new int[aux];

		// Sa_0 e Sa_1: cada turma dispõe de 16 posições consecutivas (uma por unidade da turma, mais uma posição anterior com valor zero).
		aux = 16 * Qa + 2;
		Sa_0 = new int[aux];
		Sa_1 = new int[aux];
	}
}
