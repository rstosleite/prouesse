# Prouesse — English edition

Source code and programs that accompany the book **_An Applied Study of the Constraint Satisfaction Problem_** (Raimundo Santos Leite). Portuguese edition: [`../pt-BR`](../pt-BR/README.md).

## Contents

| Folder | What it is |
|---|---|
| [`programs/`](programs) | Ready-to-run programs: `Prouesse1.jar`, `Prouesse2.jar`, the configuration page `pag_config_prouesse_en.html` and the user manual `Manual_Prouesse.pdf`. Also available as a ZIP: [**Prouesse-EN.zip**](../../../releases/latest/download/Prouesse-EN.zip) ([all releases](../../../releases)). |
| [`prouesse1/`](prouesse1) | Java source code of **Algorithm P1**, as discussed in the book. Also contains the online [**Appendix**](prouesse1/APPENDIX.md): supplement to the documentation, errata, figures and documentation of the variables. |
| [`prouesse2/`](prouesse2) | Java source code of **Algorithm P2**, as discussed in the book. |

## Using the program

Prouesse is made of two programs that work together (details in `Manual_Prouesse.pdf`):

1. **`pag_config_prouesse_en.html`** — open it in a web browser to enter the data (teachers, classes, constraints) and export a data file. It needs an internet connection (it loads jQuery and Bootstrap from CDNs).
2. **`Prouesse1.jar`** or **`Prouesse2.jar`** — import the data file, compute the timetables and export the result file (no internet needed). Run with Java 16 or later:

   ```
   java -jar Prouesse1.jar
   ```

3. Load the result file back into the configuration page to view the timetables.

The process must be repeated for each school shift.

## Reading the source code

The source code in `prouesse1/` and `prouesse2/` is the **documented version** used in the book: identifiers follow the names used in the text and the files carry explanatory comments. The `.jar` files in `programs/` are built from a parallel project whose internal class names differ, but whose behavior is the same.

Each folder has a `src/prouesse/` directory with the Java files (package `prouesse`). The main class is `Prouesse5D_X` in P1 and `Prouesse_test` in P2. To compile you need a JDK (16 or later) and the [org.json](lib) library, which is included in the [`lib`](../lib) folder. From inside `prouesse1/` or `prouesse2/`:

```
javac -encoding UTF-8 -cp ../../lib/org.json-2011-04-05.jar -d out src/prouesse/*.java
java -cp out:../../lib/org.json-2011-04-05.jar prouesse.Prouesse5D_X
```

(On Windows use `;` instead of `:` in the classpath, and `prouesse.Prouesse_test` for P2.)

## License

[Apache License 2.0 with Commons Clause](../LICENSE) — see [NOTICE](../NOTICE.md).
