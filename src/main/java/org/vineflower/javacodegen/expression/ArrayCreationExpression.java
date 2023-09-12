package org.vineflower.javacodegen.expression;

import org.vineflower.javacodegen.types.Type;

public record ArrayCreationExpression(Type type) implements Expression{

	@Override
	public void javaLike(StringBuilder builder) {
		throw new IllegalStateException("Not implemented");
	}
}
