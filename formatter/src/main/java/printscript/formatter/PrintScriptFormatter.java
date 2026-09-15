package printscript.formatter;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Optional;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.PrintScriptLexer;

public final class PrintScriptFormatter implements Formatter {
    private final FormattingRules rules;
    private int depth;
    private boolean sawPrintlnInCurrentStatement;
    private boolean previousStatementWasPrintln;

    public PrintScriptFormatter(FormattingRules rules) {
        this.rules = rules;
    }

    @Override
    public void format(Reader source, Writer out, String version) {
        try {
            depth = 0;
            sawPrintlnInCurrentStatement = false;
            previousStatementWasPrintln = false;
            PrintScriptLexer lexer = new PrintScriptLexer(source, version);
            Token previous = null;

            while (lexer.hasNext()) {
                Token current = nextToken(lexer);
                String currentGap = lexer.lastGap();
                if (current.type() == TokenType.EOF) {
                    break;
                }

                if (current.type() == TokenType.RIGHT_BRACE) {
                    depth--;
                }

                if (previous != null) {
                    out.write(separator(previous, current, currentGap));
                }
                out.write(render(current));

                if (current.type() == TokenType.LEFT_BRACE) {
                    depth++;
                }
                if (current.type() == TokenType.PRINTLN) {
                    sawPrintlnInCurrentStatement = true;
                }
                if (current.type() == TokenType.SEMICOLON) {
                    previousStatementWasPrintln = sawPrintlnInCurrentStatement;
                    sawPrintlnInCurrentStatement = false;
                }

                previous = current;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error escribiendo la salida formateada", e);
        }
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

    private String separator(Token previous, Token current, String originalGap) {
        if (current.type() == TokenType.LEFT_BRACE) {
            return rules.ifBraceSameLine() ? " " : "\n" + indent();
        }
        if (previous.type() == TokenType.LEFT_BRACE) {
            return "\n" + indent();
        }
        if (current.type() == TokenType.RIGHT_BRACE) {
            return "\n" + indent();
        }
        if (previous.type() == TokenType.RIGHT_BRACE) {
            return current.type() == TokenType.ELSE ? " " : "\n" + indent();
        }
        if (previous.type() == TokenType.SEMICOLON) {
            int blankLines = previousStatementWasPrintln ? rules.blankLinesAfterPrintln() : 0;
            return "\n".repeat(1 + blankLines) + indent();
        }
        if (current.type() == TokenType.SEMICOLON) {
            return "";
        }
        if (previous.type() == TokenType.IF && current.type() == TokenType.LEFT_PAREN) {
            return " ";
        }
        if (isOperator(previous.type()) || isOperator(current.type())) {
            return " ";
        }
        if (current.type() == TokenType.COLON) {
            return resolve(rules.spaceBeforeColon(), originalGap);
        }
        if (previous.type() == TokenType.COLON) {
            return resolve(rules.spaceAfterColon(), originalGap);
        }
        if (current.type() == TokenType.ASSIGN) {
            return resolve(rules.spaceBeforeAssign(), originalGap);
        }
        if (previous.type() == TokenType.ASSIGN) {
            return resolve(rules.spaceAfterAssign(), originalGap);
        }
        if (current.type() == TokenType.LEFT_PAREN
                || previous.type() == TokenType.LEFT_PAREN
                || current.type() == TokenType.RIGHT_PAREN) {
            return rules.singleSpaceSeparation() ? " " : originalGap;
        }
        return " ";
    }

    private String resolve(Optional<Boolean> configured, String originalGap) {
        if (configured.isPresent()) {
            return configured.get() ? " " : "";
        }
        if (rules.singleSpaceSeparation()) {
            return " ";
        }
        return originalGap;
    }

    private String indent() {
        return " ".repeat(Math.max(depth, 0) * rules.indentSizeInsideIf());
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS
                || type == TokenType.MINUS
                || type == TokenType.STAR
                || type == TokenType.SLASH;
    }
}
