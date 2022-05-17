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
import java.util.function.Function;

public class ExpressionCreator {
	private static final Random random = new Random();

	private static final Map<Type, List<ExpressionEntry>> ExpressionProviders = new HashMap<>();

	public static void random(Random random, Type type, StringBuilder builder) {
		if (type instanceof PrimitiveTypes primitiveType) {
			createPrimitiveConstantExpression(primitiveType).javaLike(builder);
		} else if (type instanceof ArrayType arrayType) {
			builder.append("new ");
			arrayType.base().javaLike(builder);
			builder.append("[0]");
			for (int i = 1; i < arrayType.depth(); i++) {
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
		return createExpression(targetType, vars, -1, 0);
	}

	static Expression createExpression(Type targetType, VarsEntry vars, int minPrecedence, int depth) {
		if (targetType instanceof ArrayType arrayType) {
			return builder -> {
				builder.append("new ");
				arrayType.base().javaLike(builder);
				builder.append("[0]");
				for (int i = 1; i < arrayType.depth(); i++) {
					builder.append("[]");
				}
			};
		} else {
			List<ExpressionEntry> expressionEntries = ExpressionProviders.get(targetType);
			if (expressionEntries == null) {
				return builder -> builder.append("<Error: no provider for ").append(targetType).append(">");
			}

			int index = random.nextInt(expressionEntries.size());
			var entry = expressionEntries.get(index);
			for(int retry = depth / 3; retry >= 0 && entry.subExpressions > 0; retry--) {
				index = random.nextInt(expressionEntries.size());
				var newEntry = expressionEntries.get(index);
				if(newEntry.subExpressions < entry.subExpressions){
					entry = newEntry;
				}
			}

			if (entry.precedence < minPrecedence) {
				return new WrappedExpression(entry.expression.create(vars, depth));
			} else {
				return entry.expression.create(vars, depth);
			}
		}
	}

	static Expression createStandaloneExpression(Type targetType, VarsEntry vars) {
		if (!vars.vars.isEmpty() && random.nextInt(3) != 0) {
			int i = vars.vars.size();
			for (Map.Entry<Var, VarState> varVarStateEntry : vars.vars.entrySet()) {
				if (varVarStateEntry.getValue().isDefiniteAssigned()) {
					if (random.nextInt(i) == 0) {
						return builder -> builder.append("System.out.println(").append(varVarStateEntry.getKey().name()).append(")");
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

	static LiteralExpression createPrimitiveConstantExpression(PrimitiveTypes primitiveType) {
		return new LiteralExpression(primitiveType, switch (primitiveType) {
			case BOOLEAN -> random.nextBoolean() ? "true" : "false";
			case BYTE -> random.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1) + "";
			case CHAR -> "'" + (char) (random.nextInt(26) + 'a') + "'";
			case SHORT -> random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1) + "";
			case INT -> (random.nextInt(5) == 0 ? random.nextInt() : random.nextInt(-150, 501)) + "";
			case LONG -> (random.nextInt(5) == 0 ? random.nextLong() : random.nextLong(-1000, 1001)) + "L";
			case FLOAT ->
					new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextFloat() - 50) + "F";
			case DOUBLE ->
					new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextDouble() - 50) + "";
		});
	}

	record ExpressionEntry(int precedence, int subExpressions, Provider expression) {
		@FunctionalInterface
		interface Provider {
			Expression create(VarsEntry vars, int depth);
		}

		@FunctionalInterface
		interface Sub {
			Expression create(Type type, int minPrecedence);
		}
	}

	static void register(Type type, int precedence, int subExpressions, ExpressionEntry.Provider expressionProvider) {
		ExpressionProviders.computeIfAbsent(type, k -> new ArrayList<>()).add(new ExpressionEntry(precedence, subExpressions, expressionProvider));
	}

	static void register(Type type, int precedence, int subExpressions, Function<ExpressionEntry.Sub, ? extends Expression> provider) {
		register(type, precedence, subExpressions, (vars, depth) ->
				provider.apply((subType, subPrecedence) -> createExpression(subType, vars, precedence, depth + 1)));
	}




	static {
		register(PrimitiveTypes.BOOLEAN, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.BOOLEAN, random.nextBoolean() ? "true" : "false"));
		register(PrimitiveTypes.BYTE, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.BYTE, random.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1) + ""));
		register(PrimitiveTypes.CHAR, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.CHAR, "'" + (char) (random.nextInt(26) + 'a') + "'"));
		register(PrimitiveTypes.SHORT, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.SHORT, random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1) + ""));
		register(PrimitiveTypes.INT, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.INT, (random.nextInt(5) == 0 ? random.nextInt() : random.nextInt(-150, 501)) + ""));
		register(PrimitiveTypes.LONG, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.LONG, (random.nextInt(5) == 0 ? random.nextLong() : random.nextLong(-1000, 1001)) + "L"));
		register(PrimitiveTypes.FLOAT, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.FLOAT, new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextFloat() - 50) + "F"));
		register(PrimitiveTypes.DOUBLE, 100, 0, sub -> new LiteralExpression(PrimitiveTypes.DOUBLE, new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextDouble() - 50) + ""));

		for (PrimitiveTypes primitiveType : PrimitiveTypes.values()) {
			// register(primitiveType, 20, 1, vars -> new CastExpression(primitiveType, createExpression(primitiveType.Box(), vars), true));

			register(primitiveType.Box(), 100, 1, sub -> new StaticMethodInvocationExpression(primitiveType.Box(), "valueOf", List.of(sub.create(primitiveType, 0))));
			// register(primitiveType.Box(), 20, 1, vars -> new CastExpression(primitiveType.Box(), createExpression(primitiveType, vars), true));

			registerExplicitCast(primitiveType.Box(), primitiveType);
			registerExplicitCast(primitiveType, primitiveType.Box());
			registerExplicitCast(BasicType.NUMBER, primitiveType.Box());

			// register(BasicType.NUMBER, 20, 1, sub -> new CastExpression(BasicType.NUMBER, sub.create(primitiveType.Box(), Expression.UNARY_PRECEDENCE), true));

			register(primitiveType, 100, 1, sub -> new InstanceMethodInvocationExpression(sub.create(BasicType.NUMBER, Expression.UNARY_PRECEDENCE), primitiveType.getPrimitiveName() + "Value", List.of()));

			register(null, 100, 1, sub -> createPrintExpression(sub.create(primitiveType, -1)));
		}

		for (BasicType basicType : TypeCreator.BASIC_TYPES) {
			registerExplicitCast(basicType, basicType);

			if(basicType == BasicType.OBJECT) {
				continue;
			}

			//register(BasicType.OBJECT, 20, 1, vars -> new CastExpression(BasicType.OBJECT, createExpression(basicType, vars), true));
			registerExplicitCast(BasicType.OBJECT, basicType);
			registerExplicitCast(basicType, BasicType.OBJECT);
		}

		register(BasicType.STRING, 100, 1, sub -> new InstanceMethodInvocationExpression(sub.create(BasicType.OBJECT, Expression.UNARY_PRECEDENCE), "toString", List.of()));
		register(BasicType.STRING, Expression.ADDITIVE_PRECEDENCE, 2, sub -> {
			Expression expression = sub.create(BasicType.STRING, Expression.MULTIPLICATIVE_PRECEDENCE);
			return new BinaryOperatorExpression(expression, sub.create(BasicType.OBJECT, Expression.ADDITIVE_PRECEDENCE), BinaryOperatorExpression.Operator.ADD);
		});

		register(null, 100, 1, sub -> createPrintExpression(sub.create(BasicType.OBJECT, -1)));
		register(BasicType.OBJECT, 100, 0, sub -> new LiteralExpression(BasicType.OBJECT, "null"));
	}


	static void registerExplicitCast(Type castTo, Type castFrom){
		register(castTo,  Expression.UNARY_PRECEDENCE, 1, sub -> createExplicitCast(castTo, castFrom, sub));
	}
	static CastExpression createExplicitCast(Type type, Type subType, ExpressionEntry.Sub sub) {
		if(type instanceof PrimitiveTypes) {
			return new CastExpression(type, sub.create(subType, Expression.UNARY_PRECEDENCE), false);
		} else {
			// TODO: this is not correct
			return new CastExpression(type, sub.create(subType, Expression.UNARY_PRECEDENCE), false);
		}
	}
}
