package org.vineflower.javacodegen.statement;

import java.util.List;

public interface Continuable extends Breakable {
	@Override
	@Deprecated
	default void initMarks(List<? extends SimpleSingleNoFallThroughStatement> breaks) {
		Breakable.super.initMarks(breaks);
	}

	default void initMarks(List<? extends SimpleSingleNoFallThroughStatement> breaks, List<? extends SimpleSingleNoFallThroughStatement> continues) {
		Breakable.super.initMarks(breaks);
		continues.forEach(this::initMark);
	}

	private void initMark(Statement statement) {
		WrappedBreakOutStatement.<Continue>baseAs(statement).setTarget(this);
	}

	List<? extends SimpleSingleNoFallThroughStatement> continues();

	default boolean hasContinue() {
		return !this.continues().isEmpty();
	}


	@Override
	default boolean needsLabel() {
		return Breakable.super.needsLabel() || !this.continues().stream().allMatch(Continue::simpleContinue);
	}
}
