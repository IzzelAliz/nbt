package io.izzel.nbt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestToString {
    @Test
    public void testEnd() {
        assertEquals(TestEnd.DUMMY_END_TAG.toString(), "");
    }

    @Test
    public void testNumber() {
        assertEquals(TestNumber.DUMMY_INT_TAG.toString(), "42");
        assertEquals(TestNumber.DUMMY_LONG_TAG.toString(), "42l");
        assertEquals(TestNumber.DUMMY_BYTE_TAG.toString(), "42b");
        assertEquals(TestNumber.DUMMY_SHORT_TAG.toString(), "42s");
        assertEquals(TestNumber.DUMMY_FLOAT_TAG.toString(), "42.0f");
        assertEquals(TestNumber.DUMMY_DOUBLE_TAG.toString(), "42.0d");
    }

    @Test
    public void testArray() {
        assertEquals(TestArray.DUMMY_INTS_TAG.toString(), "[I;7,8,9]");
        assertEquals(TestArray.DUMMY_BYTES_TAG.toString(), "[B;7,8,9]");
        assertEquals(TestArray.DUMMY_LONGS_TAG.toString(), "[L;7,8,9]");
    }

    @Test
    public void testListAndCompound() {
        assertEquals(TestChildren.DUMMY_LIST_TAG.toString(), "[42]");
        assertEquals(TestChildren.DUMMY_COMPOUND_TAG.toString(), "{Unknown:42}");
    }
}
