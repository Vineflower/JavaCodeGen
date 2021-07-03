package org.quiltmc.javacodegen.statement;

public final class WhileTrueStatement extends Continuable {
	private Statement block;

	public WhileTrueStatement(
			/* TODO: const expression */
	) {
	}

	public void setBlock(Statement block) {
		this.block = block;
	}

	@Override
	public boolean completesNormally() {
		return this.canBreak();
	}

	@Override
	public void javaLike(StringBuilder builder, String indentation) {
		this.addLabel(builder, indentation);

		builder.append(indentation).append("while (true) \n");
		this.block.javaLike(builder, indentation + (this.block instanceof Scope?"":"\t"));
	}

	@Override
	public String toString() {
		return "WhileStatement@" + System.identityHashCode(this) + "[" +
				"block=" + this.block + ']';
	}

}
