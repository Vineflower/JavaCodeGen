package org.quiltmc.javacodegen.statement;

public record Break(Breakable target) implements SimpleSingleNoFallThroughStatement {
	public Break {
		target.addBreak(this);
	}

	@Override
	public String toString() {
		return "Break[" +
				"target=" + System.identityHashCode(this.target) +
				"]";
	}
}
