package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public interface Statement {
	boolean completesNormally();

	VarsEntry varsEntry();

	void javaLike(StringBuilder builder, String indentation);

	List<? extends SimpleSingleNoFallThroughStatement> breakOuts();
}
