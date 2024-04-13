package org.vineflower.javacodegen.creating;

import org.vineflower.javacodegen.Context;
import org.vineflower.javacodegen.Creator;
import org.vineflower.javacodegen.statement.Statement;
import org.vineflower.javacodegen.vars.VarsEntry;

import java.util.random.RandomGenerator;

public class CreateUtils {
	public static Statement createMaybeScopeRestoring(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars) {
		return createMaybeScopeRestoring(creator, rng, context, params, inVars, rng.nextInt(5) == 0);
	}

	public static Statement createMaybeScopeRestoring(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars,
		boolean completes) {
		long cache = context.cache();
		// doesn't matter if the body completes or not
		var body = creator.createMaybeScope(completes, false, context, params, inVars);
		context.restore(cache); // needed for split breakouts
		return body;
	}
}
