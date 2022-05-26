package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record MonitorStatement(
		Scope body
) implements Statement {

	@Override
	public boolean completesNormally() {
		return this.body.completesNormally();
	}

	@Override
	public VarsEntry varsEntry() {
		return this.body.varsEntry();
	}

	@Override
	public List<? extends SimpleSingleNoFallThroughStatement> breakOuts() {
		return this.body.breakOuts();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("synchronized (this) \n");
		this.body.javaLike(builder, indentation);
		this.addDebugVarInfo(builder, indentation);
	}
}
