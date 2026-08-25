package printscript.lexer;

import printscript.common.result.Failure;
import printscript.common.result.Result;
import printscript.common.result.Success;
import printscript.common.token.Token;
import printscript.common.token.TokenType;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrintScriptLexerTest {

    private Token expectSuccess(Result<Token> result) {
        assertInstanceOf(Success.class, result);
        return ((Success<Token>) result).value();
    }

    @Test
    void tokenizesLetDeclaration() {
        String source = "let cantidad: number = 42;";
        PrintScriptLexer lexer = new PrintScriptLexer(new StringReader(source));

        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(expectSuccess(lexer.next()));
        }

        List<TokenType> expectedTypes = List.of(
                TokenType.LET, TokenType.IDENTIFIER, TokenType.COLON, TokenType.NUMBER_TYPE,
                TokenType.ASSIGN, TokenType.NUMBER_LITERAL, TokenType.SEMICOLON, TokenType.EOF
        );

        assertEquals(expectedTypes.size(), tokens.size());
        for (int i = 0; i < expectedTypes.size(); i++) {
            assertEquals(expectedTypes.get(i), tokens.get(i).type());
        }
    }

    @Test
    void capturesCorrectLexemes() {
        String source = "let cantidad: number = 42;";
        PrintScriptLexer lexer = new PrintScriptLexer(new StringReader(source));

        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(expectSuccess(lexer.next()));
        }

        assertEquals("let", tokens.get(0).value());
        assertEquals("cantidad", tokens.get(1).value());
        assertEquals("42", tokens.get(5).value());
    }

    @Test
    void tokenizesPrintlnWithString() {
        String source = "println(\"hola\");";
        PrintScriptLexer lexer = new PrintScriptLexer(new StringReader(source));

        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(expectSuccess(lexer.next()));
        }

        List<TokenType> expectedTypes = List.of(
                TokenType.PRINTLN, TokenType.LEFT_PAREN, TokenType.STRING_LITERAL,
                TokenType.RIGHT_PAREN, TokenType.SEMICOLON, TokenType.EOF
        );

        assertEquals(expectedTypes.size(), tokens.size());
        for (int i = 0; i < expectedTypes.size(); i++) {
            assertEquals(expectedTypes.get(i), tokens.get(i).type());
        }
        assertEquals("hola", tokens.get(2).value());
    }

    @Test
    void reportsInvalidCharacterAsFailure() {
        String source = "let x @ number;";
        PrintScriptLexer lexer = new PrintScriptLexer(new StringReader(source));

        boolean foundFailure = false;
        while (lexer.hasNext()) {
            if (lexer.next() instanceof Failure<Token>) {
                foundFailure = true;
            }
        }

        assertTrue(foundFailure);
    }

    @Test
    void tokenizesIdentifierWithUnderscoreAndDigits() {
        String source = "let mi_variable2: number = 10;";
        PrintScriptLexer lexer = new PrintScriptLexer(new StringReader(source));

        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(expectSuccess(lexer.next()));
        }

        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        assertEquals("mi_variable2", tokens.get(1).value());
    }

    @Test
    void tokenizesArithmeticExpression() {
        String source = "let resultado: number = a + b - c * d / e;";
        PrintScriptLexer lexer = new PrintScriptLexer(new StringReader(source));

        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(expectSuccess(lexer.next()));
        }

        List<TokenType> expectedTypes = List.of(
                TokenType.LET, TokenType.IDENTIFIER, TokenType.COLON, TokenType.NUMBER_TYPE, TokenType.ASSIGN,
                TokenType.IDENTIFIER, TokenType.PLUS, TokenType.IDENTIFIER, TokenType.MINUS,
                TokenType.IDENTIFIER, TokenType.STAR, TokenType.IDENTIFIER, TokenType.SLASH,
                TokenType.IDENTIFIER, TokenType.SEMICOLON, TokenType.EOF
        );

        assertEquals(expectedTypes.size(), tokens.size());
        for (int i = 0; i < expectedTypes.size(); i++) {
            assertEquals(expectedTypes.get(i), tokens.get(i).type());
        }
    }

    @Test
    void tokenizesMultipleStatementsAcrossLines() {
        String source = "let a: number = 1;\nlet b: number = 2;";
        PrintScriptLexer lexer = new PrintScriptLexer(new StringReader(source));

        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(expectSuccess(lexer.next()));
        }

        assertEquals(15, tokens.size());
        assertEquals(0, tokens.get(0).span().start().line());
        assertEquals(1, tokens.get(7).span().start().line());
    }
}