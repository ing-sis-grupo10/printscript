## Pre-commit hooks

Este repo usa [pre-commit](https://pre-commit.com/) para correr chequeos automáticos antes de cada commit: formato básico de archivos y un `./gradlew check` completo (compilación, tests, cobertura mínima con Jacoco, y estilo de código con Checkstyle).

### Instalación (una sola vez por máquina)

1. Instalar `pre-commit`:
   ```bash
   ./gradlew installGitHooks
   ```
2. Desde la raíz del repo, para MAC:
   ```bash
   chmod +x .git/hooks/pre-commit
   ```

A partir de ahí, cada `git commit` corre los hooks automáticamente. Si algún chequeo falla, el commit se cancela y hay que corregir antes de reintentar.
