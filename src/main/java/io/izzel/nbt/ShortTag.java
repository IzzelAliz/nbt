package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.stream.IntStream;

public final class ShortTag extends Tag {

    private static final ShortTag[] CACHE = IntStream
            .rangeClosed(Byte.MIN_VALUE, Byte.MAX_VALUE)
            .mapToObj(i -> new ShortTag((short) i)).toArray(ShortTag[]::new);

    private final short value;

    private ShortTag(short value) {
        super(TagType.SHORT);
        this.value = value;
    }

    public short getShort() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitShort(this.value);
    }

    @Override
    public String toString() {
        return this.value + "s";
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof ShortTag && this.value == ((ShortTag) o).value;
    }

    @Override
    public int hashCode() {
        return Short.hashCode(this.value);
    }

    public static ShortTag of(short s) {
        if (s >= Byte.MIN_VALUE && s <= Byte.MAX_VALUE) {
            return CACHE[s - Byte.MIN_VALUE];
        }
        return new ShortTag(s);
    }
}
