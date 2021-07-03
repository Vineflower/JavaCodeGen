package org.quiltmc.javacodegen.action;

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
