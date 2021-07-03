package org.quiltmc.javacodegen.statement;

public final class LabeledStatement extends Breakable {
	private Statement inner;

	public void setInner(Statement inner) {
		this.inner = inner;
	}

	@Override
	public boolean completesNormally() {
		return this.inner.completesNormally() || super.canBreak();
	}

	@Override
	public String toString() {
		return "LabeledStatement@" + System.identityHashCode(this) + "[" +
				"inner=" + this.inner +
				']';
	}


}
