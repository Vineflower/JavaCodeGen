package org.quiltmc.javacodegen.vars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VarsEntry {
	public final Map<Var, VarState> vars;
	private static int nextId = 0;

	public VarsEntry() {
		this.vars = new HashMap<>();
	}

	public VarsEntry(VarsEntry vars) {
		this.vars = new HashMap<>(vars.vars);
	}

	public void create(Var var, boolean isAssigned) {
		vars.put(var, new VarState(isAssigned));
	}

	public VarsEntry copy() {
		return new VarsEntry(this);
	}

	public String nextName() {
		return "var" + ++nextId;
	}
}
