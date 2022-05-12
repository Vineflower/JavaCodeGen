package org.quiltmc.javacodegen.expression;

import org.quiltmc.javacodegen.types.Type;

public class ClassInstanceCreationExpression implements Expression{

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public void javaLike(StringBuilder builder) {
		throw new IllegalStateException("Not implemented");
	}
}
