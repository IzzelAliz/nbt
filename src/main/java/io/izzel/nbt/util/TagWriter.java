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

public class TagWriter extends TagValueVisitor {

    private Tag tag;

    public TagWriter() {
        super(null);
    }

    public Tag getTag() {
        return this.tag;
    }

    protected void setTag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public void visitEnd() {
        this.setTag(EndTag.of());
    }

    @Override
    public void visitByte(byte b) {
        this.setTag(ByteTag.of(b));
    }

    @Override
    public void visitShort(short s) {
        this.setTag(ShortTag.of(s));
    }

    @Override
    public void visitInt(int i) {
        this.setTag(IntTag.of(i));
    }

    @Override
    public void visitLong(long l) {
        this.setTag(LongTag.of(l));
    }

    @Override
    public void visitFloat(float f) {
        this.setTag(FloatTag.of(f));
    }

    @Override
    public void visitDouble(double d) {
        this.setTag(DoubleTag.of(d));
    }

    @Override
    public void visitByteArray(ImmutableBytes bytes) {
        this.setTag(ByteArrayTag.of(bytes));
    }

    @Override
    public void visitString(String s) {
        this.setTag(StringTag.of(s));
    }

    @Override
    public TagListVisitor visitList() {
        return new ListWriter() {
            @Override
            protected void setTag(ListTag tag) {
                TagWriter.this.setTag(tag);
            }
        };
    }

    @Override
    public TagCompoundVisitor visitCompound() {
        return new CompoundWriter() {
            @Override
            protected void setTag(CompoundTag tag) {
                TagWriter.this.setTag(tag);
            }
        };
    }

    @Override
    public void visitIntArray(ImmutableInts ints) {
        this.setTag(IntArrayTag.of(ints));
    }

    @Override
    public void visitLongArray(ImmutableLongs longs) {
        this.setTag(LongArrayTag.of(longs));
    }

    private abstract static class ListWriter extends TagListVisitor {

        private ListTag.Builder builder;

        private int length;

        public ListWriter() {
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
            return new TagWriter() {
                @Override
                protected void setTag(Tag tag) {
                    ListWriter.this.builder.add(tag);
                }
            };
        }

        @Override
        public void visitEnd() {
            ListTag tag = this.builder.build();
            if (tag.size() != this.length) {
                throw new IllegalStateException("Mismatched size: expected " + this.length + " but got " + tag.size());
            }
            this.setTag(tag);
        }

        protected abstract void setTag(ListTag tag);
    }

    private static abstract class CompoundWriter extends TagCompoundVisitor {

        private final CompoundTag.Builder builder;

        public CompoundWriter() {
            super(null);
            this.builder = CompoundTag.builder(true);
        }

        @Override
        public TagValueVisitor visit(String key) {
            return new TagWriter() {
                @Override
                protected void setTag(Tag tag) {
                    CompoundWriter.this.builder.add(key, tag);
                }
            };
        }

        @Override
        public void visitEnd() {
            this.setTag(this.builder.build());
        }

        protected abstract void setTag(CompoundTag tag);
    }
}
