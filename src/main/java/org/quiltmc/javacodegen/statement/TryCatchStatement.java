package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record TryCatchStatement(
	Scope tryStatement,
	List<CatchClause> catches,
	Scope finallyStatement,
	Expression resource,
	VarsEntry varsEntry,
	List<? extends SimpleSingleNoFallThroughStatement> breakOuts
) implements Statement {
	public TryCatchStatement {
		VarsEntry.freeze(varsEntry);
	}

	@Override
	public boolean completesNormally() {
		if (this.finallyStatement != null && !this.finallyStatement.completesNormally()) {
			return false;
		} else if (this.tryStatement.completesNormally()) {
			return true;
		} else {
			for (CatchClause aCatch : this.catches) {
				if (aCatch.catchStatement().completesNormally()) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("try");

		if (resource != null) {
			builder.append("(").append(resource.toJava()).append(")");
		} else {
			builder.append(" ");
		}

		builder.append('\n');
		this.tryStatement.javaLike(builder, indentation );
		for (CatchClause aCatch : this.catches) {
			aCatch.javaLike(builder, indentation);
		}
		if (this.finallyStatement != null) {
			builder.append(indentation).append("finally ").append('\n');
			this.finallyStatement.javaLike(builder, indentation);
		}
		this.addDebugVarInfo(builder, indentation);
	}

	public record CatchClause(Var catchVar, Scope catchStatement) {
		public void javaLike(StringBuilder builder, String indentation) {
			builder
				.append(indentation)
				.append("catch (");
			this.catchVar.type().javaLike(builder);
			builder
				.append(" ")
				.append(this.catchVar.name())
				.append(")")
				.append('\n');
			this.catchStatement.javaLike(builder, indentation);
		}
	}
}
