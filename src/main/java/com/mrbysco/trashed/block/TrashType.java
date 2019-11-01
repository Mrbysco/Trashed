package com.mrbysco.trashed.block;

import net.minecraft.util.IStringSerializable;

public enum TrashType implements IStringSerializable {
    SINGLE("single", 0),
    BOTTOM("bottom", 2),
    TOP("top", 1);

    public static final TrashType[] VALUES = values();
    private final String name;
    private final int opposite;

    private TrashType(String name, int id) {
        this.name = name;
        this.opposite = id;
    }

    public String getName() {
        return this.name;
    }

    public TrashType opposite() {
        return VALUES[this.opposite];
    }
}
