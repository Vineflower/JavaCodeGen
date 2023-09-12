package org.vineflower.javacodegen.statement;

import org.vineflower.javacodegen.vars.VarsEntry;

public record EmptyStatement(VarsEntry varsEntry) implements SimpleSingleCompletingStatement {
	public EmptyStatement {
		VarsEntry.freeze(varsEntry);
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append(";\n");
		this.addDebugVarInfo(builder, indentation);
	}

	@Override
	public String toString() {
		return "EmptyStatement[]";
	}
}
