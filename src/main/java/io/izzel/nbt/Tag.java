package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public abstract class Tag<T> {

    private final TagType type;

    public Tag(TagType type) {
        this.type = type;
    }

    public final TagType getType() {
        return type;
    }

    public abstract T getValue();

    public abstract Tag<T> copy();

    public abstract void accept(TagValueVisitor visitor);
}
