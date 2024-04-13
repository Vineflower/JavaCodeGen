package org.vineflower.javacodegen;

import org.vineflower.javacodegen.statement.Statement;
import org.vineflower.javacodegen.validation.IntTrueValidator;
import org.vineflower.javacodegen.validation.NoLabelValidator;
import org.vineflower.javacodegen.validation.NoStackVarValidator;
import org.vineflower.javacodegen.vars.VarsEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public final class CompilingCreator {
	private static final String VF_JAR = System.getProperty("VF_JAR", null);

	public static void main(String[] args) throws Exception {
		int count = 500;

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
		// Like this for easy debugging
		long realSeed = seedGenerator.nextLong();
		System.out.println("Global seed: " + realSeed);
		seedGenerator = new Random(realSeed);
		List<String> paths = new ArrayList<>();

		final boolean createLabels = false;
		final boolean createInfiniteLoops = true;

		for (int i = 0; i < count; i++) {
			try {
				VarsEntry.resetId();

				Creator.Params.Builder builder = new Creator.Params.Builder();
				builder.createLabels(createLabels);
				builder.createInfiniteLoops(createInfiniteLoops);

				final long seed = seedGenerator.nextLong() + 3;
				Statement statement = (new Creator(new JavaVersion(17, true), seed)).method(builder.build(8));

				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("import java.util.*;\n");
				stringBuilder.append("import java.lang.annotation.*;\n");

				stringBuilder.append("class FuzzedClass_").append(i).append(" {\n");
				stringBuilder.append("// seed: ").append(seed).append("\n");
				stringBuilder.append("public void test()").append(" {\n");
				statement.javaLike(stringBuilder, "");
				stringBuilder.append("}\n");
				stringBuilder.append("}\n");

				Path resolved = fuzzed.resolve("FuzzedClass_" + i + ".java");
				paths.add(resolved.toString());
				Files.write(resolved, stringBuilder.toString().getBytes());
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

		if (!createLabels) {
			NoLabelValidator.INSTANCE.validateFolder(fuzzed);
		}

		Files.write(path.resolve("paths.txt"), paths);

		Process exec = Runtime.getRuntime().exec(
			"javac -encoding utf-8 -g -d " + compiled.toAbsolutePath() + " @" + path.resolve("paths.txt"));

		BufferedReader serr = new BufferedReader(new InputStreamReader(exec.getErrorStream()));

		String s;

		while ((s = serr.readLine()) != null) {
			System.out.println(s);
		}

		exec.waitFor();

//		System.exit(0);
		if (VF_JAR != null) {

			exec = Runtime.getRuntime()
				.exec("java -jar " + VF_JAR + " -jrt=1 " + compiled.toAbsolutePath() + " " + decompiled.toAbsolutePath());

			serr = new BufferedReader(new InputStreamReader(exec.getInputStream()));

			while ((s = serr.readLine()) != null) {
				if (s.startsWith("INFO:")) {
					System.out.println(s);
				} else {
					System.err.println(s);
				}
			}

			NoStackVarValidator.INSTANCE.validateFolder(decompiled);
			IntTrueValidator.INSTANCE.validateFolder(decompiled);

			if (!createLabels) {
				NoLabelValidator.INSTANCE.validateFolder(decompiled);
			}

			paths = paths.stream().map(str -> str.replace("fuzzed", "decompiled")).toList();
			Files.write(path.resolve("paths.txt"), paths);

			exec = Runtime.getRuntime().exec(
				"javac -encoding utf-8 -g -d " + recompiled.toAbsolutePath() + " @" + path.resolve("paths.txt"));

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

		try (Stream<Path> stream = Files.list(subFolder)) {
			stream.filter(Files::isRegularFile).forEach(path1 -> {
				try {
					Files.delete(path1);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
	}
}
