package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.Expression.Expression;
import org.quiltmc.javacodegen.types.Type;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.List;
import java.util.Random;

public class VarDefStatement implements SimpleSingleCompletingStatement, LabelImpossible {
	private VarsEntry vars;
	private final Type outerVarType;
	private final List<? extends VarDeclaration> varDeclarations;

	public VarDefStatement(VarsEntry vars, Type outerVarType, List<? extends VarDeclaration> varDeclarations) {
		this.vars = vars;
		this.outerVarType = outerVarType;
		this.varDeclarations = varDeclarations;
	}

	@Override
	public VarsEntry varsFor() {
		return vars;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation);
		this.outerVarType.javaLike(builder, indentation);
		builder.append(" ");
		boolean first = true;
		for (VarDeclaration varDeclaration : varDeclarations) {
			if (!first) {
				builder.append(", ");
			}
			varDeclaration.javaLike(builder, indentation);

			first = false;
		}
		builder.append(";\n");
	}

	public record VarDeclaration(
			Var var,
			int arrayDepth,
			Expression value // nullable
	) {

		public void javaLike(StringBuilder builder, String indentation) {
			var.javaLike(builder, indentation);
			if (value != null) {
				builder.append(" = ");
				value.javaLike(builder);
			}
		}
	}

}
