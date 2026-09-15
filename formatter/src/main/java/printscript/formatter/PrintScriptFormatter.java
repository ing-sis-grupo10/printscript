package printscript.formatter;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
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

    public PrintScriptFormatter(FormattingRules rules) {
        this.rules = rules;
    }

    @Override
    public void format(Reader source, Writer out, String version) {
        try {
            depth = 0;
            PrintScriptLexer lexer = new PrintScriptLexer(source, version);
            Token previous = null;

            while (lexer.hasNext()) {
                Token current = nextToken(lexer);
                if (current.type() == TokenType.EOF) {
                    break;
                }

                if (current.type() == TokenType.RIGHT_BRACE) {
                    depth--;
                }

                if (previous != null) {
                    out.write(separator(previous, current));
                }
                out.write(render(current));

                if (current.type() == TokenType.LEFT_BRACE) {
                    depth++;
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

    private String separator(Token previous, Token current) {
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
        if (current.type() == TokenType.PRINTLN) {
            return "\n".repeat(1 + rules.blankLinesBeforePrintln());
        }
        if (previous.type() == TokenType.SEMICOLON) {
            return "\n" + indent();
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
            return rules.spaceBeforeColon() ? " " : "";
        }
        if (previous.type() == TokenType.COLON) {
            return rules.spaceAfterColon() ? " " : "";
        }
        if (current.type() == TokenType.ASSIGN) {
            return rules.spaceBeforeAssign() ? " " : "";
        }
        if (previous.type() == TokenType.ASSIGN) {
            return rules.spaceAfterAssign() ? " " : "";
        }
        if (current.type() == TokenType.LEFT_PAREN
                || previous.type() == TokenType.LEFT_PAREN
                || current.type() == TokenType.RIGHT_PAREN) {
            return "";
        }
        return " ";
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
