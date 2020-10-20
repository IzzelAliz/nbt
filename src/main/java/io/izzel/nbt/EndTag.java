package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class EndTag extends Tag<Void> {

    public static final EndTag INSTANCE = new EndTag();

    public EndTag() {
        super(TagType.END);
    }

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitEnd();
    }

    @Override
    public String toString() {
        return "";
    }
}
