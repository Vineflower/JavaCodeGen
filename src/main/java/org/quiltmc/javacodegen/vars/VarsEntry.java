package org.quiltmc.javacodegen.vars;

import java.util.HashMap;
import java.util.Map;

public final class VarsEntry {
	public final Map<Var, VarState> vars;
	private boolean frozen;
	private static int nextId = 0;

	public VarsEntry() {
		this.vars = new HashMap<>();
	}

	public VarsEntry(VarsEntry vars) {
		this.vars = new HashMap<>();
		if (vars != null) {
			this.vars.putAll(vars.vars);
		}
		this.frozen = false;
	}

	public static VarsEntry merge(VarsEntry aVars, VarsEntry bVars) {
		if (aVars == null) {
			return bVars;
		} else if (bVars == null) {
			return aVars;
		} else {
			// TODO: optimize if one of the 2 isn't frozen
			VarsEntry outVars = new VarsEntry();

			for (var entry : aVars.vars.entrySet()) {
				VarState bVar = bVars.vars.get(entry.getKey());
				if (bVar != null) {
					outVars.vars.put(entry.getKey(), entry.getValue().mergeWith(bVar));
				}
			}

			return outVars;
		}
	}

	public static VarsEntry merge(VarsEntry... entries) {
		VarsEntry out = null;

		for (VarsEntry entry : entries) {
			out = merge(out, entry);
		}

		return out;
	}


	public static VarsEntry applyScopeTo(VarsEntry scope, VarsEntry inner) {
		// todo: pattern match can leak out of scopes

		if (scope == null) {
			return null;
		}

		if (inner == null) {
			return null;
		}

		VarsEntry out = inner.frozen ? inner.copy() : scope;

		inner.vars.entrySet().removeIf(e -> !scope.vars.containsKey(e.getKey()));

		return out;
	}

	public static void freeze(VarsEntry breakOutVars) {
		if (breakOutVars != null) {
			breakOutVars.freeze();
		}
	}

	public static VarsEntry applyFinallyTo(VarsEntry preFinallyVars, VarsEntry finallyVars, VarsEntry varsEntry) {
		if (finallyVars == null) {
			// huh
			return varsEntry;
		} else if (varsEntry == null) {
			return finallyVars;
		} else {
			VarsEntry out = varsEntry.frozen ? varsEntry.copy() : varsEntry;

			for (var entry : preFinallyVars.vars.entrySet()) {
				out.vars.merge(entry.getKey(), entry.getValue(), (a, b) -> a.applyFinally(b, finallyVars.vars.get(entry.getKey())));
			}
			return out;
		}
	}

	public void create(Var var, boolean isAssigned) {
		if (this.frozen) {
			throw new IllegalStateException("Frozen");
		}

		this.vars.put(var, VarState.def(isAssigned));
	}

	public void assign(Var var) {
		if (this.frozen) {
			throw new IllegalStateException("Frozen");
		}

		this.vars.put(var, VarState.ASSIGNED);
	}

	public VarsEntry copy() {
		return new VarsEntry(this);
	}

	public static void resetId() {
		nextId = 0;
	}

	public String nextName() {
		return "vvv" + ++nextId;
	}

	public static VarsEntry never() {
		return null;
	}

	public void freeze() {
		this.frozen = true;
	}

	public boolean isFrozen() {
		return this.frozen;
	}
}
