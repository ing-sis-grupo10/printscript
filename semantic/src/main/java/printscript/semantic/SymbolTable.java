package printscript.semantic;

import printscript.common.result.Diagnostic;
import printscript.common.token.Span;

import java.util.Optional;

public interface SymbolTable {
    Optional<Diagnostic> declare(String name, Type type, Span declarationSite);
    Optional<Type> lookup(String name);
}