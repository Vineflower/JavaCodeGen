package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public interface Statement {
	boolean completesNormally();

	VarsEntry varsFor();

	void javaLike(StringBuilder builder, String indentation);
}
