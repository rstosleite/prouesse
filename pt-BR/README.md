# Prouesse — edição em português

Código-fonte e programas que acompanham o livro em português sobre o Prouesse. Edição em inglês: [`../en`](../en/README.md).

## Conteúdo

| Pasta | O que é |
|---|---|
| [`programas/`](programas) | Programas prontos para uso: `Prouesse1.jar`, `Prouesse2.jar`, a página de configuração `pag_config_prouesse_pt.html` e o manual `Manual_Prouesse.pdf`. Também disponíveis em ZIP: [**Prouesse-PT.zip**](../../../releases/latest/download/Prouesse-PT.zip) ([todas as versões](../../../releases)). |
| [`prouesse1/`](prouesse1) | Código-fonte Java do **Algoritmo P1**, conforme discutido no livro (inclui a [errata e complemento da documentação](prouesse1/ERRATA_e_DOCUMENTACAO.md)). |
| [`prouesse2/`](prouesse2) | Código-fonte Java do **Algoritmo P2**, conforme discutido no livro. |

## Usando o programa

O Prouesse é composto por dois programas que trabalham em conjunto (detalhes em `Manual_Prouesse.pdf`):

1. **`pag_config_prouesse_pt.html`** — abra em um navegador para informar os dados (professores, turmas, restrições) e exportar o arquivo de dados. Requer conexão com a internet (carrega jQuery e Bootstrap de CDNs).
2. **`Prouesse1.jar`** ou **`Prouesse2.jar`** — importam o arquivo de dados, calculam os horários e exportam o arquivo de resultados (não requer internet). Execute com Java 16 ou superior:

   ```
   java -jar Prouesse1.jar
   ```

3. Carregue o arquivo de resultados na página de configuração para visualizar os horários.

O processo deve ser repetido para cada turno da escola.

## Lendo o código-fonte

O código em `prouesse1/` e `prouesse2/` é a **versão documentada** usada no livro, com comentários explicativos. Os arquivos `.jar` de `programas/` são gerados a partir de um projeto paralelo cujos nomes internos de classes podem diferir, mas cujo comportamento é o mesmo.

Cada pasta tem um diretório `src/prouesse/` com os arquivos Java (pacote `prouesse`). A classe principal é `Prouesse5D_X` no P1 e `Prouesse_teste` no P2. Para compilar é preciso um JDK (16 ou superior) e a biblioteca [org.json](../lib), que já está incluída na pasta [`lib`](../lib). De dentro de `prouesse1/` ou `prouesse2/`:

```
javac -encoding UTF-8 -cp ../../lib/org.json-2011-04-05.jar -d out src/prouesse/*.java
java -cp out:../../lib/org.json-2011-04-05.jar prouesse.Prouesse5D_X
```

(No Windows use `;` no lugar de `:` no classpath, e `prouesse.Prouesse_teste` para o P2.)

**Classes obsoletas no P1:** algumas classes explicadas no livro foram posteriormente substituídas por versões mais novas. Elas foram mantidas aqui porque o livro as descreve; a [errata](prouesse1/ERRATA_e_DOCUMENTACAO.md) explica a situação.

## Licença

[Apache License 2.0 com Commons Clause](../LICENSE) — veja o [NOTICE](../NOTICE.md).
