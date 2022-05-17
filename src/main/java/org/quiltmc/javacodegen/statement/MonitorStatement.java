package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public class MonitorStatement implements Statement {
    private Statement body;

    public MonitorStatement(Statement body) {

        this.body = body;
    }

    @Override
    public boolean completesNormally() {
        return true;
    }

    @Override
    public VarsEntry varsFor() {
        return this.body.varsFor();
    }

    @Override
    public void javaLike(StringBuilder builder, String indentation) {
        builder.append(indentation).append("synchronized (this) \n");
        this.body.javaLike(builder,indentation + (this.body instanceof Scope?"":"\t"));
    }
}
