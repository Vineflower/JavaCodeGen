package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record SwitchStatement(
	Expression expression,
	List<CaseBranch> branches,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Statement {
	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		StringBuilder cond = new StringBuilder();
		this.expression.javaLike(cond);

		builder.append(indentation).append("switch (").append(cond.toString().trim()).append(") {").append('\n');
		for (CaseBranch cb : branches) {
			String caseIndent = indentation + "\t";

			List<Expression> casevals = cb.casevals;

			for (int i = 0; i < casevals.size(); i++) {
				Expression caseval = casevals.get(i);
				builder.append(caseIndent).append("case ").append(caseval.toJava()).append(": ");
				builder.append("\n");
			}

			cb.statement.javaLike(builder, caseIndent + (cb.statement instanceof Scope ? "" : "\t"));
		}

		builder.append(indentation).append("}\n");

		this.addDebugVarInfo(builder, indentation);
	}

	public record CaseBranch(List<Expression> casevals, Statement statement) {

	}
}
