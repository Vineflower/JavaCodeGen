package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record IfStatement(
	Statement condition,
	Statement ifTrue,
	Statement ifFalse,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Statement {
	public IfStatement {
		VarsEntry.freeze(varsEntry);
	}

	@Override
	public boolean completesNormally() {
		return this.ifFalse == null || this.ifTrue.completesNormally() || this.ifFalse.completesNormally();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		StringBuilder cond = new StringBuilder();
		this.condition.javaLike(cond, "");

		builder.append(indentation).append("if (").append(cond.toString().trim()).append(") ").append('\n');
		this.ifTrue.javaLike(builder, indentation + (this.ifTrue instanceof Scope ? "" : "\t"));
		if (this.ifFalse != null) {
			builder.append(indentation).append("else \n");
			this.ifFalse.javaLike(builder, indentation + (this.ifFalse instanceof Scope ? "" : "\t"));
		}
	}

	@Override
	public VarsEntry varsEntry() {
		return this.condition.varsEntry();
	}

	@Override
	public String toString() {
		return "IfStatement[\n" +
			   "cond=" + this.condition +
			   "ifTrue=" + this.ifTrue + (this.ifFalse == null
			? "\n]"
			: "\nifFalse=" + this.ifFalse + "\n]"
			   );
	}
}
