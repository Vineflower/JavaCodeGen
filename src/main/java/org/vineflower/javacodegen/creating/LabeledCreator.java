package org.vineflower.javacodegen.creating;

import org.vineflower.javacodegen.Context;
import org.vineflower.javacodegen.Creator;
import org.vineflower.javacodegen.statement.*;
import org.vineflower.javacodegen.vars.VarsEntry;
import org.vineflower.javacodegen.statement.LabelImpossible;
import org.vineflower.javacodegen.statement.LabeledStatement;
import org.vineflower.javacodegen.statement.SimpleSingleNoFallThroughStatement;
import org.vineflower.javacodegen.statement.Statement;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.vineflower.javacodegen.Creator.applyScopesToBreakOut;
import static org.vineflower.javacodegen.Creator.mergeBreakOutVars;

public class LabeledCreator {
	public static LabeledStatement createLabeledStatement(
		Creator creator,
		RandomGenerator rng,
		boolean completesNormally,
		Context context,
		Creator.Params params,
		VarsEntry inVars) {
		if (completesNormally) {
			if (rng.nextInt(5) == 0) {
				return createWithForcedBreaks(creator, rng, context, params, inVars);
			} else {
				return createWithOptionalBreaks(creator, rng, context, params, inVars);
			}
		} else {
			return createNonCompletingWithoutBreaks(creator, rng, context, params, inVars);
		}
	}

	public static LabeledStatement createLabeled(
		VarsEntry scopeVars,
		Statement inner,
		List<? extends SimpleSingleNoFallThroughStatement> breaks,
		List<? extends SimpleSingleNoFallThroughStatement> breakOuts) {
		return new LabeledStatement(
			inner,
			breaks,
			VarsEntry.applyScopeTo(scopeVars, VarsEntry.merge(mergeBreakOutVars(breaks), inner.varsEntry())),
			applyScopesToBreakOut(scopeVars, breakOuts)
		);
	}

	public static LabeledStatement createNonCompletingWithoutBreaks(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars
	) {
		// labeled, but no breaks
		Statement st = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars, false);

		if (st instanceof LabelImpossible) {
			throw new RuntimeException("LabelImpossible");
		}

		return createLabeled(inVars, st, List.of(), st.breakOuts());
	}

	public static LabeledStatement createWithForcedBreaks(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars
	) {
		context.mustBreak();

		// labeled, with breaks
		Statement st = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars, false);

		if (st instanceof LabelImpossible) {
			throw new RuntimeException("LabelImpossible");
		}

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, st.breakOuts(), true, true, false);

		return createLabeled(inVars, st, breakOuts[1], breakOuts[0]);
	}

	public static LabeledStatement createWithOptionalBreaks(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars
	) {
		context.canBreak();

		// labeled, with breaks
		Statement st = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars, true);

		if (st instanceof LabelImpossible) {
			throw new RuntimeException("LabelImpossible");
		}

		List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
			rng, st.breakOuts(), false, true, false);

		return createLabeled(inVars, st, breakOuts[1], breakOuts[0]);
	}
}
