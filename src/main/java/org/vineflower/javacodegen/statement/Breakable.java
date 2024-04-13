package org.vineflower.javacodegen.statement;

import java.util.List;

public interface Breakable extends Statement {
	default void initMarks(List<? extends SimpleSingleNoFallThroughStatement> breaks) {
		breaks.forEach(this::initMark);
	}

	private void initMark(Statement statement) {
		WrappedBreakOutStatement.<Break>baseAs(statement).setTarget(this);
	}
	List<? extends SimpleSingleNoFallThroughStatement> breaks();


	default boolean hasBreak() {
		return !this.breaks().isEmpty();
	}

	default int getId() {
		return System.identityHashCode(this);
	}

	default boolean needsLabel() {
		return !this.breaks().stream().allMatch(Break::simpleBreak);
	}

	default void addLabel(StringBuilder builder, String indentation) {
		this.addBreakInfo(builder, indentation);
		if (this.needsLabel()) {
			builder.append(indentation).append("label_").append(System.identityHashCode(this)).append(":\n");
		}
	}

	default void addBreakInfo(StringBuilder builder, String indentation) {
		if (DEBUG_BREAKS) {
			builder.append(indentation).append("// breakable ").append(this.getId()).append("\n");
			for (Statement aBreak : this.breaks()) {
				builder.append(indentation)
					.append("// break from ")
					.append(System.identityHashCode(WrappedBreakOutStatement.base(aBreak)))
					.append(WrappedBreakOutStatement.isDead(aBreak) ? " (dead)" : " (alive)")
					.append("\n");
				builder.append(indentation).append("//\t\t");
				WrappedBreakOutStatement.idChain(aBreak, builder);
				builder.append("\n");
			}
		}
	}
}
