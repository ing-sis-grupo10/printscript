## Pre-commit hooks

Este repo usa [pre-commit](https://pre-commit.com/) para correr chequeos automáticos antes de cada commit: formato básico de archivos y un `./gradlew check` completo (compilación, tests, cobertura mínima con Jacoco, y estilo de código con Checkstyle).

### Instalación (una sola vez por máquina)

1. Instalar `pre-commit` (requiere Python):
   ```bash
   pip install pre-commit
   ```
2. Desde la raíz del repo, instalar el hook en tu copia local:
   ```bash
   pre-commit install
   ```

A partir de ahí, cada `git commit` corre los hooks automáticamente. Si algún chequeo falla, el commit se cancela y hay que corregir antes de reintentar.

Para correr los hooks manualmente sobre todos los archivos (sin hacer un commit):
```bash
pre-commit run --all-files
```
