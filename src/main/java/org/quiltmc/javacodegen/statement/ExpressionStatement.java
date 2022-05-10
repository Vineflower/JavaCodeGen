package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.Random;

public class ExpressionStatement implements SimpleSingleCompletingStatement {

	private VarsEntry vars;

	public ExpressionStatement(VarsEntry vars) {

		this.vars = vars;
	}

	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public VarsEntry varsFor() {
		return this.vars;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation);

		builder.append("System.out.println(");
		// TODO: move to constructor
		if (new Random().nextInt(2) == 0 && !vars.vars.isEmpty()) {
			new Var.Ref(vars.vars.get(new Random().nextInt(vars.vars.size())))
					.javaLike(builder, indentation);
		} else {
			builder.append(System.identityHashCode(this));
		}

		builder.append(");\n");
	}

	@Override
	public String toString() {
		return "ExpressionStatement[]";
	}
}
