package org.quiltmc.javacodegen.types;

public record ArrayType(Type base, int depth) implements Type {
	public ArrayType {
		assert !(base instanceof ArrayType) && depth > 0;
	}

	public static Type ofDepth(Type base, int depth) {
		if (depth == 0) {
			return base;
		} else if (base instanceof ArrayType arrayType) {
			return new ArrayType(arrayType.base, arrayType.depth + depth);
		} else {
			return new ArrayType(base, depth);
		}
	}

	@Override
	public void javaLike(StringBuilder builder) {
		this.base.javaLike(builder);
		for (int i = this.depth; i > 0; i--) {
			builder.append("[]");
		}
	}
}
