package org.vineflower.javacodegen.creating;

import org.jetbrains.annotations.Nullable;
import org.vineflower.javacodegen.ExpressionCreator;
import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.expression.LiteralExpression;
import org.vineflower.javacodegen.types.BasicType;
import org.vineflower.javacodegen.types.Type;
import org.vineflower.javacodegen.vars.Var;

import java.util.function.Predicate;

public class EnumSwitchContext implements SwitchContext {
	private @Nullable Var var;
	private int branchesIn = -1;

	@Override
	public Type getType(boolean isDefault) {
		// if not default, var nonnull

		return BasicType.ELEMENT_TYPE;
	}

	@Override
	public void setVar(Var var) {
		this.var = var;
	}

	@Override
	public Expression createDefault() {
		return new LiteralExpression(BasicType.ELEMENT_TYPE, "ElementType.TYPE");
	}

	@Override
	public int modifyCaseAmt(int rawIn) {
		return Math.min(rawIn, 12 / branchesIn);
	}

	@Override
	public int modifyBranchAmt(int rawIn) {
		return branchesIn = Math.min(rawIn, 12);
	}

	@Override
	public LiteralExpression makeCaseLiteral(ExpressionCreator expressionCreator) {
		return new LiteralExpression(BasicType.ELEMENT_TYPE, switch (expressionCreator.getRandom().nextInt(12)) {
			case 0 -> "TYPE";
			case 1 -> "FIELD";
			case 2 -> "METHOD";
			case 3 -> "PARAMETER";
			case 4 -> "CONSTRUCTOR";
			case 5 -> "LOCAL_VARIABLE";
			case 6 -> "ANNOTATION_TYPE";
			case 7 -> "PACKAGE";
			case 8 -> "TYPE_PARAMETER";
			case 9 -> "TYPE_USE";
			case 10 -> "MODULE";
			case 11 -> "RECORD_COMPONENT";
			default -> throw new IllegalStateException();
		});
	}

	@Override
	public Predicate<Type> buildFilter() {
		return t -> t == BasicType.ELEMENT_TYPE;
	}
}
