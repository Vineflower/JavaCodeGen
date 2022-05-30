package org.quiltmc.javacodegen.creating;

import org.quiltmc.javacodegen.ExpressionCreator;
import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.expression.LiteralExpression;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.Var;

import java.util.function.Predicate;

public interface SwitchContext {
	Type getType(boolean isDefault);

	void setVar(Var var);

	Expression createDefault();

	default int modifyCaseAmt(int rawIn) {
		return rawIn;
	}

	default int modifyBranchAmt(int rawIn) {
		return rawIn;
	}

	LiteralExpression makeCaseLiteral(ExpressionCreator expressionCreator);

	Predicate<Type> buildFilter();
}
