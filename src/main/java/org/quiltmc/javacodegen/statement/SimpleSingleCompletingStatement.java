package org.quiltmc.javacodegen.statement;

import java.util.List;

public interface SimpleSingleCompletingStatement extends SingleStatement {
	@Override
	default boolean completesNormally() {
		return true;
	}

	@Override
	default List<? extends SimpleSingleNoFallThroughStatement> breakOuts() {
		return List.of();
	}
}
