package printscript.lexer.patterns;

public class LetterPattern implements Pattern {
    @Override
    public boolean matches(char c) {
        return Character.isLetter(c) || c == '_';
    }
}