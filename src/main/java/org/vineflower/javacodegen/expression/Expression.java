package org.vineflower.javacodegen.expression;

public interface Expression {
	default boolean isConstant(){
		return false;
	}

	default Object evaluateConstant(){
		throw new IllegalStateException("Not a constant");
	}

	void javaLike(StringBuilder builder);

	default String toJava() {
		StringBuilder sb = new StringBuilder();
		javaLike(sb);

		return sb.toString();
	}
}
