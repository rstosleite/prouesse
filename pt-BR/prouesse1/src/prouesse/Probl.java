package prouesse;

// Classe Probl - superclasse principal do Algoritmo P1.
//
// Papel: reunir, em campos estáticos, os dados do problema de horários (Seção 2.1 do livro)
// e as principais variáveis de trabalho que são compartilhadas por todos os módulos.
// Praticamente todas as demais classes do Algoritmo P1 herdam, direta ou indiretamente,
// desta classe e por isso enxergam os mesmos arranjos.
//
// Como é a única classe que não executa nenhum algoritmo de busca, o seu código se resume
// a declarar os campos e a copiar os dados de entrada para eles (método inicialização).
public class Probl {

	// --- Dados do problema (Seção 2.1) ---
	// Ma  = M_a: número de aulas por turma, por dia (no código, 4, 5 ou 6).
	// Qp  = Q_p: número de Profs.;  Qa = Q_a: número de turmas.
	// nTurmasP[p] = quantidade de turmas do Prof. p.
	// lista[p][j] = número da j-ésima turma do Prof. p (cada turma tem um número de 1 a Qa).
	// ns[p][j]    = carga horária semanal do Prof. p na sua j-ésima turma (indexada por j, não pelo número da turma).
	// Mp[p][k]    = M_p: máximo de aulas do Prof. p no dia k (k = 0 e segunda-feira; valor 0 = folga/indisponibilidade).
	// Wp0[p]      = W_0p: número máximo de dias de trabalho do Prof. p na semana.
	// sequenciaProf[]: arranjo recebido junto com os dados do problema (não é consultado pelas classes analisadas).
	//
	// --- Arranjos da busca (Capítulo 3 e Seção 4.1) ---
	// Cada módulo resolve um problema de busca com n unidades; a unidade de índice i tem:
	// case_[i] = tipo (caso) da unidade; Ap[i] = Prof. e Aa[i] = turma a que a unidade se refere;
	// j_[i] = posição da turma Aa[i] no arranjo lista do Prof. Ap[i]; A[i] = número de aulas a distribuir;
	// x_[i] = valor atual da variável de decisão x[i]; maxX0[i] = limite superior de x0 calculado para a unidade i;
	// iMaxI_[i] = iMax[i] do Algoritmo 2 (unidade mais avancada atingida desde que i foi visitada).
	// z = arranjo z do Algoritmo 2 (indica para qual unidade recuar); y2 é usado apenas pelo Módulo 4.
	//
	// --- Resultados e variáveis de apoio ---
	// dd[p][j][k]  = distribuição d: número de aulas do Prof. p, na sua j-ésima turma, no dia k (Seção 2.2).
	// ns_p[p]      = total de aulas do Prof. p nos dias tratados pelo módulo em execução;
	// q_[p][j]     = campo auxiliar alocado junto com dd (não é consultado pelas classes analisadas).
	// maxDias0[p], maxDias1[p] = número máximo de dias de trabalho do Prof. p na segunda-feira (dia 0) e nos outros 4 dias (Módulo 1).
	// destino0[], destino[]    = permutação dos dias feita pelo Módulo 1 (dias mais difíceis primeiro) e sua inversa.
	// amplitude[], d2_[]       = chave de ordenação e permutação correspondente (usadas por ordena2 para ordenar dias e Profs.).
	// tempoMaximo, tempoInicial, nIteracoes = controle do tempo limite de execução informado pelo usuário.
	// imax, pUltimo1..3, min0, pMin, pMax, iMax_, apontP_, apontP, maxIteracoes, maxIteracoes2, horarCompleto:
	// variáveis auxiliares/reservadas; várias não são usadas pelas classes analisadas.
	static protected int Ma, Qp, Qa, nTurmasP[], lista[][], ns[][], Mp[][], Wp0[], sequenciaProf[], Ap[], Aa[], j_[],
			A[], case_[], x_[], maxX0[], iMaxI_[], dd[][][], ns_p[], q_[][], iMax_, maxDias0[], maxDias1[], imax,
			pUltimo1, pUltimo2, pUltimo3, destino[], destino0[], apontP_[], apontP[], min0[], tempoMaximo;
	// horariosND[p][k][h] = true se o Prof. p NÃO está disponível no dia k, no h-ésimo horário (h de 1 a Ma).
	// semGeminadasP[p] = true se p não deve ter aulas geminadas; comGeminadasP[p] = true se é desejável que tenha (Seção 1.1).
	// baliP, baliA, mantemP, horarCompleto: variáveis booleanas auxiliares/reservadas.
	static protected boolean horariosND[][][], semGeminadasP[], comGeminadasP[], baliP[][][], baliA[][][],
			horarCompleto, mantemP[];
	// Resultados finais (preenchidos pelo Módulo 5):
	// tabelaP[p][k][h] = número da turma que o Prof. p atende no dia k, no horário h (0 = sem aula);
	// tabelaA[a][k][h] = número do Prof. que atende a turma a no dia k, no horário h.
	// Sa0_, Sa1_, d2_, pMin, pMax: arranjos/variáveis auxiliares.
	static public int tabelaA[][][], tabelaP[][][], Sa0_[], Sa1_[], pMin, pMax, d2_[];
	static public float amplitude[], tempoInicial;
	static protected long nIteracoes, maxIteracoes, maxIteracoes2;
	// Os arranjos y2 e z são do tipo short para economizar memória (z pode ter dimensão n x n, com n = número de unidades).
	static protected short y2[][], z[][];

	// Construtor vazio: os campos são preenchidos por inicializacao().
	public Probl() {
	};

	// Copia os dados do problema, lidos do arquivo JSON pela classe executável (Prouesse5D_X), para os campos
	// estáticos desta classe, tornando-os acessíveis a todos os módulos.
	// Observe que o parâmetro ch corresponde a variável ns (carga horária semanal por Prof. e turma).
	public void inicializacao(int Ma, int Qp, int Qa, int[] nTurmasP, int[][] lista, int[][] ch, int[][] Mp,
			boolean[][][] horariosND, boolean[] semGeminadasP, boolean[] comGeminadasP, int[] Wp0,
			int[] sequenciaProf) {
		this.Ma = Ma;
		this.Qp = Qp;
		this.Qa = Qa;
		this.nTurmasP = nTurmasP;
		this.lista = lista;
		this.ns = ch;
		this.Mp = Mp;
		this.horariosND = horariosND;
		this.semGeminadasP = semGeminadasP;
		this.comGeminadasP = comGeminadasP;
		this.Wp0 = Wp0;
		this.sequenciaProf = sequenciaProf;
	}
}
