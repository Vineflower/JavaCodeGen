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
	public void javaLike(StringBuilder builder, String indentation) {
		this.addLabel(builder, indentation);
		this.inner.javaLike(builder,indentation);
	}

	@Override
	boolean needsLabel() {
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
