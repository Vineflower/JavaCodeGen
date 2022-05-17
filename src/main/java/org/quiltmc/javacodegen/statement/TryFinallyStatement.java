package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public class TryFinallyStatement implements Statement {
    private final Statement tryStatement;
    private final Statement finallyStatement;
    private final VarsEntry vars;

    public TryFinallyStatement(Statement tryStatement, Statement finallyStatement, VarsEntry vars) {
        this.tryStatement = tryStatement;
        this.finallyStatement = finallyStatement;
        this.vars = vars;
    }

    @Override
    public boolean completesNormally() {
        return true;
    }

    @Override
    public VarsEntry varsFor() {
        return this.vars;
    }

    @Override
    public void javaLike(StringBuilder builder, String indentation) {
        builder.append(indentation).append("try ").append('\n');
        this.tryStatement.javaLike(builder,indentation + (this.tryStatement instanceof Scope?"":"\t"));
        builder.append(indentation).append("finally ").append('\n');
        this.finallyStatement.javaLike(builder,indentation + (this.finallyStatement instanceof Scope?"":"\t"));
    }
}
