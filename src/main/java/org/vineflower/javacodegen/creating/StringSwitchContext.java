package org.vineflower.javacodegen.creating;

import org.jetbrains.annotations.Nullable;
import org.vineflower.javacodegen.ExpressionCreator;
import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.expression.LiteralExpression;
import org.vineflower.javacodegen.types.BasicType;
import org.vineflower.javacodegen.types.Type;
import org.vineflower.javacodegen.vars.Var;

import java.util.function.Predicate;

public class StringSwitchContext implements SwitchContext {
	private @Nullable Var var;

	@Override
	public Type getType(boolean isDefault) {
		// if not default, var nonnull

		return BasicType.STRING;
	}

	@Override
	public void setVar(Var var) {
		this.var = var;
	}

	@Override
	public Expression createDefault() {
		return new LiteralExpression(BasicType.STRING, "\"default\"");
	}

	@Override
	public LiteralExpression makeCaseLiteral(ExpressionCreator expressionCreator) {
		return expressionCreator.createRandomString(5);
	}

	@Override
	public Predicate<Type> buildFilter() {
		return t -> t == BasicType.STRING;
	}
}
