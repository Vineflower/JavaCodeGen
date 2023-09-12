package org.vineflower.javacodegen.expression;

public record ParenthesizedExpression(Expression inner) implements Expression{
	@Override
	public boolean isConstant() {
		return this.inner.isConstant();
	}

	@Override
	public Object evaluateConstant() {
		return this.inner.evaluateConstant();
	}

	@Override
	public void javaLike(StringBuilder builder) {
		builder.append('(');
	}
}
