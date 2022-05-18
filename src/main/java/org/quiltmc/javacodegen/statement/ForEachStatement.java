package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

public class ForEachStatement extends Continuable {
    private final VarDefStatement.VarDeclaration varDec;
    private final Var col;
    private final VarsEntry vars;
    private Statement block;

    public ForEachStatement(VarDefStatement.VarDeclaration varDec, Var col, VarsEntry vars) {
        this.varDec = varDec;
        this.col = col;
        this.vars = vars;
    }

    public void setBlock(Statement block) {
        this.block = block;
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
        // check if we need a label
        this.addLabel(builder, indentation);

        StringBuilder init = new StringBuilder();
        this.varDec.var().type().javaLike(init);
        init.append(" ");
        this.varDec.javaLike(init);
        builder.append(indentation).append("for (").append(init).append(" : ").append(this.col.name()).append(") \n");

        this.block.javaLike(builder,indentation + (this.block instanceof Scope? "" : "\t"));
    }

    @Override
    public String toString() {
        return "ForEachStatement[TODO impl]";
    }
}
