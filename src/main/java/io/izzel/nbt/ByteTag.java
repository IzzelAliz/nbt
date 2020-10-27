package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.stream.IntStream;

public final class ByteTag extends Tag {

    private static final ByteTag[] CACHE = IntStream
            .rangeClosed(Byte.MIN_VALUE, Byte.MAX_VALUE)
            .mapToObj(i -> new ByteTag((byte) i)).toArray(ByteTag[]::new);

    private final byte value;

    private ByteTag(byte value) {
        super(TagType.BYTE);
        this.value = value;
    }

    public byte getByte() {
        return this.value;
    }

    public boolean getBoolean() {
        return this.value != 0;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitByte(this.value);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof ByteTag && this.value == ((ByteTag) o).value;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(this.value);
    }

    public static ByteTag of(boolean b) {
        return CACHE[(b ? 1 : 0) - Byte.MIN_VALUE];
    }

    public static ByteTag of(byte b) {
        return CACHE[(int) b - Byte.MIN_VALUE];
    }
}
