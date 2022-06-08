//package org.quiltmc.javacodegen.reducing;
//
//import org.quiltmc.javacodegen.creating.ForCreator;
//import org.quiltmc.javacodegen.creating.ForEachCreator;
//import org.quiltmc.javacodegen.creating.IfCreator;
//import org.quiltmc.javacodegen.creating.LabeledCreator;
//import org.quiltmc.javacodegen.expression.Expression;
//import org.quiltmc.javacodegen.statement.*;
//import org.quiltmc.javacodegen.vars.VarsEntry;
//
//import java.util.List;
//import java.util.Map;
//
//public abstract class Reducer {
//	/*
//	 * break
//	 * continue
//	 * empty statement
//	 * expression statement
//	 * for each statement
//	 * for statement
//	 * if statement
//	 * labeled statement
//	 * monitor statement
//	 * return
//	 * scope
//	 * switch statement
//	 * throw
//	 * try catch statement
//	 * var def statement
//	 * while statement
//	 * while true statement
//	 */
//	public Statement reduceStatement(Statement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		Statement res;
//		if (stat == null) {
//			return null;
//		} else if (stat instanceof Break breakStat) {
//			res = this.reduceBreak(breakStat, statementMap, inVars);
//		} else if (stat instanceof Continue continueStat) {
//			res = this.reduceContinue(continueStat, statementMap, inVars);
//		} else if (stat instanceof EmptyStatement emptyStat) {
//			res = this.reduceEmptyStatement(emptyStat, statementMap, inVars);
//		} else if (stat instanceof ExpressionStatement exprStat) {
//			res = this.reduceExpressionStatement(exprStat, statementMap, inVars);
//		} else if (stat instanceof ForEachStatement forEachStat) {
//			res = this.reduceForEachStatement(forEachStat, statementMap, inVars);
//		} else if (stat instanceof ForStatement forStat) {
//			res = this.reduceForStatement(forStat, statementMap, inVars);
//		} else if (stat instanceof IfStatement ifStat) {
//			res = this.reduceIfStatement(ifStat, statementMap, inVars);
//		} else if (stat instanceof LabeledStatement labelStat) {
//			res = this.reduceLabeledStatement(labelStat, statementMap, inVars);
//		} else if (stat instanceof MonitorStatement monitorStat) {
//			res = this.reduceMonitorStatement(monitorStat, statementMap, inVars);
//		} else if (stat instanceof Return retStat) {
//			res = this.reduceReturn(retStat, statementMap, inVars);
//		} else if (stat instanceof Scope scopeStat) {
//			res = this.reduceScope(scopeStat, statementMap, inVars);
//		} else if (stat instanceof SwitchStatement switchStat) {
//			res = this.reduceSwitchStatement(switchStat, statementMap, inVars);
//		} else if (stat instanceof Throw throwStat) {
//			res = this.reduceThrow(throwStat, statementMap, inVars);
//		} else if (stat instanceof TryCatchStatement tryCatchStat) {
//			res = this.reduceTryCatchStatement(tryCatchStat, statementMap, inVars);
//		} else if (stat instanceof VarDefStatement varDefStat) {
//			res = this.reduceVarDefStatement(varDefStat, statementMap, inVars);
//		} else if (stat instanceof WhileStatement whileStat) {
//			res = this.reduceWhileStatement(whileStat, statementMap, inVars);
//		} else {
//			throw new RuntimeException("Unknown statement type: " + stat.getClass().getSimpleName());
//		}
//		this.addToStatementMap(statementMap, stat, res);
//		return res;
//	}
//
//	public Statement reduceBreak(Break stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		return new Break(stat.isSimple(), inVars);
//	}
//
//	public Statement reduceContinue(Continue stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		return new Continue(stat.isSimple(), inVars);
//	}
//
//	public Statement reduceEmptyStatement(EmptyStatement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		return new EmptyStatement(inVars);
//	}
//
//	public Statement reduceExpressionStatement(ExpressionStatement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		return new ExpressionStatement(inVars, this.reduceExpression(stat.getExpression(), inVars));
//	}
//
//	public Statement reduceForEachStatement(ForEachStatement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		var col = this.reduceExpression(stat.col(), inVars);
//		var varDecl = this.reduceVarDec(stat.varDec(), inVars);
//		var block = this.reduceStatement(stat.block(), statementMap, inVars);
//		var breaks = this.reduceBreakOutList(stat.breaks(), statementMap);
//		var continues = this.reduceBreakOutList(stat.continues(), statementMap);
//		var breakOuts = this.reduceBreakOutList(stat.breakOuts(), statementMap);
//
//		return ForEachCreator.createForEach(inVars, col, varDecl, block, breaks, continues, breakOuts);
//	}
//
//	public Statement reduceForStatement(ForStatement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		var varDec = this.reduceVarDec(stat.varDec(), inVars);
//		var condition = this.reduceExpression(stat.condition(), inVars);
//		var block = this.reduceStatement(stat.block(), statementMap, inVars);
//		var incr = this.reduceExpression(stat.incr(), inVars);
//		var breaks = this.reduceBreakOutList(stat.breaks(), statementMap);
//		var continues = this.reduceBreakOutList(stat.continues(), statementMap);
//		var breakOuts = this.reduceBreakOutList(stat.breakOuts(), statementMap);
//
//		return ForCreator.createFor(inVars, varDec, condition, block, incr, breaks, continues, breakOuts);
//	}
//
//	public Statement reduceIfStatement(IfStatement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		var condition = this.reduceExpression(stat.condition(), inVars);
//		var ifTrue = this.reduceStatement(stat.ifTrue(), statementMap, inVars);
//		var ifFalse = this.reduceStatement(stat.ifFalse(), statementMap, inVars);
//		var breakOuts = this.reduceBreakOutList(stat.breakOuts(), statementMap);
//
//		return IfCreator.createIf(inVars, condition, ifTrue, ifFalse, breakOuts);
//	}
//
//	public Statement reduceLabeledStatement(LabeledStatement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		var inner = this.reduceStatement(stat.inner(), statementMap, inVars);
//		var breaks = this.reduceBreakOutList(stat.breaks(), statementMap);
//		var breakOuts = this.reduceBreakOutList(stat.breakOuts(), statementMap);
//
//		return LabeledCreator.createLabeled(inVars, inner, breaks, breakOuts);
//	}
//
//	// monitor
//	public Statement reduceMonitorStatement(MonitorStatement stat, Map<Statement, Statement> statementMap, VarsEntry inVars) {
//		var body = this.reduceScope(stat.body(), statementMap, inVars);
//
//		return new MonitorStatement(body);
//	}
//
//	private VarDefStatement.VarDeclaration reduceVarDec(VarDefStatement.VarDeclaration varDec, VarsEntry inVars) {
//		return varDec; // assume vars are available
//	}
//
//
//	private List<? extends SimpleSingleNoFallThroughStatement> reduceBreakOutList(
//		List<? extends SimpleSingleNoFallThroughStatement> items,
//		Map<Statement, Statement> statementMap) {
//		if (items == null) {
//			return null;
//		}
//		return items.stream().map(statementMap::get).map(c -> (SimpleSingleNoFallThroughStatement) c).toList();
//	}
//
//
//	public Expression reduceExpression(Expression expression, VarsEntry inVars) {
//		return expression;
//	}
//
//	public Scope wrapWithScopeIfNeeded(Statement statement) {
//		if (statement instanceof Scope scope) {
//			return scope;
//		}
//		return new Scope(List.of(statement), statement.varsEntry(), statement.breakOuts());
//	}
//}
