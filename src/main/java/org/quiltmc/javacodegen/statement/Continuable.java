package org.quiltmc.javacodegen.statement;

import java.util.List;

public interface Continuable extends Breakable {
	@Override
	@Deprecated
	default void initMarks(List<? extends Statement> breaks) {
		Breakable.super.initMarks(breaks);
	}

	default void initMarks(List<? extends Statement> breaks, List<? extends Statement> continues) {
		Breakable.super.initMarks(breaks);
		continues.forEach(this::initMark);
	}

	private void initMark(Statement statement) {
		WrappedBreakOutStatement.<Continue>baseAs(statement).setTarget(this);
	}

	List<? extends Statement> continues();

	default boolean hasContinue() {
		return !this.continues().isEmpty();
	}


	@Override
	default boolean needsLabel() {
		return Breakable.super.needsLabel() || !this.continues().stream().allMatch(Continue::simpleContinue);
	}
}
