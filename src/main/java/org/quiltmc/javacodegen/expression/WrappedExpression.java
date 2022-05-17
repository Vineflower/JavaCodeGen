package org.quiltmc.javacodegen.expression;

public record WrappedExpression(Expression expression) implements Expression{

	@Override
	public void javaLike(StringBuilder builder) {
		builder.append("(");
		this.expression.javaLike(builder);
		builder.append(")");
	}
}
