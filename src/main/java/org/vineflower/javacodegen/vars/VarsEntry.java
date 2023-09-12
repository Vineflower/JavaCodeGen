package org.vineflower.javacodegen.vars;

import org.jetbrains.annotations.Contract;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class VarsEntry {
	public final Map<Var, VarState> vars;
	private boolean frozen;
	private static int nextId = 0;

	public VarsEntry() {
		this.vars = new LinkedHashMap<>();
	}

	public VarsEntry(VarsEntry vars) {
		this.vars = new LinkedHashMap<>();
		if (vars != null) {
			this.vars.putAll(vars.vars);
		}
		this.frozen = false;
	}

	@Contract(value = "null, _ -> param2; !null, null -> param1; !null, !null -> !null", pure = true)
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
		Objects.requireNonNull(scope, "scope has to be reachable");

		if (inner == null) {
			return null;
		}

		VarsEntry out = inner.frozen ? inner.copy() : inner;

		out.vars.entrySet().removeIf(e -> !scope.vars.containsKey(e.getKey()));

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

	private static VarsEntry EMPTY = new VarsEntry();
	static {
		EMPTY.freeze();
	}
	public static VarsEntry empty() {
		return EMPTY;
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

	@Contract(pure = true, value = "-> null")
	public static VarsEntry never() {
		return null;
	}

	public VarsEntry freeze() {
		this.frozen = true;
		return this;
	}

	public boolean isFrozen() {
		return this.frozen;
	}

	public void debugPrint(StringBuilder builder, String indentation) {
		this.vars.forEach((var, state) -> builder
			.append(indentation).append("// ").append(var.name()).append(" = ").append(state.toString()).append("\n"));
	}
}
