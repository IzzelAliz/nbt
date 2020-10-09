package io.izzel.nbt.util;

import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

public class NbtWriter extends TagValueVisitor {

    private final ByteArrayOutputStream out;
    private final DataOutputStream data;
    private final String name;
    private final boolean tagType;

    public NbtWriter() {
        super(null);
        this.out = new ByteArrayOutputStream();
        this.data = new DataOutputStream(this.out);
        this.name = null;
        this.tagType = true;
    }

    public NbtWriter(boolean gzip) throws IOException {
        super(null);
        this.out = new ByteArrayOutputStream();
        if (gzip) {
            this.data = new DataOutputStream(new GZIPOutputStream(this.out));
        } else {
            this.data = new DataOutputStream(this.out);
        }
        this.name = null;
        this.tagType = true;
    }

    private NbtWriter(DataOutputStream data, String name, boolean tagType) {
        super(null);
        this.out = null;
        this.data = data;
        this.name = name;
        this.tagType = tagType;
    }

    public byte[] toByteArray() {
        try {
            data.close();
            out.close();
            return Objects.requireNonNull(out).toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void visitByteArray(byte[] bytes) {
        try {
            if (tagType) data.write(7);
            this.maybeWriteName();
            data.writeInt(bytes.length);
            data.write(bytes, 0, bytes.length);
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
    public TagCompoundVisitor visitNamedCompound(String name) {
        try {
            if (tagType) data.write(10);
            byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
            data.writeShort(bytes.length);
            data.write(bytes, 0, bytes.length);
            return new CompoundWriter(this.data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitIntArray(int[] ints) {
        try {
            if (tagType) data.write(11);
            this.maybeWriteName();
            data.writeInt(ints.length);
            for (int i : ints) {
                data.writeInt(i);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitLongArray(long[] longs) {
        try {
            if (tagType) data.write(12);
            this.maybeWriteName();
            data.writeInt(longs.length);
            for (long l : longs) {
                data.writeLong(l);
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
            try {
                data.write(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
