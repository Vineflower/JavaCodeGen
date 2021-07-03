package org.quiltmc.javacodegen.statement;

public class ExpressionStatement implements SimpleSingleCompletingStatement {
	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("...\n");
	}

	@Override
	public String toString() {
		return "ExpressionStatement[]";
	}
}
