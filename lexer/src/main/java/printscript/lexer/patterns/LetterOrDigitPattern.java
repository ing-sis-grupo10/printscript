package printscript.lexer.patterns;

public class LetterOrDigitPattern implements Pattern {
    @Override
    public boolean matches(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}