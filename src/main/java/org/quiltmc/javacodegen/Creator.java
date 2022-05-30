package org.quiltmc.javacodegen;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.javacodegen.creating.*;
import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.expression.LiteralExpression;
import org.quiltmc.javacodegen.expression.VariableExpression;
import org.quiltmc.javacodegen.statement.*;
import org.quiltmc.javacodegen.types.ArrayType;
import org.quiltmc.javacodegen.types.BasicType;
import org.quiltmc.javacodegen.types.PrimitiveTypes;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.FinalType;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.*;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

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

	SimpleSingleCompletingStatement createExpressionStatement(VarsEntry vars, boolean allowSingleVarDef) {
		assert vars.isFrozen();
		if (this.random.nextInt(3) == 0 && allowSingleVarDef) {
			// TODO: Vars def statements aren't considered expressions statements in the spec,
			//       these are also not allowed to be the statement in ifs and stuff.
			return this.createVarDefStatement(vars, 3);
		} else {
			VarsEntry postVars = new VarsEntry(vars);
			return new ExpressionStatement(postVars, this.expressionCreator.createStandaloneExpression(null, postVars));
		}
	}

	VarDefStatement createVarDefStatement(VarsEntry inVars, int expectedVarCount) {
		var outerType = this.typeCreator.createType();
		var vars = new VarsEntry(inVars);
		if (this.random.nextInt(5) != 0) {
			// simple single postFinallyVars
			Expression value = this.random.nextInt(3) == 0 ? null : this.expressionCreator.createExpression(outerType, vars);
			final Var var = new Var(vars.nextName(), outerType, FinalType.NOT_FINAL);
			vars.create(var, value != null);
			vars.freeze();
			return new VarDefStatement(
				outerType,
				List.of(new VarDefStatement.VarDeclaration(var, 0, value)),
				vars);
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

			vars.freeze();

			return new VarDefStatement(outerType, varDeclarations, vars);
		}

	}

	SimpleSingleCompletingStatement createSimpleSingleCompletingStatement(VarsEntry vars, boolean allowSingleVarDef) {
		return this.random.nextInt(20) == 0
			? new EmptyStatement(vars)
			: this.createExpressionStatement(vars, allowSingleVarDef);
	}

	SimpleSingleNoFallThroughStatement createSimpleSingleNoFallThroughStatement(Context context, VarsEntry vars) {
		return context.createBreak(this.random, vars);
	}

	SingleStatement createSingleStatement(boolean completesNormally, boolean allowSingleVarDef, Context context, VarsEntry vars) {
		assert vars.isFrozen();
		if (completesNormally) {
			return this.createSimpleSingleCompletingStatement(vars, allowSingleVarDef);
		} else {
			return this.createSimpleSingleNoFallThroughStatement(context, vars);
		}
	}

	Statement createStatement(boolean completesNormally, boolean allowSingleVarDef, Context context, Params params, @NotNull VarsEntry vars) {
		assert vars.isFrozen();
		int neededBreaks = context.neededBreaks;

		final Statement stat;
		if (context.canBeSingle(completesNormally) && this.random.nextDouble() * this.random.nextDouble() * params.size <= .5) {
			stat = this.createSingleStatement(completesNormally, allowSingleVarDef, context, vars);
		} else {
			stat = switch (this.random.nextInt(20)) {
				case 0, 9 -> this.createLabeledStatement(completesNormally, context, params, vars);
				case 1 -> this.createScope(completesNormally, false, context, params, vars);
				case 2, 3, 4 -> this.createIfStatement(completesNormally, context, params, vars);
				case 5, 6 -> this.createWhileStatement(completesNormally, context, params, vars);
				case 7, 8 -> ForCreator.createForStatement(this, this.random, completesNormally, context, params, vars);
				case 10, 11 -> this.createMonitorStatement(completesNormally, context, params, vars);
				case 12, 13, 14, 15 -> this.createTryCatchStatement(completesNormally, context, params, vars);
				case 16, 17 -> completesNormally // foreach always completes normally
					? ForEachCreator.createForEachStatement(this, this.random, context, params, vars)
					: ForCreator.createForStatement(this, this.random, false, context, params, vars);
				case 18, 19 -> this.createSwitchStatement(completesNormally, context, params, vars);

				default -> throw new IllegalStateException();
			};

		}

		assert neededBreaks == 0 || stat.breakOuts().stream().filter(s -> WrappedBreakOutStatement.base(s) instanceof Break && !WrappedBreakOutStatement.isDead(s)).count() >= neededBreaks;
		assert stat.completesNormally() == completesNormally;
		assert context.breakTargets > 0 || stat.breakOuts().stream().noneMatch(s -> WrappedBreakOutStatement.base(s) instanceof Break);
		assert stat.completesNormally() == (stat.varsEntry() != null);
		assert stat.varsEntry() == null || stat.varsEntry().isFrozen();
		assert stat.varsEntry() == null != completesNormally;
		return stat;
	}

	private IfStatement createIfStatement(boolean completesNormally, Context context, Params params, VarsEntry inVars) {
		// fixme: conditions can introduce new variables
		var condition = new ConditionStatement(inVars, this.expressionCreator.buildCondition(inVars));


		// TODO: expressions for all of these
		if (completesNormally && this.random.nextInt(params.size < 5 ? 2 : 3) == 0) {

			var ifBlock = this.createMaybeScope(this.random.nextInt(3) != 0, false, context, params, inVars);

			var outVars = ifBlock.completesNormally() ? VarsEntry.merge(inVars, ifBlock.varsEntry()) : inVars;

			return new IfStatement(
				condition,
				ifBlock,
				null,
				outVars,
				ifBlock.breakOuts()
			);
		}

		var sub = params.div(1.5);
		if (!completesNormally || this.random.nextInt(3) == 0) {
			long cache = context.partial(this.random, 2);

			var ifBlock = this.createMaybeScope(false, false, context, sub, inVars);

			context.restore(cache);
			context.applyBreakOuts(ifBlock.breakOuts());

			var elseBlock = this.createMaybeScope(completesNormally, false, context, sub, inVars);

			var breakOuts = mergeBreakOuts(ifBlock.breakOuts(), elseBlock.breakOuts());

			return new IfStatement(
				condition,
				ifBlock,
				elseBlock,
				completesNormally ? elseBlock.varsEntry() : VarsEntry.never(),
				breakOuts
			);
		} else {
			long cache = context.partial(this.random, 2);

			var ifBlock = this.createMaybeScope(true, false, context, sub, inVars);

			context.restore(cache);
			context.applyBreakOuts(ifBlock.breakOuts());

			var elseBlock =
				this.createMaybeScope(this.random.nextInt(3) != 0, false, context, sub, inVars);

			var breakOuts = mergeBreakOuts(ifBlock.breakOuts(), elseBlock.breakOuts());

			return new IfStatement(
				condition,
				ifBlock,
				elseBlock,
				elseBlock.completesNormally()
					? VarsEntry.merge(ifBlock.varsEntry(), elseBlock.varsEntry())
					: ifBlock.varsEntry(),
				breakOuts
			);
		}
	}

	private static List<? extends SimpleSingleNoFallThroughStatement> mergeBreakOuts(List<? extends SimpleSingleNoFallThroughStatement> as, List<? extends SimpleSingleNoFallThroughStatement> bs) {
		if (as == null) {
			return bs;
		} else if (bs == null) {
			return as;
		} else {
			List<SimpleSingleNoFallThroughStatement> stats = new ArrayList<>(as.size() + bs.size());
			stats.addAll(as);
			stats.addAll(bs);
			return stats;
		}
	}

	private Statement createWhileStatement(boolean completesNormally, Context context, Params params, VarsEntry inVars) {
		if (completesNormally) {
			if (this.random.nextInt(4) == 0) {
				// while true with breaks
				context.neededBreaks++;
				context.breakTargets++;
				context.continueTargets++;

				var body = CreateUtils.createMaybeScopeRestoring(this, this.random, context, params, inVars);

				List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
					this.random, body.breakOuts(), true, true, true);

				return new WhileTrueStatement(
					body,
					breakOuts[1],
					breakOuts[2],
					VarsEntry.applyScopeTo(inVars, mergeBreakOutVars(breakOuts[1])),
					applyScopesToBreakOut(inVars, breakOuts[0])
				);
			} else {
				// while with condition
				context.breakTargets++;
				context.continueTargets++;

				// FIXME: conditions can introduce new variables
				var condition = new ConditionStatement(inVars, this.expressionCreator.buildCondition(inVars));


				long cache = context.cache();
				// doesn't matter if the body completes or not
				var body = this.createMaybeScope(
					this.random.nextInt(5) == 0, false, context, params, inVars);
				context.restore(cache); // needed for split breakouts

				List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
					this.random, body.breakOuts(), false, true, true);

				return new WhileStatement(
					condition,
					body,
					breakOuts[1],
					breakOuts[2],
					VarsEntry.merge(
						VarsEntry.applyScopeTo(inVars, VarsEntry.merge(mergeBreakOutVars(breakOuts[1]), body.varsEntry())),
						inVars
					),
					applyScopesToBreakOut(inVars, breakOuts[0])
				);
			}
		} else {
			// while true without any breaks
			context.continueTargets++;

			// doesn't matter if the body completes or not
			var body = this.createMaybeScope(
				this.random.nextInt(5) == 0, false, context, params, inVars);
			// we dont need breaks, so restoring isn't really needed

			List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
				this.random, body.breakOuts(), false, false, true);

			return new WhileTrueStatement(
				body,
				breakOuts[1],
				breakOuts[2],
				VarsEntry.never(),
				applyScopesToBreakOut(inVars, breakOuts[0])
			);
		}
	}

	public static VarsEntry mergeBreakOutVars(List<? extends SimpleSingleNoFallThroughStatement> breakOut) {
		VarsEntry vars = VarsEntry.never();
		if (breakOut != null) {
			for (SimpleSingleNoFallThroughStatement s : breakOut) {
				vars = VarsEntry.merge(vars, s.breakOutVars());
			}
		}
		return vars;
	}

	public static List<? extends SimpleSingleNoFallThroughStatement> applyScopesToBreakOut(VarsEntry inVars, List<? extends SimpleSingleNoFallThroughStatement> breakOut) {
		return breakOut.stream().map(s -> applyScopeToBreakOut(s, inVars)).toList();
	}

	public SwitchContext getNextSwitchContext(SwitchContext old) {
		if (old instanceof StringSwitchContext) {
			return new IntegralSwitchContext();
		} else if (old instanceof EnumSwitchContext) {
			return new StringSwitchContext();
		} else if (old instanceof IntegralSwitchContext) {
			return null;
		} else {
			return null;
		}
	}

	public SwitchContext randomSwitchContext() {
		return switch (random.nextInt(3)) {
			case 0 -> new StringSwitchContext();
			case 1 -> new IntegralSwitchContext();
			case 2 -> new EnumSwitchContext();
			default -> throw new IllegalStateException();
		};
	}

	private Statement createSwitchStatement(boolean completesNormally, Context context, Params params, VarsEntry inVars) {

		SwitchContext switchContext = randomSwitchContext();
		List<Var> foundVars = new ArrayList<>();

		while (foundVars.isEmpty()) {
			if (switchContext == null) {
				switchContext = randomSwitchContext();
				break;
			}

			Predicate<Type> typeFilter = switchContext.buildFilter();
			foundVars = inVars.vars.entrySet().stream()
				.filter(v -> typeFilter.test(v.getKey().type()) && v.getValue().isDefiniteAssigned())
				.map(Map.Entry::getKey).toList();

			if (foundVars.isEmpty()) {
				switchContext = getNextSwitchContext(switchContext);
			}
		}

		// TODO: move into a creator class

		boolean lastCompletesNormally;
		boolean includeDefault;
		int branchAmt;
		if (completesNormally) {
			context.breakTargets++;
			if (this.random.nextBoolean() && !context.needsBreakOuts()) {
				// allow there to be no branches
				branchAmt = this.poisson(3);
				lastCompletesNormally = true;
				includeDefault = false;
			} else {
				branchAmt = this.poisson(3) + 1;
				if (this.random.nextBoolean()) {
					lastCompletesNormally = true;
					includeDefault = this.random.nextBoolean();
				} else { // make sure we have some breaks
					context.neededBreaks++;
					lastCompletesNormally = false;
					includeDefault = true;
				}
			}
		} else {
			branchAmt = this.poisson(3) + 1;
			lastCompletesNormally = false;
			includeDefault = true;
		}
		branchAmt = switchContext.modifyBranchAmt(branchAmt); // TODO: default
		long preCache = context.cache(); // cache the initial context

		params.div(Math.sqrt(branchAmt));

		Type type;
		Expression expression;
		if (foundVars.isEmpty()) {
			type = switchContext.getType(true);
			expression = switchContext.createDefault();
		} else {
			Var var = foundVars.get(this.random.nextInt(foundVars.size()));
			switchContext.setVar(var);

			type = switchContext.getType(false);
			expression = new VariableExpression(var);
		}

		List<Expression> exprsAll = new ArrayList<>();

		VarsEntry previousCaseVars = VarsEntry.never();
		List<SwitchStatement.CaseBranch> caseBranches = new ArrayList<>();
		List<SimpleSingleNoFallThroughStatement> allBreakOuts = new ArrayList<>();
		int needsDefault = includeDefault ? this.random.nextInt(branchAmt) + 1 : -1;

		for (int i = branchAmt; i > 0; i--) {
			int caseAmt = switchContext.modifyCaseAmt(this.random.nextInt(3) == 0 ? 1 + this.poisson(3) : 1);
			int defaultIdx = this.random.nextInt(caseAmt);
			long cache = context.partial(this.random, i);
			VarsEntry scopeInVars = VarsEntry.merge(inVars, previousCaseVars); // we don't care about variable names (yet)
			boolean shouldCaseCompleteNormally = i == 1 ? lastCompletesNormally : completesNormally && this.random.nextBoolean();

			List<Expression> caseExprs = new ArrayList<>();

			for (int j = 0; j < caseAmt; j++) {
				LiteralExpression litex = switchContext.makeCaseLiteral(this.expressionCreator);
				// FIXME: this is unbelievably bad
				while (exprsAll.contains(litex)) {
					litex = switchContext.makeCaseLiteral(this.expressionCreator);
				}

				caseExprs.add(i == needsDefault && j == defaultIdx ? SwitchStatement.DEFAULT : litex);
				exprsAll.add(litex);
			}

			scopeInVars.freeze();

			List<? extends Statement> statements;
			if (this.random.nextBoolean()) {
				// create scope, and unbox it
				Scope fakeScope = this.createScope(shouldCaseCompleteNormally, false, context, params, scopeInVars);
				statements = fakeScope.statements();
			} else {
				// single statement (maybe a scope)
				statements = List.of(this.createMaybeScope(shouldCaseCompleteNormally, true, context, params, scopeInVars));
			}
			if (!statements.isEmpty()) {
				previousCaseVars = statements.get(statements.size() - 1).varsEntry();
			}

			caseBranches.add(new SwitchStatement.CaseBranch(caseExprs, statements));

			context.restore(cache);
			for (Statement statement : statements) {
				var subBreakOuts = statement.breakOuts();
				if (subBreakOuts != null) {
					context.applyBreakOuts(subBreakOuts);
					allBreakOuts.addAll(subBreakOuts);
				}
			}
		}

		context.restore(preCache);
		var breakOuts = context.splitBreakOuts(
			this.random, applyScopesToBreakOut(inVars, allBreakOuts),
			completesNormally && !lastCompletesNormally, completesNormally, false);

		return new SwitchStatement(
			expression,
			caseBranches,
			includeDefault,
			breakOuts[1],
			completesNormally
				? VarsEntry.merge(VarsEntry.merge(inVars, previousCaseVars), mergeBreakOutVars(breakOuts[1]))
				: VarsEntry.never(),
			breakOuts[0]
		);
	}

	private Statement createMonitorStatement(boolean completesNormally, Context context, Params params, VarsEntry vars) {
		Scope st = this.createScope(completesNormally, false, context, params, vars);

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

			VarsEntry entry = new VarsEntry(vars);
			final Var var = new Var(entry.nextName(), BasicType.EXCEPTION, FinalType.NOT_FINAL);
			entry.create(var, true);
			entry.freeze();
			long cache = context.partial(this.random, catchCount);
			var clause = new TryCatchStatement.CatchClause(var, this.createScope(shouldComplete, false, context, sub, entry));
			catches.add(clause);
			context.restore(cache);
			context.applyBreakOuts(clause.catchStatement().breakOuts());
		}

		return catches;
	}

	private Statement createTryCatchStatement(boolean completesNormally, Context context, Params params, VarsEntry inVars) {
		int tryCatchFinallyCase = this.random.nextInt(3); // 0 => only catch, 1 => only finally

		boolean tryComplete = completesNormally;
		boolean catchComplete = completesNormally;
		boolean finallyComplete = completesNormally;
		double paramDiv;
		double contextDiv;
		switch (tryCatchFinallyCase) {
			case 0 -> {
				switch (this.random.nextInt(3)) {
					case 0 -> tryComplete = false;
					case 1 -> catchComplete = false;
				}
				paramDiv = 1.4;
				contextDiv = 2.3;
			}
			case 1 -> {
				switch (this.random.nextInt(3)) {
					case 0 -> tryComplete = true;
					case 1 -> finallyComplete = true;
				}
				paramDiv = 1.4;
				contextDiv = 1.6;
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
				paramDiv = 1.6;
				contextDiv = 2.9;
			}
			default -> throw new IllegalStateException();
		}

		VarsEntry vars = inVars;
		Expression resource = null;
		if (this.random.nextInt(3) == 0) {
			vars = new VarsEntry(inVars);
			Var var = new Var(vars.nextName(), BasicType.SCANNER, FinalType.IMPLICIT_FINAL);
			vars.create(var, true);
			vars.freeze();

			// FIXME: assignment expression
			resource = new LiteralExpression(var.type(), "Scanner " + var.name() + " = new Scanner(System.in)");
		}

		Params sub = params.div(paramDiv);
		long preTryCache = context.partial(this.random, contextDiv);

		Scope tryStat = this.createScope(tryComplete, false, context, sub, vars);

		context.restore(preTryCache);
		context.applyBreakOuts(tryStat.breakOuts());
		contextDiv -= 1;


		VarsEntry postTryVars = VarsEntry.merge(inVars, tryStat.varsEntry()).freeze();

		List<TryCatchStatement.CatchClause> catchClauses;
		if (tryCatchFinallyCase == 1) {
			catchClauses = List.of();
		} else {
			long preCatch = context.partial(this.random, contextDiv / 2.301); // epsilon for rounding
			catchClauses = this.makeCatches(catchComplete, context, sub, postTryVars);
			context.restore(preCatch);
			for (TryCatchStatement.CatchClause catchClause : catchClauses) {
				context.applyBreakOuts(catchClause.catchStatement().breakOuts());
			}
		}


		Scope finallyStat;
		List<? extends SimpleSingleNoFallThroughStatement> breakOuts;
		VarsEntry postVars;

		if (tryCatchFinallyCase == 0) {
			// no finally, breakouts are from try and catch
			finallyStat = null;
			breakOuts = catchClauses.isEmpty() ? tryStat.breakOuts() : Stream.concat(
				tryStat.breakOuts().stream(),
				catchClauses.stream()
					.map(TryCatchStatement.CatchClause::catchStatement)
					.map(Statement::breakOuts)
					.filter(Objects::nonNull)
					.flatMap(Collection::stream)
			).toList();
			postVars = catchClauses.isEmpty() ? tryStat.varsEntry() : VarsEntry.merge(
				tryStat.varsEntry(),
				catchClauses.stream()
					.map(TryCatchStatement.CatchClause::catchStatement)
					.map(Statement::varsEntry)
					.filter(Objects::nonNull)
					.reduce(VarsEntry::merge).orElse(VarsEntry.never()));
			postVars = VarsEntry.applyScopeTo(inVars, postVars);
		} else {
			VarsEntry mergedTryBreakOutsVars = mergeBreakOutVars(tryStat.breakOuts());
			final List<? extends SimpleSingleNoFallThroughStatement> mergedCatchBreakOuts = catchClauses.stream()
				.map(TryCatchStatement.CatchClause::catchStatement)
				.map(Statement::breakOuts)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.toList();
			VarsEntry mergedCatchBreakOutVars = mergeBreakOutVars(mergedCatchBreakOuts);
			final VarsEntry postCatchVars = catchClauses.stream()
				.map(TryCatchStatement.CatchClause::catchStatement)
				.map(Statement::varsEntry)
				.filter(Objects::nonNull)
				.reduce(VarsEntry::merge).orElse(VarsEntry.never());
			VarsEntry preFinally = VarsEntry.merge(postTryVars, mergedCatchBreakOutVars, mergedTryBreakOutsVars, postCatchVars).freeze();
			// catch vars are already gone cause of the merge with try

			if (!finallyComplete) {
				// if finally doesn't complete, all breakouts are from finally only
				context.restore(preTryCache);
			}

			finallyStat = this.createScope(finallyComplete, false, context, sub, preFinally);

			if (completesNormally) {
				postVars = VarsEntry.applyScopeTo(inVars, finallyStat.varsEntry());
			} else {
				postVars = VarsEntry.never();
			}

			if (finallyComplete) {
				// finally completes normally, breakouts are from try, catch and finally
				breakOuts = Stream.concat(
					finallyStat.breakOuts().stream(),
					Stream.concat(
							tryStat.breakOuts().stream(),
							mergedCatchBreakOuts.stream()
						)
						.map(stat -> applyFinallyToBreakOut(stat, inVars, preFinally, finallyStat.varsEntry())
						)).toList();
			} else {
				// finally doesn't complete normally, real breakouts are from finally only
				breakOuts = Stream.concat(
					Stream.concat(
							tryStat.breakOuts().stream(),
							mergedCatchBreakOuts.stream()
						)
						.map(WrappedBreakOutStatement::markDead),
					finallyStat.breakOuts().stream()
				).toList();
			}
		}

		final TryCatchStatement tryCatchStatement = new TryCatchStatement(
			tryStat,
			catchClauses,
			finallyStat,
			resource,
			postVars,
			breakOuts
		);

		assert tryCatchStatement.completesNormally() == completesNormally;
		return tryCatchStatement;

	}

	private static WrappedBreakOutStatement applyFinallyToBreakOut(
		SimpleSingleNoFallThroughStatement stat,
		VarsEntry scopeVars,
		VarsEntry preFinallyVars,
		VarsEntry finallyVars) {
		if (stat instanceof WrappedBreakOutStatement wrapped && wrapped.dead()) {
			return wrapped;
		}
		return new WrappedBreakOutStatement(stat, VarsEntry.applyFinallyTo(
			preFinallyVars,
			finallyVars,
			VarsEntry.applyScopeTo(scopeVars, stat.varsEntry())));
	}

	private static WrappedBreakOutStatement applyScopeToBreakOut(
		SimpleSingleNoFallThroughStatement stat,
		VarsEntry scopeVars) {
		if (stat instanceof WrappedBreakOutStatement wrapped && wrapped.dead()) {
			return wrapped;
		}
		// todo: optimize if no wrapping is needed?
		return new WrappedBreakOutStatement(stat, VarsEntry.applyScopeTo(scopeVars, stat.breakOutVars()));
	}

	private Statement createLabeledStatement(boolean completesNormally, Context context, Params params, VarsEntry inVars) {
		if (completesNormally) {
			if (this.random.nextInt(5) == 0) {
				context.neededBreaks++;
				context.breakTargets++;


				long cache = context.cache();
				Statement st = this.createStatement(false, false, context, params, inVars);

				if (st instanceof LabelImpossible) {
					// how?
					throw new RuntimeException("LabelImpossible");
				}

				context.restore(cache); // needed for split breakouts when "canHaveBreaks" is true

				List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
					this.random, st.breakOuts(), true, true, false);

				return new LabeledStatement(
					st,
					breakOuts[1],
					VarsEntry.applyScopeTo(inVars, mergeBreakOutVars(breakOuts[1])),
					applyScopesToBreakOut(inVars, breakOuts[0])
				);

			} else {
				context.breakTargets++;

				long cache = context.cache();
				Statement st = this.createStatement(true, false, context, params, inVars);

				if (st instanceof LabelImpossible) {
					throw new RuntimeException("LabelImpossible");
				}

				context.restore(cache); // needed for split breakouts when "canHaveBreaks" is true

				List<? extends SimpleSingleNoFallThroughStatement>[] breakOuts = context.splitBreakOuts(
					this.random, st.breakOuts(), false, true, false);

				return new LabeledStatement(
					st,
					breakOuts[1],
					VarsEntry.applyScopeTo(inVars, VarsEntry.merge(mergeBreakOutVars(breakOuts[1]), st.varsEntry())),
					applyScopesToBreakOut(inVars, breakOuts[0])
				);
			}
		} else {
			// labeled, but no breaks
			Statement st = this.createStatement(false, false, context, params, inVars);
			if (st instanceof LabelImpossible) {
				throw new RuntimeException("LabelImpossible");
			}
			return new LabeledStatement(st, List.of(), VarsEntry.never(), applyScopesToBreakOut(inVars, st.breakOuts()));
		}
	}

	public Statement createMaybeScope(boolean completesNormally, boolean allowSingleVarDef, Context context, Params params, VarsEntry vars) {
		if (this.random.nextInt(params.size < 3 ? 2 : 4) == 0) {
			return this.createStatement(completesNormally, allowSingleVarDef, context, params, vars);
		} else {
			return this.createScope(completesNormally, false, context, params, vars);
		}
	}

	Scope createScope(boolean completesNormally, boolean root, Context context, Params params, VarsEntry inVars) {
		assert inVars.isFrozen();
		// can't have an empty scope if we are not supposed to complete normally,
		// or if we need to have any breaks
		int targetSize = params.targetSize(this.random) + (completesNormally && !context.needsBreakOuts() ? 0 : 1);
		if (targetSize == 0) {
			return new Scope(List.of(), inVars, List.of());
		}

		List<Statement> statements = new ArrayList<>();
		VarsEntry vars = inVars;

		Params sub = params.div(Math.sqrt(targetSize));

		boolean stolenBreak = false;
		if (!completesNormally && context.neededBreaks > 0 && this.random.nextBoolean()) {
			context.neededBreaks--;
			stolenBreak = true;
		}

		if (root) {
			Statement subStatement = this.createVarDefStatement(vars, 4 + this.random.nextInt(4));
			statements.add(subStatement);
			vars = subStatement.varsEntry();
		}

		// all but the last statement have to complete normally
		for (int i = targetSize; i > 1; i--) {
			long cache = context.partial(this.random, i);
			Statement statement = this.createStatement(true, true, context, sub, vars);
			statements.add(statement);
			context.restore(cache);
			context.applyBreakOuts(statement.breakOuts());
			vars = statement.varsEntry();

			assert vars.isFrozen();
			assert context.breakTargets > 0 || statement.breakOuts().stream().noneMatch(s -> WrappedBreakOutStatement.base(s) instanceof Break);
		}

		// last statement can be breaking
		{
			if (stolenBreak) {
				// add the needed break back
				context.neededBreaks++;
			}
			Statement statement = this.createStatement(completesNormally, true, context, sub, vars);
			statements.add(statement);
			vars = statement.varsEntry();

			assert vars == null || vars.isFrozen();
			assert context.breakTargets > 0 || statement.breakOuts().stream().noneMatch(s -> WrappedBreakOutStatement.base(s) instanceof Break);
		}

		return new Scope(
			statements,
			VarsEntry.applyScopeTo(inVars, vars),
			statements.stream()
				.map(Statement::breakOuts)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.map(s -> applyScopeToBreakOut(s, inVars))
				.toList()
		);
	}


	public record Params(double size) {


		Params div(double scale) {
			return new Params(this.size / scale);
		}

		// poisson distribution
		int targetSize(RandomGenerator randomGenerator) {
			return poisson(this.size, randomGenerator);
		}
	}

	public static void main(String[] args) {
		Statement statement = (new Creator(-2706962048981605674L)).method(
			20
		);
		System.out.println(statement);

		System.out.println();
		System.out.println();
		System.out.println();

		StringBuilder stringBuilder = new StringBuilder();
		statement.javaLike(stringBuilder, "");
		System.out.println(stringBuilder);
	}

	public Statement method(int size) {
		Context context = new Context(this.typeCreator, this.expressionCreator);
		Params params = new Params(size);

		final Scope scope = this.createScope(false, true, context, params, VarsEntry.empty());
		assert !context.needsBreakOuts();
		assert context.continueTargets == 0;
		assert context.breakTargets == 0;
		assert scope.breakOuts().stream().allMatch(s -> {
			var base = WrappedBreakOutStatement.base(s);
			return base instanceof Return || base instanceof Throw;
		});
		return scope;
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
