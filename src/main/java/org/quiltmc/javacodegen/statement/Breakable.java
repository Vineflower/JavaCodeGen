package org.quiltmc.javacodegen.statement;

import java.util.ArrayList;
import java.util.List;

public abstract class Breakable implements Statement {
	private List<Break> breaks = new ArrayList<>();

	void addBreak(Break b){
		this.breaks.add(b);
	}

	protected boolean canBreak(){
		return !this.breaks.isEmpty();
	}
}
