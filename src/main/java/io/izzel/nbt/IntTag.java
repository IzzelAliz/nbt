package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.stream.IntStream;

public final class IntTag extends Tag {

    private static final IntTag[] CACHE = IntStream
            .rangeClosed(Byte.MIN_VALUE, Byte.MAX_VALUE)
            .mapToObj(IntTag::new).toArray(IntTag[]::new);

    private final int value;

    private IntTag(int value) {
        super(TagType.INT);
        this.value = value;
    }

    public int getInt() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitInt(this.value);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof IntTag && this.value == ((IntTag) o).value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    public static IntTag of(int i) {
        if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
            return CACHE[i - Byte.MIN_VALUE];
        }
        return new IntTag(i);
    }
}
