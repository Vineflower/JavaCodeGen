package org.vineflower.javacodegen.statement;

import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.vars.VarsEntry;

import java.util.List;

public record IfStatement(
	Expression condition,
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
		builder.append(indentation).append("if (");
		this.condition.javaLike(builder);
		builder.append(") ").append('\n');
		this.ifTrue.javaLike(builder, indentation + (this.ifTrue instanceof Scope ? "" : "\t"));
		if (this.ifFalse != null) {
			builder.append(indentation).append("else \n");
			this.ifFalse.javaLike(builder, indentation + (this.ifFalse instanceof Scope ? "" : "\t"));
		}
		this.addDebugVarInfo(builder, indentation);
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
