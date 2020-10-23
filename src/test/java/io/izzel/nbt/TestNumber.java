package io.izzel.nbt;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestNumber {
    public static final int DUMMY_INT = 42;
    public static final long DUMMY_LONG = 42L;
    public static final float DUMMY_FLOAT = 42.0F;
    public static final double DUMMY_DOUBLE = 42.0;
    public static final byte DUMMY_BYTE = (byte) 42;
    public static final short DUMMY_SHORT = (short) 42;

    public static final ByteTag DUMMY_TRUE_TAG = ByteTag.of(true);
    public static final ByteTag DUMMY_FALSE_TAG = ByteTag.of(false);
    public static final IntTag DUMMY_INT_TAG = IntTag.of(DUMMY_INT);
    public static final LongTag DUMMY_LONG_TAG = LongTag.of(DUMMY_LONG);
    public static final ByteTag DUMMY_BYTE_TAG = ByteTag.of(DUMMY_BYTE);
    public static final ShortTag DUMMY_SHORT_TAG = ShortTag.of(DUMMY_SHORT);
    public static final FloatTag DUMMY_FLOAT_TAG = FloatTag.of(DUMMY_FLOAT);
    public static final DoubleTag DUMMY_DOUBLE_TAG = DoubleTag.of(DUMMY_DOUBLE);

    @Test
    public void testValue() {
        assertEquals(DUMMY_INT_TAG.getInt(), DUMMY_INT);
        assertEquals(DUMMY_LONG_TAG.getLong(), DUMMY_LONG);
        assertEquals(DUMMY_BYTE_TAG.getByte(), DUMMY_BYTE);
        assertEquals(DUMMY_SHORT_TAG.getShort(), DUMMY_SHORT);
        assertEquals(DUMMY_FLOAT_TAG.getFloat(), DUMMY_FLOAT, 0.0F);
        assertEquals(DUMMY_DOUBLE_TAG.getDouble(), DUMMY_DOUBLE, 0.0);
    }

    @Test
    public void testEquals() {
        assertEquals(DUMMY_INT_TAG, IntTag.of(DUMMY_INT));
        assertEquals(DUMMY_LONG_TAG, LongTag.of(DUMMY_LONG));
        assertEquals(DUMMY_BYTE_TAG, ByteTag.of(DUMMY_BYTE));
        assertEquals(DUMMY_SHORT_TAG, ShortTag.of(DUMMY_SHORT));
        assertEquals(DUMMY_FLOAT_TAG, FloatTag.of(DUMMY_FLOAT));
        assertEquals(DUMMY_DOUBLE_TAG, DoubleTag.of(DUMMY_DOUBLE));
    }

    @Test
    public void testHashCode() {
        assertEquals(DUMMY_INT_TAG.hashCode(), Integer.hashCode(DUMMY_INT));
        assertEquals(DUMMY_LONG_TAG.hashCode(), Long.hashCode(DUMMY_LONG));
        assertEquals(DUMMY_BYTE_TAG.hashCode(), Byte.hashCode(DUMMY_BYTE));
        assertEquals(DUMMY_SHORT_TAG.hashCode(), Short.hashCode(DUMMY_SHORT));
        assertEquals(DUMMY_FLOAT_TAG.hashCode(), Float.hashCode(DUMMY_FLOAT));
        assertEquals(DUMMY_DOUBLE_TAG.hashCode(), Double.hashCode(DUMMY_DOUBLE));
    }

    @Test
    public void testBoolean() {
        assertTrue(DUMMY_TRUE_TAG.getBoolean());
        assertFalse(DUMMY_FALSE_TAG.getBoolean());
        assertTrue(ByteTag.of((byte) 1).getBoolean());
        assertFalse(ByteTag.of((byte) 0).getBoolean());
        assertEquals(DUMMY_TRUE_TAG.getByte(), (byte) 1);
        assertEquals(DUMMY_FALSE_TAG.getByte(), (byte) 0);
    }

    @Test
    public void testFloatingComparison() {
        assertNotEquals(FloatTag.of(0.0F), FloatTag.of(-0.0F));
        assertNotEquals(DoubleTag.of(0.0D), DoubleTag.of(-0.0D));
        assertEquals(FloatTag.of(Float.NaN), FloatTag.of(Float.NaN));
        assertEquals(DoubleTag.of(Double.NaN), DoubleTag.of(Double.NaN));
        assertEquals(DUMMY_FLOAT_TAG, FloatTag.of(FloatTag.of(21.0F).getFloat() * 2.0F));
        assertEquals(DUMMY_DOUBLE_TAG, DoubleTag.of(DoubleTag.of(21.0).getDouble() * 2.0));
    }

    @Test
    public void testCache() {
        assertSame(DUMMY_INT_TAG, DUMMY_INT_TAG);
        assertSame(DUMMY_LONG_TAG, DUMMY_LONG_TAG);
        assertSame(DUMMY_BYTE_TAG, DUMMY_BYTE_TAG);
        assertSame(DUMMY_SHORT_TAG, DUMMY_SHORT_TAG);

        assertNotSame(IntTag.of(DUMMY_INT * DUMMY_INT), IntTag.of(DUMMY_INT * DUMMY_INT));
        assertNotSame(IntTag.of(DUMMY_INT * -DUMMY_INT), IntTag.of(DUMMY_INT * -DUMMY_INT));
        assertNotSame(LongTag.of(DUMMY_LONG * DUMMY_LONG), LongTag.of(DUMMY_LONG * DUMMY_LONG));
        assertNotSame(LongTag.of(DUMMY_LONG * -DUMMY_LONG), LongTag.of(DUMMY_LONG * -DUMMY_LONG));
        assertNotSame(ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)), ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)));
        assertNotSame(ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)), ShortTag.of((short) (DUMMY_INT * -DUMMY_INT)));
    }

    @Test
    public void testDefaultElement() {
        assertEquals(ListTag.builder().build().getNumberOrDefault(0), 0);
        assertEquals(CompoundTag.builder().build().getNumberOrDefault("Unknown"), 0);
    }
}
