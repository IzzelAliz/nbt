package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tag && ((Tag<?>) o).type == type) {
            return Objects.equals(((Tag<?>) o).getValue(), getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }
}
