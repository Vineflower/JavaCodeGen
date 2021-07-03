package org.quiltmc.javacodegen.statement;

public class ExpressionStatement implements SimpleSingleCompletingStatement {
	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public String toString() {
		return "ExpressionStatement[]";
	}
}
