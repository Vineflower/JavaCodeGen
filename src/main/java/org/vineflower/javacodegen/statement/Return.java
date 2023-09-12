package org.vineflower.javacodegen.statement;

import org.vineflower.javacodegen.vars.VarsEntry;

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
