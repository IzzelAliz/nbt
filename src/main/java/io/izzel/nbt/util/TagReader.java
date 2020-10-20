package io.izzel.nbt.util;

import io.izzel.nbt.ByteArrayTag;
import io.izzel.nbt.ByteTag;
import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.DoubleTag;
import io.izzel.nbt.EndTag;
import io.izzel.nbt.FloatTag;
import io.izzel.nbt.IntArrayTag;
import io.izzel.nbt.IntTag;
import io.izzel.nbt.ListTag;
import io.izzel.nbt.LongArrayTag;
import io.izzel.nbt.LongTag;
import io.izzel.nbt.ShortTag;
import io.izzel.nbt.StringTag;
import io.izzel.nbt.Tag;
import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

public class TagReader extends TagValueVisitor {

    protected Tag<?> tag;

    public TagReader() {
        super(null);
    }

    public Tag<?> getTag() {
        return tag;
    }

    protected void setTag(Tag<?> tag) {
        this.tag = tag;
    }

    @Override
    public void visitEnd() {
        this.setTag(new EndTag());
    }

    @Override
    public void visitByte(byte b) {
        this.setTag(new ByteTag(b));
    }

    @Override
    public void visitShort(short s) {
        this.setTag(new ShortTag(s));
    }

    @Override
    public void visitInt(int i) {
        this.setTag(new IntTag(i));
    }

    @Override
    public void visitLong(long l) {
        this.setTag(new LongTag(l));
    }

    @Override
    public void visitFloat(float f) {
        this.setTag(new FloatTag(f));
    }

    @Override
    public void visitDouble(double d) {
        this.setTag(new DoubleTag(d));
    }

    @Override
    public void visitByteArray(byte[] bytes) {
        this.setTag(new ByteArrayTag(bytes));
    }

    @Override
    public void visitString(String s) {
        this.setTag(new StringTag(s));
    }

    @Override
    public TagListVisitor visitList() {
        return new TagReader.ListReader() {
            @Override
            public void visitEnd() {
                super.visitEnd();
                setTag(builder.build());
            }
        };
    }

    @Override
    public TagCompoundVisitor visitCompound() {
        return new CompoundReader() {
            @Override
            public void visitEnd() {
                super.visitEnd();
                setTag(builder.build());
            }
        };
    }

    @Override
    public void visitIntArray(int[] ints) {
        this.setTag(new IntArrayTag(ints));
    }

    @Override
    public void visitLongArray(long[] longs) {
        this.setTag(new LongArrayTag(longs));
    }

    private static class ListReader extends TagListVisitor {

        ListTag.Builder builder;

        private int length;
        private int index = 0;

        public ListReader() {
            super(null);
        }

        @Override
        public void visitType(TagType tagType) {
            this.builder = ListTag.builder(tagType);
        }

        @Override
        public void visitLength(int length) {
            this.length = length;
        }

        @Override
        public TagValueVisitor visitValue() {
            if (++this.index > this.length) {
                throw new IndexOutOfBoundsException("Index: " + this.index + ", Size: " + this.length);
            }
            return new TagReader() {
                @Override
                protected void setTag(Tag<?> tag) {
                    super.setTag(tag);
                    builder.add(tag);
                }
            };
        }
    }

    private static class CompoundReader extends TagCompoundVisitor {

        CompoundTag.Builder builder;

        public CompoundReader() {
            super(null);
            this.builder = CompoundTag.builder(true);
        }

        @Override
        public TagValueVisitor visit(String key) {
            return new TagReader() {
                @Override
                protected void setTag(Tag<?> tag) {
                    super.setTag(tag);
                    builder.add(key, tag);
                }
            };
        }
    }
}
