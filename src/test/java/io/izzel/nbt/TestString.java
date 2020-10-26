package io.izzel.nbt;

import io.izzel.nbt.util.StringNbtReader;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

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

    @Test
    public void testParser() throws IOException {
        Tag tag = new StringNbtReader(new StringReader(TestIO.DUMMY_STRING_LIST.get(0))).toTag();

        assertEquals(tag.toString(), TestIO.DUMMY_STRING_LIST.get(0));
        assertEquals(tag, new StringNbtReader(new StringReader(TestIO.DUMMY_STRING_LIST.get(0))).toTag());
        assertEquals(tag, new StringNbtReader(new StringReader(TestIO.DUMMY_STRING_LIST.get(1))).toTag());
    }
}
