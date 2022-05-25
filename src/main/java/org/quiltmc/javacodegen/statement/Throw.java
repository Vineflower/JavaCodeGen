package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public record Throw(
	VarsEntry breakOutVars
) implements SimpleSingleNoFallThroughStatement {
	public Throw {
		VarsEntry.freeze(breakOutVars);
	}

	@Override
	public String toString() {
		return "Throw";
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("throw new RuntimeException();\n");
	}
}
