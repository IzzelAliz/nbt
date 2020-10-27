package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class EndTag extends Tag {

    private static final EndTag INSTANCE = new EndTag();

    private EndTag() {
        super(TagType.END);
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitEnd();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EndTag;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public static EndTag of() {
        return INSTANCE;
    }
}
