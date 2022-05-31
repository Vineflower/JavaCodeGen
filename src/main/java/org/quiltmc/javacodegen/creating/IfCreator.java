package org.quiltmc.javacodegen.creating;

import org.quiltmc.javacodegen.Context;
import org.quiltmc.javacodegen.Creator;
import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.statement.IfStatement;
import org.quiltmc.javacodegen.statement.SimpleSingleNoFallThroughStatement;
import org.quiltmc.javacodegen.statement.Statement;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.quiltmc.javacodegen.Creator.applyScopesToBreakOut;
import static org.quiltmc.javacodegen.Creator.mergeBreakOuts;

public class IfCreator {

	public static IfStatement createIfStatement(
		Creator creator,
		RandomGenerator rng,
		boolean completesNormally,
		Context context,
		Creator.Params params,
		VarsEntry inVars
	) {
		if (completesNormally && rng.nextInt(params.size() < 5 ? 2 : 3) == 0) {
			return createSimpleIf(creator, rng, context, params, inVars);
		} else if (!completesNormally || rng.nextInt(3) == 0) {
			return createIfElse(creator, rng, context, params, inVars, false, completesNormally);
		} else {
			return createIfElse(creator, rng, context, params, inVars, true, rng.nextInt(3) != 0);
		}
	}

	public static IfStatement createSimpleIf(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars
	) {
		var condition = context.expressionCreator.buildCondition(inVars);
		var ifBlock = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars);

		return createIf(
			inVars,
			condition,
			ifBlock,
			null,
			ifBlock.breakOuts()
		);
	}

	public static IfStatement createIfElse(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars,
		boolean ifCompletes,
		boolean elseCompletes
	) {
		var condition = context.expressionCreator.buildCondition(inVars);

		var ifBlock = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars, ifCompletes);
		context.applyBreakOuts(ifBlock.breakOuts());

		var elseBlock = CreateUtils.createMaybeScopeRestoring(creator, rng, context, params, inVars, elseCompletes);

		var breakOuts = mergeBreakOuts(ifBlock.breakOuts(), elseBlock.breakOuts());

		return createIf(
			inVars,
			condition,
			ifBlock,
			elseBlock,
			breakOuts
		);
	}


	public static IfStatement createIf(
		VarsEntry scopeVars,
		Expression condition,
		Statement ifTrue,
		Statement ifFalse,
		List<? extends SimpleSingleNoFallThroughStatement> breakOuts) {
		return new IfStatement(
			condition,
			ifTrue,
			ifFalse,
			ifFalse != null ?
				VarsEntry.applyScopeTo(scopeVars, VarsEntry.merge(
					ifTrue.varsEntry(),
					ifFalse.varsEntry()
				)) :
				VarsEntry.merge(
					VarsEntry.applyScopeTo(scopeVars,
						ifTrue.varsEntry()
					), scopeVars),
			applyScopesToBreakOut(scopeVars, breakOuts)
		);
	}
}
