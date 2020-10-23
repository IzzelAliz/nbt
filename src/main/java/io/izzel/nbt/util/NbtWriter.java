package io.izzel.nbt.util;

import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class NbtWriter extends TagValueVisitor implements Flushable, Closeable {

    private final DataOutputStream data;
    private final List<IOException> suppressed;

    public NbtWriter(OutputStream stream) throws IOException {
        this(stream instanceof DataOutputStream ? (DataOutputStream) stream : new DataOutputStream(stream), "", new ArrayList<>(1));
    }

    public NbtWriter(OutputStream stream, String name) throws IOException {
        this(stream instanceof DataOutputStream ? (DataOutputStream) stream : new DataOutputStream(stream), name, new ArrayList<>(1));
    }

    public NbtWriter(OutputStream stream, boolean gzip) throws IOException {
        this(gzip ? new GZIPOutputStream(stream) : stream);
    }

    public NbtWriter(OutputStream stream, boolean gzip, String name) throws IOException {
        this(gzip ? new GZIPOutputStream(stream) : stream, name);
    }

    @Override
    public void flush() throws IOException {
        this.data.flush();
    }

    @Override
    public void close() throws IOException {
        this.closeParentQuietly();
        this.throwException();
    }

    private void closeParentQuietly() {
        try {
            this.data.close();
        } catch (IOException e) {
            this.suppressed.add(e);
        }
    }

    public void throwException() throws IOException {
        Iterator<IOException> iterator = this.suppressed.iterator();
        if (iterator.hasNext()) {
            IOException exception = iterator.next();
            while (iterator.hasNext()) {
                IOException next = iterator.next();
                next.addSuppressed(exception);
                exception = next;
            }
            throw exception;
        }
    }

    private NbtWriter(DataOutputStream data, String name, List<IOException> suppressed) {
        super(new ValueWriter(data, name, suppressed));
        this.suppressed = suppressed;
        this.data = data;
    }

    private static final class ValueWriter extends TagValueVisitor {

        private final List<IOException> suppressed;
        private final DataOutputStream data;
        private final String name;

        private ValueWriter(DataOutputStream data, String name, List<IOException> suppressed) {
            super(null);
            this.name = name;
            this.data = data;
            this.suppressed = suppressed;
        }

        @Override
        public void visitEnd() {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.END);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitByte(byte b) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.BYTE);
                    this.data.writeByte(b);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitShort(short s) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.SHORT);
                    this.data.writeShort(s);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitInt(int i) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.INT);
                    this.data.writeInt(i);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitLong(long l) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.LONG);
                    this.data.writeLong(l);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitFloat(float f) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.FLOAT);
                    this.data.writeFloat(f);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitDouble(double d) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.DOUBLE);
                    this.data.writeDouble(d);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitByteArray(ImmutableBytes bytes) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.BYTE_ARRAY);
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
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitString(String s) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.STRING);
                    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
                    data.writeShort(bytes.length);
                    data.write(bytes, 0, bytes.length);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public TagListVisitor visitList() {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.LIST);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
            return new ListWriter(this.data, this.suppressed);
        }

        @Override
        public TagCompoundVisitor visitCompound() {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.COMPOUND);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
            return new CompoundWriter(this.data, this.suppressed);
        }

        @Override
        public void visitIntArray(ImmutableInts ints) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.INT_ARRAY);
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
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitLongArray(ImmutableLongs longs) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.writePrefix(TagType.LONG_ARRAY);
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
                    this.suppressed.add(e);
                }
            }
        }

        private void writePrefix(TagType type) throws IOException {
            if (this.name != null) {
                data.write(type.getId());
                byte[] bytes = this.name.getBytes(StandardCharsets.UTF_8);
                data.writeShort(bytes.length);
                data.write(bytes, 0, bytes.length);
            }
        }
    }

    private static final class ListWriter extends TagListVisitor {

        private final DataOutputStream data;
        private final List<IOException> suppressed;

        public ListWriter(DataOutputStream data, List<IOException> suppressed) {
            super(null);
            this.data = data;
            this.suppressed = suppressed;
        }

        @Override
        public void visitType(TagType tagType) {
            if (this.suppressed.isEmpty()) {
                try {
                    data.write(tagType.getId());
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitLength(int length) {
            if (this.suppressed.isEmpty()) {
                try {
                    data.writeInt(length);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public TagValueVisitor visitValue() {
            return new ValueWriter(this.data, null, this.suppressed);
        }
    }

    private static final class CompoundWriter extends TagCompoundVisitor {

        private final DataOutputStream data;
        private final List<IOException> suppressed;

        public CompoundWriter(DataOutputStream data, List<IOException> suppressed) {
            super(null);
            this.data = data;
            this.suppressed = suppressed;
        }

        @Override
        public TagValueVisitor visit(String key) {
            return new ValueWriter(this.data, key, this.suppressed);
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            if (this.suppressed.isEmpty()) {
                try {
                    data.write(0);
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }
    }
}
