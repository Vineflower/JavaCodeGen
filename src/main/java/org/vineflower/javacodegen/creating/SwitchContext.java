package org.vineflower.javacodegen.creating;

import org.vineflower.javacodegen.ExpressionCreator;
import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.expression.LiteralExpression;
import org.vineflower.javacodegen.types.Type;
import org.vineflower.javacodegen.vars.Var;
import org.vineflower.javacodegen.vars.VarsEntry;

import java.util.Random;
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

	default VarsEntry mapInvars(VarsEntry scopeInVars) {
		return scopeInVars;
	}

	default boolean shouldCaseCompleteNormally(Random random) {
		return random.nextBoolean();
	}

	default boolean modifyIncludesDefault(boolean includeDefault) {
		return includeDefault;
	}

	default int changeDefaultPosition(int defaultPosition) {
		return defaultPosition;
	}

	LiteralExpression makeCaseLiteral(ExpressionCreator expressionCreator);

	Predicate<Type> buildFilter();
}
