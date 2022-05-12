package org.quiltmc.javacodegen.types;

import java.text.DecimalFormat;
import java.util.Random;

public final class TypeCreator {
    public static void random(Random random, Type type, StringBuilder builder) {
        if (type instanceof PrimitiveTypes primitiveType) {
            builder.append(switch (primitiveType) {
                case BOOLEAN -> random.nextBoolean() ? "true" : "false";
                case BYTE -> random.nextInt(256) + "";
                case CHAR -> Character.valueOf((char) random.nextInt(256)).toString();
                case SHORT -> random.nextInt(65536) + "";
                case INT -> random.nextInt(500) + "";
                case LONG -> random.nextInt(1000) + "";
                case FLOAT -> new DecimalFormat("###.##").format(100 * random.nextFloat()) + "F";
                case DOUBLE -> new DecimalFormat("###.##").format(100 * random.nextDouble()) + "";
            });
        } else if (type instanceof ArrayType arrayType) {
            builder.append("new ");
            arrayType.base().javaLike(builder);
            builder.append("[0]");
        } else if (type instanceof BasicType basicType) {
            if (type == BasicType.STRING) {
                builder.append("\"Hi!\"");
            } else {
                builder.append("null");
            }
        }
    }
}
