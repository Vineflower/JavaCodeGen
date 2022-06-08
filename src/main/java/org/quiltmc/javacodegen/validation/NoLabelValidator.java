package org.quiltmc.javacodegen.validation;

import java.util.regex.Pattern;

public enum NoLabelValidator implements FileValidator.OfText {
	INSTANCE;

	static final Pattern LABEL_PATTERN = Pattern.compile("label(\\d+;|\\d+:|_\\d+)");

	@Override
	public void validate(String text) {
		if(LABEL_PATTERN.matcher(text).find()) {
			throw new IllegalStateException("A label was found");
		}
	}
}
