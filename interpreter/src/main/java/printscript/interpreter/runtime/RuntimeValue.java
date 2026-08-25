package printscript.interpreter.runtime;

import java.math.BigDecimal;

public sealed interface RuntimeValue permits RuntimeValue.NumberValue, RuntimeValue.StringValue {

    record NumberValue(BigDecimal value) implements RuntimeValue {}

    record StringValue(String value) implements RuntimeValue {}
}
