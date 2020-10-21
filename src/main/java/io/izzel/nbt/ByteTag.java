package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.stream.IntStream;

public final class ByteTag extends Tag<Byte> {

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
    public Byte getValue() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitByte(this.value);
    }

    @Override
    public String toString() {
        return value + "b";
    }

    public static ByteTag of(boolean b) {
        return CACHE[(b ? 1 : 0) - Byte.MIN_VALUE];
    }

    public static ByteTag of(byte b) {
        return CACHE[(int) b - Byte.MIN_VALUE];
    }
}
