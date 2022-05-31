package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record WhileStatement(
	Expression condition,
	Statement block,
	List<? extends SimpleSingleNoFallThroughStatement> breaks,
	List<? extends SimpleSingleNoFallThroughStatement> continues,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Continuable {
	public WhileStatement {
		VarsEntry.freeze(varsEntry);
		this.initMarks(breaks, continues);
	}

	@Override
	public boolean completesNormally() {
		return this.condition != null || this.hasBreak();
	}


	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		// check if we need a label
		this.addLabel(builder, indentation);

		builder.append(indentation).append("while (");

		if (this.condition != null) {
			this.condition.javaLike(builder);
		} else {
			builder.append("true");
		}

		builder.append(") \n");
		this.block.javaLike(builder, indentation + (this.block instanceof Scope ? "" : "\t"));
		this.addDebugVarInfo(builder, indentation);
	}


	@Override
	public String toString() {
		return "WhileStatement@" + System.identityHashCode(this) + "[" +
			   "cond=" + this.condition + ']' +
			   "block=" + this.block + ']';
	}

}
