package org.quiltmc.javacodegen.creating;

import org.quiltmc.javacodegen.Context;
import org.quiltmc.javacodegen.Creator;
import org.quiltmc.javacodegen.statement.Statement;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.random.RandomGenerator;

public class CreateUtils {
	public static Statement createMaybeScopeRestoring(
		Creator creator,
		RandomGenerator rng,
		Context context,
		Creator.Params params,
		VarsEntry inVars) {
		long cache = context.cache();
		// doesn't matter if the body completes or not
		var body = creator.createMaybeScope(
			rng.nextInt(5) == 0, false, context, params, inVars);
		context.restore(cache); // needed for split breakouts
		return body;
	}
}
