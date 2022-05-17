package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

public class TryCatchStatement implements Statement {
    private final Statement tryStatement;
    private final Statement catchStatement;
    private final Var catchVar;
    private final VarsEntry vars;

    public TryCatchStatement(Statement tryStatement, Statement catchStatement, Var catchVar, VarsEntry vars) {
        this.tryStatement = tryStatement;
        this.catchStatement = catchStatement;
        this.catchVar = catchVar;
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
        // TODO: don't hardcode the exception type
        builder.append(indentation).append("catch(Exception ").append(this.catchVar.name()).append(")").append('\n');
        this.catchStatement.javaLike(builder,indentation + (this.catchStatement instanceof Scope?"":"\t"));
    }
}
