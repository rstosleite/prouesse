package prouesse;

import javax.swing.ImageIcon;

// Classe FaseA1_Super - encapsula e inicializa variáveis da sua subclasse (Module_1/Module_1B), que executa a Fase 1.1.
//
// Papel: (1) declarar variáveis usadas pelos Módulos 1 e A; (2) no construtor, instanciar as classes dos Módulos 2 e 3
// (via FaseB2_E e Module_2B) e alocar os arranjos de unidades; (3) em inicializacao(), alocar os arranjos de resultados
// (dd, tabelaP, tabelaA) e de controle. Module_A_ também herda desta classe.
public class FaseA1_Super extends Probl {
	// Variáveis auxiliares e de busca do Módulo 1 (i, x0, p, a, j, k, nClasses, Sp0, Sp2, Sa0, Sa2, n0, n2...).
	// nHorarios_P0[p], nHorarios_P1[p] = máximo de aulas de p na segunda-feira (dia 0) e nos outros 4 dias (variáveis de ligação do Módulo
	//   1);
	// ns_p_[p] = carga horária semanal total do Prof. p.
	// possibP, nPossibP, contador, iMin, nIteracoes2, teste1, teste2: variáveis auxiliares (reservadas).
	static public int aux1, aux2, n2, Sp2, Sa2, i1, p, a, j, n0, x0, i, nClasses, aux, min_0, min_1, k, Sp0, Sa0,
			nHorarios_P0[], nHorarios_P1[], iMin, ns_p_[], aux_[];
	static public int nPossibP, possibP[][], contador;
	static public long nIteracoes2;
	static public boolean teste1, teste2;
	// g2_I_W_3 = instância do Módulo 2 (Module_2B), chamada ao final da Fase 1.1;  g2_E = objeto que inicializa as variáveis de
	//   FaseB2_Super.
	static public Module_2B g2_I_W_3;
	static public FaseB2_E g2_E;
	ImageIcon icone;

	// Construtor: aloca estruturas dependentes das dimensões do problema e cria os objetos dos módulos seguintes.
	public FaseA1_Super() {
		// Estruturas auxiliares reservadas (possibP/contador/aux_) e arranjos de permutação dos dias (destino0, destino).
		possibP = new int[100][4];
		contador = 0;
		aux_ = new int[5];
		destino0 = new int[5];
		destino = new int[5];
		// Arranjos por Prof. do Módulo 1: máximos de dias de trabalho (maxDias0/1) e de aulas (nHorarios_P0/1).
		maxDias0 = new int[Qp + 1];
		maxDias1 = new int[Qp + 1];
		nHorarios_P0 = new int[Qp + 1];
		nHorarios_P1 = new int[Qp + 1];
		// Cria FaseB2_E (que aloca os arranjos de FaseB2_Super) e o objeto do Módulo 2; o construtor deste último cria o Módulo 3.
		// Assim, ao se criar o Módulo 1, toda a cadeia de módulos fica pronta para uso.
		g2_E = new FaseB2_E();
		g2_I_W_3 = new Module_2B();
		// amplitude e d2_: chave de ordenação e permutação usadas para ordenar dias (índices 0 a 4) e Profs. (índices 1 a Qp).
		amplitude = new float[Qp + 1];
		d2_ = new int[Qp + 1];
		// mantemP, ns_p_, ns_p: indicadores/arranjos por Prof.
		mantemP = new boolean[Qp + 1];
		ns_p_ = new int[Qp + 1];
		ns_p = new int[Qp + 1];
		// Dimensiona os arranjos de unidades (Probl): aux = 7 + total de pares (Prof., turma).
		// z e iMaxI_ recebem este tamanho; os arranjos Ap, Aa, j_, A, x_, maxX0 e case_ recebem ainda 4*Qp posições a mais,
		// pois o Módulo 1 usa unidades adicionais por Prof. (casos 510, 501, 1 e 5, além da unidade 511).
		aux = 7;
		for (p = 1; p <= Qp; p++)
			aux += nTurmasP[p];
		z = new short[aux][aux];
		iMaxI_ = new int[aux];
		aux = 4 * Qp + aux;
		Ap = new int[aux];
		Aa = new int[aux];
		j_ = new int[aux];
		A = new int[aux];
		x_ = new int[aux];
		maxX0 = new int[aux];
		case_ = new int[aux];
		d2_ = new int[Qp + 1];
	}

	// Aloca os arranjos de resultados e de controle (e zera). Deve ser chamado antes de iniciar o processamento (ver Prouesse5D_X).
	public void inicializacao() {
		int p1, aux2, p;
		// Quadros de horários finais: um por Prof. (tabelaP) e um por turma (tabelaA), com 5 dias e Ma horários por dia.
		tabelaP = new int[Qp + 1][5][Ma + 1];
		tabelaA = new int[Qa + 1][5][Ma + 1];
		// Marcadores booleanos de ocupação (reservados) e totais de aulas por turma (Sa0_, Sa1_).
		baliP = new boolean[5][Qp + 1][Ma + 1];
		baliA = new boolean[5][Qa + 1][Ma + 1];
		Sa0_ = new int[Qa + 1];
		Sa1_ = new int[Qa + 1];
		ns_p = new int[Qp + 1];
		d2_ = new int[Qp + 1];
		// dd[p] = matriz [turma j][dia 0..4] com a distribuição de aulas;  q_[p] = arranjo auxiliar por turma do Prof.
		dd = new int[Qp + 1][][];
		q_ = new int[Qp + 1][];
		for (p1 = 1; p1 <= Qp; p1++) {
			dd[p1] = new int[nTurmasP[p1] + 1][5];
			q_[p1] = new int[nTurmasP[p1] + 1];
		}
	}

}
