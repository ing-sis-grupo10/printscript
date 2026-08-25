package printscript.lexer.patterns;

public class DigitPattern implements Pattern {
    @Override
    public boolean matches(char c) {
        return Character.isDigit(c);
    }
}