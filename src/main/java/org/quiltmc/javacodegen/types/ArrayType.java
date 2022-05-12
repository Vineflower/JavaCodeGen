package org.quiltmc.javacodegen.types;

public record ArrayType(Type base) implements Type {

	public static Type ofDepth(Type base, int depth) {
		if (depth <= 0) {
			return base;
		}
		return ofDepth(new ArrayType(base), depth - 1);
	}

	@Override
	public void javaLike(StringBuilder builder) {
		this.base.javaLike(builder);
		builder.append("[]");
	}
}
