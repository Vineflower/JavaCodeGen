package org.quiltmc.javacodegen.action;

public record Continue(Continuable target) implements SimpleSingleNoFallThroughStatement {
	public Continue {
		target.addContinue(this);
	}

	@Override
	public String toString() {
		return "Continue[" +
				"target=" + System.identityHashCode(this.target) +
				"]";
	}
}
