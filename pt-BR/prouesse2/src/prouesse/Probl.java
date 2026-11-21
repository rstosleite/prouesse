package prouesse;

// Classe Probl - superclasse principal que encapsula as principais variáveis (dados do problema, variáveis de busca e resultados).
//
// Quase todas as classes do programa herdam, direta ou indiretamente, de Probl e compartilham seus campos estáticos. Os campos
// estão agrupados abaixo. Alguns campos (por exemplo pUltimo1, pUltimo2, pUltimo3, pMax, min0, horarCompleto, iMax_, d_0_, d_1_, x1,
// aux5, aux41, aux51, x_case_10, complex_p02, maxIteracoes e maxIteracoes2) não são usados pelas demais classes desta versão.
public class Probl {

	// Dados do problema (Seção 2.1 do livro): Ma = aulas por turma e dia; Qp = número de Profs.; Qa = número de turmas;
	// nTurmasP[p] = quantidade de turmas do Prof. p; lista[p][j] = número da j-ésima turma de p; ns[p][j] = carga horária semanal de p na
	// sua j-ésima turma; Mp[p][k] = máximo de aulas de p no dia k (0 a 4); Wp0[p] = máximo de dias de trabalho de p na semana.
	// Resultados e variáveis comuns: sequenciaProf (arranjo recebido em inicializacao, não usado pelos módulos desta versão); dd[p][j][k] = distribuição (aulas
	// de p na j-ésima turma no dia k); ns_p[p] = carga horária de p nos dias considerados pelo módulo em execução; q_[p][j] = vetor
	// auxiliar por Prof. e turma; maxDias0 e maxDias1 = máximos de dias de trabalho nas duas partes da semana (Módulo 2A);
	// destino0 e destino = permutação dos dias feita na Fase 1 e sua inversa; tempoMaximo = tempo máximo de execução, em minutos.
	static protected int Ma, Qp, Qa, nTurmasP[], lista[][], ns[][], Mp[][], Wp0[], sequenciaProf[], dd[][][], ns_p[],
			q_[][], iMax_, maxDias0[], maxDias1[], pUltimo1, pUltimo2, pUltimo3, destino[], destino0[], tempoMaximo;

	// horariosND[p][k][h] = true se p não está disponível no dia k, no horário h (h de 1 a Ma); semGeminadasP e comGeminadasP =
	// preferências de aulas geminadas (Seção 1.1 do livro); baliP e baliA = marcações de horários (alocadas em FaseA1_Super);
	// mantemP[p] = p tem aulas nos três dias considerados pelo Módulo 4.
	static protected boolean horariosND[][][], semGeminadasP[], comGeminadasP[], baliP[][][], baliA[][][],
			horarCompleto, mantemP[];

	// tabelaP[p][d][h] = número da turma de p no dia d, no horário h (0 = vago); tabelaA[a][d][h] = número do Prof. da turma a no dia d,
	// no horário h (quadros de horários finais); d2_ = arranjo de ordenação de dias ou de Profs.; Sa0_ e Sa1_ = auxiliares por turma.
	static public int tabelaA[][][], tabelaP[][][], Sa0_[], Sa1_[], pMin, pMax, d2_[], min0[];
	// amplitude[] = medida usada para ordenar dias ou Profs.; tempoInicial = instante de início da contagem de tempo de execução.
	static public float amplitude[], tempoInicial;

	// Variáveis de trabalho dos módulos: i, x0, p, j, a, k, aux* etc. (índices e valores temporários); x0 = valor sucessor encontrado
	// por máximo*(); nClasses = número de aulas; Sp0, Sp2, Sa0 e Sa2 = valores lidos das variáveis de controle de Prof. e de turma;
	// wp0 e wp1 = dias de trabalho de p em partes da semana; Np0 a Np3 (e variantes) = variáveis de ligação: máximos de aulas de p em
	// partes da semana ou em dias; Wp1 = máximo de dias de trabalho de p nos dias do módulo; dias, d_0, d_1 e d_2 = dias considerados.
	// Unidades da busca (Capítulo 3): case_[i] = caso da unidade i; Ap[i] e Aa[i] = Prof. e turma da unidade; j_[i] = posição da turma
	// em lista; A[i] = aulas a distribuir; x_[i] = valor atual da variável de decisão; maxX0[i] = limite superior de x_[i]; n2 = número de unidades;
	// q2_[p][j] e q_0[p][j] = aulas do par (p, j) em partes da semana ou nos dias do módulo; iMaxI_ = arranjo iMax do Algoritmo 2.
	// Variáveis de controle (aulas acumuladas por Prof. e por turma; Sp0_, Sp1_, Sp2_, Sp3_, Sa_0, Sa_1, Sa_4, Sa_5) e seus ponteiros
	// (apontP2, apontP, apontA, apontA2), que indicam a posição correspondente a cada unidade.
	static protected int i, x0, i1, p, j, a, k, nClasses, Sp0, Sa0, Sp2, Sa2, aux0, n0, aux1, aux2, wp0[], wp1[], Np0[],
			Np1[], Np2[], Np3[], Np2_0[], Np3_0[], Np2_[], Np3_[], x1, dias[], d_0_, d_1_, d_0, d_1, d_2, Wp1[], imax,
			case_[], Ap[], Aa[], j_[], A[], x_[], maxX0[], p2_[], n2, q2_[][], q_0[][], apontP2[], apontA[], aux,
			Sp2_[], Sp3_[], Sa_4[], Sa_5[], apontP[], apontA2[], Sa_0[], Sa_1[], Sp0_[], Sp1_[], cont, iMaxI_[], auxA[];

	// y2: tipo short usado em FaseB2_E para guardar soluções de Casos 3 e 4 (reaproveitamento após recuos); z: arranjo z do Algoritmo 2 (Capítulo 3).
	static protected short y2[][], z[][];

	// teste1, teste2, teste e sucesso: booleanos auxiliares; case_Trivial: true quando o valor do sucessor é obrigatório.
	static protected boolean teste1, teste2, case_Trivial;

	// Dp[p] = mínimo de dias de trabalho de p; arranjo, nDias_, x_case_1 = auxiliares dos módulos; sumA_0, sumA_1 e sumA_1_ =
	// capacidades acumuladas por turma; nPossibP, i1_, possib, exec = auxiliares; x_case_10 e demais não são usados.
	static protected int Dp[], arranjo[], nDias_[], x_case_1[], x_case_10[], d_3, aux4, aux5, aux41, aux51, sumA_0[],
			sumA_1[], sumA_1_[], nPossibP[], i1_, possib, exec;

	// complex_p0 = indicador de Profs. 'complexos' (Seção 5.2.1); possibP = possibilidades auxiliares (Módulo A).
	static protected boolean complex_p0[], complex_p02[], possibP[][][], sucesso, teste;

	// Contadores de iterações da busca (nIteracoes é verificado a cada 50000 iterações para o limite de tempo).
	static protected long maxIteracoes2, nIteracoes, maxIteracoes;

	// Objetos dos Módulos 3A e 3B criados pelo Módulo 2B.
	static protected Module_3A g2_;
	static protected Module_3B g2_0;

	// Construtor vazio.
	public Probl() {
	};

	// Guarda nas variáveis estáticas os dados do problema (Seção 2.1 do livro) recebidos da interface.
	public void inicializacao(int Ma, int Qp, int Qa, int[] nTurmasP, int[][] lista, int[][] ns, int[][] Mp,
			boolean[][][] horariosND, boolean[] semGeminadasP, boolean[] comGeminadasP, int[] Wp0,
			int[] sequenciaProf) {
		this.Ma = Ma;
		this.Qp = Qp;
		this.Qa = Qa;
		this.nTurmasP = nTurmasP;
		this.lista = lista;
		this.ns = ns;
		this.Mp = Mp;
		this.horariosND = horariosND;
		this.semGeminadasP = semGeminadasP;
		this.comGeminadasP = comGeminadasP;
		this.Wp0 = Wp0;
		this.sequenciaProf = sequenciaProf;
	}
}
