package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.statement.*;
import org.quiltmc.javacodegen.types.ArrayType;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.FinalType;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Creator {
	// Not thread safe?
	private static final Random random = new Random();

	SimpleSingleCompletingStatement createExpressionStatement(VarsEntry vars) {
		if (random.nextInt(3) == 0)
			return createVarDefStatement(vars, 3); // var def statements aren't considered expressions statements in the spec
		else
			return new ExpressionStatement(vars.copy(), ExpressionCreator.createStandaloneExpression(null, vars));
	}

	VarDefStatement createVarDefStatement(VarsEntry vars, int expectedVarCount) {
		Type outerType = TypeCreator.createType();
		if (random.nextInt(5) != 0) {
			// simple single var
			Expression value = random.nextInt(3) == 0 ? null : ExpressionCreator.createExpression(outerType, vars);
			final Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
			vars.create(var, value != null);
			return new VarDefStatement(vars, outerType,
					List.of(new VarDefStatement.VarDeclaration(var, 0, value)));
		} else {
			int varCount = poisson(expectedVarCount) + 1;

			List<VarDefStatement.VarDeclaration> varDeclarations = new ArrayList<>(varCount);

			for (int i = 0; i < varCount; i++) {
				int depth = random.nextInt(5) == 0 ? poisson(3) : 0;
				Type innerType = ArrayType.ofDepth(outerType, depth);
				Expression value = random.nextInt(3) == 0 ? null : ExpressionCreator.createExpression(innerType, vars);
				final Var var = new Var(vars.nextName(), innerType, FinalType.NOT_FINAL);
				vars.create(var, value != null);
				varDeclarations.add(new VarDefStatement.VarDeclaration(var, depth, value));
			}

			return new VarDefStatement(vars, outerType, varDeclarations);
		}

	}

	SimpleSingleCompletingStatement createSimpleSingleCompletingStatement(VarsEntry vars) {
		return random.nextInt(20) == 0
				? new EmptyStatement()
				: this.createExpressionStatement(vars);
	}

	SimpleSingleNoFallThroughStatement createSimpleSingleNoFallThroughStatement(Context context) {
		return context.createBreak();
	}

	SingleStatement createSingleStatement(boolean completesNormally, Context context, VarsEntry vars) {
		if (completesNormally) {
			return this.createSimpleSingleCompletingStatement(vars);
		} else {
			return this.createSimpleSingleNoFallThroughStatement(context);
		}
	}

	Statement createStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (random.nextDouble() * random.nextDouble() * params.size <= .5) {
			return this.createSingleStatement(completesNormally, context, vars);
		}

		return switch (random.nextInt(10)) {
			case 0, 9 -> this.createLabeledStatement(completesNormally, context, params, vars.copy());
			case 1 -> this.createScope(completesNormally, false, context, params, vars.copy());
			case 2, 3, 4 -> this.createIfStatement(completesNormally, context, params, vars.copy());
			case 5, 6 -> this.createWhileStatement(completesNormally, context, params, vars.copy());
			case 7, 8 -> this.createForStatement(completesNormally, context, params, vars.copy());
			default -> throw new IllegalStateException();
		};

	}

	private IfStatement createIfStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		// TODO: expressions for all of these
		if (completesNormally) {
			if (random.nextInt(params.size < 5 ? 2 : 3) == 0) {
				return new IfStatement(
						new ConditionStatement(vars.copy(), ExpressionCreator.buildCondition(vars)),
						this.createMaybeScope(random.nextInt(3) != 0, context, params, vars.copy()),
						null
				);
			}
		}

		var sub = params.div(1.5);
		if (!completesNormally || random.nextInt(3) == 0) {
			return new IfStatement(
					new ConditionStatement(vars.copy(), ExpressionCreator.buildCondition(vars)),
					this.createMaybeScope(false, context, sub, vars.copy()),
					this.createMaybeScope(completesNormally, context, sub, vars.copy())
			);
		} else {
			return new IfStatement(
					new ConditionStatement(vars.copy(), ExpressionCreator.buildCondition(vars)),
					this.createMaybeScope(true, context, sub, vars.copy()),
					this.createMaybeScope(random.nextInt(3) != 0, context, sub, vars.copy())
			);
		}
	}

	private Statement createWhileStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (completesNormally) {
			if (random.nextInt(5) == 0) {
				// TODO add must break
			}
			WhileStatement whileStatement = new WhileStatement(new ConditionStatement(vars.copy(), ExpressionCreator.buildCondition(vars)));
			context.addContinuable(whileStatement);
			context.addBreak(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(whileStatement);
			context.removeBreak(whileStatement);
			return whileStatement;
		} else {
			// while true without a break
			WhileTrueStatement whileStatement = new WhileTrueStatement();
			context.addContinuable(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(whileStatement);
			return whileStatement;
		}
	}

	private Statement createForStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		if (completesNormally) {
			if (random.nextInt(5) == 0) {
				// TODO add must break
			}
			Type outerType = TypeCreator.createNumericalType();
			final Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
			vars.create(var, true);

			// TODO: weirder for loops (i.e. init, cond, incr not using the same var)

			ForStatement forStatement = new ForStatement(
					new VarDefStatement.VarDeclaration(var, 0, ExpressionCreator.createExpression(outerType, vars)),
					new ConditionStatement(vars.copy(), ExpressionCreator.buildCondition(var)),
					ExpressionCreator.buildIncrement(var), vars);

			context.addContinuable(forStatement);
			context.addBreak(forStatement);
			// doesn't matter if it the inner completes normally or not
			forStatement.setBlock(this.createMaybeScope(random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(forStatement);
			context.removeBreak(forStatement);
			return forStatement;
		} else {
			// TODO: non completing for?

			// while true without a break
			WhileTrueStatement whileStatement = new WhileTrueStatement();
			context.addContinuable(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(random.nextInt(5) == 0, context, params, vars.copy()));
			context.removeContinuable(whileStatement);
			return whileStatement;
		}
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
		if (random.nextInt(params.size < 3 ? 2 : 4) == 0) {
			Scope scope = new Scope(vars.copy());

			scope.addStatement(this.createStatement(completesNormally, context, params, vars.copy()));

			return scope;
		} else {
			return this.createScope(completesNormally, false, context, params, vars.copy());
		}
	}

	Scope createScope(boolean completesNormally, boolean root, Context context, Params params, VarsEntry vars) {
		Scope scope = new Scope(vars);

		int targetSize = params.targetSize() + (completesNormally ? 0 : 1);
		if (targetSize == 0) {
			if (completesNormally) {
				return scope;
			}
		}

		Params sub = params.div(Math.sqrt(targetSize));

		if (root) {
			scope.addStatement(
					createVarDefStatement(vars, 4 + random.nextInt(4))
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


	static record Params(double size) {


		Params div(double scale) {
			return new Params(this.size / scale);
		}

		// poisson distribution
		int targetSize() {
			return poisson(this.size);
		}
	}

	public static void main(String[] args) {
		Statement statement = (new Creator()).createScope(
				false,
				true,
				new Context(),
				new Params(20),
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

	private static int poisson(double size) {
		int res = 0;
		double p = 1;
		double l = Math.exp(-size);
		while ((p *= random.nextDouble()) >= l) {
			res++;
		}
		return res;
	}
}
