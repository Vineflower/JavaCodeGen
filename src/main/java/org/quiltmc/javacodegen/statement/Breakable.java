package org.quiltmc.javacodegen.statement;

import java.util.ArrayList;
import java.util.List;

public abstract class Breakable implements Statement {
	private final List<Break> breaks = new ArrayList<>();

	void addBreak(Break b) {
		this.breaks.add(b);
	}

	protected boolean canBreak() {
		return !this.breaks.isEmpty();
	}

	int getId() {
		return System.identityHashCode(this);
	}

	boolean needsLabel() {
		return !this.breaks.stream().allMatch(Break::simple);
	}

	void addLabel(StringBuilder builder, String indentation) {
		if (this.needsLabel()) {
			builder.append(indentation).append("label_").append(System.identityHashCode(this)).append(":\n");
		}
	}
}
