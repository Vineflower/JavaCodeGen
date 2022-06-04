package org.quiltmc.javacodegen.types;

public interface Type {
	Type NULL = sb -> sb.append("null");


	void javaLike(StringBuilder builder);

	default String toJava() {
		StringBuilder sb = new StringBuilder();
		javaLike(sb);
		return sb.toString();
	}
}
