package printscript.interpreter.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import printscript.ast.DeclaredType;
import printscript.common.token.Position;
import printscript.common.token.Span;
import printscript.interpreter.runtime.RuntimeValue.NumberValue;

class GlobalEnvironmentTest {

    private final Span span = Span.of(new Position(1, 0), new Position(1, 5));

    @Test
    void childCanDeclareSameNameAsParentWithoutConflict() {
        Environment parent = new GlobalEnvironment();
        parent.declare("x", DeclaredType.NUMBER, false, span);

        Environment child = parent.child();

        assertTrue(child.declare("x", DeclaredType.NUMBER, false, span).isEmpty());
    }

    @Test
    void variableDeclaredInChildIsNotVisibleInParent() {
        Environment parent = new GlobalEnvironment();
        Environment child = parent.child();

        child.declare("mensaje", DeclaredType.STRING, false, span);

        assertTrue(parent.typeOf("mensaje").isEmpty());
    }

    @Test
    void assignInChildPropagatesToVariableDeclaredInParent() {
        Environment parent = new GlobalEnvironment();
        parent.declare("contador", DeclaredType.NUMBER, false, span);
        parent.assign("contador", new NumberValue(BigDecimal.ZERO));

        Environment child = parent.child();
        child.assign("contador", new NumberValue(BigDecimal.ONE));

        assertEquals(Optional.of(new NumberValue(BigDecimal.ONE)), parent.valueOf("contador"));
    }

    @Test
    void constantCannotBeReassigned() {
        Environment env = new GlobalEnvironment();
        env.declare("pi", DeclaredType.NUMBER, true, span);

        assertTrue(env.isConstant("pi"));
    }
}
