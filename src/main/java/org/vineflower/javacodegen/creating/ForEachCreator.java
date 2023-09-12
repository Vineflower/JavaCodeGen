package org.vineflower.javacodegen.creating;

import org.vineflower.javacodegen.Context;
import org.vineflower.javacodegen.Creator;
import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.statement.*;
import org.vineflower.javacodegen.types.ArrayType;
import org.vineflower.javacodegen.types.Type;
import org.vineflower.javacodegen.vars.FinalType;
import org.vineflower.javacodegen.vars.Var;
import org.vineflower.javacodegen.vars.VarsEntry;
import org.vineflower.javacodegen.statement.ForEachStatement;
import org.vineflower.javacodegen.statement.SimpleSingleNoFallThroughStatement;
import org.vineflower.javacodegen.statement.Statement;
import org.vineflower.javacodegen.statement.VarDefStatement;

import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

import static org.vineflower.javacodegen.Creator.applyScopesToBreakOut;
import static org.vineflower.javacodegen.Creator.mergeBreakOutVars;

public final class ForEachCreator {
	public static ForEachStatement createForEachStatement(Creator creator, RandomGenerator rng, Context context, Creator.Params params, VarsEntry inVars) {
		context.catchesUnlabeledBreaks().canBreak().canContinue();

		List<Var> arrayTypes = inVars != null ? inVars.vars.entrySet().stream()
			.filter(v -> v.getKey().type() instanceof ArrayType && v.getValue().isDefiniteAssigned())
			.map(Map.Entry::getKey).toList() : List.of();

		VarsEntry vars = new VarsEntry(inVars);
		Expression collection;
		Type base;

		if (arrayTypes.isEmpty() || rng.nextInt(10) == 0) {
			final ArrayType arrayType = context.typeCreator.createArrayType();
			collection = context.expressionCreator.createExpression(arrayType, inVars);
			base = arrayType.componentType();
		} else {
			final Var var = arrayTypes.get(rng.nextInt(arrayTypes.size()));
			collection = var::javaLike;
			base = ((ArrayType) var.type()).componentType();
		}

		final Var var = new Var(vars.nextName(), base, FinalType.NOT_FINAL);
		vars.create(var, true);
		vars.freeze();

		var body = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, vars);

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, body.breakOuts(), false, true, true);

		return ForEachCreator.createForEach(
			inVars,
			collection,
			new VarDefStatement.VarDeclaration(var, 0, null),
			body,
			breakOuts[1],
			breakOuts[2],
			breakOuts[0]
		);
	}

	public static ForEachStatement createForEach(
		VarsEntry scopeVars,
		Expression collection,
		VarDefStatement.VarDeclaration varDecl,
		Statement body,
		List<? extends SimpleSingleNoFallThroughStatement> breaks,
		List<? extends SimpleSingleNoFallThroughStatement> continues,
		List<? extends SimpleSingleNoFallThroughStatement> breakOuts) {
		return new ForEachStatement(
			varDecl,
			collection,
			body,
			breaks,
			continues,
			VarsEntry.merge(
				VarsEntry.applyScopeTo(scopeVars, VarsEntry.merge(mergeBreakOutVars(breaks), body.varsEntry())),
				scopeVars),
			applyScopesToBreakOut(scopeVars, breakOuts)
		);
	}
}
