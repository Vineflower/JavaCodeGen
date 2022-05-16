package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.statement.Statement;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class CompilingCreator {
    public static void main(String[] args) throws Exception {
        int count = 10;

        for (int i = 0; i < count; i++) {
            Statement statement = (new Creator()).createScope(
                    false,
                    true,
                    new Context(),
                    new Creator.Params(5),
                    new VarsEntry()
            );

            Paths.get(".", "fuzzed").toFile().mkdirs();
            Paths.get(".", "compiled").toFile().mkdirs();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("import java.util.Random;\n");

            stringBuilder.append("class FuzzedClass_").append(i).append(" {\n");
            stringBuilder.append("public void test()").append(" {\n");
            statement.javaLike(stringBuilder, "");
            stringBuilder.append("}\n");
            stringBuilder.append("}\n");

            Files.write(Paths.get(".", "fuzzed", "FuzzedClass_" + i + ".java"), stringBuilder.toString().getBytes());
        }

        Process exec = Runtime.getRuntime().exec("javac -g " + Paths.get(".", "fuzzed").toAbsolutePath() + "\\*.java -d " + Paths.get(".", "compiled").toAbsolutePath());

        BufferedReader serr = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

        String s;

        while ((s = serr.readLine()) != null) {
            System.out.println(s);
        }
    }
}
