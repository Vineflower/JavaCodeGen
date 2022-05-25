package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record LabeledStatement(
	Statement inner,
	List<? extends Statement> breaks,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Breakable {
	public LabeledStatement{
		this.initMarks(breaks);
	}
	@Override
	public VarsEntry varsEntry() {
		return this.inner.varsEntry();
	}

	@Override
	public boolean completesNormally() {
		return Breakable.super.hasBreak() || this.inner.completesNormally();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		this.addLabel(builder, indentation);
		this.inner.javaLike(builder, indentation);
	}


	@Override
	public boolean needsLabel() {
		// always show label
		return true;
	}


	@Override
	public String toString() {
		return "LabeledStatement@" + System.identityHashCode(this) + "[" +
			   "inner=" + this.inner +
			   ']';
	}


}
