package org.quiltmc.javacodegen.statement;

public interface SimpleSingleCompletingStatement extends SingleStatement{
	@Override
	default boolean completesNormally(){
		return true;
	}
}
