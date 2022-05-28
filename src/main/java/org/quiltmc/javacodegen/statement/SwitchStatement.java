package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record SwitchStatement(
	Expression expression,
	List<? extends CaseBranch> branches,
	boolean includeDefault,
	List<? extends SimpleSingleNoFallThroughStatement> breaks,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Breakable {
	public SwitchStatement {
		this.initMarks(breaks);

		VarsEntry.freeze(varsEntry);
	}

	@Override
	public boolean completesNormally() {
		if (!this.includeDefault || this.branches.isEmpty() || this.hasBreak()) {
			return true;
		}

		var last = this.branches.get(this.branches.size() - 1);
		return last.statement.isEmpty() || last.statement.get(last.statement.size() - 1).completesNormally();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		this.addLabel(builder, indentation);
		StringBuilder cond = new StringBuilder();
		this.expression.javaLike(cond);

		builder.append(indentation).append("switch (").append(cond.toString().trim()).append(") {").append('\n');
		for (CaseBranch cb : this.branches) {
			String caseIndent = indentation + "\t";

			var casevals = cb.casevals;

			for (Expression caseval : casevals) {
				if (caseval == DEFAULT) {
					builder.append(caseIndent).append("default: \n");
				} else {
					builder.append(caseIndent).append("case ").append(caseval.toJava()).append(": ");
					builder.append("\n");
				}
			}

			for (Statement statement : cb.statement) {
				statement.javaLike(builder, caseIndent + (statement instanceof Scope ? "" : "\t"));
			}
		}

		builder.append(indentation).append("}\n");

		this.addDebugVarInfo(builder, indentation);
	}

	public record CaseBranch(List<? extends Expression> casevals, List<? extends Statement> statement) {

	}

	public static Expression DEFAULT = __ -> {throw new IllegalStateException();};
}
