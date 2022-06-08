package org.quiltmc.javacodegen.validation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@FunctionalInterface
public interface FileValidator {
	static final boolean THROWS = false;

	void validate(Path path);

	default void validateFolder(Path path) {
		try (var files = Files.list(path)) {
			files.forEach(file -> {
				if (THROWS) {
					this.validate(file);
				} else {
					try {
						this.validate(file);
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			});
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@FunctionalInterface
	interface OfLines extends FileValidator {
		void validate(Stream<String> path);

		@Override
		default void validate(Path path) {
			try (var lines = Files.lines(path)) {
				this.validate(lines);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (Exception e) {
				throw new RuntimeException("Error validating file: " + path.getFileName() + "\n\t" + e.getMessage(), e);
			}
		}
	}

	@FunctionalInterface
	interface OfText extends FileValidator {
		void validate(String path);

		@Override
		default void validate(Path path) {
			try {
				this.validate(Files.readString(path));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (Exception e) {
				throw new RuntimeException("Error validating file: " + path.getFileName() + "\n\t" + e.getMessage(), e);
			}
		}
	}
}
