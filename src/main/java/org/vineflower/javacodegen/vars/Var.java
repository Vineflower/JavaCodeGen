package org.vineflower.javacodegen.vars;

import org.vineflower.javacodegen.types.Type;

public record Var(String name, Type type, FinalType finalType) {

	public void javaLike(StringBuilder builder) {
		builder.append(this.name);
	}
}
