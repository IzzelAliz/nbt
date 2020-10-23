package io.izzel.nbt;

import io.izzel.nbt.util.ImmutableBytes;
import io.izzel.nbt.util.ImmutableInts;
import io.izzel.nbt.util.ImmutableLongs;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.junit.Assert.*;

public class TestArray {
    public static final int[] DUMMY_INTS = {1, 2, 3};
    public static final byte[] DUMMY_MUTABLE_BYTES = {1, 2, 3};
    public static final long[] DUMMY_MUTABLE_LONGS = {1, 2, 3};
    public static final IntBuffer DUMMY_BUFFERED_INTS = IntBuffer.wrap(new int[]{4, 5, 6});
    public static final ByteBuffer DUMMY_BUFFERED_BYTES = ByteBuffer.wrap(new byte[]{4, 5, 6});
    public static final LongBuffer DUMMY_BUFFERED_LONGS = LongBuffer.wrap(new long[]{4, 5, 6});
    public static final ImmutableInts DUMMY_IMMUTABLE_INTS = ImmutableInts.builder().add(new int[]{7, 8, 9}).build();
    public static final ImmutableBytes DUMMY_IMMUTABLE_BYTES = ImmutableBytes.builder().add(new byte[]{7, 8, 9}).build();
    public static final ImmutableLongs DUMMY_IMMUTABLE_LONGS = ImmutableLongs.builder().add(new long[]{7, 8, 9}).build();

    public static final IntArrayTag DUMMY_INTS_TAG = IntArrayTag.of(DUMMY_IMMUTABLE_INTS);
    public static final ByteArrayTag DUMMY_BYTES_TAG = ByteArrayTag.of(DUMMY_IMMUTABLE_BYTES);
    public static final LongArrayTag DUMMY_LONGS_TAG = LongArrayTag.of(DUMMY_IMMUTABLE_LONGS);

    @Test
    public void testValue() {
        assertEquals(DUMMY_INTS_TAG.getInts(), DUMMY_IMMUTABLE_INTS);
        assertEquals(DUMMY_BYTES_TAG.getBytes(), DUMMY_IMMUTABLE_BYTES);
        assertEquals(DUMMY_LONGS_TAG.getLongs(), DUMMY_IMMUTABLE_LONGS);
    }

    @Test
    public void testEquals() {
        assertEquals(DUMMY_INTS_TAG, IntArrayTag.of(DUMMY_IMMUTABLE_INTS));
        assertEquals(DUMMY_BYTES_TAG, ByteArrayTag.of(DUMMY_IMMUTABLE_BYTES));
        assertEquals(DUMMY_LONGS_TAG, LongArrayTag.of(DUMMY_IMMUTABLE_LONGS));
    }

    @Test
    public void testHashCode() {
        assertEquals(DUMMY_INTS_TAG.hashCode(), DUMMY_IMMUTABLE_INTS.hashCode());
        assertEquals(DUMMY_BYTES_TAG.hashCode(), DUMMY_IMMUTABLE_BYTES.hashCode());
        assertEquals(DUMMY_LONGS_TAG.hashCode(), DUMMY_IMMUTABLE_LONGS.hashCode());
    }
}
