package printscript.formatter;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import printscript.common.Version;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.formatter.spacing.FormatContext;
import printscript.formatter.spacing.SpacingRule;
import printscript.formatter.spacing.SpacingRules;
import printscript.lexer.LexerFactory;
import printscript.lexer.PrintScriptLexer;

public final class PrintScriptFormatter implements Formatter {
    private final List<SpacingRule> spacingRules;
    private final int indentSize;

    public PrintScriptFormatter(FormattingRules config) {
        this(SpacingRules.from(config), config.indentSizeInsideIf());
    }

    public PrintScriptFormatter(List<SpacingRule> spacingRules, int indentSize) {
        this.spacingRules = List.copyOf(spacingRules);
        this.indentSize = indentSize;
    }

    @Override
    public void format(Reader source, Writer out, Version version) {
        try {
            PrintScriptLexer lexer = LexerFactory.create(source, version);
            FormatState state = new FormatState(indentSize);

            while (lexer.hasNext()) {
                Token current = nextToken(lexer);
                if (current.type() == TokenType.EOF) {
                    break;
                }
                state.beforeToken(current);
                Optional<Token> previous = state.previous();
                if (previous.isPresent()) {
                    out.write(separator(previous.get(), current, state.context(lexer.lastGap())));
                }
                out.write(render(current));
                state.afterToken(current);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error escribiendo la salida formateada", e);
        }
    }

    private String separator(Token previous, Token current, FormatContext context) {
        for (SpacingRule rule : spacingRules) {
            Optional<String> separator = rule.separator(previous, current, context);
            if (separator.isPresent()) {
                return separator.get();
            }
        }
        throw new IllegalStateException(
                "Ninguna regla decidió el espacio entre "
                        + previous.type()
                        + " y "
                        + current.type());
    }

    private Token nextToken(PrintScriptLexer lexer) {
        Result<Token> result = lexer.next();
        return switch (result) {
            case Success<Token> s -> s.value();
            case Failure<Token> f -> {
                Diagnostic diagnostic = f.diagnostics().get(0);
                throw new IllegalArgumentException(
                        "No se puede formatear, error léxico: " + diagnostic.message());
            }
        };
    }

    private String render(Token token) {
        if (token.type() == TokenType.STRING_LITERAL) {
            return "\"" + token.value() + "\"";
        }
        return token.value();
    }
}
