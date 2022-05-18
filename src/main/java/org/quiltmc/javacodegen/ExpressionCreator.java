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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class ExpressionCreator {
	private final Random random;
	private static final Expression DEFAULT = builder -> builder.append("new Random().nextInt()");

	public ExpressionCreator(Random random) {
		this.random = random;
	}

	public void random(Type type, StringBuilder builder) {
		if (type instanceof PrimitiveTypes primitiveType) {
			this.createPrimitiveConstantExpression(primitiveType).javaLike(builder);
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
			} else {
				builder.append("null");
			}
		} else if (type instanceof PrimitiveTypes.Boxed box) {
			if (box.type() != PrimitiveTypes.BYTE && box.type() != PrimitiveTypes.SHORT) {
				builder.append(box.name()).append(".valueOf(");
			} else {
				builder.append("((").append(box.type().name().toLowerCase(Locale.ROOT)).append(")");
			}
			this.random(box.type(), builder);
			builder.append(")");
		}
	}


	Expression createExpression(Type targetType, VarsEntry vars) {
		return builder -> this.random(targetType, builder);
	}

	Expression createStandaloneExpression(Type targetType, VarsEntry vars) {
		if (!vars.vars.isEmpty() && this.random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (this.random.nextInt(i) == 0) {
						Expression expr = this.buildReassign(vars);

						if (expr != DEFAULT) {
							return expr;
						}

						expr = this.buildIncrement(varVarStateEntry.getKey());

						if (this.random.nextInt(3) == 0 || expr == DEFAULT) {
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

	Expression buildCondition(VarsEntry vars) {
		if (!vars.vars.isEmpty() && this.random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (this.random.nextInt(i) == 0) {
						return this.buildCondition(varVarStateEntry.getKey());
					}
				}
				i--;
			}
		}

		return builder -> builder.append("new Random().nextBoolean()");
	}

	Expression buildCondition(Var var) {
		if (var.type() instanceof PrimitiveTypes primitiveType) {
			if (primitiveType == PrimitiveTypes.BOOLEAN) {
				return builder -> builder.append(this.random.nextBoolean() ? "!" : "").append(var.name());
			} else if (primitiveType == PrimitiveTypes.CHAR) {
				return builder -> builder.append(var.name()).append(this.random.nextBoolean() ? " != " : " == ")
						.append("'").append((char) (this.random.nextInt(26) + 'a')).append("'");
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

			return builder -> {
				builder.append(var.name()).append(" ").append(cond).append(" ");
				this.createPrimitiveConstantExpression(primitiveType).javaLike(builder);
			};
		} else {
			return builder -> builder.append(var.name()).append(this.random.nextBoolean() ? " != null" : " == null");
		}
	}

	Expression buildReassign(VarsEntry vars) {
		if (vars.vars.size() > 1 && this.random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (this.random.nextInt(i) == 0) {
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

									vars.vars.get(varVarStateEntry.getKey()).setDefiniteAssigned(true);

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
						return this.buildIncrement(varVarStateEntry.getKey());
					}
				}
				i--;
			}
		}

		return DEFAULT;
	}

	Expression buildIncrement(Var var) {
		Expression value;
		if (var.type() instanceof PrimitiveTypes primitiveType) {
			if (!isPrimitiveNumerical(primitiveType)) {
				return DEFAULT;
			}

			value = this.createPrimitiveConstantExpression(primitiveType);
		} else if (var.type() instanceof PrimitiveTypes.Boxed boxed) {
			if (!isPrimitiveNumerical(boxed.type())) {
				return DEFAULT;
			}

			value = this.createPrimitiveConstantExpression(boxed.type());
		} else {
			return DEFAULT;
		}

		// TODO: %=, &=, |=, ^=, <<=, >>=, >>>= for integral types only

		String incr = switch (this.random.nextInt(4)) {
			case 0 -> "+=";
			case 1 -> "-=";
			case 2 -> "*=";
			case 3 -> "/=";
			default -> throw new IllegalStateException();
		};

		StringBuilder val = new StringBuilder();
		value.javaLike(val);

		return builder -> builder.append(var.name()).append(" ").append(incr).append(" ").append(val);
	}

	LiteralExpression createPrimitiveConstantExpression(PrimitiveTypes primitiveType) {
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
