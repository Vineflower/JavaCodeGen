package org.vineflower.javacodegen.types;

public record BasicType(String name) implements Type {
	public static final BasicType OBJECT = new BasicType("Object");
	public static final BasicType STRING = new BasicType("String");
	public static final BasicType SYSTEM = new BasicType("System");
	public static final BasicType EXCEPTION = new BasicType("Exception");
	public static final BasicType SCANNER = new BasicType("Scanner");
	public static final BasicType ELEMENT_TYPE = new BasicType("ElementType");

	@Override
	public void javaLike(StringBuilder builder) {
		builder.append(this.name);
	}
}
