package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

// TODO: resources
public record TryCatchStatement(
	Scope tryStatement,
	List<CatchClause> catches,
	Scope finallyStatement,
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
		builder.append(indentation).append("try ").append('\n');
		this.tryStatement.javaLike(builder, indentation );
		for (CatchClause aCatch : this.catches) {
			aCatch.javaLike(builder, indentation);
		}
		if (this.finallyStatement != null) {
			builder.append(indentation).append("finally ").append('\n');
			this.finallyStatement.javaLike(builder, indentation);
		}
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
