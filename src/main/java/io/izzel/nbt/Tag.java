package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public abstract class Tag {

    private final TagType type;

    protected Tag(TagType type) {
        this.type = type;
    }

    public final TagType getType() {
        return this.type;
    }

    public abstract void accept(TagValueVisitor visitor);
}
