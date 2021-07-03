package org.quiltmc.javacodegen.statement;

public interface SimpleSingleNoFallThroughStatement extends SingleStatement{
	@Override
	default boolean completesNormally() {
		return false;
	}
}
