package io.izzel.nbt;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestChildren {
    public static final ListTag DUMMY_LIST_TAG = ListTag.builder().add(TestNumber.DUMMY_INT).build();
    public static final CompoundTag DUMMY_COMPOUND_TAG = CompoundTag.builder().add("Unknown", TestNumber.DUMMY_INT).build();

    @Test
    public void testCount() {
        assertEquals(DUMMY_LIST_TAG.size(), 1);
        assertEquals(DUMMY_COMPOUND_TAG.names().size(), 1);
    }

    @Test
    public void testListHit() {
        CompoundTag tag = TestIO.DUMMY_LARGE_TAG;
        assertTrue(tag.getListOrDefault("ListBoolean").getBooleanOrDefault(0));
        assertEquals(tag.getListOrDefault("ListEnd").getOrDefault(0), TestEnd.DUMMY_END_TAG);
        assertEquals(tag.getListOrDefault("ListList").getListOrDefault(0), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListList").getListOrDefault(0, TagType.INT), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListCompound").getCompoundOrDefault(0), DUMMY_COMPOUND_TAG);
        assertEquals(tag.getListOrDefault("ListInt").getIntOrDefault(0), TestNumber.DUMMY_INT);
        assertEquals(tag.getListOrDefault("ListInt").getNumberOrDefault(0), TestNumber.DUMMY_INT);
        assertEquals(tag.getListOrDefault("ListLong").getLongOrDefault(0), TestNumber.DUMMY_LONG);
        assertEquals(tag.getListOrDefault("ListLong").getNumberOrDefault(0), TestNumber.DUMMY_LONG);
        assertEquals(tag.getListOrDefault("ListByte").getByteOrDefault(0), TestNumber.DUMMY_BYTE);
        assertEquals(tag.getListOrDefault("ListByte").getNumberOrDefault(0), TestNumber.DUMMY_BYTE);
        assertEquals(tag.getListOrDefault("ListShort").getShortOrDefault(0), TestNumber.DUMMY_SHORT);
        assertEquals(tag.getListOrDefault("ListShort").getNumberOrDefault(0), TestNumber.DUMMY_SHORT);
        assertEquals(tag.getListOrDefault("ListFloat").getFloatOrDefault(0), TestNumber.DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getListOrDefault("ListFloat").getNumberOrDefault(0), TestNumber.DUMMY_FLOAT);
        assertEquals(tag.getListOrDefault("ListDouble").getDoubleOrDefault(0), TestNumber.DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getListOrDefault("ListDouble").getNumberOrDefault(0), TestNumber.DUMMY_DOUBLE);
        assertEquals(tag.getListOrDefault("ListString").getStringOrDefault(0), TestString.DUMMY_STRING);
        assertEquals(tag.getListOrDefault("ListInts3").getIntsOrDefault(0), TestArray.DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getListOrDefault("ListBytes3").getBytesOrDefault(0), TestArray.DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getListOrDefault("ListLongs3").getLongsOrDefault(0), TestArray.DUMMY_IMMUTABLE_LONGS);
    }

    @Test
    public void testCompoundHit() {
        CompoundTag tag = TestIO.DUMMY_LARGE_TAG;
        assertTrue(tag.getBooleanOrDefault("Boolean"));
        assertEquals(tag.getOrDefault("End"), TestEnd.DUMMY_END_TAG);
        assertEquals(tag.getListOrDefault("List"), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("List", TagType.INT), DUMMY_LIST_TAG);
        assertEquals(tag.getCompoundOrDefault("Compound"), DUMMY_COMPOUND_TAG);
        assertEquals(tag.getIntOrDefault("Int"), TestNumber.DUMMY_INT);
        assertEquals(tag.getNumberOrDefault("Int"), TestNumber.DUMMY_INT);
        assertEquals(tag.getLongOrDefault("Long"), TestNumber.DUMMY_LONG);
        assertEquals(tag.getNumberOrDefault("Long"), TestNumber.DUMMY_LONG);
        assertEquals(tag.getByteOrDefault("Byte"), TestNumber.DUMMY_BYTE);
        assertEquals(tag.getNumberOrDefault("Byte"), TestNumber.DUMMY_BYTE);
        assertEquals(tag.getShortOrDefault("Short"), TestNumber.DUMMY_SHORT);
        assertEquals(tag.getNumberOrDefault("Short"), TestNumber.DUMMY_SHORT);
        assertEquals(tag.getFloatOrDefault("Float"), TestNumber.DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getNumberOrDefault("Float"), TestNumber.DUMMY_FLOAT);
        assertEquals(tag.getDoubleOrDefault("Double"), TestNumber.DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getNumberOrDefault("Double"), TestNumber.DUMMY_DOUBLE);
        assertEquals(tag.getStringOrDefault("String"), TestString.DUMMY_STRING);
        assertEquals(tag.getBytesOrDefault("Bytes3"), TestArray.DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getIntsOrDefault("Ints3"), TestArray.DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getLongsOrDefault("Longs3"), TestArray.DUMMY_IMMUTABLE_LONGS);
    }

    @Test
    public void testListMiss() {
        CompoundTag tag = TestIO.DUMMY_LARGE_TAG;
        assertTrue(tag.getListOrDefault("ListBoolean").getBoolean(1, true));
        assertEquals(tag.getListOrDefault("ListEnd").get(1, TestEnd.DUMMY_END_TAG), TestEnd.DUMMY_END_TAG);
        assertEquals(tag.getListOrDefault("ListList").getList(1, DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListList").getList(1, TagType.INT, DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getListOrDefault("ListCompound").getCompound(1, DUMMY_COMPOUND_TAG), DUMMY_COMPOUND_TAG);
        assertEquals(tag.getListOrDefault("ListInt").getInt(1, TestNumber.DUMMY_INT), TestNumber.DUMMY_INT);
        assertEquals(tag.getListOrDefault("ListLong").getLong(1, TestNumber.DUMMY_LONG), TestNumber.DUMMY_LONG);
        assertEquals(tag.getListOrDefault("ListByte").getByte(1, TestNumber.DUMMY_BYTE), TestNumber.DUMMY_BYTE);
        assertEquals(tag.getListOrDefault("ListShort").getShort(1, TestNumber.DUMMY_SHORT), TestNumber.DUMMY_SHORT);
        assertEquals(tag.getListOrDefault("ListFloat").getFloat(1, TestNumber.DUMMY_FLOAT), TestNumber.DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getListOrDefault("ListDouble").getDouble(1, TestNumber.DUMMY_DOUBLE), TestNumber.DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getListOrDefault("ListString").getString(1, TestString.DUMMY_STRING), TestString.DUMMY_STRING);
        assertEquals(tag.getListOrDefault("ListInts3").getInts(1, TestArray.DUMMY_IMMUTABLE_INTS), TestArray.DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getListOrDefault("ListBytes3").getBytes(1, TestArray.DUMMY_IMMUTABLE_BYTES), TestArray.DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getListOrDefault("ListLongs3").getLongs(1, TestArray.DUMMY_IMMUTABLE_LONGS), TestArray.DUMMY_IMMUTABLE_LONGS);
    }

    @Test
    public void testCompoundMiss() {
        CompoundTag tag = TestIO.DUMMY_LARGE_TAG;
        assertTrue(tag.getBoolean("Unknown", true));
        assertEquals(tag.get("Unknown", TestEnd.DUMMY_END_TAG), TestEnd.DUMMY_END_TAG);
        assertEquals(tag.getInt("Unknown", TestNumber.DUMMY_INT), TestNumber.DUMMY_INT);
        assertEquals(tag.getLong("Unknown", TestNumber.DUMMY_LONG), TestNumber.DUMMY_LONG);
        assertEquals(tag.getByte("Unknown", TestNumber.DUMMY_BYTE), TestNumber.DUMMY_BYTE);
        assertEquals(tag.getShort("Unknown", TestNumber.DUMMY_SHORT), TestNumber.DUMMY_SHORT);
        assertEquals(tag.getFloat("Unknown", TestNumber.DUMMY_FLOAT), TestNumber.DUMMY_FLOAT, 0.0F);
        assertEquals(tag.getDouble("Unknown", TestNumber.DUMMY_DOUBLE), TestNumber.DUMMY_DOUBLE, 0.0D);
        assertEquals(tag.getString("Unknown", TestString.DUMMY_STRING), TestString.DUMMY_STRING);
        assertEquals(tag.getBytes("Unknown", TestArray.DUMMY_IMMUTABLE_BYTES), TestArray.DUMMY_IMMUTABLE_BYTES);
        assertEquals(tag.getInts("Unknown", TestArray.DUMMY_IMMUTABLE_INTS), TestArray.DUMMY_IMMUTABLE_INTS);
        assertEquals(tag.getLongs("Unknown", TestArray.DUMMY_IMMUTABLE_LONGS), TestArray.DUMMY_IMMUTABLE_LONGS);
        assertEquals(tag.getList("Unknown", DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getList("Unknown", TagType.INT, DUMMY_LIST_TAG), DUMMY_LIST_TAG);
        assertEquals(tag.getCompound("Unknown", DUMMY_COMPOUND_TAG), DUMMY_COMPOUND_TAG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListTypeMismatch() {
        CompoundTag tag = TestIO.DUMMY_LARGE_TAG;
        assertEquals(tag.getListOrDefault("ListBoolean").get(0, TagType.END, TestNumber.DUMMY_TRUE_TAG), TestNumber.DUMMY_TRUE_TAG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompoundTypeMismatch() {
        CompoundTag tag = TestIO.DUMMY_LARGE_TAG;
        assertEquals(tag.get("Boolean", TagType.END, TestNumber.DUMMY_TRUE_TAG), TestNumber.DUMMY_TRUE_TAG);
    }
}
