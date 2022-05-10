package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.ArrayList;
import java.util.List;

public class Scope implements Statement {
	private VarsEntry vars;

	public Scope(VarsEntry vars) {

		this.vars = vars;
	}

	List<Statement> statements = new ArrayList<>();

	public void addStatement(Statement statement) {
		this.statements.add(statement);
	}

	@Override
	public boolean completesNormally() {
		if (this.statements.isEmpty()) {
			return true;
		}

		return this.statements.get(this.statements.size() - 1).completesNormally();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		builder.append(indentation).append("{\n");
		for (Statement statement : statements) {
			statement.javaLike(builder, indentation + "\t");
		}
		builder.append(indentation).append("}\n");
	}

	@Override
	public VarsEntry varsFor() {
		return this.vars;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder().append("Scope[").append("statements={");
		for (Statement statement : this.statements) {
			builder.append("\n").append(statement);
		}
		if (!this.statements.isEmpty()) {
			builder.append("\n");
		}
		builder.append("}]");
		return builder.toString();
	}
}
