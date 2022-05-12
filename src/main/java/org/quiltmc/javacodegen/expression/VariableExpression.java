package org.quiltmc.javacodegen.expression;


import org.quiltmc.javacodegen.vars.Var;

public record VariableExpression(Var var) implements Expression{
	@Override
	public void javaLike(StringBuilder builder) {
		var.javaLike(builder);
	}
}
