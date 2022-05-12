package org.quiltmc.javacodegen.expression;

import org.quiltmc.javacodegen.types.Type;

/**
 * assert left.type == right.type
 */
public record CompareBinaryOperatorExpression(
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

	public enum Operator {
		GE(">="), GT(">"), LE("<="), LT("<"), EQ("=="), NE("!=");

		final String s;

		Operator(String s) {
			this.s = s;
		}
	}
}
