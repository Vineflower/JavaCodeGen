package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public final class Break implements SimpleSingleNoFallThroughStatement {
	private Breakable target;
	private boolean simple;
	private final VarsEntry breakOutVars;

	public Break(
		boolean simple,
		VarsEntry breakOutVars
	) {
		this.simple = simple;
		this.breakOutVars = breakOutVars;
		assert this.breakOutVars != null;

		VarsEntry.freeze(breakOutVars);
	}

	@Override
	public String toString() {
		return "Break[" +
			   "target=" + this.target.getId() + ", " +
			   "simple=" + this.simple + "]";
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		if (this.simple) {
			builder.append(indentation).append("break;\n");
		} else {
			builder.append(indentation).append("break label_").append(this.target.getId()).append(";\n");
		}
		if (Statement.DEBUG_BREAKS) {
			builder.append(indentation)
				.append("// id: ")
				.append(System.identityHashCode(this))
				.append(" -> ")
				.append(this.target.getId())
				.append("\n");
		}
	}

	public Breakable target() {
		return this.target;
	}

	public static boolean simpleBreak(Statement statement) {
		if (statement instanceof Break breakStatement) {
			return breakStatement.simple;
		} else if (statement instanceof WrappedBreakOutStatement wrapped) {
			return simpleBreak(wrapped.statement());
		} else {
			throw new IllegalArgumentException("Not a break statement: " + statement);
		}
	}

	public VarsEntry breakOutVars() {
		return this.breakOutVars;
	}

	void setTarget(Breakable breakable) {
		this.target = breakable;
	}

	public void setSimple(boolean b) {
		this.simple = b;
	}

	public boolean isSimple() {
		return this.simple;
	}
}
