package org.quiltmc.javacodegen.statement;

public record IfStatement(
		/* TODO: expression */
		Statement ifTrue,
		Statement ifFalse
) implements Statement {

	@Override
	public boolean completesNormally() {
		return this.ifFalse == null || this.ifTrue.completesNormally() || this.ifFalse.completesNormally();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("if (...) ").append('\n');
		this.ifTrue.javaLike(builder,indentation + (this.ifTrue instanceof Scope?"":"\t"));
		if(this.ifFalse != null){
			builder.append(indentation).append("else \n");
			this.ifFalse.javaLike(builder,indentation + (this.ifFalse instanceof Scope?"":"\t"));
		}
	}

	@Override
	public String toString() {
		return "IfStatement[\n" +
				"ifTrue=" + this.ifTrue + (this.ifFalse == null
				? "\n]"
				: "\nifFalse=" + this.ifFalse + "\n]"
		);
	}
}
