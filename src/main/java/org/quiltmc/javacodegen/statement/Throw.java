package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public class Throw implements SimpleSingleNoFallThroughStatement {
	@Override
	public String toString() {
		return "Throw";
	}

	@Override
	public VarsEntry varsFor() {
		return new VarsEntry();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("throw new RuntimeException();\n");
	}
}
