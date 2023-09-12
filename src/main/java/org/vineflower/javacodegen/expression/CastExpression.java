package org.vineflower.javacodegen.expression;

import org.vineflower.javacodegen.types.PrimitiveTypes;
import org.vineflower.javacodegen.types.Type;

public record CastExpression(
		Type type,
		Expression expression,
		boolean implicit
) implements Expression {
	@Override
	public boolean isConstant() {
		return this.type instanceof PrimitiveTypes && this.expression.isConstant(); // todo: support strings
	}

	@Override
	public void javaLike(StringBuilder builder) {
		if (!this.implicit) {
			builder.append("(");
			this.type.javaLike(builder);
			builder.append(")");
		}
		this.expression.javaLike(builder);
	}
}
