package org.quiltmc.javacodegen.statement;

public interface Statement {
	boolean completesNormally();

	void javaLike(StringBuilder builder, String indentation);
}
