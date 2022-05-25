package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.expression.Expression;
import org.quiltmc.javacodegen.vars.VarsEntry;

// TODO: what is this?
public class ConditionStatement extends ExpressionStatement {
	public ConditionStatement(VarsEntry vars, Expression expression) {
        super(vars, expression);
	}

    @Override
    public void javaLike(StringBuilder builder, String indentation) {
        StringBuilder expr = new StringBuilder();
        this.expression.javaLike(expr);

        builder.append(indentation).append(expr).append("\n");
    }

    @Override
    public String toString() {
        return "ConditionStatement[]";
    }
}
