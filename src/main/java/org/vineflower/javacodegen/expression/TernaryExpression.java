package org.vineflower.javacodegen.expression;

public record TernaryExpression(Expression cond, Expression a, Expression b) implements Expression {

	@Override
	public void javaLike(StringBuilder builder) {
		this.cond.javaLike(builder);
		builder.append(" ? ");
		this.a.javaLike(builder);
		builder.append(" : ");
		this.b.javaLike(builder);
	}
}
