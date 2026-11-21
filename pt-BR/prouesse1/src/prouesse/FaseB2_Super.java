package prouesse;

import java.util.BitSet;

// Classe FaseB2_Super - encapsula variáveis (estáticas) usadas pelas suas subclasses.
//
// Papel: reunir as variáveis de trabalho dos Módulos 2, 3 e 4 (Module_2A/2B/2B_V1, Module_3/3_V1 e Module_4),
// que herdam desta classe e, portanto, compartilham os mesmos arranjos. Esta classe não contém lógica: as variáveis
// são apenas declaradas aqui e são alocadas pela subclasse FaseB2_E (e pelos construtores dos módulos).
// Nomes do código x notação do livro (Seções 4.1.2, 4.3, 4.4 e 4.5) estão nos comentários abaixo.
public class FaseB2_Super extends Probl {

	// --- Variáveis de busca ---
	// i      = índice da unidade corrente;  x0 = candidato a sucessor de x[i] determinado pelos métodos maximo*;
	// i1, p, j, a, k, aux* = índices e variáveis temporárias;  n0 = número de aulas da unidade; nClasses = aulas no segundo grupo de dias.
	// case_, Ap, Aa, j_, A, x_, maxX0, iMaxI_: redeclaram os arranjos de unidades (ver Probl); nesta hierarquia são os usados
	// pelos Módulos 2 e 4. Os Módulos 1, 3 e 5 usam os arranjos homônimos de Probl (conjunto separado).
	//
	// --- Variáveis de ligação e controle (Seções 4.3 a 4.5) ---
	// wp0[p], wp1[p] = w_0p, w_1p: dias de trabalho de p em (d0,d1) e em (d2,d3);   Wp1[p] = W_1p: máximo de dias nos 4 dias da fase.
	// Np0[p], Np1[p] = N_0p, N_1p: máximo de aulas de p em (d0,d1) e em (d2,d3);
	// Np2[p], Np3[p] = N_2p, N_3p: máximo de aulas de p no 1o e no 2o dia do par (casos 3, 35 e Módulos 3 e 4);
	// Np2_0[p], Np3_0[p] = o mesmo para o par (d2,d3) (caso 30/305).  Np2_, Np3_, Sa02_, Sa03_: reservadas.
	// Dp[p] = D_p (número mínimo de dias de trabalho); complex_p0[p], complex_p1[p] = complex_0p, complex_1p;
	// x_case_1[p], x_case_10[p] = x_caso1, x_caso10 (valor da unidade dos casos 1 e 10 do Prof. p).
	// q_0[p][j] = aulas do Prof. p na j-ésima turma nos dias tratados pelo módulo; q2_[p][j] e q2_0[p][j] = aulas dessa turma
	// no primeiro e no segundo grupo de dias (resultado do caso 2).
	// Sp0_, Sp1_, Sp2_, Sp3_, Sa_0, Sa_1, Sa_4, Sa_5 = variáveis de controle S_p0_, S_p1_, S_p2_, S_p3_, S_a0, S_a1, S_a4, S_a5.
	// Os arranjos Sa_* guardam, para cada turma, um bloco de 16 posições (auxA[a] = próxima posição livre do bloco da turma a);
	// apontP, apontP2, apontA, apontA2 = ponteiros que dizem, para cada unidade, em que posição dos arranjos de controle ela atua.
	// d_0..d_3 (e dias[]) = dias tratados pelo módulo;  nDias_ = {0,1,1,2,3}: número de dias de trabalho associado a cada valor de x[k]
	// quando complex = true.  possibP[exec][k][p] = k-ésima possibilidade de dias de trabalho (true se p trabalha no 1o dia do par);
	// nPossibP[exec] = quantidade de possibilidades;  exec = 0 para o par (d0,d1) e 1 para o par (d2,d3).
	// g2_ = instância do Módulo 3 chamada pelo Módulo 2;  sucesso, teste*, case_Trivial = indicadores booleanos temporários.
	static protected int i, x0, i1, p, j, a, k, nClasses, Sp0, Sa0, Sp2, Sa2, aux0, n0, aux1, aux2, wp0[], wp1[], Np0[],
			Np1[], Np2[], Np3[], Np2_0[], Np3_0[], Np2_[], Np3_[], x1, dias[], d_0_, d_1_, d_0, d_1, d_2, Wp1[],
			Sa02_[], Sa03_[], imax, case_[], Ap[], Aa[], j_[], A[], x_[], maxX0[], n2, q2_[][], q2_0[][], q_0[][],
			apontP[], apontP2[], apontA[], apontA2[], aux, Sa_0[], Sa_1[], Sa_4[], Sa_5[], Sp0_[], Sp1_[], Sp2_[],
			Sp3_[], cont, iMaxI_[], auxA[];

	static protected boolean teste1, teste2, case_Trivial;
	static protected int Dp[], arranjo[], nDias_[], x_case_1[], x_case_10[], d_3, aux4, aux5, aux41, aux51, sumA_0[],
			sumA_1[], nPossibP[], i1_, possib, exec;
	static protected boolean complex_p0[], complex_p1[], sucesso, teste, possibP[][][];
	static protected long maxIteracoes2, nIteracoes;
	static protected BitSet bitSet0, bitSet1;
	static protected Module_3 g2_;
	static protected short z[][];

}
