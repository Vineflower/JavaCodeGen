package org.quiltmc.javacodegen.vars;

public class VarState {
	private boolean definiteUnassigned;
	private boolean definiteAssigned;

	public VarState(boolean definiteUnassigned, boolean definiteAssigned) {
		this.definiteUnassigned = definiteUnassigned;
		this.definiteAssigned = definiteAssigned;
	}

	public VarState() {
		this(false, false);
	}

	public VarState(boolean isAssigned) {
		this(!isAssigned, isAssigned);
	}

	public boolean isDefiniteUnassigned() {
		return this.definiteUnassigned;
	}

	public boolean isDefiniteAssigned() {
		return this.definiteAssigned;
	}

	public VarState copy() {
		return new VarState(this.definiteUnassigned, this.definiteAssigned);
	}

	public void mergeWith(VarState other) {
		this.definiteUnassigned = this.definiteUnassigned && other.definiteUnassigned;
		this.definiteAssigned = this.definiteAssigned && other.definiteAssigned;
	}
}
