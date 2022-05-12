package org.quiltmc.javacodegen.expression;

import org.quiltmc.javacodegen.types.Type;

/**
 * assert left.type == right.type
 */
public record BooleanBinaryOperatorExpression(
		Expression left,
		Expression right,
		Operator op
) implements Expression {
	public BooleanBinaryOperatorExpression {
		// assert this.left().type().equals(this.right().type());
	}

//	@Override
//	public Type.Primitive.BOOLEAN type() {
//		return Type.Primitive.BOOLEAN.INSTANCE;
//	}

	@Override
	public boolean isConstant() {
		return switch (this.op) {
			case ANDAND -> this.left().isConstant()
					&& this.left().evaluateConstant().equals(false) || this.right.isConstant();
			case OROR -> this.left().isConstant()
					&& this.left().evaluateConstant().equals(true) || this.right.isConstant();
			default -> this.left.isConstant() && this.right.isConstant();
		};
	}

	@Override
	public Boolean evaluateConstant() {
		return switch (this.op) {
			case ANDAND -> ((Boolean) this.left().evaluateConstant()) && ((Boolean) this.right().evaluateConstant());
			case OROR -> ((Boolean) this.left().evaluateConstant()) || ((Boolean) this.right().evaluateConstant());
			case AND -> ((Boolean) this.left().evaluateConstant()) & ((Boolean) this.right().evaluateConstant());
			case OR -> ((Boolean) this.left().evaluateConstant()) | ((Boolean) this.right().evaluateConstant());
		};
	}

	@Override
	public void javaLike(StringBuilder builder) {
		this.left().javaLike(builder);
		builder.append(" ").append(this.op.s).append(" ");
		this.right().javaLike(builder);
	}

	public enum Operator {
		AND("&"), OR("|"), ANDAND("&&"), OROR("||");

		final String s;

		Operator(String s) {
			this.s = s;
		}
	}
}
