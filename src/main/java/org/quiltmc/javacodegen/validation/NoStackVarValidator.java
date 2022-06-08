package org.quiltmc.javacodegen.validation;

import java.util.regex.Pattern;

public enum NoStackVarValidator implements FileValidator.OfText{
	INSTANCE;

	static Pattern STACK_VAR_PATTERN = Pattern.compile("var100\\d\\d");
	@Override
	public void validate(String text) {
		if(STACK_VAR_PATTERN.matcher(text).find()) {
			throw new IllegalStateException("A stack var was found");
		}
	}
}
