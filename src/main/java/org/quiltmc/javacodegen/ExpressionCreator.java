package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.expression.*;
import org.quiltmc.javacodegen.types.ArrayType;
import org.quiltmc.javacodegen.types.BasicType;
import org.quiltmc.javacodegen.types.PrimitiveTypes;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarState;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.random.RandomGenerator;

public class ExpressionCreator {
	private final Random random;
	private static final Expression DEFAULT = builder -> builder.append("new Random().nextInt()");

	public ExpressionCreator(Random random) {
		this.random = random;
	}

	public void random(Type type, StringBuilder builder, VarsEntry vars) {
		if (type instanceof PrimitiveTypes primitiveType) {
			if (primitiveType == PrimitiveTypes.BOOLEAN && random.nextInt(2) == 0) {
				this.buildCondition(vars).javaLike(builder);
			} else {
				this.createPrimitiveConstantExpression(primitiveType).javaLike(builder);
			}
		} else if (type instanceof ArrayType arrayType) {

			builder.append("new ");
			arrayType.base().javaLike(builder);
			builder.append("[0]");
			if (arrayType.depth() > 1) {
				builder.append("[]".repeat(Math.max(0, arrayType.depth() - 1)));
			}
		} else if (type instanceof BasicType basicType) {
			if (type == BasicType.STRING) {
				builder.append("\"Hi!\"");
			} else if (type == BasicType.ELEMENT_TYPE) {
				builder.append("ElementType.METHOD");
			} else {
				builder.append("null");
			}
		} else if (type instanceof PrimitiveTypes.Boxed box) {
			if (box.type() != PrimitiveTypes.BYTE && box.type() != PrimitiveTypes.SHORT) {
				builder.append(box.name()).append(".valueOf(");
			} else {
				builder.append("((").append(box.type().name().toLowerCase(Locale.ROOT)).append(")");
			}
			this.random(box.type(), builder, vars);
			builder.append(")");
		}
	}


	public Expression createExpression(Type targetType, VarsEntry vars) {
		return builder -> this.random(targetType, builder, vars);
	}

	Expression createStandaloneExpression(Type targetType, VarsEntry vars) {
		if (vars != null && !vars.vars.isEmpty() && this.random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (this.random.nextInt(i) == 0) {
						Expression expr;

						if (random.nextInt(3) > 0) {
							expr = this.buildReassign(vars);

							if (expr != DEFAULT) {
								return expr;
							}
						}

						if (random.nextInt(3) == 0) {
							expr = this.buildPPMM(varVarStateEntry.getKey(), vars);

							if (expr != DEFAULT) {
								return expr;
							}
						}

						expr = this.buildIncrement(varVarStateEntry.getKey(), vars);

						if (this.random.nextInt(4) == 0 || expr == DEFAULT) {
							return builder -> builder.append("System.out.println(").append(varVarStateEntry.getKey().name()).append(")");
						} else {
							return expr;
						}
					}
				}
				i--;
			}
		}

		return builder -> builder.append("System.out.println(\"Hi\")");
	}

	static Expression createPrintExpression(Expression expression) {
		return new InstanceMethodInvocationExpression(
				new StaticFieldAccessExpression(
						BasicType.SYSTEM,
						"out"
				),
				"println",
				List.of(expression)
		);
	}

	public Expression buildPPMM(Var var, VarsEntry vars) {
		if (var.type() instanceof PrimitiveTypes pt && pt.integralType()) {
			return builder -> builder.append(var.name()).append(random.nextBoolean() ? "++" : "--");
		}

		if (var.type() instanceof ArrayType at && at.base() instanceof PrimitiveTypes pt && pt.integralType()) {
			return builder -> {
				builder.append(var.name());
				for (int i = 0; i < at.depth(); i++) {
					builder.append("[").append(buildIntegralExpression(vars).toJava()).append("]");
				}

				builder.append(random.nextBoolean() ? "++" : "--");
			};
		}

		return DEFAULT;
	}

	public Expression buildIntegralExpression(VarsEntry vars) {
		List<Var> varsChoice = new ArrayList<>();

		for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
			if (varVarStateEntry.getValue().isDefiniteAssigned()) {
				Var key = varVarStateEntry.getKey();

				if (key.type() instanceof PrimitiveTypes pt && pt.integralType()) {
					varsChoice.add(key);
				}
//				else if (key.type() instanceof ArrayType at && at.base() instanceof PrimitiveTypes pt && pt.integralType()) {
//					varsChoice.add(key);
//				}
			}
		}

		if (varsChoice.isEmpty() || random.nextInt(3) == 0) {
			return new LiteralExpression(PrimitiveTypes.INT, 0);
		}

		Var choice1 = varsChoice.get(random.nextInt(varsChoice.size()));
		if (random.nextBoolean()) {
			if (choice1.type() instanceof ArrayType at) {
//				return at
			} else {
				return new VariableExpression(choice1);
			}
		}

		// math expression

		String cond = switch (this.random.nextInt(8)) {
			case 0 -> " + ";
			case 1 -> " - ";
			case 2 -> " * ";
			case 3 -> " / ";
			case 4 -> " % ";
			case 5 -> " & ";
			case 6 -> " | ";
			case 7 -> " ^ ";
			default -> throw new IllegalStateException();
		};

		if (random.nextBoolean()) {
			return builder -> builder.append(choice1.name()).append(cond).append(this.createPrimitiveConstantExpression(PrimitiveTypes.INT).toJava());
		}

		if (random.nextBoolean()) {
			Var choice2 = varsChoice.get(random.nextInt(varsChoice.size()));

			return builder -> builder.append(choice1.name()).append(cond).append(choice2.name());
		}

		return builder -> builder.append(choice1.name()).append(cond).append(this.buildIntegralExpression(vars).toJava());
	}

	public Expression buildCondition(VarsEntry vars) {
		return buildCondition(vars, false);
	}

	public Expression buildCondition(VarsEntry vars, boolean allowLiterals) {
		List<Var> varsChoice = new ArrayList<>();
		
		if (vars != null && !vars.vars.isEmpty() && this.random.nextInt(8) != 0) {
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					varsChoice.add(varVarStateEntry.getKey());
				}
			}
		}

		if (!varsChoice.isEmpty()) {
			if (random.nextInt(8) == 0) {
				Expression cond = buildCondition(vars, true);
				Expression a = buildCondition(vars, true);
				Expression b = buildCondition(vars, true);

				return new TernaryExpression(cond, a, b);
			} else if (random.nextInt(4) == 0) {
				Expression a = buildCondition(vars, true);
				Expression b = buildCondition(vars, true);

				BooleanBinaryOperatorExpression.Operator opr = switch (random.nextInt(2)) {
					case 0 -> BooleanBinaryOperatorExpression.Operator.ANDAND;
					case 1 -> BooleanBinaryOperatorExpression.Operator.OROR;
					default -> throw new IllegalStateException();
				};

				return new BooleanBinaryOperatorExpression(a, b, opr);
			}

			return buildCondition(varsChoice.get(this.random.nextInt(varsChoice.size())));
		}

		if (random.nextInt(4) == 0 && allowLiterals) {
			return random.nextBoolean() ? new LiteralExpression(PrimitiveTypes.BOOLEAN, true) : new LiteralExpression(PrimitiveTypes.BOOLEAN, false);
		}

		return builder -> builder.append("new Random().nextBoolean()");
	}

	public Expression buildCondition(Var var) {
		if (var.type() instanceof PrimitiveTypes primitiveType) {
			if (primitiveType == PrimitiveTypes.BOOLEAN) {
				return builder -> builder.append(this.random.nextBoolean() ? "!" : "").append(var.name());
			} else if (primitiveType == PrimitiveTypes.CHAR) {
				return builder -> builder.append(var.name()).append(this.random.nextBoolean() ? " != " : " == ")
						.append("'").append((char) (this.random.nextInt(26) + 'a')).append("'");
			}

			String varStr = var.name();
			if (((PrimitiveTypes) var.type()).integralType() && random.nextInt(5) == 0) {
				String ppmm = random.nextBoolean() ? "++" : "--";
				varStr = random.nextBoolean() ? varStr + ppmm : ppmm + varStr;
			}

			String cond = switch (this.random.nextInt(6)) {
				case 0 -> "!=";
				case 1 -> "==";
				case 2 -> ">";
				case 3 -> "<";
				case 4 -> ">=";
				case 5 -> "<=";
				default -> throw new IllegalStateException();
			};

			String finalVarStr = varStr;
			return builder -> {
				builder.append(finalVarStr).append(" ").append(cond).append(" ");
				this.createPrimitiveConstantExpression(primitiveType).javaLike(builder);
			};
		} else {
			return builder -> builder.append(var.name()).append(this.random.nextBoolean() ? " != null" : " == null");
		}
	}

	Expression buildReassign(VarsEntry vars) {
		if(vars.isFrozen()) {
			throw new IllegalArgumentException("Cannot reassign frozen vars");
		}
		if (vars.vars.size() > 1 && this.random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (!varVarStateEntry.getKey().finalType().isFinal() && this.random.nextInt(i) == 0) {
					int j = vars.vars.size();
					for (Map.Entry<Var, VarState> varVarStateEntryInner : vars.vars.entrySet()) {
						if (varVarStateEntryInner.getValue().isDefiniteAssigned()) {
							if (this.random.nextInt(j) == 0 && varVarStateEntryInner.getKey() != varVarStateEntry.getKey()) {
								if (varVarStateEntryInner.getKey().type() == varVarStateEntry.getKey().type()) {
									String assign = (!varVarStateEntry.getValue().isDefiniteAssigned() || !canPerformMath(varVarStateEntry.getKey().type())) ? "=" : switch (this.random.nextInt(10)) {
										case 0 -> "+=";
										case 1 -> "-=";
										case 2 -> "*=";
										case 3 -> "/=";
										default -> "=";
									};

									vars.assign(varVarStateEntry.getKey());

									return builder -> builder.append(varVarStateEntry.getKey().name())
											.append(" ").append(assign).append(" ").append(varVarStateEntryInner.getKey().name());
								}
							}
						}
						j--;
					}
				}
				i--;
			}
		}

		return DEFAULT;
	}

	Expression buildIncrement(VarsEntry vars) {
		if (!vars.vars.isEmpty()) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (this.random.nextInt(i) == 0) {
						return this.buildIncrement(varVarStateEntry.getKey(), vars);
					}
				}
				i--;
			}
		}

		return DEFAULT;
	}

	public Expression buildIncrement(Var var, VarsEntry vars) {
		Expression value;
		Type type = var.type();
		if (type instanceof ArrayType at) {
			type = at.base();
		}

		if (type instanceof PrimitiveTypes primitiveType) {
			if (!isPrimitiveNumerical(primitiveType)) {
				return DEFAULT;
			}

			value = this.createPrimitiveConstantExpression(primitiveType);
		} else if (type instanceof PrimitiveTypes.Boxed boxed) {
			if (!isPrimitiveNumerical(boxed.type())) {
				return DEFAULT;
			}

			value = this.createPrimitiveConstantExpression(boxed.type());
		} else {
			if (type == BasicType.STRING) {
				value = this.createRandomString(3);
			} else {
				return DEFAULT;
			}
		}
		Type realType = var.type();

		// TODO: %=, &=, |=, ^=, <<=, >>=, >>>= for integral types only

		String incr = type == BasicType.STRING ? "+=" : switch (this.random.nextInt(4)) {
			case 0 -> "+=";
			case 1 -> "-=";
			case 2 -> "*=";
			case 3 -> "/=";
			default -> throw new IllegalStateException();
		};

		StringBuilder val = new StringBuilder();
		value.javaLike(val);

		return builder -> {
			builder.append(var.name());
			if (realType instanceof ArrayType at) {
				for (int i = 0; i < at.depth(); i++) {
					builder.append("[").append(buildIntegralExpression(vars).toJava()).append("]");
				}
			}
			builder.append(" ").append(incr).append(" ").append(val);
		};
	}

	public LiteralExpression createPrimitiveConstantExpression(PrimitiveTypes primitiveType) {
		return new LiteralExpression(primitiveType, switch (primitiveType) {
			case BOOLEAN -> this.random.nextBoolean() ? "true" : "false";
			case BYTE -> "(byte)" + this.random.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1) + "";
			case CHAR -> "'" + (char) (this.random.nextInt(26) + 'a') + "'";
			case SHORT -> "((short)" + this.random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1) + ")";
			case INT -> (this.random.nextInt(5) == 0 ? this.random.nextInt() : this.random.nextInt(-150, 501)) + "";
			case LONG -> (this.random.nextInt(5) == 0 ? this.random.nextLong() : this.random.nextLong(-1000, 1001)) + "L";
			case FLOAT -> new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * this.random.nextFloat() - 50) + "F";
			case DOUBLE -> new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * this.random.nextDouble() - 50) + "";
		});
	}

	public LiteralExpression createRandomString(int size) {
		int amt = 1 + this.poisson(size);

		StringBuilder buf = new StringBuilder();

		for (int i = amt; i > 0; i--) {
			int letter = this.random.nextInt(26);
			boolean lowercase = this.random.nextBoolean();

			String gen = String.valueOf((char)(65 + letter));

			if (lowercase) {
				gen = gen.toLowerCase(Locale.ROOT);
			}

			buf.append(gen);
		}

		return new LiteralExpression(BasicType.STRING, "\"" + buf.toString() + "\"");
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

	public Random getRandom() {
		return random;
	}

	static boolean isPrimitiveNumerical(PrimitiveTypes type) {
		return switch (type) {
			case BOOLEAN, BYTE, CHAR, SHORT -> false;
			default -> true;
		};
	}

	static boolean canPerformMath(Type type) {
		if (type instanceof PrimitiveTypes primitiveType) {
			return isPrimitiveNumerical(primitiveType);
		} else if (type instanceof PrimitiveTypes.Boxed boxed) {
			return isPrimitiveNumerical(boxed.type());
		}

		return false;
	}
}
