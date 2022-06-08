package org.quiltmc.javacodegen.creating;

import org.quiltmc.javacodegen.Context;
import org.quiltmc.javacodegen.Creator;
import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.statement.ForStatement;
import org.quiltmc.javacodegen.statement.SimpleSingleNoFallThroughStatement;
import org.quiltmc.javacodegen.statement.Statement;
import org.quiltmc.javacodegen.statement.VarDefStatement;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.FinalType;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.quiltmc.javacodegen.Creator.applyScopesToBreakOut;
import static org.quiltmc.javacodegen.Creator.mergeBreakOutVars;

public class ForCreator {
	public static ForStatement createForStatement(Creator creator, RandomGenerator rng, boolean completesNormally, Context context, Creator.Params params, VarsEntry inVars) {
		// TODO: weirder for loops (i.e. init, cond, incr not using the same var)

		if (completesNormally) {
			if (rng.nextInt(5) == 0) {
				return ForCreator.createInfiniteForWithBreaks(creator, rng, context, params, inVars);
			} else {
				return ForCreator.createFiniteFor(creator, rng, context, params, inVars);
			}
		} else {
			return ForCreator.createInfiniteForWithoutBreaks(creator, rng, context, params, inVars);
		}
	}

	public static ForStatement createInfiniteForWithBreaks(Creator creator, RandomGenerator rng, Context context, Creator.Params params, VarsEntry inVars) {
		// infinite for with breaks
		context.catchesUnlabeledBreaks().mustBreak().canContinue();

		Type outerType = context.typeCreator.createNumericalType();
		VarsEntry vars = new VarsEntry(inVars);
		Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
		vars.create(var, true);
		vars.freeze();

		var body = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars);

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, body.breakOuts(), true, true, true);

		return ForCreator.createFor(
			inVars,
			new VarDefStatement.VarDeclaration(var, 0, context.expressionCreator.createExpression(outerType, inVars)),
			null,
			body,
			context.expressionCreator.buildIncrement(var),
			breakOuts[1],
			breakOuts[2],
			breakOuts[0]
		);
	}


	public static ForStatement createFiniteFor(Creator creator, RandomGenerator rng, Context context, Creator.Params params, VarsEntry inVars) {
		// normal for
		context.catchesUnlabeledBreaks().canBreak().canContinue();


		Type outerType = context.typeCreator.createNumericalType();
		VarsEntry vars = new VarsEntry(inVars);
		Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
		vars.create(var, true);
		vars.freeze();

		var body = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars);

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, body.breakOuts(), false, true, true);

		return ForCreator.createFor(
			inVars,
			new VarDefStatement.VarDeclaration(var, 0, context.expressionCreator.createExpression(outerType, inVars)),
			context.expressionCreator.buildCondition(var),
			body,
			context.expressionCreator.buildIncrement(var),
			breakOuts[1],
			breakOuts[2],
			breakOuts[0]
		);
	}

	public static ForStatement createInfiniteForWithoutBreaks(Creator creator, RandomGenerator rng, Context context, Creator.Params params, VarsEntry inVars) {
		// infinite for without breaks
		context.catchesUnlabeledBreaks().canContinue();
		int breakCache = context.disableBreakGenerationForLabels();

		Type outerType = context.typeCreator.createNumericalType();
		VarsEntry vars = new VarsEntry(inVars);
		Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
		vars.create(var, true);
		vars.freeze();

		var body = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars);

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, body.breakOuts(), false, false, true);

		context.restoreBreakGeneration(breakCache);

		return ForCreator.createFor(
			inVars,
			new VarDefStatement.VarDeclaration(var, 0, context.expressionCreator.createExpression(outerType, inVars)),
			null,
			body,
			context.expressionCreator.buildIncrement(var),
			breakOuts[1],
			breakOuts[2],
			breakOuts[0]
		);
	}
	public static ForStatement createFor(
		VarsEntry scopeVars,
		VarDefStatement.VarDeclaration varDec,
		Expression condition,
		Statement body,
		Expression incr,
		List<? extends SimpleSingleNoFallThroughStatement> breaks,
		List<? extends SimpleSingleNoFallThroughStatement> continues,
		List<? extends SimpleSingleNoFallThroughStatement> breakOuts) {

		VarsEntry outVars;
		if (condition == null) {
			outVars = VarsEntry.applyScopeTo(scopeVars, mergeBreakOutVars(breaks));
		} else {
			outVars = VarsEntry.merge(
				scopeVars,
				VarsEntry.applyScopeTo(scopeVars, VarsEntry.merge(mergeBreakOutVars(breaks), body.varsEntry()))
			);
		}

		return new ForStatement(
			varDec,
			condition,
			incr,
			body,
			breaks,
			continues,
			outVars,
			applyScopesToBreakOut(scopeVars, breakOuts)
		);
	}

}
