package org.quiltmc.javacodegen.statement;

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
	public void javaLike(StringBuilder builder, String indentation) {
		if (this.simple) {
			builder.append(indentation).append("continue;\n");
		} else {
			builder.append(indentation).append("continue label_").append(this.target.getId()).append(";\n");
		}
	}


}
