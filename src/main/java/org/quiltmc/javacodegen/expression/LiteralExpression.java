package org.quiltmc.javacodegen.expression;


import org.quiltmc.javacodegen.types.Type;

public record LiteralExpression(
		Type type,
		Object value
) implements Expression{
	@Override
	public boolean isConstant() {
		return true; // false if this is null
	}

	@Override
	public Object evaluateConstant() {
		return this.value;
	}

	@Override
	public void javaLike(StringBuilder builder) {
		builder.append(this.value);
	}
}
