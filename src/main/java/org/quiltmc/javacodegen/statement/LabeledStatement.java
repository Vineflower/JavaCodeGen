package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record LabeledStatement(
	Statement inner,
	List<? extends SimpleSingleNoFallThroughStatement> breaks,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Breakable {
	public LabeledStatement{
		this.initMarks(breaks);

		VarsEntry.freeze(varsEntry);

		for (Statement stat : breaks) {
			WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false);
		}
	}

	@Override
	public boolean completesNormally() {
		return Breakable.super.hasBreak() || this.inner.completesNormally();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		this.addLabel(builder, indentation);
		this.inner.javaLike(builder, indentation);
		this.addDebugVarInfo(builder, indentation);
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
