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
	private static final Random random = new Random();
	private static final Expression DEFAULT = builder -> builder.append("new Random().nextInt()");;

	public static void random(Random random, Type type, StringBuilder builder) {
		if (type instanceof PrimitiveTypes primitiveType) {
			createPrimitiveConstantExpression(primitiveType).javaLike(builder);
		} else if (type instanceof ArrayType arrayType) {
			builder.append("new ");
			arrayType.base().javaLike(builder);
			builder.append("[0]");
			for(int i = 1; i < arrayType.depth(); i++) {
				builder.append("[]");
			}
		} else if (type instanceof BasicType basicType) {
			if (type == BasicType.STRING) {
				builder.append("\"Hi!\"");
			} else {
				builder.append("null");
			}
		} else if (type instanceof PrimitiveTypes.Boxed box) {
			builder.append(box.name()).append(".valueOf(");
			random(random, box.type(), builder);
			builder.append(")");
		}
	}


	static Expression createExpression(Type targetType, VarsEntry vars) {
		return builder -> random(random, targetType, builder);
	}

	static Expression createStandaloneExpression(Type targetType, VarsEntry vars) {
		if (!vars.vars.isEmpty() && random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (random.nextInt(i) == 0) {
						Expression expr = buildIncrement(varVarStateEntry.getKey());

						if (random.nextInt(3) == 0 || expr == DEFAULT) {
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

	static Expression buildCondition(VarsEntry vars) {
		if (!vars.vars.isEmpty() && random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (random.nextInt(i) == 0) {
						return buildCondition(varVarStateEntry.getKey());
					}
				}
				i--;
			}
		}

		return builder -> builder.append("new Random().nextBoolean()");
	}

	static Expression buildCondition(Var var) {
		if (var.type() instanceof PrimitiveTypes primitiveType) {
			if (primitiveType == PrimitiveTypes.BOOLEAN) {
				return builder -> builder.append(random.nextBoolean() ? "!" : "").append(var.name());
			} else if (primitiveType == PrimitiveTypes.CHAR) {
				return builder -> builder.append(var.name()).append(random.nextBoolean() ? " != " : " == ")
						.append("'").append((char) (random.nextInt(26) + 'a')).append("'");
			}

			String cond = switch (random.nextInt(6)) {
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
				createPrimitiveConstantExpression(primitiveType).javaLike(builder);
			};
		} else {
			return builder -> builder.append(var.name()).append(random.nextBoolean() ? " != null" : " == null");
		}
	}

	static Expression buildIncrement(VarsEntry vars) {
		if (!vars.vars.isEmpty() && random.nextInt(8) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (random.nextInt(i) == 0) {
						return buildIncrement(varVarStateEntry.getKey());
					}
				}
				i--;
			}
		}

		return DEFAULT;
	}

	static Expression buildIncrement(Var var) {
		Expression value;
		if (var.type() instanceof PrimitiveTypes primitiveType) {
			if (!isPrimitiveNumerical(primitiveType)) {
				return DEFAULT;
			}

			value = createPrimitiveConstantExpression(primitiveType);
		} else if (var.type() instanceof PrimitiveTypes.Boxed boxed) {
			if (!isPrimitiveNumerical(boxed.type())) {
				return DEFAULT;
			}

			value = createPrimitiveConstantExpression(boxed.type());
		} else {
			return DEFAULT;
		}

		// TODO: %=, &=, |=, ^=, <<=, >>=, >>>= for integral types only

		String incr = switch (random.nextInt(4)) {
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

	static LiteralExpression createPrimitiveConstantExpression(PrimitiveTypes primitiveType) {
		return new LiteralExpression(primitiveType, switch (primitiveType) {
			case BOOLEAN -> random.nextBoolean() ? "true" : "false";
			case BYTE -> random.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1) + "";
			case CHAR -> "'" + (char) (random.nextInt(26) + 'a') + "'";
			case SHORT -> random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1) + "";
			case INT -> (random.nextInt(5) == 0 ? random.nextInt() : random.nextInt(-150, 501)) + "";
			case LONG -> (random.nextInt(5) == 0 ? random.nextLong() : random.nextLong(-1000, 1001)) + "L";
			case FLOAT -> new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextFloat() - 50) + "F";
			case DOUBLE -> new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextDouble() - 50) + "";
		});
	}

	static boolean isPrimitiveNumerical(PrimitiveTypes type) {
		return switch (type) {
			case BOOLEAN, BYTE, CHAR -> false;
			default -> true;
		};
	}
}
