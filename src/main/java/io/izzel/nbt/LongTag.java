package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.stream.IntStream;

public final class LongTag extends Tag<Long> {

    private static final LongTag[] CACHE = IntStream
            .rangeClosed(Byte.MIN_VALUE, Byte.MAX_VALUE)
            .mapToObj(LongTag::new).toArray(LongTag[]::new);

    private final long value;

    private LongTag(long value) {
        super(TagType.LONG);
        this.value = value;
    }

    public long getLong() {
        return this.value;
    }

    @Override
    public Long getValue() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitLong(this.value);
    }

    @Override
    public String toString() {
        return value + "l";
    }

    public static LongTag of(long l) {
        if (l >= Byte.MIN_VALUE && l <= Byte.MAX_VALUE) {
            return CACHE[(int) l - Byte.MIN_VALUE];
        }
        return new LongTag(l);
    }
}
