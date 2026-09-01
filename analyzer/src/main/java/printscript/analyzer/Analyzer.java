package printscript.analyzer;

import printscript.ast.Statement;
import printscript.common.result.Result;

import java.util.Iterator;

public interface Analyzer extends Iterator<Result<Statement>> {}
