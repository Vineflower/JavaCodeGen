package org.quiltmc.javacodegen.expression;

public record UnaryOperatorExpression (
		Expression inner,
		Operator operator
) implements Expression{
	@Override
	public boolean isConstant() {
		return this.inner.isConstant();
	}

	@Override
	public void javaLike(StringBuilder builder) {
		if(!this.operator.postfix) {
			builder.append(this.operator.operator);
			this.inner.javaLike(builder);
		} else {
			this.inner.javaLike(builder);
			builder.append(this.operator.operator);
		}
	}

	public enum Operator {
		NOT("!"),
		NEGATE("-"),
		BITWISE_NOT("~"),
		PLUS("+"),
		PRE_INCREMENT("++"),
		PRE_DECREMENT("--"),
		POST_INCREMENT("++", true),
		POST_DECREMENT("--", true);

		private final String operator;
		private final boolean postfix;

		Operator(String operator, boolean postfix) {
			this.operator = operator;
			this.postfix = postfix;
		}

		Operator(String operator) {
			this(operator, false);
		}
	}
}
