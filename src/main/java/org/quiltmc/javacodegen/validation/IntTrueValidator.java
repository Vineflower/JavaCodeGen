package org.quiltmc.javacodegen.validation;

import java.util.regex.Pattern;

public enum IntTrueValidator implements FileValidator.OfText {
	INSTANCE;

	static final Pattern INT_TRUE_PATTERN = Pattern.compile("int \\w+ = true;");

	@Override
	public void validate(String text) {
		if(INT_TRUE_PATTERN.matcher(text).find()) {
			throw new IllegalStateException("int true was found");
		}
	}
}
