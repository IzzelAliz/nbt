package io.izzel.nbt.util;

import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedNbtWriter extends TagValueVisitor implements Flushable, Closeable {
    private final NbtWriter data;

    public CompressedNbtWriter(OutputStream stream) throws IOException {
        super(new NbtWriter(new GZIPOutputStream(stream)));
        this.data = (NbtWriter) super.visitor;
    }

    public CompressedNbtWriter(OutputStream stream, String name) throws IOException {
        super(new NbtWriter(new GZIPOutputStream(stream), name));
        this.data = (NbtWriter) super.visitor;
    }

    @Override
    public void close() throws IOException {
        this.data.close();
    }

    @Override
    public void flush() throws IOException {
        this.data.flush();
    }
}
