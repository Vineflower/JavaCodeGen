package org.vineflower.javacodegen.creating;

import org.vineflower.javacodegen.ExpressionCreator;
import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.expression.LiteralExpression;
import org.vineflower.javacodegen.types.BasicType;
import org.vineflower.javacodegen.types.PrimitiveTypes;
import org.vineflower.javacodegen.types.Type;
import org.vineflower.javacodegen.vars.FinalType;
import org.vineflower.javacodegen.vars.Var;
import org.vineflower.javacodegen.vars.VarsEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class PatternSwitchContext implements SwitchContext {
	private static List<Type> POSSIBLE = List.of(
		PrimitiveTypes.BOOLEAN.Box(),
		PrimitiveTypes.BYTE.Box(),
		PrimitiveTypes.CHAR.Box(),
		PrimitiveTypes.SHORT.Box(),
		PrimitiveTypes.INT.Box(),
		PrimitiveTypes.LONG.Box(),
		PrimitiveTypes.FLOAT.Box(),
		PrimitiveTypes.DOUBLE.Box(),
		Type.NULL
		// TODO: more types, array types generated on demand
	);


	private final List<Type> usedTypes = new ArrayList<>();

	private VarsEntry localInVars;

	@Override
	public Type getType(boolean isDefault) {
		return BasicType.OBJECT;
	}

	@Override
	public void setVar(Var var) {

	}

	@Override
	public int modifyCaseAmt(int rawIn) {
		return 1;
	}

	@Override
	public int modifyBranchAmt(int rawIn) {
		return Math.min(rawIn, POSSIBLE.size());
	}

	@Override
	public Expression createDefault() {
		return sb -> sb.append("new Object()");
	}

	@Override
	public LiteralExpression makeCaseLiteral(ExpressionCreator expressionCreator) {
		Type type = POSSIBLE.get(expressionCreator.getRandom().nextInt(POSSIBLE.size()));
		while (this.usedTypes.contains(type)) {
			type = POSSIBLE.get(expressionCreator.getRandom().nextInt(POSSIBLE.size()));
		}

		this.usedTypes.add(type);

		if (type == Type.NULL) {
			return new LiteralExpression(type, "null");
		}

		Var var = new Var(localInVars.nextName(), type, FinalType.NOT_FINAL);
		localInVars.create(var, true);

		return new LiteralExpression(type, type.toJava() + " " + var.name());
	}

	@Override
	public VarsEntry mapInvars(VarsEntry scopeInVars) {
		return localInVars = new VarsEntry(scopeInVars);
	}

	@Override
	public boolean shouldCaseCompleteNormally(Random random) {
		return false;
	}

	// Return switch pattern matching needs to cover all values, and as we can't analyze individual cases for returns yet we will simply always include a default branch
	@Override
	public boolean modifyIncludesDefault(boolean includeDefault) {
		return true;
	}

	@Override
	public int changeDefaultPosition(int defaultPosition) {
		return 1; // default is always last for patterns, otherwise we may end up with dominance issues.
	}

	@Override
	public Predicate<Type> buildFilter() {
		return t -> t == BasicType.OBJECT;
	}
}
