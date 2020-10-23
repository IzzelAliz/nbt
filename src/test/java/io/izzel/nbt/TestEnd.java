package io.izzel.nbt;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestEnd {
    public static final EndTag DUMMY_END_TAG = EndTag.of();

    @Test
    public void testEquals() {
        assertEquals(DUMMY_END_TAG, EndTag.of());
    }

    @Test
    public void testHashCode() {
        assertEquals(DUMMY_END_TAG.hashCode(), 0);
    }

    @Test
    public void testCache() {
        assertSame(DUMMY_END_TAG, EndTag.of());
    }

    @Test
    public void testDefaultElement() {
        assertEquals(ListTag.builder().build().getOrDefault(0), DUMMY_END_TAG);
        assertEquals(CompoundTag.builder().build().getOrDefault("Unknown"), DUMMY_END_TAG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompoundInteroperability() {
        assertEquals(CompoundTag.builder().add("End", DUMMY_END_TAG).build().getOrDefault("End"), DUMMY_END_TAG);
    }
}