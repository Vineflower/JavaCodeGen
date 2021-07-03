package org.quiltmc.javacodegen.statement;

public class StandardLocalDeclaration implements Statement, LocalDeclaration {

	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return "StandardLocalDeclaration";
	}
}
