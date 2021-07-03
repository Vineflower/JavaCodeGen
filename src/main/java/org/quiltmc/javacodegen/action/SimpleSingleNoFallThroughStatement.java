package org.quiltmc.javacodegen.action;

public interface SimpleSingleNoFallThroughStatement extends SingleStatement{
	@Override
	default boolean completesNormally() {
		return false;
	}
}
