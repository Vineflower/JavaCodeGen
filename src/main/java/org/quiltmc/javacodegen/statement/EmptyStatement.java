package org.quiltmc.javacodegen.statement;

public class EmptyStatement implements SimpleSingleCompletingStatement {
	// could be a singleton I think

	@Override
	public boolean completesNormally() {
		return true;
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
