package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public record Continue(Continuable target, boolean simple) implements SimpleSingleNoFallThroughStatement {
	public Continue {
		target.addContinue(this);
	}

	@Override
	public String toString() {
		return "Continue[" +
				"target=" + this.target.getId() + ", " +
				"simple=" + this.simple + "]";
	}

	@Override
	public VarsEntry varsFor() {
		return this.target.varsFor();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		if (this.simple) {
			builder.append(indentation).append("continue;\n");
		} else {
			builder.append(indentation).append("continue label_").append(this.target.getId()).append(";\n");
		}
	}


}
