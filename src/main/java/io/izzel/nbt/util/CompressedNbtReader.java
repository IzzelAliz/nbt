package io.izzel.nbt.util;

import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.Tag;
import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class CompressedNbtReader implements Closeable {

    private final NbtReader data;

    public CompressedNbtReader(InputStream stream) throws IOException {
        this.data = new NbtReader(new GZIPInputStream(stream));
    }

    public CompressedNbtReader(Path path) throws IOException {
        this(Files.newInputStream(path));
    }

    public CompressedNbtReader(byte[] bytes) throws IOException {
        this(new ByteArrayInputStream(bytes));
    }

    public void accept(TagValueVisitor visitor) throws IOException {
        this.data.accept(visitor);
    }

    @Override
    public void close() throws IOException {
        this.data.close();
    }

    public Tag toTag() throws IOException {
        return this.data.toTag();
    }

    public CompoundTag toCompoundTag() throws IOException {
        Tag tag = this.toTag();
        if (tag.getType() != TagType.COMPOUND) {
            throw new IOException("Expect " + TagType.COMPOUND.getTagName() + " but got " + tag.getType());
        }
        return (CompoundTag) tag;
    }

    public String toStringNbt() throws IOException {
        return this.data.toStringNbt();
    }

    public String getName() {
        return this.data.getName();
    }
}
