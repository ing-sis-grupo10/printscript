package printscript.analyzer;

import java.util.Iterator;
import printscript.ast.Statement;
import printscript.common.result.Result;

public interface Analyzer extends Iterator<Result<Statement>> {}
