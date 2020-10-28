package io.izzel.nbt;

import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.TagReader;
import org.junit.Test;

import java.io.IOException;

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

    @Test
    public void testIO() throws IOException {
        assertEquals(new NbtReader(new byte[]{0}).toTag(), DUMMY_END_TAG);
        assertArrayEquals(new TagReader(DUMMY_END_TAG).toBinaryNbt(), new byte[]{0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListInteroperability() {
        assertEquals(ListTag.builder().add(DUMMY_END_TAG).build().getOrDefault(0), DUMMY_END_TAG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompoundInteroperability() {
        assertEquals(CompoundTag.builder().add("End", DUMMY_END_TAG).build().getOrDefault("End"), DUMMY_END_TAG);
    }
}