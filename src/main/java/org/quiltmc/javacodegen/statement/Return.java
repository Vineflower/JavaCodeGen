package org.quiltmc.javacodegen.statement;

public class Return implements SimpleSingleNoFallThroughStatement {
	@Override
	public String toString() {
		return "Return";
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("return;\n");
	}
}
