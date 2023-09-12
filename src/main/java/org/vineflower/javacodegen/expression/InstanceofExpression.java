package org.vineflower.javacodegen.expression;

import org.vineflower.javacodegen.types.Type;

public record InstanceofExpression(
		Type checkedType,
		Expression expression
) implements Expression{
	@Override
	public void javaLike(StringBuilder builder) {
		this.expression.javaLike(builder);
		builder.append(" instanceof ");
		this.checkedType.javaLike(builder);
	}
}
