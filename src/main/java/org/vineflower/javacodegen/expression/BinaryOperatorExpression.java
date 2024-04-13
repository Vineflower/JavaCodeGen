package org.vineflower.javacodegen.expression;


public record BinaryOperatorExpression(
		Expression left,
		Expression right,
		Operator op
) implements Expression {

	@Override
	public boolean isConstant() {
		return this.left.isConstant() && this.right.isConstant();
	}

	@Override
	public void javaLike(StringBuilder builder) {
		this.left().javaLike(builder);
		builder.append(" ").append(this.op.s).append(" ");
		this.right().javaLike(builder);
	}

	enum Operator {
		ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("/"), AND("&"), OR("|"), XOR("^"),
		SHL("<<"), SHR(">>"), USHR(">>>");

		final String s;

		Operator(String s) {
			this.s = s;
		}
	}
}
