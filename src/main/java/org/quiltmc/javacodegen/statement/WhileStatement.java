package org.quiltmc.javacodegen.statement;

public final class WhileStatement extends Continuable {
	private final Statement condition;
	private Statement block;

	public WhileStatement(
			Statement condition
	) {
		this.condition = condition;
	}

	public void setBlock(Statement block) {
		this.block = block;
	}

	@Override
	public boolean completesNormally() {
		return true;
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		// check if we need a label
		this.addLabel(builder, indentation);

		StringBuilder cond = new StringBuilder();
		this.condition.javaLike(cond, "");

		builder.append(indentation).append("while (").append(cond.toString().trim()).append(") \n");
		this.block.javaLike(builder,indentation + (this.block instanceof Scope?"":"\t"));
	}

	@Override
	public String toString() {
		return "WhileStatement@" + System.identityHashCode(this) + "[" +
				"block=" + this.block + ']';
	}

}
