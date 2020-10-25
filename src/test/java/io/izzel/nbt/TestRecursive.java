package io.izzel.nbt;

import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.NbtWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class TestRecursive {
    public static CompoundTag DUMMY_RECURSIVE_TAG;

    static {
        ListTag tag = ListTag.empty();

        for (int i = 0; i < 0x7FF7; ++i) {
            tag = ListTag.builder(TagType.LIST).add(tag).build();
        }

        DUMMY_RECURSIVE_TAG = CompoundTag.builder().add("DeepRecursiveList", tag).build();
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
    public void testDeepRecursive() throws IOException {
        try (NbtWriter writer = new NbtWriter(Files.newOutputStream(this.tmpFile), true)) {
            DUMMY_RECURSIVE_TAG.accept(writer);
        }

        try (NbtReader reader = new NbtReader(Files.newInputStream(this.tmpFile), true)) {
            CompoundTag newCompoundTag = reader.toCompoundTag();
            Tag tag = newCompoundTag.getOrDefault("DeepRecursiveList");
            for (int i = 0; i < 0x7FF7; ++i) {
                assertEquals(tag.getType(), TagType.LIST);
                tag = ((ListTag) tag).getOrDefault(0);
            }
            assertEquals(tag.getType(), TagType.LIST);
            assertEquals(((ListTag) tag).size(), 0);
        }
    }
}
