package org.quiltmc.javacodegen.expression;

import org.quiltmc.javacodegen.types.Type;

public record ArrayCreationExpression(Type type) implements Expression{

	@Override
	public void javaLike(StringBuilder builder) {
		throw new IllegalStateException("Not implemented");
	}
}
