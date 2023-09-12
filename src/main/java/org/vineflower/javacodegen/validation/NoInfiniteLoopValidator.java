package org.vineflower.javacodegen.validation;

import java.util.regex.Pattern;

public enum NoInfiniteLoopValidator implements FileValidator.OfText {
	INSTANCE;

	static final Pattern WHILETRUE_PATTERN = Pattern.compile("while\\s?\\(true\\)");

	@Override
	public void validate(String text) {
		if(WHILETRUE_PATTERN.matcher(text).find()) {
			throw new IllegalStateException("An infinite loop was found");
		}
	}
}
