# PrintScript

Implementación de las herramientas del lenguaje **PrintScript** (un subset de TypeScript): intérprete,
formatter y analizador estático (linter), usables como librería o desde una CLI.

Versiones del lenguaje soportadas: **1.0** y **1.1** (`const`, `boolean`, `if`/`else`, `readInput`, `readEnv`).

## Módulos

| Módulo | Responsabilidad | Depende de |
|---|---|---|
| `common` | Tipos compartidos: `Token`, `Span`/`Position`, `Result` (`Success`/`Failure`), `Diagnostic` y `Version` | — |
| `ast` | Nodos del árbol sintáctico: sentencias y expresiones como `sealed interface` + `record` | `common` |
| `lexer` | Convierte el código fuente en tokens, línea a línea, mediante `Finder`s intercambiables | `common` |
| `parser` | Convierte tokens en sentencias del AST, mediante `StatementParser`s intercambiables | `ast`, `common` |
| `interpreter` | Ejecuta las sentencias, mediante `StatementHandler`s intercambiables | `ast`, `common` |
| `formatter` | Reescribe el código según reglas de espaciado configurables (`SpacingRule`s) | `lexer`, `common` |
| `analyzer` | Detecta incumplimientos de convenciones mediante reglas configurables (`LintRule`s) | `ast`, `common` |
| `cli` | Punto de entrada: arma el pipeline según la operación pedida y reporta los errores | todos |

## Diagramas

En la carpeta [`diagrams/`](diagrams), en PlantUML:

- [`components.puml`](diagrams/components.puml): módulos y dependencias entre ellos.
- [`pipeline.puml`](diagrams/pipeline.puml): cómo interactúan en ejecución.

## Cómo interactúan

- **Todo fluye como `Iterator`.** Cada etapa le pide el siguiente elemento a la anterior: el lexer lee el archivo
  línea a línea, el parser arma una sentencia por vez y el intérprete la ejecuta. El archivo nunca se carga
  completo en memoria, como pide la consigna para fuentes muy grandes.
- **El parser no depende del lexer.** Recibe un `Iterator` de tokens (un tipo de `common`), así que se puede usar
  con cualquier fuente de tokens y se testea con tokens armados a mano.
- **Los errores viajan como datos, no como excepciones.** Cada etapa devuelve `Result<T>`: `Success` con el valor,
  o `Failure` con una lista de `Diagnostic` (mensaje + posición de inicio y fin). Un error en una sentencia no
  corta el procesamiento: se reporta y se sigue con la siguiente, así que se ven todos los errores de una vez.
- **Formatter:** trabaja sobre los tokens y no sobre el AST, para poder respetar el espaciado original
  cuando una regla no está configurada.
- **Analyzer:** es un `Iterator` de sentencias que no modifica lo que pasa por él y acumula warnings. En la CLI
  recibe la salida del intérprete, así que también se reportan los errores semánticos.

| Operación (CLI) | Pipeline |
|---|---|
| `Execution` | lexer → parser → interpreter |
| `Validation` | lexer → parser → interpreter (con la salida silenciada) |
| `Analyzing` | lexer → parser → interpreter (silenciado) → analyzer |
| `Formatting` | lexer → formatter |

## Extensibilidad

Todos los componentes siguen el mismo patrón: **una lista de estrategias** y la primera que puede manejar el
caso es la que actúa (Strategy + Chain of Responsibility). Qué estrategias entran lo decide una factory, a partir
de la versión del lenguaje o de la configuración del usuario:

| Componente | Estrategia | Quién arma la lista |
|---|---|---|
| Lexer | `Finder` | `LexerFactory` (según `Version`) |
| Parser | `StatementParser` | `ParserFactory` (según `Version`) |
| Interpreter | `StatementHandler` | `InterpreterFactory` (según `Version`) |
| Formatter | `SpacingRule` | `SpacingRules` (según la config) |
| Analyzer | `LintRule` | `LintRules` (según la config) |

Agregar una versión del lenguaje es sumar un caso al `enum Version`. Las factories hacen `switch` exhaustivo
sobre él, así que el código no compila hasta que cada componente declare qué agrega en esa versión.

Las **expresiones** no son plugins a propósito: son un `sealed interface` y se evalúan con `switch` exhaustivo.
Si se agrega un tipo de expresión, el compilador obliga a manejarlo en el evaluador; no puede quedar a medias.

Uso como librería:

```java
var tokens = LexerFactory.create(reader, Version.V1_1);
var statements = ParserFactory.create(tokens, Version.V1_1);
var interpreter = InterpreterFactory.create(statements, Version.V1_1, System.out, inputSource);
while (interpreter.hasNext()) {
    Result<Statement> result = interpreter.next();
}
```

## CLI

```bash
./gradlew :cli:run --args="<Validation|Execution|Formatting|Analyzing> <archivo> [--config <archivo.json>] [--version 1.0|1.1]"
```

- La versión por defecto es la `1.1`.
- Los errores se muestran como `[ERROR] mensaje (fila:columna - fila:columna)`, contando desde 1.
- Mientras se procesa el archivo se muestra el progreso (`Procesando sentencia N...`).
- Código de salida: `0` si no hubo errores, `1` si los hubo, `2` si los argumentos son inválidos.

En [`demo/README.md`](demo/README.md) hay ejemplos listos para correr.

### Configuración del formatter (`--config`)

| Clave | Valores | Si no está |
|---|---|---|
| `declaration_space_before_colon` | `true` / `false` | se respeta el original |
| `declaration_space_after_colon` | `true` / `false` | se respeta el original |
| `assignment_space_before_equals` | `true` / `false` | se respeta el original |
| `assignment_space_after_equals` | `true` / `false` | se respeta el original |
| `single_space_separation` | `true` / `false` | `false` |
| `println_new_lines_after_call` | `0`, `1` o `2` | `1` |
| `if_brace_same_line` | `true` / `false` | `true` |
| `if_indent_size` | número | `2` |

### Configuración del analyzer (`--config`)

| Clave | Valores | Si no está |
|---|---|---|
| `identifier_format` | `camel_case` / `snake_case` | `camel_case` |
| `identifier_case_enabled` | `true` / `false` | `true` |
| `println_identifier_or_literal_only` | `true` / `false` | `true` |
| `read_input_identifier_or_literal_only` | `true` / `false` | `true` |

## Desarrollo

```bash
./gradlew check
```

Corre en todos los módulos: compilación, tests, formato (Spotless), estilo (Checkstyle), bugs comunes (SpotBugs)
y cobertura mínima del 80% (JaCoCo). La configuración común está en los convention plugins de `buildSrc/`.

### Pre-commit hook

Antes de cada commit chequea el formato (Spotless), el estilo (Checkstyle) y corre los tests. Se instala una vez
por máquina (y también solo con cualquier `./gradlew build`):

```bash
./gradlew installGitHooks
```

Si algún chequeo falla, el commit se cancela.

### CI/CD

- `ci.yml`: corre `./gradlew check` en cada pull request.
- `publish.yml`: al crear un release en GitHub, publica los módulos en GitHub Packages
  (`ar.edu.austral.ingsis.printscript:printscript-<módulo>`).
