package prouesse;

import javax.swing.ImageIcon;

// Classe FaseA1_Super - encapsula e inicializa variáveis de sua subclasse Module_2A (e de Module_A).
//
// Papel: declara e aloca as variáveis do Módulo 2A e as estruturas de resultados (distribuição e quadros de horários). O
// construtor cria também o objeto FaseB2_E (que aloca os arranjos compartilhados) e o Módulo 2B, que o Módulo 2A chama ao terminar.
public class FaseA1_Super extends Probl {

	// Variáveis estáticas herdadas pelos Módulos 2A e A: índices e valores auxiliares (i, k, p, a, j, i1, n0, x0, n2, nClasses, aux*,
	// Sp0, Sp2, Sa0, Sa2) e alguns arranjos (nHorarios_P0, nHorarios_P1, ns_p_, aux_, somaMax, somaMax2, Sp2_, Sp3_, apontP) alocados
	// no construtor. Parte deles não é usada pelas classes derivadas desta versão, que declaram campos próprios de mesmo nome.
	static public int aux1, aux2, n2, Sp2, Sa2, i1, p, a, j, n0, x0, i, nClasses, aux, min_0, min_1, k, Sp0, Sp2_[],
			Sp3_[], apontP[], Sa0, nHorarios_P0[], nHorarios_P1[], iMin, ns_p_[], aux_[], somaMax[], somaMax2[][];

	// nPossibP, possibP e contador: campos auxiliares (Module_A declara campos próprios de mesmo nome para a contagem de possibilidades).
	static public int nPossibP, possibP[][], contador;
	// Contador de iterações em precisão long.
	static public long nIteracoes2;

	// Indicadores booleanos auxiliares.
	static public boolean teste1, teste2;

	// g2_I_W_: objeto do Módulo 2B; g2_E: objeto que aloca os arranjos compartilhados (FaseB2_E).
	static public Module_2B g2_I_W_;
	static public FaseB2_E g2_E;
	// Ícone usado nas caixas de diálogo.
	ImageIcon icone;

	// Construtor: aloca arranjos auxiliares, cria FaseB2_E e Module_2B e prepara os arranjos de ordenação de dias e Profs.
	public FaseA1_Super() {
		possibP = new int[100][4];
		somaMax = new int[5 * (Qp + 1)];
		somaMax2 = new int[5 * (Qp + 1)][Qa + 1];
		contador = 0;
		// aux_, destino0 e destino têm 5 posições, uma por dia da semana (destino0 e destino guardam a permutação dos dias e sua inversa).
		aux_ = new int[5];
		destino0 = new int[5];
		destino = new int[5];
		nHorarios_P0 = new int[Qp + 1];
		nHorarios_P1 = new int[Qp + 1];
		// Cria os objetos cuja inicialização depende de Qp, Qa e nTurmasP.
		g2_E = new FaseB2_E();
		g2_I_W_ = new Module_2B();
		// amplitude e d2_ (ordenação de Profs.), mantemP e cargas: um elemento por Prof.
		amplitude = new float[Qp + 1];
		d2_ = new int[Qp + 1];
		mantemP = new boolean[Qp + 1];
		ns_p_ = new int[Qp + 1];
		ns_p = new int[Qp + 1];
		d2_ = new int[Qp + 1];
	}

	// Inicializa as estruturas de resultados (chamado antes da Fase 1).
	public void inicializacao() {
		int p1, p;
		// Quadros de horários: tabelaP[p][dia][horário] (turma do Prof.) e tabelaA[a][dia][horário] (Prof. da turma).
		tabelaP = new int[Qp + 1][5][Ma + 1];
		tabelaA = new int[Qa + 1][5][Ma + 1];
		// baliP e baliA: marcações de horários por dia, Prof. e turma.
		baliP = new boolean[5][Qp + 1][Ma + 1];
		baliA = new boolean[5][Qa + 1][Ma + 1];
		Sa0_ = new int[Qa + 1];
		Sa1_ = new int[Qa + 1];
		ns_p = new int[Qp + 1];
		d2_ = new int[Qp + 1];
		// dd[p][j][dia]: distribuição (aulas do Prof. p na j-ésima turma em cada um dos 5 dias); q_[p][j]: vetor auxiliar.
		dd = new int[Qp + 1][][];
		q_ = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++) {
			dd[p1] = new int[nTurmasP[p1] + 1][5];
			q_[p1] = new int[nTurmasP[p1] + 1];
		}
	}

}
