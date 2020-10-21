package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class EndTag extends Tag<Void> {

    private static final EndTag INSTANCE = new EndTag();

    private EndTag() {
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

    public static EndTag of() {
        return INSTANCE;
    }
}
