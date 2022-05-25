package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public record EmptyStatement(VarsEntry varsEntry) implements SimpleSingleCompletingStatement {
	public EmptyStatement {
		VarsEntry.freeze(varsEntry);
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append(";\n");
	}

	@Override
	public String toString() {
		return "EmptyStatement[]";
	}
}
