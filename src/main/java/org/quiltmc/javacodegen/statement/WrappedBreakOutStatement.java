package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public record WrappedBreakOutStatement(
	SimpleSingleNoFallThroughStatement statement,
	VarsEntry breakOutVars,
	boolean dead
) implements SimpleSingleNoFallThroughStatement {
	public WrappedBreakOutStatement(SimpleSingleNoFallThroughStatement statement, VarsEntry breakOutVars) {
		this(statement, breakOutVars, isDead(statement));
	}

	public WrappedBreakOutStatement {
		VarsEntry.freeze(breakOutVars);
	}

	@SuppressWarnings("unchecked")
	public static <T extends SimpleSingleNoFallThroughStatement> T baseAs(Statement stat) {
		return (T) base(stat);
	}


	public static Statement base(Statement stat) {
		if (stat instanceof WrappedBreakOutStatement wrapped) {
			return baseAs(wrapped.statement);
		}
		return stat;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		throw new UnsupportedOperationException();
	}

	public boolean isBreak() {
		return base(this.statement) instanceof Break;
	}

	public boolean isContinue() {
		return base(this.statement) instanceof Continue;
	}

	public static boolean isDead(Statement stat) {
		return stat instanceof WrappedBreakOutStatement wrapped && wrapped.dead;
	}

	public static WrappedBreakOutStatement markDead(SimpleSingleNoFallThroughStatement stat) {
		return new WrappedBreakOutStatement(stat, VarsEntry.never(), true);
	}
}
