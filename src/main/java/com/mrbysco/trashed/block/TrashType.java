package com.mrbysco.trashed.block;

import net.minecraft.util.StringRepresentable;

public enum TrashType implements StringRepresentable {
	SINGLE("single", 0),
	BOTTOM("bottom", 2),
	TOP("top", 1);

	public static final TrashType[] VALUES = values();
	private final String name;
	private final int opposite;

	TrashType(String name, int id) {
		this.name = name;
		this.opposite = id;
	}

	public String getSerializedName() {
		return this.name;
	}

	public TrashType opposite() {
		return VALUES[this.opposite];
	}
}
