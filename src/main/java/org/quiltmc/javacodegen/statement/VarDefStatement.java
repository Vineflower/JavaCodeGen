package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;

public record VarDefStatement(
	Type outerVarType,
	List<? extends VarDeclaration> varDeclarations,
	VarsEntry varsEntry
) implements SimpleSingleCompletingStatement, LabelImpossible {
	public VarDefStatement {
		VarsEntry.freeze(varsEntry);
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation);
		this.outerVarType.javaLike(builder);
		builder.append(" ");
		boolean first = true;
		for (VarDeclaration varDeclaration : this.varDeclarations) {
			if (!first) {
				builder.append(", ");
			}
			varDeclaration.javaLike(builder);

			first = false;
		}
		builder.append(";\n");
		this.addDebugVarInfo(builder, indentation);
	}

	public record VarDeclaration(
		Var var,
		int arrayDepth,
		Expression value // nullable
	) {

		public void javaLike(StringBuilder builder) {
			this.var.javaLike(builder);
			builder.append("[]".repeat(this.arrayDepth));
			if (this.value != null) {
				builder.append(" = ");
				this.value.javaLike(builder);
			}
		}
	}

}
