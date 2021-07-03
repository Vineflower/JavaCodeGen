package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.statement.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Context {
	private static final Random random = new Random();

	private final List<Breakable> breakables = new ArrayList<>();
	private final List<Continuable> continuables = new ArrayList<>();

	SimpleSingleNoFallThroughStatement createBreak() {
		int target = this.breakables.size() + this.continuables.size() + 2;

		Breakable simpleBreak = null;
		for (Breakable i : this.breakables) {
			if (!(i instanceof LabeledStatement)) {
				simpleBreak = i;
			}
		}

		Continuable simpleContinue = this.continuables.isEmpty() ? null : this.continuables.get(this.continuables.size() - 1);

		target += (simpleBreak != null ? 1 : 0) + (simpleContinue != null ? 1 : 0);


		int selected = random.nextInt(target);

		if (selected < this.breakables.size()) {
			return new Break(this.breakables.get(selected), false);
		}
		selected -= this.breakables.size();

		if (selected < this.continuables.size()) {
			return new Continue(this.continuables.get(selected), false);
		}
		selected -= this.continuables.size();

		if (selected == 0) {
			return new Return(); // todo might need an expression
		} else if (selected == 1) {
			return new Return(); // todo add throw
		} else if (selected == 2) {
			if (simpleBreak != null) {
				return new Break(simpleBreak, true);
			}
		}
		return new Continue(simpleContinue, true);
	}

	public void addBreak(Breakable breakable) {
		this.breakables.add(breakable);
	}

	public void removeBreak(Breakable breakable) {
		if (this.breakables.remove(this.breakables.size() - 1) !=
				breakable) {
			throw new IllegalStateException();
		}
	}

	public void addContinuable(Continuable continuable) {
		this.continuables.add(continuable);
	}

	public void removeContinuable(Continuable continuable) {
		if (this.continuables.remove(this.continuables.size() - 1) !=
				continuable) {
			throw new IllegalStateException();
		}
	}
}
