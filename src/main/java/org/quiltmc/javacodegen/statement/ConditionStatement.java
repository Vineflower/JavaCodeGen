package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.VarsEntry;

public class ConditionStatement extends ExpressionStatement {
    public ConditionStatement(VarsEntry vars) {
        super(vars);
    }

    @Override
    public void javaLike(StringBuilder builder, String indentation) {
        builder.append(indentation).append("new Random().nextBoolean()\n");
    }

    @Override
    public String toString() {
        return "ConditionStatement[]";
    }
}
