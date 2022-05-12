package org.quiltmc.javacodegen.expression;

import org.quiltmc.javacodegen.types.Type;

public record StaticFieldAccessExpression(Type target, String fieldName) implements Expression {

	@Override
	public void javaLike(StringBuilder builder) {
		this.target.javaLike(builder);
		builder.append(".");
		builder.append(this.fieldName);
	}
}
