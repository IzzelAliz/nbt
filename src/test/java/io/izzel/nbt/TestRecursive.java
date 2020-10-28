package io.izzel.nbt;

import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.StringNbtReader;
import io.izzel.nbt.util.TagReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class TestRecursive {
    public static final CompoundTag DUMMY_RECURSIVE_LIST_TAG;
    public static final CompoundTag DUMMY_RECURSIVE_COMPOUND_TAG;
    public static final CompoundTag DUMMY_RECURSIVE_COMPOUND_LIST_TAG;

    static {
        ListTag tag = ListTag.empty();

        for (int i = 0; i < 0x7FF7; ++i) {
            tag = ListTag.builder(TagType.LIST).add(tag).build();
        }

        DUMMY_RECURSIVE_LIST_TAG = CompoundTag.builder().add("DeepRecursiveList", tag).build();
    }

    static {
        CompoundTag tag = CompoundTag.empty();

        for (int i = 0; i < 0x7FF7; ++i) {
            tag = CompoundTag.builder().add("", tag).build();
        }

        DUMMY_RECURSIVE_COMPOUND_TAG = CompoundTag.builder().add("DeepRecursiveCompound", tag).build();
    }

    static {
        ListTag tag = ListTag.empty();

        for (int i = 0; i < 0x7FF7; ++i) {
            tag = ListTag.builder(TagType.COMPOUND).add(CompoundTag.builder().add("", tag).build()).build();
        }

        DUMMY_RECURSIVE_COMPOUND_LIST_TAG = CompoundTag.builder().add("DeepRecursiveCompoundList", tag).build();
    }

    private Path tmpFile;

    @Before
    public void beforeTest() throws IOException {
        this.tmpFile = Files.createTempFile("io.izzel.nbt.", ".tmp");
    }

    @After
    public void afterTest() throws IOException {
        Files.delete(this.tmpFile);
    }

    @Test
    public void testDeepRecursiveList() throws IOException {
        Tag subtag = DUMMY_RECURSIVE_LIST_TAG.getOrDefault("DeepRecursiveList");

        String substring = subtag.toString();
        assertEquals(substring.length(), 2 * 0x7FF7 + 2);
        assertEquals(new StringNbtReader(substring).toTag(), subtag);

        for (int i = 0; i <= 0x7FF7; ++i) {
            assertEquals(substring.charAt(i), '[');
            assertEquals(substring.charAt(i + 0x7FF7 + 1), ']');
        }

        new TagReader(DUMMY_RECURSIVE_LIST_TAG).toGzippedBinaryFile(this.tmpFile);
        Tag tag = new NbtReader(this.tmpFile, true).toCompoundTag().getOrDefault("DeepRecursiveList");

        assertEquals(tag, subtag);
        for (int i = 0; i < 0x7FF7; ++i) {
            assertEquals(tag.getType(), TagType.LIST);
            tag = ((ListTag) tag).getOrDefault(0);
        }
        assertEquals(tag.getType(), TagType.LIST);
        assertEquals(((ListTag) tag).size(), 0);
    }

    @Test
    public void testDeepRecursiveCompound() throws IOException {
        Tag subtag = DUMMY_RECURSIVE_COMPOUND_TAG.getOrDefault("DeepRecursiveCompound");

        String substring = subtag.toString();
        assertEquals(substring.length(), 5 * 0x7FF7 + 2);
        assertEquals(new StringNbtReader(substring).toTag(), subtag);

        for (int i = 0; i <= 0x7FF7; ++i) {
            assertEquals(substring.charAt(i * 4), '{');
            assertEquals(substring.charAt(i + 0x7FF7 * 4 + 1), '}');
            if (i > 0) {
                assertEquals(substring.charAt(i * 4 - 1), ':');
                assertEquals(substring.charAt(i * 4 - 2), '"');
                assertEquals(substring.charAt(i * 4 - 3), '"');
            }
        }

        new TagReader(DUMMY_RECURSIVE_COMPOUND_TAG).toGzippedBinaryFile(this.tmpFile);
        Tag tag = new NbtReader(this.tmpFile, true).toCompoundTag().getOrDefault("DeepRecursiveCompound");

        assertEquals(tag, subtag);
        for (int i = 0; i < 0x7FF7; ++i) {
            assertEquals(tag.getType(), TagType.COMPOUND);
            tag = ((CompoundTag) tag).getOrDefault("");
        }
        assertEquals(tag.getType(), TagType.COMPOUND);
        assertEquals(((CompoundTag) tag).names().size(), 0);
    }

    @Test
    public void testDeepRecursiveCompoundList() throws IOException {
        Tag subtag = DUMMY_RECURSIVE_COMPOUND_LIST_TAG.getOrDefault("DeepRecursiveCompoundList");

        String substring = subtag.toString();
        assertEquals(substring.length(), 7 * 0x7FF7 + 2);
        assertEquals(new StringNbtReader(substring).toTag(), subtag);

        for (int i = 0; i <= 0x7FF7; ++i) {
            assertEquals(substring.charAt(i * 5), '[');
            assertEquals(substring.charAt(i * 2 + 0x7FF7 * 5 + 1), ']');
            if (i > 0) {
                assertEquals(substring.charAt(i * 5 - 1), ':');
                assertEquals(substring.charAt(i * 5 - 2), '"');
                assertEquals(substring.charAt(i * 5 - 3), '"');
                assertEquals(substring.charAt(i * 5 - 4), '{');
                assertEquals(substring.charAt(i * 2 + 0x7FF7 * 5), '}');
            }
        }

        new TagReader(DUMMY_RECURSIVE_COMPOUND_LIST_TAG).toGzippedBinaryFile(this.tmpFile);
        Tag tag = new NbtReader(this.tmpFile, true).toCompoundTag().getOrDefault("DeepRecursiveCompoundList");

        assertEquals(tag, subtag);
        for (int i = 0; i < 0x7FF7; ++i) {
            assertEquals(tag.getType(), TagType.LIST);
            tag = ((ListTag) tag).getOrDefault(0);
            assertEquals(tag.getType(), TagType.COMPOUND);
            tag = ((CompoundTag) tag).getOrDefault("");
        }
        assertEquals(tag.getType(), TagType.LIST);
        assertEquals(((ListTag) tag).size(), 0);
    }
}
