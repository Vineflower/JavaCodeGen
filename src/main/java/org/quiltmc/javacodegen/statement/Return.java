package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public class Return implements SimpleSingleNoFallThroughStatement {
	@Override
	public String toString() {
		return "Return";
	}

	@Override
	public VarsEntry varsFor() {
		return new VarsEntry();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("return;\n");
	}
}
