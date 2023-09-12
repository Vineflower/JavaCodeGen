package org.vineflower.javacodegen.vars;

public enum VarState {
	MIXED(false , false),
	ASSIGNED(false, true),
	UNASSIGNED(true, false),
	NEVER(true, true)
	;


	private final boolean definiteUnassigned;
	private final boolean definiteAssigned;

	VarState(
		boolean definiteUnassigned,
		boolean definiteAssigned
	) {

		this.definiteUnassigned = definiteUnassigned;
		this.definiteAssigned = definiteAssigned;
	}

	private static final VarState[] values = values();

	public static VarState def(boolean isAssigned) {
		return isAssigned? ASSIGNED : UNASSIGNED;
	}

	public VarState mergeWith(VarState other) {
		return values[this.ordinal() & other.ordinal()];
	}

	public boolean isDefiniteAssigned() {
		return this.definiteAssigned;
	}

	public boolean isDefiniteUnassigned() {
		return this.definiteUnassigned;
	}

	public VarState applyFinally(VarState preFinally, VarState finallyVar) {
		boolean definiteAssigned = this.definiteAssigned || finallyVar.definiteAssigned;
		// this is technically wrong, but as definiteUnassigned ony matters for final vars
		// then if a final var isn't definite unassigned before finally, then there
		// are no assignments to it in the 'finally' block.
		boolean definiteUnassigned = this.definiteUnassigned && (finallyVar.definiteUnassigned || !preFinally.definiteUnassigned);
		return values[(definiteAssigned? 1 : 0) | (definiteUnassigned? 2 : 0)];
	}
}
