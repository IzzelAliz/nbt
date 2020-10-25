package io.izzel.nbt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestString {
    public static final String DUMMY_STRING = "42";

    @Test
    public void testValue() {
        assertEquals(StringTag.of("").getString(), "");
        assertEquals(StringTag.of(DUMMY_STRING).getString(), DUMMY_STRING);
    }

    @Test
    public void testEscape() {
        assertEquals(StringTag.of("").toString(), "\"\"");
        assertEquals(StringTag.of("'").toString(), "\"'\"");
        assertEquals(StringTag.of("\"").toString(), "'\"'");
        assertEquals(StringTag.of("\\").toString(), "\"\\\\\"");
        assertEquals(StringTag.of("\"'").toString(), "'\"\\''");
        assertEquals(StringTag.of("'\"").toString(), "\"'\\\"\"");
        assertEquals(StringTag.of("\\\\").toString(), "\"\\\\\\\\\"");
        assertEquals(StringTag.of(DUMMY_STRING).toString(), "\"42\"");
    }

    @Test
    public void testEquals() {
        assertEquals(StringTag.of(""), StringTag.of(""));
        assertEquals(StringTag.of(DUMMY_STRING), StringTag.of(DUMMY_STRING));
    }

    @Test
    public void testHashCode() {
        assertEquals(StringTag.of("").hashCode(), "".hashCode());
        assertEquals(StringTag.of(DUMMY_STRING).hashCode(), DUMMY_STRING.hashCode());
    }
}