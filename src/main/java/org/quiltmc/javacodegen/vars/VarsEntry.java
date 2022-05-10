package org.quiltmc.javacodegen.vars;

import java.util.ArrayList;
import java.util.List;

public final class VarsEntry {
    public final List<Var> vars;
    private static int nextId = 0;

    public VarsEntry() {
        this.vars = new ArrayList<>();
    }

    public VarsEntry(VarsEntry vars) {
        this.vars = new ArrayList<>(vars.vars);
    }

    public void add(Var var) {
        vars.add(var);
    }

    public VarsEntry copy() {
        return new VarsEntry(this);
    }

    public String nextName() {
        return "var" + ++nextId;
    }
}
