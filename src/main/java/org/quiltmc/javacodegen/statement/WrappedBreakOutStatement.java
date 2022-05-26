package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.HashMap;
import java.util.Map;

public record WrappedBreakOutStatement(
	SimpleSingleNoFallThroughStatement statement,
	VarsEntry breakOutVars,
	boolean dead
) implements SimpleSingleNoFallThroughStatement {
	static boolean TRACK_BREAK_OUTS = false;
	static Map<SimpleSingleNoFallThroughStatement, WrappedBreakOutStatement> alreadyWrapped = TRACK_BREAK_OUTS ? new HashMap<>() : null;
	public WrappedBreakOutStatement {
		assert breakOutVars != null != dead;

		if(TRACK_BREAK_OUTS) {
			WrappedBreakOutStatement old;
			if((old = alreadyWrapped.put(statement, this)) != null) {
				throw new IllegalStateException("already wrapped");
			}
		}

		VarsEntry.freeze(breakOutVars);
	}

	public WrappedBreakOutStatement(SimpleSingleNoFallThroughStatement statement, VarsEntry breakOutVars) {
		this(statement, breakOutVars, isDead(statement));
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

	public static void idChain(Statement stat, StringBuilder builder) {
		builder.append(System.identityHashCode(stat));
		if (stat instanceof WrappedBreakOutStatement wrapped) {
			builder.append(wrapped.dead? " (dead) < " : " (alive) < ");
			idChain(wrapped.statement, builder);
		}
	}
}
