package printscript.lexer;

import printscript.common.exception.InvalidTokenException;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class PrintScriptLexer implements Iterator<Token> {

    private final BufferedReader reader;
    private final List<Finder> finders = List.of(
            new KeywordFinder(),
            new IdentifierFinder(),
            new NumberFinder(),
            new StringFinder(),
            new SymbolFinder()
    );

    private String currentLine;
    private int currentIndex;
    private int currentRow;
    private boolean eofEmitted;
    private Token nextToken;

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
        if (nextToken != null) {
            return true;
        }
        nextToken = findNextToken();
        return nextToken != null;
    }

    @Override
    public Token next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No hay más tokens");
        }
        Token token = nextToken;
        nextToken = null;
        return token;
    }

    private Token findNextToken() {
        while (currentLine != null) {
            skipWhitespace();

            if (currentIndex >= currentLine.length()) {
                currentRow++;
                advanceLine();
                continue;
            }

            char currentChar = currentLine.charAt(currentIndex);
            Token token = tryFinders(currentChar);
            currentIndex = token.span().end().column();
            return token;
        }

        if (!eofEmitted) {
            eofEmitted = true;
            Span span = Span.of(new Position(currentRow, 0), new Position(currentRow, 0));
            return new Token(TokenType.EOF, "", span);
        }

        return null;
    }

    private Token tryFinders(char currentChar) {
        for (Finder finder : finders) {
            if (finder.canHandle(currentChar)) {
                Token token = finder.find(currentLine, currentIndex, currentRow, currentIndex);
                if (token != null) {
                    return token;
                }
            }
        }
        Span span = Span.of(new Position(currentRow, currentIndex), new Position(currentRow, currentIndex + 1));
        throw new InvalidTokenException("Carácter no reconocido: '" + currentChar + "'", span);
    }

    private void skipWhitespace() {
        while (currentIndex < currentLine.length() && Character.isWhitespace(currentLine.charAt(currentIndex))) {
            currentIndex++;
        }
    }
}