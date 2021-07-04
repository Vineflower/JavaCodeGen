package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.statement.*;

import java.util.Random;

public class Creator {
	// Not thread safe?
	private static final Random random = new Random();

	ExpressionStatement createExpressionStatement() {
		//if(random.nextInt(5) == 0)
		//	return new StandardLocalDeclaration();
		//else
		return new ExpressionStatement();
	}

	SimpleSingleCompletingStatement createSimpleSingleCompletingStatement() {
		return random.nextInt(20) == 0
				? new EmptyStatement()
				: this.createExpressionStatement();
	}

	SimpleSingleNoFallThroughStatement createSimpleSingleNoFallThroughStatement(Context context) {
		return context.createBreak();
	}

	SingleStatement createSingleStatement(boolean completesNormally, Context context) {
		if (completesNormally) {
			return this.createSimpleSingleCompletingStatement();
		} else {
			return this.createSimpleSingleNoFallThroughStatement(context);
		}
	}

	Statement createStatement(boolean completesNormally, Context context, Params params) {
		if (random.nextDouble() * random.nextDouble() * params.size <= .5) {
			return this.createSingleStatement(completesNormally, context);
		}

		return switch (random.nextInt(7)) {
			case 0 -> this.createLabeledStatement(completesNormally, context, params);
			case 1 -> this.createScope(completesNormally, context, params);
			case 2, 3, 4 -> this.createIfStatement(completesNormally, context, params);
			case 5, 6 -> this.createWhileStatement(completesNormally, context, params);
			default -> throw new IllegalStateException();
		};

	}

	private IfStatement createIfStatement(boolean completesNormally, Context context, Params params) {
		// TODO: expressions for all of these
		if (completesNormally) {
			if (random.nextInt(params.size < 5 ? 2 : 3) == 0) {
				return new IfStatement(
						new ExpressionStatement(),
						this.createMaybeScope(random.nextInt(3) != 0, context, params),
						null
				);
			}
		}

		var sub = params.div(1.5);
		if (!completesNormally || random.nextInt(3) == 0) {
			return new IfStatement(
					new ExpressionStatement(),
					this.createMaybeScope(false, context, sub),
					this.createMaybeScope(completesNormally, context, sub)
			);
		} else {
			return new IfStatement(
					new ExpressionStatement(),
					this.createMaybeScope(true, context, sub),
					this.createMaybeScope(random.nextInt(3) != 0, context, sub)
			);
		}
	}

	private Statement createWhileStatement(boolean completesNormally, Context context, Params params) {
		if (completesNormally) {
			if (random.nextInt(5) == 0) {
				// TODO add must break
			}
			WhileStatement whileStatement = new WhileStatement(new ExpressionStatement()); // TODO: add different conditions
			context.addContinuable(whileStatement);
			context.addBreak(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(random.nextInt(5) == 0, context, params));
			context.removeContinuable(whileStatement);
			context.removeBreak(whileStatement);
			return whileStatement;
		} else {
			// while true without a break
			WhileTrueStatement whileStatement = new WhileTrueStatement();
			context.addContinuable(whileStatement);
			// doesn't matter if it the inner completes normally or not
			whileStatement.setBlock(this.createMaybeScope(random.nextInt(5) == 0, context, params));
			context.removeContinuable(whileStatement);
			return whileStatement;
		}
	}

	private LabeledStatement createLabeledStatement(boolean completesNormally, Context context, Params params) {
		LabeledStatement label = new LabeledStatement();
		if (completesNormally) {
			context.addBreak(label);

			// TODO: also allow it to not complete normally and as long as there is at least one break to this one
			label.setInner(
					this.createStatement(true, context, params)
			);
			context.removeBreak(label);
		} else {
			label.setInner(
					this.createStatement(false, context, params)
			);
		}
		return label;
	}

	Statement createMaybeScope(boolean completesNormally, Context context, Params params) {
		if (random.nextInt(params.size < 3 ? 2 : 4) == 0) {
			return this.createStatement(completesNormally, context, params);
		} else{
			return this.createScope(completesNormally, context, params);
		}
	}

	Scope createScope(boolean completesNormally, Context context, Params params) {
		Scope scope = new Scope();

		int targetSize = params.targetSize() + (completesNormally ? 0 : 1);
		if (targetSize == 0) {
			if (completesNormally) {
				return scope;
			}
		}

		Params sub = params.div(Math.sqrt(targetSize));

		// all but the last statement have to complete normally
		for (int i = 1; i < targetSize; i++) {
			scope.AddStatement(
					this.createStatement(true, context, sub)
			);
		}

		scope.AddStatement(
				this.createStatement(completesNormally, context, sub)
		);

		return scope;
	}


	static record Params(double size) {


		Params div(double scale) {
			return new Params(this.size / scale);
		}

		// poison distribution
		int targetSize() {
			int res = 0;
			double p = 1;
			double l = Math.exp(-this.size);
			while ((p *= random.nextDouble()) >= l) {
				res++;
			}
			return res;
		}
	}

	public static void main(String[] args) {
		Statement statement = (new Creator()).createScope(
				false,
				new Context(),
				new Params(10)
		);
		System.out.println(statement);

		System.out.println();
		System.out.println();
		System.out.println();

		StringBuilder stringBuilder = new StringBuilder();
		statement.javaLike(stringBuilder, "");
		System.out.println(stringBuilder);
	}
}
