package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.statement.*;
import org.quiltmc.javacodegen.types.ArrayType;
import org.quiltmc.javacodegen.types.BasicType;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.FinalType;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

public class Creator {
	private final Random random;
	private final TypeCreator typeCreator;
	private final ExpressionCreator expressionCreator;


	public Creator(Random random) {
		this.random = random;
		this.typeCreator = new TypeCreator(new Random(random.nextLong()));
		this.expressionCreator = new ExpressionCreator(new Random(random.nextLong()));
	}

	public Creator(long seed) {
		this(new Random(seed));
	}

	public Creator() {
		this(new Random());
	}

	SimpleSingleCompletingStatement createExpressionStatement(VarsEntry vars) {
		if (this.random.nextInt(3) == 0)
			return this.createVarDefStatement(vars, 3); // var def statements aren't considered expressions statements in the spec
		else
			return new ExpressionStatement(vars.copy(), this.expressionCreator.createStandaloneExpression(null, vars));
	}

	VarDefStatement createVarDefStatement(VarsEntry vars, int expectedVarCount) {
		Type outerType = this.typeCreator.createType();
		if (this.random.nextInt(5) != 0) {
			// simple single var
			Expression value = this.random.nextInt(3) == 0 ? null : this.expressionCreator.createExpression(outerType, vars);
			final Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
			vars.create(var, value != null);
			return new VarDefStatement(vars, outerType,
					List.of(new VarDefStatement.VarDeclaration(var, 0, value)));
		} else {
			int varCount = this.poisson(expectedVarCount) + 1;

			List<VarDefStatement.VarDeclaration> varDeclarations = new ArrayList<>(varCount);

			for (int i = 0; i < varCount; i++) {
				int depth = this.random.nextInt(5) == 0 ? this.poisson(3) : 0;
				Type innerType = ArrayType.ofDepth(outerType, depth);
				Expression value = this.random.nextInt(3) == 0 ? null : this.expressionCreator.createExpression(innerType, vars);
				final Var var = new Var(vars.nextName(), innerType, FinalType.NOT_FINAL);
				vars.create(var, value != null);
				varDeclarations.add(new VarDefStatement.VarDeclaration(var, depth, value));
			}

			return new VarDefStatement(vars, outerType, varDeclarations);
		}

	}

	SimpleSingleCompletingStatement createSimpleSingleCompletingStatement(VarsEntry vars) {
		return this.random.nextInt(20) == 0
				? new EmptyStatement()
				: this.createExpressionStatement(vars);
	}

	SimpleSingleNoFallThroughStatement createSimpleSingleNoFallThroughStatement(Context context) {
		return context.createBreak(this.random);
	}

	SingleStatement createSingleStatement(boolean completesNormally, Context context, VarsEntry vars) {
		if (completesNormally) {
			return this.createSimpleSingleCompletingStatement(vars);
		} else {
			return this.createSimpleSingleNoFallThroughStatement(context);
		}
	}

	Statement createStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (this.random.nextDouble() * this.random.nextDouble() * params.size <= .5) {
			return this.createSingleStatement(completesNormally, context, vars);
		}

		return switch (this.random.nextInt(18)) {
			case 0, 9 -> this.createLabeledStatement(completesNormally, context, params, vars.copy());
			case 1 -> this.createScope(completesNormally, false, context, params, vars.copy());
			case 2, 3, 4 -> this.createIfStatement(completesNormally, context, params, vars.copy());
			case 5, 6 -> this.createWhileStatement(completesNormally, context, params, vars.copy());
			case 7, 8 -> this.createForStatement(completesNormally, context, params, vars.copy());
			case 10, 11 -> this.createMonitorStatement(completesNormally, context, params, vars.copy());
			case 12, 13, 14, 15 -> this.createTryCatchStatement(completesNormally, context, params, vars.copy());
			case 16, 17 -> this.createForEachStatement(completesNormally, context, params, vars.copy());
			default -> throw new IllegalStateException();
		};

	}

	private IfStatement createIfStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		// TODO: expressions for all of these
		if (completesNormally) {
			if (this.random.nextInt(params.size < 5 ? 2 : 3) == 0) {
				return new IfStatement(
						new ConditionStatement(vars.copy(), this.expressionCreator.buildCondition(vars)),
						this.createMaybeScope(this.random.nextInt(3) != 0, context, params, vars.copy()),
						null
				);
			}
		}

		var sub = params.div(1.5);
		if (!completesNormally || this.random.nextInt(3) == 0) {
			return new IfStatement(
					new ConditionStatement(vars.copy(), this.expressionCreator.buildCondition(vars)),
					this.createMaybeScope(false, context, sub, vars.copy()),
					this.createMaybeScope(completesNormally, context, sub, vars.copy())
			);
		} else {
			return new IfStatement(
					new ConditionStatement(vars.copy(), this.expressionCreator.buildCondition(vars)),
					this.createMaybeScope(true, context, sub, vars.copy()),
					this.createMaybeScope(this.random.nextInt(3) != 0, context, sub, vars.copy())
			);
		}
	}

	private Statement createWhileStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (completesNormally) {
			if (this.random.nextInt(5) == 0) {
				// TODO add must break
			}
			WhileStatement whileStatement = new WhileStatement(new ConditionStatement(vars.copy(), this.expressionCreator.buildCondition(vars)));
			context.addContinuable(whileStatement);
			context.addBreak(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(this.random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(whileStatement);
			context.removeBreak(whileStatement);
			return whileStatement;
		} else {
			// while true without a break
			WhileTrueStatement whileStatement = new WhileTrueStatement();
			context.addContinuable(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(this.random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(whileStatement);
			return whileStatement;
		}
	}

	private Statement createForStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (completesNormally) {
			if (this.random.nextInt(5) == 0) {
				// TODO add must break
			}
			Type outerType = this.typeCreator.createNumericalType();
			final Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
			vars.create(var, true);

			// TODO: weirder for loops (i.e. init, cond, incr not using the same var)

			ForStatement forStatement = new ForStatement(
					new VarDefStatement.VarDeclaration(var, 0, this.expressionCreator.createExpression(outerType, vars)),
					new ConditionStatement(vars.copy(), this.expressionCreator.buildCondition(var)),
					this.expressionCreator.buildIncrement(var), vars);

			context.addContinuable(forStatement);
			context.addBreak(forStatement);
			// doesn't matter if it the inner completes normally or not
			forStatement.setBlock(this.createMaybeScope(this.random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(forStatement);
			context.removeBreak(forStatement);
			return forStatement;
		} else {
			// TODO: non completing for?

			// while true without a break
			WhileTrueStatement whileStatement = new WhileTrueStatement();
			context.addContinuable(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(this.random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(whileStatement);
			return whileStatement;
		}
	}

	private Statement createForEachStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (completesNormally) {
			if (this.random.nextInt(5) == 0) {
				// TODO add must break
			}

			List<Var> arrayTypes = vars.vars.entrySet().stream()
					.filter(v -> v.getKey().type() instanceof ArrayType && v.getValue().isDefiniteAssigned())
					.map(Map.Entry::getKey).toList();

			if (arrayTypes.isEmpty()) {
				return this.createForStatement(completesNormally, context, params, vars);
			}

			Var arrVar = arrayTypes.get(this.random.nextInt(arrayTypes.size()));

			ArrayType type = (ArrayType) arrVar.type();
			Type base = type.depth() == 1 ? type.base() : ArrayType.ofDepth(type.base(), type.depth() - 1);

			final Var var = new Var(vars.nextName(), base, FinalType.NOT_FINAL);
			vars.create(var, true);

			ForEachStatement forEachStatement = new ForEachStatement(
					new VarDefStatement.VarDeclaration(var, 0, null),
					arrVar, vars);

			context.addContinuable(forEachStatement);
			context.addBreak(forEachStatement);
			// doesn't matter if it the inner completes normally or not
			forEachStatement.setBlock(this.createMaybeScope(this.random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(forEachStatement);
			context.removeBreak(forEachStatement);
			return forEachStatement;
		} else {
			// TODO: non completing foreach?

			// while true without a break
			WhileTrueStatement whileStatement = new WhileTrueStatement();
			context.addContinuable(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(this.random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(whileStatement);
			return whileStatement;
		}
	}

	private Statement createMonitorStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		Statement st = this.createMaybeScope(completesNormally, context, params, vars.copy());

		return new MonitorStatement(st);
	}

	private List<TryCatchStatement.CatchClause> makeCatches(
			boolean atLeastOneCompletesNormally, Context context, Params params, VarsEntry vars) {
		int catchCount = 1;// TODO: multiple clauses, but needs dominance checking: poisson(0.7) + 1;
		List<TryCatchStatement.CatchClause> catches = new ArrayList<>(catchCount);

		Params sub = params.div(Math.sqrt(catchCount));

		for (; catchCount > 0; catchCount--) {
			boolean shouldComplete = false;
			if (atLeastOneCompletesNormally) {
				if (this.random.nextInt(catchCount / 2 + 1) == 0) {
					atLeastOneCompletesNormally = this.random.nextBoolean();
					shouldComplete = true;
				}
			}

			VarsEntry entry = vars.copy();
			final Var var = new Var(vars.nextName(), BasicType.EXCEPTION, FinalType.NOT_FINAL);
			entry.create(var, true);
			catches.add(new TryCatchStatement.CatchClause(var, this.createMaybeScope(shouldComplete, context, sub, entry)));
		}

		return catches;
	}

	private Statement createTryCatchStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		int tryCatchFinallyCase = this.random.nextInt(3); // 0 => only try, 1 => only finally

		Params sub = params.div(tryCatchFinallyCase == 2 ? 1.6 : 1.4);

		boolean tryComplete = completesNormally;
		boolean catchComplete = completesNormally;
		boolean finallyComplete = completesNormally;
		switch (tryCatchFinallyCase) {
			case 0 -> {
				switch (this.random.nextInt(3)) {
					case 0 -> tryComplete = false;
					case 1 -> catchComplete = false;
				}
			}
			case 1 -> {
				switch (this.random.nextInt(3)) {
					case 0 -> tryComplete = true;
					case 1 -> finallyComplete = true;
				}
			}
			case 2 -> {
				if (completesNormally) {
					switch (this.random.nextInt(3)) {
						case 0 -> tryComplete = false;
						case 1 -> catchComplete = false;
					}
				} else {
					if (this.random.nextBoolean()) {
						finallyComplete = true;
					} else {
						tryComplete = this.random.nextBoolean();
						catchComplete = this.random.nextBoolean();
					}
				}
			}
		}

		Statement tryStat = this.createMaybeScope(tryComplete, context, sub, vars.copy());
		List<TryCatchStatement.CatchClause> catchClauses = tryCatchFinallyCase == 1
				? List.of()
				: this.makeCatches(catchComplete, context, sub, vars);
		Statement finallyStat = tryCatchFinallyCase == 0
				? null
				: this.createMaybeScope(finallyComplete, context, sub, vars.copy());

		final TryCatchStatement tryCatchStatement = new TryCatchStatement(tryStat, catchClauses, finallyStat, vars.copy());
		assert tryCatchStatement.completesNormally() == completesNormally;
		return tryCatchStatement;

	}

	private Statement createLabeledStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		LabeledStatement label = new LabeledStatement();

		Statement st = this.createStatement(true, context, params, vars.copy());

		if (st instanceof LabelImpossible) {
			return st;
		}

		if (completesNormally) {
			context.addBreak(label);

			// TODO: also allow it to not complete normally and as long as there is at least one break to this one
			label.setInner(
					st
			);
			context.removeBreak(label);
		} else {
			label.setInner(
					st
			);
		}

		return label;
	}

	Statement createMaybeScope(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (this.random.nextInt(params.size < 3 ? 2 : 4) == 0) {
			Scope scope = new Scope(vars.copy());

			scope.addStatement(this.createStatement(completesNormally, context, params, vars.copy()));

			return scope;
		} else {
			return this.createScope(completesNormally, false, context, params, vars.copy());
		}
	}

	Scope createScope(boolean completesNormally, boolean root, Context context, Params params, VarsEntry vars) {
		Scope scope = new Scope(vars);

		int targetSize = params.targetSize(this.random) + (completesNormally ? 0 : 1);
		if (targetSize == 0) {
			if (completesNormally) {
				return scope;
			}
		}

		Params sub = params.div(Math.sqrt(targetSize));

		if (root) {
			scope.addStatement(
					this.createVarDefStatement(vars, 4 + this.random.nextInt(4))
			);
		}

		// all but the last statement have to complete normally
		for (int i = 1; i < targetSize; i++) {
			scope.addStatement(
					this.createStatement(true, context, sub, vars)
			);
		}

		scope.addStatement(
				this.createStatement(completesNormally, context, sub, vars)
		);

		return scope;
	}


	record Params(double size) {


		Params div(double scale) {
			return new Params(this.size / scale);
		}

		// poisson distribution
		int targetSize(RandomGenerator randomGenerator) {
			return poisson(this.size, randomGenerator);
		}
	}

	public static void main(String[] args) {
		Statement statement = (new Creator(0L)).createScope(
				false,
				true,
				new Context(),
				new Params(30),
				new VarsEntry()
		);
		System.out.println(statement);

		System.out.println();
		System.out.println();
		System.out.println();

		StringBuilder stringBuilder = new StringBuilder();
		statement.javaLike(stringBuilder, "");
		System.out.println(stringBuilder);
	}

	private int poisson(double size) {
		return poisson(size, this.random);
	}

	private static int poisson(double size, RandomGenerator randomGenerator) {
		int res = 0;
		double p = 1;
		double l = Math.exp(-size);
		while ((p *= randomGenerator.nextDouble()) >= l) {
			res++;
		}
		return res;
	}
}
