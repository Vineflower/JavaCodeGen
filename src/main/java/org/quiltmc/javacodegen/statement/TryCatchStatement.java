package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public class TryCatchStatement implements Statement {
	private final Statement tryStatement;
	private final List<CatchClause> catches;
	private final Statement finallyStatement;
	private final VarsEntry vars;

	public TryCatchStatement(Statement tryStatement, List<CatchClause> catches, Statement finallyStatement, VarsEntry vars) {
		this.tryStatement = tryStatement;
		this.catches = catches;
		this.finallyStatement = finallyStatement;
		this.vars = vars;
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
	public VarsEntry varsFor() {
		return this.vars;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("try ").append('\n');
		this.tryStatement.javaLike(builder, indentation + (this.tryStatement instanceof Scope ? "" : "\t"));
		for (CatchClause aCatch : this.catches) {
			aCatch.javaLike(builder, indentation);
		}
		if (this.finallyStatement != null) {
			builder.append(indentation).append("finally ").append('\n');
			this.finallyStatement.javaLike(builder, indentation + (this.finallyStatement instanceof Scope ? "" : "\t"));
		}
	}

	public record CatchClause(Var catchVar, Statement catchStatement) {
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
			this.catchStatement.javaLike(builder, indentation + (this.catchStatement instanceof Scope ? "" : "\t"));
		}
	}
}
