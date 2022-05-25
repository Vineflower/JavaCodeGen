package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public record Return(
		VarsEntry breakOutVars
) implements SimpleSingleNoFallThroughStatement {
	public Return {
		VarsEntry.freeze(breakOutVars);
	}

	@Override
	public String toString() {
		return "Return";
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("return;\n");
	}
}
