package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.VarsEntry;

public class ForStatement extends Continuable {
    private final VarDefStatement.VarDeclaration varDec;
    private final Statement condition;
    private final Expression incr;
    private final VarsEntry vars;
    private Statement block;

    public ForStatement(VarDefStatement.VarDeclaration varDec, Statement condition, Expression incr, VarsEntry vars) {
        this.varDec = varDec;
        this.condition = condition;
        this.incr = incr;
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

        StringBuilder cond = new StringBuilder();
        this.condition.javaLike(cond, "");

        StringBuilder init = new StringBuilder();
        this.varDec.var().type().javaLike(init);
        init.append(" ");
        this.varDec.javaLike(init);

        StringBuilder incr = new StringBuilder();
        this.incr.javaLike(incr);

        builder.append(indentation).append("for (").append(init).append("; ").append(cond.toString().trim()).append("; ").append(incr).append(") \n");
        this.block.javaLike(builder,indentation + (this.block instanceof Scope? "" : "\t"));
    }

    @Override
    public String toString() {
        return "ForStatement[TODO impl]";
    }
}
