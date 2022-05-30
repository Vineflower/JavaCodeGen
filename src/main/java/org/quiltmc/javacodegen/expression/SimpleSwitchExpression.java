package org.quiltmc.javacodegen.expression;


import java.util.List;

public record SimpleSwitchExpression(
	Expression in,
	List<Expression> cases,
	List<Expression> out
) implements Expression {

	@Override
	public void javaLike(StringBuilder builder) {

	}
}
