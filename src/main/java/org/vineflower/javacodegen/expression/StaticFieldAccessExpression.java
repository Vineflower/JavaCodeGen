package org.vineflower.javacodegen.expression;

import org.vineflower.javacodegen.types.Type;

public record StaticFieldAccessExpression(Type target, String fieldName) implements Expression {

	@Override
	public void javaLike(StringBuilder builder) {
		this.target.javaLike(builder);
		builder.append(".");
		builder.append(this.fieldName);
	}
}
