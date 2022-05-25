package org.quiltmc.javacodegen.statement;

import java.util.List;

public interface Breakable extends Statement {
	default void initMarks(List<? extends Statement> breaks) {
		breaks.forEach(this::initMark);
	}

	private void initMark(Statement statement) {
		WrappedBreakOutStatement.<Break>baseAs(statement).setTarget(this);
	}
	List<? extends Statement> breaks();


	default boolean hasBreak() {
		return !this.breaks().isEmpty();
	}

	default int getId() {
		return System.identityHashCode(this);
	}

	default boolean needsLabel() {
		return !this.breaks().stream().allMatch(Break::simpleBreak);
	}

	default void addLabel(StringBuilder builder, String indentation) {
		if (this.needsLabel()) {
			builder.append(indentation).append("label_").append(System.identityHashCode(this)).append(":\n");
		}
	}
}
