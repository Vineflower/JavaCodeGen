package org.quiltmc.javacodegen.expression;

public record InstanceFieldAccessExpression(Expression target, String fieldName) implements Expression {

	@Override
	public void javaLike(StringBuilder builder) {
		this.target.javaLike(builder);
		builder.append(".");
		builder.append(this.fieldName);
	}
}
