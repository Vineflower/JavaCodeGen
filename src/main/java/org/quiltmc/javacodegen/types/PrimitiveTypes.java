package org.quiltmc.javacodegen.types;

public enum PrimitiveTypes implements Type {
	BOOLEAN("boolean", "Boolean"),
	BYTE("byte", "Byte"),
	CHAR("char", "Character"),
	SHORT("short", "Short"),
	INT("int", "Integer"),
	LONG("long", "Long"),
	FLOAT("float", "Float"),
	DOUBLE("double", "Double"),
	;

	private final String primitiveName;
	private final String boxedName;

	PrimitiveTypes(String primitive, String boxed) {
		this.primitiveName = primitive;
		this.boxedName = boxed;
	}

	public Type Box() {
		return (builder) -> builder.append(this.boxedName);
	}


	@Override
	public void javaLike(StringBuilder builder) {
		builder.append(this.primitiveName);
	}
}
