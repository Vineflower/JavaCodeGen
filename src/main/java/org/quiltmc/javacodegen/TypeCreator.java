package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.types.ArrayType;
import org.quiltmc.javacodegen.types.BasicType;
import org.quiltmc.javacodegen.types.PrimitiveTypes;
import org.quiltmc.javacodegen.types.Type;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class TypeCreator {
	private static final Random random = new Random();

	static Type createType() {
		return switch (random.nextInt(20)) {
			case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 -> createPrimitiveType();
			case 10 -> createPrimitiveType().Box();
			case 11, 12 -> ArrayType.ofDepth(createType(), 1);
			case 13, 14, 15 -> BasicType.OBJECT;
			case 16, 17, 18, 19 -> BasicType.STRING;
			default -> throw new IllegalStateException();
		};
	}


	static PrimitiveTypes createPrimitiveType() {
		return switch (random.nextInt(20)) {
			case 0, 1, 2, 3, 4, 5 -> PrimitiveTypes.INT;
			case 6, 7, 8, 9 -> PrimitiveTypes.LONG;
			case 10, 11, 12, 13 -> PrimitiveTypes.FLOAT;
			case 14, 15 -> PrimitiveTypes.DOUBLE;
			case 16 -> PrimitiveTypes.BOOLEAN;
			case 17 -> PrimitiveTypes.BYTE;
			case 18 -> PrimitiveTypes.CHAR;
			case 19 -> PrimitiveTypes.SHORT;
			default -> throw new IllegalStateException();
		};
	}

	public static List<? extends PrimitiveTypes.Boxed> BOXED_TYPES = Arrays.stream(PrimitiveTypes.values()).map(PrimitiveTypes::Box).toList();
	public static List<? extends PrimitiveTypes> PRIMITIVE_TYPES = Arrays.stream(PrimitiveTypes.values()).toList();
	public static List<? extends BasicType> BASIC_TYPES = List.of(BasicType.OBJECT, BasicType.STRING);
}
