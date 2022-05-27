package org.quiltmc.javacodegen.types;

public record BasicType(String name) implements Type {
	public static final BasicType OBJECT = new BasicType("Object");
	public static final BasicType STRING = new BasicType("String");
	public static final BasicType SYSTEM = new BasicType("System");
	public static final BasicType EXCEPTION = new BasicType("Exception");
	public static final BasicType SCANNER = new BasicType("Scanner");

	@Override
	public void javaLike(StringBuilder builder) {
		builder.append(this.name);
	}
}
