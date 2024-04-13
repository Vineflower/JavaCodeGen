package org.vineflower.javacodegen.expression;


import org.vineflower.javacodegen.vars.Var;

public record VariableExpression(Var var) implements Expression{
	@Override
	public void javaLike(StringBuilder builder) {
		var.javaLike(builder);
	}
}
