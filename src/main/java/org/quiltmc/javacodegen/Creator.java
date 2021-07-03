package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.action.*;

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
		if (random.nextDouble() * random.nextDouble() * params.size <= 1) {
			return this.createSingleStatement(completesNormally, context);
		}

		return switch (random.nextInt(2)) {
			case 0 -> this.createLabeledStatement(completesNormally, context, params);
			case 1 -> this.createScope(completesNormally, context, params);
			default -> throw new IllegalStateException();
		};

	}

	private LabeledStatement createLabeledStatement(boolean completesNormally, Context context, Params params) {
		LabeledStatement label = new LabeledStatement();
		if(completesNormally) {
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
		for(int i = 1; i < targetSize; i++){
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
		System.out.println((new Creator()).createStatement(
				false,
				new Context(),
				new Params(10)
				));
	}
}
