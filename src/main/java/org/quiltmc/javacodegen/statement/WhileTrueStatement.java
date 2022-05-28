package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;


public record WhileTrueStatement(
	/* TODO: const expression */
	Statement block,
	List<? extends SimpleSingleNoFallThroughStatement> breaks,
	List<? extends SimpleSingleNoFallThroughStatement> continues,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts

) implements Continuable {
	public WhileTrueStatement {
		VarsEntry.freeze(varsEntry);
		this.initMarks(breaks, continues);
	}

	@Override
	public boolean completesNormally() {
		return this.hasBreak();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		this.addLabel(builder, indentation);

		builder.append(indentation).append("while (true) \n");
		this.block.javaLike(builder, indentation + (this.block instanceof Scope ? "" : "\t"));
		this.addDebugVarInfo(builder, indentation);
	}

	@Override
	public String toString() {
		return "WhileStatement@" + System.identityHashCode(this) + "[" +
			   "block=" + this.block + ']';
	}

}
