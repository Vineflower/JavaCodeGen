package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record Scope(
		List<? extends Statement> statements,
		VarsEntry varsEntry,
		List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Statement {
	public Scope {
		VarsEntry.freeze(varsEntry);
	}

	@Override
	public boolean completesNormally() {
		if (this.statements.isEmpty()) {
			return true;
		}

		return this.statements.get(this.statements.size() - 1).completesNormally();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("{\n");
		for (Statement statement : this.statements) {
			statement.javaLike(builder, indentation + "\t");
		}
		builder.append(indentation).append("}\n");
	}
}
