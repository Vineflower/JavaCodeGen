package org.vineflower.javacodegen.creating;

import org.vineflower.javacodegen.Context;
import org.vineflower.javacodegen.Creator;
import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.statement.*;
import org.vineflower.javacodegen.vars.VarsEntry;
import org.vineflower.javacodegen.statement.SimpleSingleNoFallThroughStatement;
import org.vineflower.javacodegen.statement.Statement;
import org.vineflower.javacodegen.statement.WhileStatement;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.vineflower.javacodegen.Creator.applyScopesToBreakOut;
import static org.vineflower.javacodegen.Creator.mergeBreakOutVars;

public class WhileCreator {
	public static WhileStatement createWhileStatement(Creator creator, RandomGenerator rng, boolean completesNormally, Context context, Creator.Params params, VarsEntry inVars) {
		// TODO: weirder for loops (i.e. init, cond, incr not using the same var)

		if (completesNormally) {
			if (rng.nextInt(5) == 0 && params.createInfiniteLoops()) {
				return WhileCreator.createInfiniteWhileWithBreaks(creator, rng, context, params, inVars);
			} else {
				return WhileCreator.createFiniteWhile(creator, rng, context, params, inVars);
			}
		} else {
			return WhileCreator.createInfiniteWhileWithoutBreaks(creator, rng, context, params, inVars);
		}
	}

	public static WhileStatement createInfiniteWhileWithBreaks(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars) {
		// infinite for with breaks
		context.catchesUnlabeledBreaks().mustBreak().canContinue();

		var body = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars);

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, body.breakOuts(), true, true, true);

		return createWhile(
			inVars,
			null,
			body,
			breakOuts[1],
			breakOuts[2],
			breakOuts[0]
		);
	}


	public static WhileStatement createFiniteWhile(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars) {
		// normal for
		context.catchesUnlabeledBreaks().canBreak().canContinue();


		final Expression condition = context.expressionCreator.buildCondition(inVars);

		var body = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars);

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, body.breakOuts(), false, true, true);

		return createWhile(
			inVars,
			condition,
			body,
			breakOuts[1],
			breakOuts[2],
			breakOuts[0]
		);
	}

	public static WhileStatement createInfiniteWhileWithoutBreaks(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars
	) {
		// infinite for without breaks
		context.catchesUnlabeledBreaks().canContinue();

		int breakCache = context.disableBreakGenerationForLabels();

		var body = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars);

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, body.breakOuts(), false, false, true);

		context.restoreBreakGeneration(breakCache);

		return createWhile(
			inVars,
			null,
			body,
			breakOuts[1],
			breakOuts[2],
			breakOuts[0]
		);
	}

	public static WhileStatement createWhile(
		VarsEntry scopeVars,
		Expression condition,
		Statement body,
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


		return new WhileStatement(
			condition,
			body,
			breaks,
			continues,
			outVars,
			applyScopesToBreakOut(scopeVars, breakOuts)
		);

	}
}
