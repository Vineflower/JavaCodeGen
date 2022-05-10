package org.quiltmc.javacodegen.vars;

public final class Var {
    private final String name;
    private final Type type;

    public Var(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public static class Ref {
        private final Var var;

        public Ref(Var var) {
            this.var = var;
        }

        public void javaLike(StringBuilder builder, String indentation) {
            builder.append(this.var.name);
        }
    }

    public void javaLike(StringBuilder builder, String indentation) {
        builder.append(indentation).append(this.type.name).append(" ").append(this.name);
    }

    public enum Type {
        INT("int");

        private final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
