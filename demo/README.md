# Demo PrintScript — comandos rápidos

Todos los comandos se corren desde la raíz del repo (`printscript/`), con
`./gradlew :cli:run --args="..."`. Las rutas llevan `../demo/...` porque
Gradle ejecuta la tarea `run` con el working directory puesto en `cli/`.

Si algún comando termina en `BUILD FAILED`, no te asustes: es normal cuando
el programa reporta errores (exit code 1) — Gradle lo muestra como fallo de
la tarea aunque el CLI hizo exactamente lo que tenía que hacer. Mirá el
`[ERROR]`/`[WARNING]` en la salida, esa es la respuesta real.

---

## 1. Caso exitoso

```bash
./gradlew :cli:run --args="Validation ../demo/basico/01-caso-exitoso.prs"
```
Debe dar `BUILD SUCCESSFUL`. Usa `let`/`const`/`if`/`else`/`readInput`, concatenación con `+`.

Para verlo ejecutar de verdad (con `readInput` pidiendo input real por teclado):
```bash
./gradlew :cli:installDist
```
```bash
& "cli\build\install\cli\bin\cli.bat" Execution demo\basico\01-caso-exitoso.prs
```
(ojo el `&` adelante — PowerShell lo necesita para ejecutar una ruta entre comillas).

---

## 2. Caso con errores (5 tipos distintos, en una sola corrida)

```bash
./gradlew :cli:run --args="Validation ../demo/basico/02-caso-con-errores.prs"
```
Muestra: tipo mal asignado, reasignación de constante, división por cero,
condición de `if` no-boolean, variable no declarada — todo junto porque el
`drain()` no corta en el primer error.

---

## 3. Version-gating (1.0 vs 1.1)

```bash
./gradlew :cli:run --args="Validation ../demo/version/solo-1.1.prs"
```
Pasa limpio (1.1 default).

```bash
./gradlew :cli:run --args="Validation ../demo/version/solo-1.1.prs --version 1.0"
```
Tira errores de sintaxis en cascada — `const`/`boolean`/`true`/`if` no
existen en 1.0, el `KeywordFinder` los deja pasar como `IDENTIFIER` y el
parser los rechaza al intentar parsearlos como asignaciones.

---

## 4. Formatter — mismo archivo, 4 configs distintas

```bash
./gradlew :cli:run --args="Formatting ../demo/formatter/desprolijo.prs"
```
Sin config: respeta el espaciado original tal cual estaba (pass-through).

```bash
./gradlew :cli:run --args="Formatting ../demo/formatter/desprolijo.prs --config ../demo/formatter/config-todo-forzado.json"
```
Fuerza espacios en `:` y `=`, no toca los paréntesis (no están configurados).

```bash
./gradlew :cli:run --args="Formatting ../demo/formatter/desprolijo.prs --config ../demo/formatter/config-sin-espacios.json"
```
Fuerza SIN espacios en `:` y `=`.

```bash
./gradlew :cli:run --args="Formatting ../demo/formatter/desprolijo.prs --config ../demo/formatter/config-single-space.json"
```
`single_space_separation`: un solo espacio en TODOS lados, incluidos los paréntesis.

```bash
./gradlew :cli:run --args="Formatting ../demo/formatter/desprolijo.prs --config ../demo/formatter/config-brace-abajo.json"
```
Llave del `if` en línea aparte, indentado de 4 — no toca `:`/`=` (tampoco configurados acá).

---

## 5. Analyzer — mismo archivo, 3 configs

```bash
./gradlew :cli:run --args="Analyzing ../demo/analyzer/ejemplo.prs"
```
Default: 1 warning (expresión como argumento de `println`).

```bash
./gradlew :cli:run --args="Analyzing ../demo/analyzer/ejemplo.prs --config ../demo/analyzer/config-snake-case.json"
```
Suma un segundo warning: `miVariable` no respeta `snake_case`.

```bash
./gradlew :cli:run --args="Analyzing ../demo/analyzer/ejemplo.prs --config ../demo/analyzer/config-permitir-expresiones.json"
```
Apaga la restricción de `println` — 0 warnings.

---

## Orden sugerido para la demo

1. Caso exitoso → prueba que el intérprete funciona de punta a punta.
2. Caso con errores → prueba el sistema de diagnósticos acumulados.
3. Version-gating → prueba que 1.0/1.1 se controla desde el lexer.
4. Formatter (las 4 configs en fila) → prueba las reglas configurables.
5. Analyzer (las 3 variantes) → prueba el linter.
