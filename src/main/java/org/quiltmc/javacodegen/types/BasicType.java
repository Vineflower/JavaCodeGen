package org.quiltmc.javacodegen.types;

public record BasicType(String name) implements Type {
	public static final BasicType OBJECT = new BasicType("Object");
	public static final BasicType STRING = new BasicType("String");

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(this.name);
	}
}
