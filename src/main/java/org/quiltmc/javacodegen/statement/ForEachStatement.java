package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record ForEachStatement(
	VarDefStatement.VarDeclaration varDec,
	Expression col,
	Statement block,
	List<? extends Statement> breaks,
	List<? extends Statement> continues,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Continuable{
	public ForEachStatement{
		VarsEntry.freeze(varsEntry);
		this.initMarks(breaks, continues);
	}

	@Override
	public boolean completesNormally() {
		return true;
	}


	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		// check if we need a label
		this.addLabel(builder, indentation);

		builder.append(indentation).append("for ( ");
		this.varDec.var().type().javaLike(builder);
		builder.append(" ");
		this.varDec.javaLike(builder);
		builder.append(" : ");
		this.col.javaLike(builder);
		builder.append(" )\n");

		this.block.javaLike(builder, indentation + (this.block instanceof Scope ? "" : "\t"));
		this.addDebugVarInfo(builder, indentation);
	}


	@Override
	public String toString() {
		return "ForEachStatement[TODO impl]";
	}
}
