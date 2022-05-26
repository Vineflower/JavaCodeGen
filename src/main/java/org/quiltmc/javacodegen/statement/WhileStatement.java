package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record WhileStatement(
	Statement condition,
	Statement block,
	List<? extends Statement> breaks,
	List<? extends Statement> continues,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Continuable{
	public WhileStatement {
		VarsEntry.freeze(varsEntry);
		this.initMarks(breaks, continues);
	}

	@Override
	public boolean completesNormally() {
		return true;// TODO merge with "WhileTrue"
	}


	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		// check if we need a label
		this.addLabel(builder, indentation);

		StringBuilder cond = new StringBuilder();
		this.condition.javaLike(cond, "");

		builder.append(indentation).append("while (").append(cond.toString().trim()).append(") \n");
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
