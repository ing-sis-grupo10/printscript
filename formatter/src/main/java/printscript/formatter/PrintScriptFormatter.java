package printscript.formatter;

import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.PrintScriptLexer;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class PrintScriptFormatter implements Formatter {
    private final FormattingRules rules;

    public PrintScriptFormatter(FormattingRules rules) {
        this.rules = rules;
    }

    @Override
    public void format(Reader source, Writer out) {
        try {
            PrintScriptLexer lexer = new PrintScriptLexer(source);
            Token previous = null;

            while (lexer.hasNext()) {
                Token current = lexer.next();
                if (current.type() == TokenType.EOF) {
                    break;
                }

                if (previous != null) {
                    out.write(separator(previous, current));
                }
                out.write(render(current));
                previous = current;
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error escribiendo la salida formateada", e);
        }
    }

    private String render(Token token) {
        if (token.type() == TokenType.STRING_LITERAL) {
            return "\"" + token.value() + "\"";
        }
        return token.value();
    }

    private String separator(Token previous, Token current) {
        if (current.type() == TokenType.PRINTLN) {
            return "\n".repeat(1 + rules.blankLinesBeforePrintln());
        }
        if (previous.type() == TokenType.SEMICOLON) {
            return "\n";
        }
        if (current.type() == TokenType.SEMICOLON) {
            return "";
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
        if (current.type() == TokenType.LEFT_PAREN || previous.type() == TokenType.LEFT_PAREN
                || current.type() == TokenType.RIGHT_PAREN) {
            return "";
        }
        return " ";
    }

    private boolean isOperator(TokenType type) {
        return type == TokenType.PLUS || type == TokenType.MINUS
                || type == TokenType.STAR || type == TokenType.SLASH;
    }
}
