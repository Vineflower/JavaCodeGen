package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public interface Statement {
	boolean DEBUG_VARS = false;
	boolean DEBUG_BREAKS = false;
	boolean DEBUG_BREAKOUTS = false;

	boolean completesNormally();

	VarsEntry varsEntry();

	void javaLike(StringBuilder builder, String indentation);

	List<? extends SimpleSingleNoFallThroughStatement> breakOuts();

	default void addDebugVarInfo(StringBuilder builder, String indentation) {
		if (DEBUG_VARS) {
			final VarsEntry varsEntry = varsEntry();
			if (varsEntry != null) {
				varsEntry.debugPrint(builder, indentation);
			} else {
				builder.append(indentation).append("// no vars\n");
			}
			builder.append("\n");
		}
		if (DEBUG_BREAKOUTS) {
			final List<? extends SimpleSingleNoFallThroughStatement> breakOuts = breakOuts();
			if (breakOuts != null) {
				builder.append(indentation).append("// breakOuts: ");
				if (breakOuts.isEmpty()) {
					builder.append("none\n");
				} else {
					builder.append("\n");
					for (SimpleSingleNoFallThroughStatement breakOut : breakOuts) {
						var base = WrappedBreakOutStatement.base(breakOut);
						builder.append(indentation).append("// ")
							.append(base.getClass().getSimpleName())
							.append(" ")
							.append(System.identityHashCode(base))
							.append(WrappedBreakOutStatement.isDead(breakOut) ? " (dead)" : " (alive)");

						final VarsEntry vars = breakOut.breakOutVars();
						builder.append(": \n");
						builder.append(indentation).append("//\t\t");
						WrappedBreakOutStatement.idChain(breakOut, builder);
						builder.append("\n");
						if (vars != null) {
							vars.debugPrint(builder, indentation + "\t");
						}
					}
				}
			}
			builder.append("\n");
		}
	}
}
