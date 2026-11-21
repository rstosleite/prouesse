# Prouesse — School Timetable Generator · Gerador de Horários Escolares

**English** · [Português](#português)

---

## English

**Prouesse** is free software (Java) that generates **class timetables for elementary and high schools**. It treats timetabling as a *constraint satisfaction problem* and solves it with backtracking search algorithms. For each teacher it lets you:

- limit the number of lessons per weekday;
- limit the number of working days per week, guaranteeing days off;
- set unavailable periods on each weekday;
- state a preference (or not) for double periods.

There is no guarantee that a solution exists for every set of constraints, but Prouesse searches hard for one.

The code is explained and documented in the book **_An Applied Study of the Constraint Satisfaction Problem_** by Raimundo Santos Leite. This repository holds the source code discussed in the book (Algorithms **P1** and **P2**) and the ready-to-run programs.

| I want to… | Go to |
|---|---|
| **Download and use the program** (JARs, manual, configuration page) | [**Prouesse-EN.zip**](../../releases/latest/download/Prouesse-EN.zip) · [all releases](../../releases) · folder [`en/programs`](en/programs) |
| **Read the source code of Algorithm P1** | [`en/prouesse1`](en/prouesse1) |
| **Read the source code of Algorithm P2** | [`en/prouesse2`](en/prouesse2) |
| More details in English | [`en/README.md`](en/README.md) |

Requires Java 16 or later to run the `.jar` files.

---

## Português

O **Prouesse** é um software livre (Java) que gera **quadros de horários de aulas para escolas de ensino fundamental e médio**. Ele trata a elaboração de horários como um *problema de satisfação de restrições* e o resolve com algoritmos de busca com retrocesso (*backtracking*). Para cada professor é possível:

- limitar o número de aulas por dia da semana;
- limitar o número de dias de trabalho por semana, garantindo folgas;
- indicar horários não disponíveis em cada dia da semana;
- informar se o professor tem preferência (ou não) por aulas geminadas.

Não há garantia de que exista solução para qualquer conjunto de restrições, mas o Prouesse se empenha em encontrá-la.

O código-fonte é explicado e documentado no livro em português sobre o tema (Algoritmos **P1** e **P2**). Este repositório reúne o código-fonte discutido no livro e os programas prontos para uso.

| Quero… | Acesse |
|---|---|
| **Baixar e usar o programa** (JARs, manual, página de configuração) | [**Prouesse-PT.zip**](../../releases/latest/download/Prouesse-PT.zip) · [todas as versões](../../releases) · pasta [`pt-BR/programas`](pt-BR/programas) |
| **Ler o código-fonte do Algoritmo P1** | [`pt-BR/prouesse1`](pt-BR/prouesse1) |
| **Ler o código-fonte do Algoritmo P2** | [`pt-BR/prouesse2`](pt-BR/prouesse2) |
| Mais detalhes em português | [`pt-BR/README.md`](pt-BR/README.md) |

Requer Java 16 ou superior para executar os arquivos `.jar`.

---

## License · Licença

Source code and programs: [Apache License 2.0 with Commons Clause](LICENSE) — free to use, copy, modify and share by anyone, including private schools and companies, but **not to be sold** (this includes paid hosting, consulting and support based on the software, unless permitted by the licensor). Book text and manuals are not covered by this license. See [NOTICE.md](NOTICE.md).

Código-fonte e programas: [Apache License 2.0 com Commons Clause](LICENSE) — uso, cópia, modificação e compartilhamento livres por qualquer pessoa, inclusive escolas particulares e empresas, mas **não pode ser vendido** (isso inclui hospedagem, consultoria e suporte pagos baseados no software, salvo autorização do titular). O texto dos livros e os manuais não são cobertos por esta licença. Veja [NOTICE.md](NOTICE.md).

**Keywords:** timetabling, school timetable, class schedule generator, constraint satisfaction problem (CSP), backtracking, Java · *horários escolares, quadro de horários, gerador de horários, problema de satisfação de restrições, Java*
