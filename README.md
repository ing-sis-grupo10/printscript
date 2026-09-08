## Pre-commit hooks

Este repo usa un git hook local, instalado via Gradle, para correr chequeos automáticos antes de cada commit: reformatea el código con Spotless y corre los tests

### Instalación (una sola vez por máquina)

1. Instalar el hook:
   ```bash
   ./gradlew installGitHooks
   ```
2. Desde la raíz del repo, para MAC:
   ```bash
   chmod +x .git/hooks/pre-commit
   ```

A partir de ahí, cada `git commit` corre los hooks automáticamente. Si algún chequeo falla, el commit se cancela y hay que corregir antes de reintentar.
