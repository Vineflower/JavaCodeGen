package org.quiltmc.javacodegen.expression;


public record ConditionalExpression(
		Expression condition,
		Expression ifTrue,
		Expression ifFalse
) implements Expression {


	@Override
	public boolean isConstant() {
		return this.condition.isConstant() && this.ifTrue.isConstant() && this.ifFalse.isConstant();
	}

	@Override
	public void javaLike(StringBuilder builder) {
		this.condition.javaLike(builder);
		builder.append(" ? ");
		this.ifTrue.javaLike(builder);
		builder.append(" : ");
		this.ifFalse.javaLike(builder);
	}
}
