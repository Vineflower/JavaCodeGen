package org.quiltmc.javacodegen.action;

public class StandardLocalDeclaration implements Statement, LocalDeclaration {

	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public String toString() {
		return "StandardLocalDeclaration";
	}
}
