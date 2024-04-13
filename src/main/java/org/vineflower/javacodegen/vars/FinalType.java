package org.vineflower.javacodegen.vars;

public enum FinalType {
	FINAL(true),
	NOT_FINAL(false),
	IMPLICIT_FINAL(true);

	private final boolean isFinal;

	FinalType(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public boolean isFinal() {
		return isFinal;
	}
}
