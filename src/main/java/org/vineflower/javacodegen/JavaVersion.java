package org.vineflower.javacodegen;

public record JavaVersion(int version, boolean preview) {

	// TODO: all other version-locked java features

	public boolean hasSwitchExpressions() {
		return version >= 17 && preview;
	}
}
