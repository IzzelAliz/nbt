package io.izzel.nbt.util;

import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class NbtWriter extends TagValueVisitor implements Closeable {

    private final DataOutputStream data;
    private final String name;
    private final boolean tagType;

    public NbtWriter(OutputStream stream) {
        super(null);
        this.name = "";
        this.tagType = true;
        this.data = stream instanceof DataOutputStream ? (DataOutputStream) stream : new DataOutputStream(stream);
    }

    public NbtWriter(OutputStream stream, String name) {
        super(null);
        this.name = name;
        this.tagType = true;
        this.data = stream instanceof DataOutputStream ? (DataOutputStream) stream : new DataOutputStream(stream);
    }

    public NbtWriter(OutputStream stream, boolean gzip) throws IOException {
        this(gzip ? new GZIPOutputStream(stream) : stream);
    }

    public NbtWriter(OutputStream stream, boolean gzip, String name) throws IOException {
        this(gzip ? new GZIPOutputStream(stream) : stream, name);
    }

    @Override
    public void close() throws IOException {
        this.data.close();
    }

    @Override
    public void visitEnd() {
        try {
            if (tagType) data.write(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitByte(byte b) {
        try {
            if (tagType) data.write(1);
            this.maybeWriteName();
            data.writeByte(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitShort(short s) {
        try {
            if (tagType) data.write(2);
            this.maybeWriteName();
            data.writeShort(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitInt(int i) {
        try {
            if (tagType) data.write(3);
            this.maybeWriteName();
            data.writeInt(i);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitLong(long l) {
        try {
            if (tagType) data.write(4);
            this.maybeWriteName();
            data.writeLong(l);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitFloat(float f) {
        try {
            if (tagType) data.write(5);
            this.maybeWriteName();
            data.writeFloat(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitDouble(double d) {
        try {
            if (tagType) data.write(6);
            this.maybeWriteName();
            data.writeDouble(d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitByteArray(ImmutableBytes bytes) {
        try {
            if (tagType) data.write(7);
            this.maybeWriteName();
            int len = bytes.size();
            data.writeInt(len);
            byte[] bufferArray = new byte[8192];
            ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
            for (int bufferLimit = buffer.limit(), offset = 0, bufferStep;
                 (bufferStep = Math.min(bufferLimit, len - offset)) > 0; offset += bufferStep) {
                buffer.put(ImmutableBytes.slice(bytes, offset, bufferStep).toByteArray());
                data.write(bufferArray, 0, 8192 * bufferStep / bufferLimit);
                buffer.rewind();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitString(String s) {
        try {
            if (tagType) data.write(8);
            this.maybeWriteName();
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            data.writeShort(bytes.length);
            data.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TagListVisitor visitList() {
        try {
            if (tagType) data.write(9);
            this.maybeWriteName();
            return new ListWriter(this.data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TagCompoundVisitor visitCompound() {
        try {
            if (tagType) data.write(10);
            this.maybeWriteName();
            return new CompoundWriter(this.data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitIntArray(ImmutableInts ints) {
        try {
            if (tagType) data.write(11);
            this.maybeWriteName();
            int len = ints.size();
            data.writeInt(len);
            byte[] bufferArray = new byte[8192];
            IntBuffer buffer = ByteBuffer.wrap(bufferArray).asIntBuffer();
            for (int bufferLimit = buffer.limit(), offset = 0, bufferStep;
                 (bufferStep = Math.min(bufferLimit, len - offset)) > 0; offset += bufferStep) {
                buffer.put(ImmutableInts.slice(ints, offset, bufferStep).toIntArray());
                data.write(bufferArray, 0, 8192 * bufferStep / bufferLimit);
                buffer.rewind();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitLongArray(ImmutableLongs longs) {
        try {
            if (tagType) data.write(12);
            this.maybeWriteName();
            int len = longs.size();
            data.writeInt(len);
            byte[] bufferArray = new byte[8192];
            LongBuffer buffer = ByteBuffer.wrap(bufferArray).asLongBuffer();
            for (int bufferLimit = buffer.limit(), offset = 0, bufferStep;
                 (bufferStep = Math.min(bufferLimit, len - offset)) > 0; offset += bufferStep) {
                buffer.put(ImmutableLongs.slice(longs, offset, bufferStep).toLongArray());
                data.write(bufferArray, 0, 8192 * bufferStep / bufferLimit);
                buffer.rewind();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void maybeWriteName() throws IOException {
        if (this.name != null) {
            byte[] bytes = this.name.getBytes(StandardCharsets.UTF_8);
            data.writeShort(bytes.length);
            data.write(bytes, 0, bytes.length);
        }
    }

    private NbtWriter(DataOutputStream data, String name, boolean tagType) {
        super(null);
        this.data = data;
        this.name = name;
        this.tagType = tagType;
    }

    private static class ListWriter extends TagListVisitor {

        private final DataOutputStream data;

        public ListWriter(DataOutputStream data) {
            super(null);
            this.data = data;
        }

        @Override
        public void visitType(TagType tagType) {
            try {
                data.write(tagType.getId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void visitLength(int length) {
            try {
                data.writeInt(length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public TagValueVisitor visitValue() {
            return new NbtWriter(this.data, null, false);
        }
    }

    private static class CompoundWriter extends TagCompoundVisitor {

        private final DataOutputStream data;

        public CompoundWriter(DataOutputStream data) {
            super(null);
            this.data = data;
        }

        @Override
        public TagValueVisitor visit(String key) {
            return new NbtWriter(this.data, key, true);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            try {
                data.write(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
