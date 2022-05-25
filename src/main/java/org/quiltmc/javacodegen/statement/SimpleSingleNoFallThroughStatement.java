package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public interface SimpleSingleNoFallThroughStatement extends SingleStatement{
	@Override
	default boolean completesNormally() {
		return false;
	}

	@Override
	default List<? extends SimpleSingleNoFallThroughStatement> breakOuts() {
		return List.of(this);
	}

	VarsEntry breakOutVars();

	@Override
	default VarsEntry varsEntry() {
		return VarsEntry.never();
	}
}
