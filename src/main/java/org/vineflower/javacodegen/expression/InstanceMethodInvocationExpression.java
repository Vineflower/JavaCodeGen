package org.vineflower.javacodegen.expression;


import java.util.List;

public record InstanceMethodInvocationExpression(
		Expression instance,
		String name,
		List<? extends Expression> parameters
) implements Expression {
	@Override
	public void javaLike(StringBuilder builder) {
		this.instance.javaLike(builder);
		builder.append(".");
		builder.append(this.name);
		builder.append("(");
		boolean first = true;
		for (Expression parameter : this.parameters) {
			if (!first) {
				builder.append(", ");
			}
			first = false;
			parameter.javaLike(builder);
		}
		builder.append(")");
	}
}
