package org.quiltmc.javacodegen.statement;

import org.quiltmc.javacodegen.vars.Var;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.Random;

public class VarDefStatement extends ExpressionStatement implements LabelImpossible {
    private final Var var;
    private final int value;

    public VarDefStatement(VarsEntry vars) {
        super(vars);

        this.var = new Var(vars.nextName(), Var.Type.INT);
        this.value = new Random().nextInt(200);
        vars.add(var);
    }

    @Override
    public void javaLike(StringBuilder builder, String indentation) {
        this.var.javaLike(builder, indentation);
        builder.append(" = ");
        builder.append(this.value);
        builder.append(";\n");
    }
}
