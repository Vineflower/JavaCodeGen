package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.VarsEntry;

public class ConditionStatement extends ExpressionStatement {
    public ConditionStatement(VarsEntry vars, Expression expression) {
        super(vars, expression);
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
