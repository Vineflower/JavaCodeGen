package org.quiltmc.javacodegen.creating;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.javacodegen.ExpressionCreator;
import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.expression.LiteralExpression;
import org.quiltmc.javacodegen.types.PrimitiveTypes;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.Var;

import java.util.function.Predicate;

public class IntegralSwitchContext implements SwitchContext {
	private @Nullable Var var;

	@Override
	public Type getType(boolean isDefault) {
		// if not default, var nonnull

		return isDefault ? PrimitiveTypes.INT : var.type();
	}

	@Override
	public void setVar(Var var) {
		this.var = var;
	}

	@Override
	public Expression createDefault() {
		return new LiteralExpression(PrimitiveTypes.INT, 10000);
	}

	@Override
	public LiteralExpression makeCaseLiteral(ExpressionCreator expressionCreator) {
		return expressionCreator.createPrimitiveConstantExpression((PrimitiveTypes) getType(var == null));
	}

	@Override
	public Predicate<Type> buildFilter() {
		return t -> t instanceof PrimitiveTypes pt && pt.integralType();
	}
}
