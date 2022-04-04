package org.quiltmc.javacodegen.statement;

public record Break(Breakable target, boolean simple) implements SimpleSingleNoFallThroughStatement {
	public Break {
		target.addBreak(this);
	}

	@Override
	public String toString() {
		return "Break[" +
				"target=" + this.target.getId() + ", " +
				"simple=" + this.simple + "]";
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		if (this.simple) {
			builder.append(indentation).append("break;\n");
		} else {
			builder.append(indentation).append("break label_").append(this.target.getId()).append(";\n");
		}
	}
}
