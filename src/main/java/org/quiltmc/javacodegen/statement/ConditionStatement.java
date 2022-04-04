package org.quiltmc.javacodegen.statement;

public class ConditionStatement extends ExpressionStatement {
    @Override
    public void javaLike(StringBuilder builder, String indentation) {
        builder.append(indentation).append("new Random().nextBoolean()\n");
    }

    @Override
    public String toString() {
        return "ConditionStatement[]";
    }
}
