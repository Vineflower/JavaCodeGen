package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.statement.Statement;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public final class CompilingCreator {
	private static final String QF_JAR = System.getProperty("QF_JAR", null);

	public static void main(String[] args) throws Exception {
		int count = 50;

		Path path = deleteDirs();

		final Path fuzzed = path.resolve("fuzzed");
		final Path compiled = path.resolve("compiled");
		final Path decompiled = path.resolve("decompiled");
		final Path recompiled = path.resolve("recompiled");

		Files.createDirectories(fuzzed);
		Files.createDirectories(compiled);
		Files.createDirectories(decompiled);
		Files.createDirectories(recompiled);

		int failedToGenerate = 0;
		Random seedGenerator = new Random();

		for (int i = 0; i < count; i++) {
			try {
				VarsEntry.resetId();

				final long seed = seedGenerator.nextLong() + 3;
				Statement statement = (new Creator(seed)).method(20);

				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("import java.util.*;\n");

				stringBuilder.append("class FuzzedClass_").append(i).append(" {\n");
				stringBuilder.append("// seed: ").append(seed).append("\n");
				stringBuilder.append("public void test()").append(" {\n");
				statement.javaLike(stringBuilder, "");
				stringBuilder.append("}\n");
				stringBuilder.append("}\n");

				Files.write(fuzzed.resolve("FuzzedClass_" + i + ".java"), stringBuilder.toString().getBytes());
			} catch (Throwable t) {
				failedToGenerate++;
				System.out.println("Failed to create class " + i);
				t.printStackTrace();
			}
		}

		if (failedToGenerate > 0) {
			System.out.println("Failed to generate " + failedToGenerate + " classes");
			System.exit(1);
		}

		Process exec = Runtime.getRuntime().exec(
			"javac -encoding utf-8 -g " + fuzzed.toAbsolutePath() + "\\*.java -d " + compiled.toAbsolutePath());

		BufferedReader serr = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

		String s;

		while ((s = serr.readLine()) != null) {
			System.out.println(s);
		}

		System.exit(0);
		if (QF_JAR != null) {

			exec = Runtime.getRuntime()
				.exec("java -jar " + QF_JAR + " -jrt=1 " + compiled.toAbsolutePath() + " " + decompiled.toAbsolutePath());

			serr = new BufferedReader(new InputStreamReader(exec.getInputStream()));

			while ((s = serr.readLine()) != null) {
				if (s.startsWith("INFO:")) {
					System.out.println(s);
				} else {
					System.err.println(s);
				}
			}

			exec = Runtime.getRuntime()
				.exec("javac -encoding utf-8 -g " + decompiled.toAbsolutePath() + "\\*.java -d " + recompiled.toAbsolutePath());

			serr = new BufferedReader(new InputStreamReader(exec.getErrorStream()));


			while ((s = serr.readLine()) != null) {
				System.out.println(s);
			}
		}
	}

	private static Path deleteDirs() throws IOException {
		final Path path = Paths.get(".").resolve("output");
		Files.createDirectories(path);
		removeSubFolder(path, "fuzzed");
		removeSubFolder(path, "compiled");
		removeSubFolder(path, "decompiled");
		removeSubFolder(path, "recompiled");
		return path;
	}

	private static void removeSubFolder(Path path, String fuzzed) throws IOException {
		final Path subFolder = path.resolve(fuzzed);
		if (!Files.exists(subFolder)) {
			return;
		}
		if (!Files.isDirectory(subFolder)) {
			Files.delete(subFolder);
			return;
		}
		Files.list(subFolder).filter(Files::isRegularFile).forEach(path1 -> {
			try {
				Files.delete(path1);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
