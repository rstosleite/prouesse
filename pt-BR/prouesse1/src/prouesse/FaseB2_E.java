package prouesse;

// Classe FaseB2_E - inicializa (aloca) as variáveis da sua superclasse FaseB2_Super.
//
// Papel: tudo o que esta classe faz está no construtor, que dimensiona os arranjos de acordo com os dados do problema
// (Qp, Qa e nTurmasP). É instanciada uma única vez, pelo construtor de FaseA1_Super.
public class FaseB2_E extends FaseB2_Super {

	// Construtor: aloca todos os arranjos usados pelos Módulos 2, 3 e 4.
	public FaseB2_E() {
		int p1, aux_;
		// Arranjos por Prof. e por turma do Prof.: q_0, q2_ e q2_0 (aulas da turma nos dias de cada fase). Linha p tem nTurmasP[p]+1 posições
		//   (as turmas são numeradas a partir de 1).
		q_0 = new int[Qp + 1][];
		q2_ = new int[Qp + 1][];
		q2_0 = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++) {
			q_0[p1] = new int[nTurmasP[p1] + 1];
			q2_[p1] = new int[nTurmasP[p1] + 1];
			q2_0[p1] = new int[nTurmasP[p1] + 1];
		}
		// Arranjos indexados por Prof. (posição 0 não usada): dias de trabalho (wp0, wp1, Wp1) e máximos de aulas (Np0..Np3_0).
		dias = new int[4];
		wp0 = new int[Qp + 1];
		wp1 = new int[Qp + 1];
		Np0 = new int[Qp + 1];
		Np1 = new int[Qp + 1];
		Np2 = new int[Qp + 1];
		Np3 = new int[Qp + 1];
		Np2_ = new int[Qp + 1];
		Np3_ = new int[Qp + 1];
		Np2_0 = new int[Qp + 1];
		Np3_0 = new int[Qp + 1];
		Wp1 = new int[Qp + 1];
		// Arranjos indexados por turma: auxA (próxima posição livre no bloco da turma) e Sa02_/Sa03_ (reservados).
		auxA = new int[Qa + 1];
		Sa02_ = new int[Qa + 1];
		Sa03_ = new int[Qa + 1];
		// Calcula o tamanho máximo dos arranjos de unidades: aux_ = total de pares (Prof., turma).
		// O número de unidades do Módulo 2 e da ordem de 7*Qp + aux_ (ou 2*Qp + 2*aux_), mais uma folga; usa-se o maior dos dois valores.
		aux_ = 0;
		for (p = 1; p <= Qp; p++)
			aux_ += nTurmasP[p];
		aux = Math.max(7 * Qp + aux_ + 10, 2 * Qp + 2 * aux_ + 6);
		// Arranjos de unidades (ver Probl): Prof., turma, índice j, número de aulas A, variável de decisão x, limite maxX0, caso e iMaxI_.
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		iMaxI_ = new int[aux];
		// Matriz z do Algoritmo 2, com dimensão n x n.
		z = new short[aux][aux];

		// Variáveis de controle por Prof. do caso 2 do Módulo 2 (Sp0_, Sp1_) e ponteiros apontP2 e apontA.
		Sp0_ = new int[aux];
		Sp1_ = new int[aux];
		apontP2 = new int[aux];
		apontA = new int[aux];

		// Variáveis de controle por Prof. do Módulo 3/4 (Sp2_, Sp3_) e ponteiros apontP e apontA2 (número de unidades menor).
		aux = 2 * Qp + 2 * aux_ + 6;
		Sp2_ = new int[aux];
		Sp3_ = new int[aux];
		apontP = new int[aux];
		apontA2 = new int[aux];
		// Variáveis de controle por turma (Sa_0, Sa_1, Sa_4, Sa_5): um bloco de 16 posições para cada turma, mais folga.
		aux = 16 * Qa + 2;
		Sa_0 = new int[aux];
		Sa_1 = new int[aux];
		Sa_4 = new int[aux];
		Sa_5 = new int[aux];
	}
}
