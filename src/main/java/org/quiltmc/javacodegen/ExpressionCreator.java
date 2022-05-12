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
			case FLOAT -> new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextFloat() - 50) + "F";
			case DOUBLE -> new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(200 * random.nextDouble() - 50) + "";
		});
	}
}
