package org.quiltmc.javacodegen.vars;

import org.quiltmc.javacodegen.types.Type;

public record Var(String name, Type type, FinalType finalType) {

	public void javaLike(StringBuilder builder) {
		builder.append(this.name);
	}
}
