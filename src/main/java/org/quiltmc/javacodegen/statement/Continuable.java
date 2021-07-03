package org.quiltmc.javacodegen.statement;

import java.util.ArrayList;
import java.util.List;

public abstract class Continuable extends Breakable {
	private final List<Continue> continues = new ArrayList<>();

	void addContinue(Continue c) {
		this.continues.add(c);
	}

	protected boolean canContinue() {
		return !this.continues.isEmpty();
	}


	@Override
	boolean needsLabel() {
		return super.needsLabel() || !this.continues.stream().allMatch(Continue::simple);
	}
}
