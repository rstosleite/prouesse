package prouesse;

import java.util.Arrays;

import javax.swing.*;

// Classe Horar_ - métodos utilizados pela classe executável Prouesse_teste, que herda desta classe.
//
// Papel: guardar, em variáveis estáticas, o estado compartilhado da interface (dados do problema lidos do JSON, nomes de Profs.
// e turmas, textos exibidos) e fornecer (1) a verificação de uma distribuição já existente (testaDistribuicao) e (2) a formatação
// dos quadros de horários em texto (exibeHorariosPA2, exibeHorarios2 e obtem_quadro_horários). Herda de Editor_, que
// implementa os componentes visuais.
public class Horar_ extends Editor_ {

	// Dados do problema (ver Seção 2.1 do livro): Ma, Qp, Qa, Mp (máximo de aulas por dia), Wp0 (máximo de dias de trabalho), nTurmasP,
	// lista (números das turmas de cada Prof.), ns (cargas horárias, indexadas pela posição da turma em lista), horariosND (horários
	// não disponíveis), semGeminadasP e comGeminadasP; distrPA[p][a][k]: distribuição no formato 'por turma' (a é o número da
	// turma, não a posição j em lista) lida de distribuição.json; nomeP e nomeA: nomes dos Profs. e das turmas; s, s_, s9 e sErros_:
	// textos para as abas da interface; temDistrPronta: indica se há uma distribuição aproveitável.
	static protected int linha, distrPA[][][], Ma, Qp, Qa, Mp[][], sequenciaProf[], chP[];
	static protected boolean teste, teste2, temDistrPronta;

	static protected int k, p, d1, p1, aux, aux1, aux2, k1, a, cont, d, soma[], j, nTurmasP[], lista[][], ns[][], Wp0[];
	static protected boolean horariosND[][][], semGeminadasP[], comGeminadasP[];

	static protected String sErros_, nomeP[], nomeA[], s9, s, s_;
	static protected char c;
	static protected int codigo, apont, contaLinhas, tipoDeAba;

	// Ícone usado nas caixas de diálogo.
	ImageIcon icone;

	// Construtor: inicializa a interface (Editor_), carrega o ícone e marca que ainda não há distribuição pronta.
	public Horar_() {
		super();
		icone = new ImageIcon("forward.gif");
		temDistrPronta = false;
	}

	// Verifica se a distribuição armazenada em distrPA é válida para os dados do problema atual, isto é, se respeita as restrições
	// de viabilidade da Seção 2.2. Retorna true se puder ser aproveitada. (Prouesse_teste redefine este método com uma verificação
	// adicional dos números de Profs. e turmas.)
	public boolean testaDistribuicao() {
		int a, j;
		// As dimensões da distribuição devem coincidir com o número de Profs. e de turmas do problema.
		if (Qp != distrPA.length - 1 || Qa != distrPA[1].length - 1)
			return false;

		// Em cada dia, cada turma recebe no máximo Ma aulas.
		for (a = 1; a <= Qa; a++) {
			for (k = 0; k <= 4; k++) {
				aux = 0;
				for (p1 = 1; p1 <= Qp; p1++)
					aux += distrPA[p1][a][k];
				if (aux > Ma)
					return false;
			}
		}
		// Para cada Prof.: nenhuma turma tem mais que 1 aula por dia (se semGeminadasP) ou 2; o total do dia não excede Mp[p][k]; o número de dias de trabalho não excede Wp0.
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
			if (cont > Wp0[p1])
				return false;

			// O total de aulas de cada turma do Prof. deve ser igual à carga horária ns correspondente.
			for (a = 1; a <= Qa; a++) {
				aux = 0;
				for (k = 0; k <= 4; k++)
					aux += distrPA[p1][a][k];
				if (aux > 0) {
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

	// Monta o texto do quadro de horários de uma entidade k (um Prof. ou uma turma): uma linha por horário do dia e uma coluna por dia da semana.
	// 'tabela' é tabelaP (turma de cada Prof. em cada horário) ou tabelaA (Prof. de cada turma em cada horário); 'nome' traz os nomes a exibir.
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

	// Monta o texto com os quadros de todos os Profs. e de todas as turmas.
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

	// Monta o texto dos quadros de horários de Qp entidades (Profs. ou turmas), dispostas de duas em duas lado a lado, separadas por '||'.
	public String obtem_quadro_horarios(int[][][] tabelaP, String[] nomeP, String[] nomeA, int Qp) {
		int h, k, p, aux;
		String s = "";
		p = 1;
		while (p <= Qp) {
			// Cabeçalho: número e nome do primeiro e, se existir, do segundo quadro da linha.
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
			// Linha com os dias da semana.
			s += "SEG       \tTER       \tQUA       \tQUI       \tSEX       \t||       \t";
			if (p + 1 <= Qp)
				s += "SEG       \tTER       \tQUA       \tQUI       \tSEX       \t";
			s += "\n";
			// Uma linha por horário: para cada dia, o nome da turma (ou do Prof.) ou '---------' se o horário está vago.
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
			// Passa para o próximo par de quadros.
			p = p + 2;
		}
		return s;
	}

}
