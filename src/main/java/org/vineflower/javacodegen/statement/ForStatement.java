package org.vineflower.javacodegen.statement;

import org.vineflower.javacodegen.expression.Expression;
import org.vineflower.javacodegen.vars.VarsEntry;

import java.util.List;

/**
 * @param varDec should be a statement expression list
 * @param incr   should be a statement expression list
 */
public record ForStatement(
	VarDefStatement.VarDeclaration varDec,
	Expression condition,
	Expression incr,
	Statement block,
	List<? extends SimpleSingleNoFallThroughStatement> breaks,
	List<? extends SimpleSingleNoFallThroughStatement> continues,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Continuable {
	public ForStatement {
		VarsEntry.freeze(varsEntry);
		this.initMarks(breaks, continues);
	}

	@Override
	public boolean completesNormally() {
		return this.condition != null || this.hasBreak(); // TODO: check if condition is "true"
	}


	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		// check if we need a label
		this.addLabel(builder, indentation);

		builder.append(indentation).append("for (");
		if (this.varDec != null) {
			this.varDec.var().type().javaLike(builder);
			builder.append(" ");
			this.varDec.javaLike(builder);
		}
		builder.append("; ");
		if (this.condition != null) {
			condition.javaLike(builder);
		}
		builder.append("; ");
		if (this.incr != null) {
			this.incr.javaLike(builder);
		}
		builder.append(") \n");
		this.block.javaLike(builder, indentation + (this.block instanceof Scope ? "" : "\t"));
		this.addDebugVarInfo(builder, indentation);
	}


	@Override
	public String toString() {
		return "ForStatement[TODO impl]";
	}
}
