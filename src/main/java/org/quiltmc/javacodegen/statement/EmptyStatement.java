package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public class EmptyStatement implements SimpleSingleCompletingStatement {
	// could be a singleton I think

	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public VarsEntry varsFor() {
		return new VarsEntry();
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
