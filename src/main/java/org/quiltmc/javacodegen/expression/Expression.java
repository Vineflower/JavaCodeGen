package org.quiltmc.javacodegen.expression;

public interface Expression {
	default boolean isConstant(){
		return false;
	}

	default Object evaluateConstant(){
		throw new IllegalStateException("Not a constant");
	}

	void javaLike(StringBuilder builder);
}
