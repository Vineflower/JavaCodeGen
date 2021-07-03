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
		int selected = random.nextInt(target);

		if (selected < this.breakables.size()) {
			return new Break(this.breakables.get(selected));
		}
		selected -= this.breakables.size();

		if (selected < this.continuables.size()) {
			return new Continue(this.continuables.get(selected));
		}
		selected -= this.continuables.size();

		if (selected == 0) {
			return new Return(); // todo might need an expression
		} else {
			return new Return(); // todo add throw
		}
	}

	public void addBreak(Breakable breakable) {
		this.breakables.add(breakable);
	}

	public void removeBreak(Breakable breakable) {
		if(this.breakables.remove(this.breakables.size() - 1) !=
				breakable){
			throw new IllegalStateException();
		}
	}
}
