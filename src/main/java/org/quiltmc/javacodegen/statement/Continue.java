package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.Objects;

public final class Continue implements SimpleSingleNoFallThroughStatement {
	private Continuable target;
	private boolean simple;
	private final VarsEntry breakOutVars;

	public Continue(boolean simple, VarsEntry breakOutVars) {
		this.simple = simple;
		this.breakOutVars = breakOutVars;

		VarsEntry.freeze(breakOutVars);
	}

	@Override
	public String toString() {
		return "Continue[" +
			   "target=" + this.target.getId() + ", " +
			   "simple=" + this.simple + "]";
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		if (this.simple) {
			builder.append(indentation).append("continue;\n");
		} else {
			builder.append(indentation).append("continue label_").append(this.target.getId()).append(";\n");
		}
	}

	public Continuable target() {
		return target;
	}

	public static boolean simpleContinue(Statement statement) {
		if (statement instanceof Continue cont) {
			return cont.simple;
		} else if (statement instanceof WrappedBreakOutStatement wrapped) {
			return simpleContinue(wrapped.statement());
		} else {
			throw new IllegalArgumentException("Not a continue statement: " + statement);
		}
	}

	public VarsEntry breakOutVars() {
		return breakOutVars;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (Continue) obj;
		return Objects.equals(this.target, that.target) &&
			   this.simple == that.simple &&
			   Objects.equals(this.breakOutVars, that.breakOutVars);
	}

	@Override
	public int hashCode() {
		return Objects.hash(target, simple, breakOutVars);
	}


	void setTarget(Continuable continuable) {
		this.target = continuable;
	}

	public void setSimple(boolean b) {
		this.simple = b;
	}
}
