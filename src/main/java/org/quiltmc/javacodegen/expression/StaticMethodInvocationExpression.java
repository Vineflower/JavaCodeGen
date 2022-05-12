package org.quiltmc.javacodegen.expression;

import org.quiltmc.javacodegen.types.Type;

import java.util.List;

public record StaticMethodInvocationExpression(
		Type type,
		String name,
		List<? extends Expression> parameters
) implements Expression{
	@Override
	public void javaLike(StringBuilder builder) {
		type.javaLike(builder);
		builder.append(".");
		builder.append(name);
		builder.append("(");
		boolean first = true;
		for (Expression parameter : parameters) {
			if (!first) {
				builder.append(", ");
			}
			first = false;
			parameter.javaLike(builder);
		}
		builder.append(")");
	}
}
