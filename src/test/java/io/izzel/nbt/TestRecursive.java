package io.izzel.nbt;

import io.izzel.nbt.util.NbtReader;
import io.izzel.nbt.util.NbtWriter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@Ignore("not correctly implemented yet")
public class TestRecursive {
    @Test
    public void testDeepRecursive() throws IOException {
        ListTag tag = ListTag.empty();

        for (int i = 0; i < 0x7FF7; ++i) {
            tag = ListTag.builder(TagType.LIST).add(tag).build();
        }

        CompoundTag compoundTag = CompoundTag.builder().add("List", tag).build();

        byte[] bytes;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NbtWriter writer = new NbtWriter(stream, true)) {
                compoundTag.accept(writer);
            }
            bytes = stream.toByteArray();
        }

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            try (NbtReader reader = new NbtReader(stream, true)) {
                CompoundTag newCompoundTag = reader.toCompoundTag();
                ListTag newTag = newCompoundTag.getListOrDefault("List");
                assertEquals(newTag, tag);
            }
        }
    }
}
