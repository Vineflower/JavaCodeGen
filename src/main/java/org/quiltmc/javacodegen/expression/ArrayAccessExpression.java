package org.quiltmc.javacodegen.expression;

import org.quiltmc.javacodegen.types.Type;

/**
 * assert array.type is array
 * assert index.type is int
 */
public record ArrayAccessExpression(
		Expression array,
		Expression index
) implements Expression {
	@Override
	public void javaLike(StringBuilder builder) {
		this.array.javaLike(builder);
		builder.append('[');
		this.index.javaLike(builder);
		builder.append(']');
	}
}
