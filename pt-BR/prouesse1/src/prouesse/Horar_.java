package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Classe Horar_ - métodos de apoio da classe executável Prouesse5D_X (que herda desta classe).
//
// Papel: guardar o estado compartilhado da interface (dados do problema lidos do JSON, nomes de Profs. e turmas, textos
// exibidos) e fornecer (1) a verificação de uma distribuição já existente e (2) a formatação dos quadros de horários em texto.
// Herda de Editor_, que implementa os componentes visuais.
public class Horar_ extends Editor_ {
	// Campos estáticos: dados do problema (Ma, Qp, Qa, Mp, nTurmasP, lista, ns, Wp0, horariosND, semGeminadasP, comGeminadasP; ver Probl)
	// e distrPA[p][a][k] = distribuição no formato 'por turma' (a é o número da turma, não a posição j em lista);
	// nomeP[] e nomeA[] = nomes dos Profs. e das turmas; s, s_, s9, sErros_ = textos para as abas da interface;
	// temDistrPronta = indica que existe uma distribuição aproveitável (arquivo distribuicao.json).
	static protected int linha, distrPA[][][], Ma, Qp, Qa, Mp[][], sequenciaProf[], chP[];
	static protected boolean teste, teste2, temDistrPronta;
	static protected int k, p, d1, p1, aux, aux1, aux2, k1, a, cont, d, soma[], j, nTurmasP[], lista[][], ns[][], Wp0[];
	static protected boolean horariosND[][][], semGeminadasP[], comGeminadasP[];
	static protected String sErros_, nomeP[], nomeA[], s9, s, s_;
	static protected char c;
	static protected int codigo, apont, contaLinhas, tipoDeAba;

	ImageIcon icone;

	public Horar_() {
		super();
		// Construtor: inicializa o ícone usado nas caixas de diálogo e marca que ainda não há distribuição pronta.
		icone = new ImageIcon("forward.gif");
		temDistrPronta = false;
	}

	// Verifica se a distribuição armazenada em distrPA é válida para os dados do problema atual, isto e, se respeita as
	// restrições de viabilidade da Seção 2.2. Retorna true se puder ser aproveitada.
	public boolean testaDistribuicao() {
		int a, j;
		// 1) As dimensões da distribuição devem coincidir com o número de Profs. e de turmas do problema.
		if (Qp != distrPA.length - 1 || Qa != distrPA[1].length - 1)
			return false;

		// 2) Em cada turma e em cada dia, o total de aulas (somando todos os Profs.) não pode exceder Ma.
		for (a = 1; a <= Qa; a++) {
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux += distrPA[p1][a][k];
				if (aux > Ma)
					return false;
			}
		}
		// 3) Para cada Prof.: em cada dia, o número de aulas por turma deve ser no máximo 1 (sem geminadas) ou 2; o total do dia não pode
		//   exceder Mp;
		for (p1 = 1; p1 <= Qp; p1++) {
			cont = 0;
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (a = 1; a <= Qa; a++) {
					aux += distrPA[p1][a][k];
					if (distrPA[p1][a][k] > (semGeminadasP[p1] ? 1 : 2) || distrPA[p1][a][k] < 0)
						return false;
				}
				if (aux > 0)
					cont++;
				if (aux > Mp[p1][k])
					return false;
			}
			// O número de dias com aula não pode ultrapassar Wp0[p1] (máximo de dias de trabalho).
			if (cont > Wp0[p1])
				return false;

			for (a = 1; a <= Qa; a++) {
				aux = 0;
				for (k = 0; k <= 4; k++)
					aux += distrPA[p1][a][k];
				if (aux > 0) {
					// 4) Para cada turma com aulas do Prof., procura-se a posição j dessa turma na lista do Prof. (j == 0: a turma não é do Prof.);
					// o total de aulas na semana deve ser exatamente ns[p1][j].
					j = nTurmasP[p1];
					while (j > 0 && lista[p1][j] != a)
						j--;
					if (j == 0 || aux != ns[p1][j])
						return false;
				}
			}
		}
		return true;
	}

	// Monta o texto do quadro de horários de um Prof. (ou de uma turma) k, com os dias nas colunas e os horários nas linhas.
	// tabela = tabelaP ou tabelaA; nome[] = nomes que serão exibidos em cada célula ('----------' se não há aula).
	public String exibeHorariosPA2(int[][][] tabela, String[] nome, int k) {
		int h1, k1, aux;
		String s;
		s = "SEG        TER        QUA        QUI        SEX        \n";
		for (h1 = 1; h1 <= Ma; h1++) {
			for (k1 = 0; k1 <= 4; k1++) {
				aux = tabela[k][k1][h1];
				if (aux != 0)
					s += nome[aux] + " ";
				else
					s += "---------- ";
			}
			s += "\n";
		}
		return s;
	}

	// Monta o texto com os quadros de horários de todos os Profs. e, em seguida, de todas as turmas.
	public String exibeHorarios2() {
		int k;
		String s;
		s = "";
		for (k = 1; k <= Qp; k++) {
			s += "\nPROF. " + k + " " + nomeP[k] + "\n";
			s += exibeHorariosPA2(Probl.tabelaP, nomeA, k);
		}
		for (k = 1; k <= Qa; k++) {
			s += "\nTURMA " + k + " " + nomeA[k] + "\n";
			s += exibeHorariosPA2(Probl.tabelaA, nomeP, k);
		}
		return s;
	}

	// Monta o texto dos quadros de horários para exibição na aba de resultados: os quadros são apresentados dois a dois, lado a lado
	// (Prof./turma p e p+1), separados por '||', com colunas separadas por tabulação.
	// tabelaP = tabela com os horários; nomeP = nomes das linhas (quadros); nomeA = nomes das células; Qp = número de quadros.
	public String obtem_quadro_horarios(int[][][] tabelaP, String[] nomeP, String[] nomeA, int Qp) {
		int h, k, p, aux;
		String s = "";
		p = 1;
		while (p <= Qp) {
			s += p + "\t" + nomeP[p] + "\t";
			for (k = 1; k <= 3; k++)
				s += " \t";
			s += "||      \t";
			if (p + 1 <= Qp) {
				s += (p + 1) + "\t" + nomeP[p + 1] + "\t";
				for (k = 1; k <= 3; k++)
					s += " \t";
			}
			s += "\n";
			s += "SEG       \tTER       \tQUA       \tQUI       \tSEX       \t||       \t";
			if (p + 1 <= Qp)
				s += "SEG       \tTER       \tQUA       \tQUI       \tSEX       \t";
			s += "\n";
			for (h = 1; h <= Ma; h++) {
				for (k = 0; k <= 4; k++) {
					aux = tabelaP[p][k][h];
					if (aux != 0)
						s += nomeA[aux] + " \t";
					else
						s += "---------\t";
				}
				s += "||      \t";
				if (p + 1 <= Qp) {
					for (k = 0; k <= 4; k++) {
						aux = tabelaP[p + 1][k][h];
						if (aux != 0)
							s += nomeA[aux] + " \t";
						else
							s += "---------\t";
					}
				}
				s += "\n";
			}
			s += "\n";
			p = p + 2;
		}
		return s;
	}

}
