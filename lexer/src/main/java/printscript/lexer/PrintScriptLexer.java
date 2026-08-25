package printscript.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import printscript.common.result.Diagnostic;
import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import printscript.lexer.finders.Finder;
import printscript.lexer.finders.IdentifierFinder;
import printscript.lexer.finders.KeywordFinder;
import printscript.lexer.finders.NumberFinder;
import printscript.lexer.finders.StringFinder;
import printscript.lexer.finders.SymbolFinder;

public class PrintScriptLexer implements Iterator<Result<Token>> {

    private final BufferedReader reader;
    private final List<Finder> finders =
            List.of(
                    new KeywordFinder(),
                    new IdentifierFinder(),
                    new NumberFinder(),
                    new StringFinder(),
                    new SymbolFinder());

    private String currentLine;
    private int currentIndex;
    private int currentRow;
    private boolean eofEmitted;
    private Result<Token> nextResult;

    public PrintScriptLexer(Reader reader) {
        this.reader = new BufferedReader(reader);
        this.currentRow = 0;
        this.eofEmitted = false;
        advanceLine();
    }

    private void advanceLine() {
        try {
            currentLine = reader.readLine();
            currentIndex = 0;
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo fuente", e);
        }
    }

    @Override
    public boolean hasNext() {
        if (nextResult != null) {
            return true;
        }
        nextResult = findNextResult();
        return nextResult != null;
    }

    @Override
    public Result<Token> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No hay más tokens");
        }
        Result<Token> result = nextResult;
        nextResult = null;
        return result;
    }

    private Result<Token> findNextResult() {
        while (currentLine != null) {
            skipWhitespace();

            if (currentIndex >= currentLine.length()) {
                currentRow++;
                advanceLine();
                continue;
            }

            char currentChar = currentLine.charAt(currentIndex);
            return tryFinders(currentChar);
        }

        if (!eofEmitted) {
            eofEmitted = true;
            Span span = Span.of(new Position(currentRow, 0), new Position(currentRow, 0));
            return Result.success(new Token(TokenType.EOF, "", span));
        }

        return null;
    }

    private Result<Token> tryFinders(char currentChar) {
        for (Finder finder : finders) {
            if (finder.canHandle(currentChar)) {
                Optional<Result<Token>> found =
                        finder.find(currentLine, currentIndex, currentRow, currentIndex);
                if (found.isPresent()) {
                    Result<Token> result = found.get();
                    currentIndex = endColumnOf(result);
                    return result;
                }
            }
        }
        Span span =
                Span.of(
                        new Position(currentRow, currentIndex),
                        new Position(currentRow, currentIndex + 1));
        currentIndex++;
        return Result.failure(
                Diagnostic.error("Carácter no reconocido: '" + currentChar + "'", span));
    }

    private int endColumnOf(Result<Token> result) {
        return switch (result) {
            case Success<Token> s -> s.value().span().end().column();
            case Failure<Token> f -> f.diagnostics().get(0).span().end().column();
        };
    }

    private void skipWhitespace() {
        while (currentIndex < currentLine.length()
                && Character.isWhitespace(currentLine.charAt(currentIndex))) {
            currentIndex++;
        }
    }
}
