package io.izzel.nbt.util;

import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.ListTag;
import io.izzel.nbt.Tag;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.util.Arrays;
import java.util.Iterator;

public class TagReader {
    private final Tag tag;

    public TagReader(Tag tag) {
        this.tag = tag;
    }

    public void accept(TagValueVisitor visitor) {
        this.read(new ValueContext(visitor, this.tag));
    }

    private void read(ValueContext initContext) {
        Object[] stack = new Object[]{initContext};
        int pointer = 1;
        while (pointer > 0) {
            Object context = stack[--pointer];
            if (context instanceof ValueContext) {
                TagValueVisitor tagVisitor = ((ValueContext) context).tagVisitor;
                Tag tag = ((ValueContext) context).tag;
                switch (tag.getType()) {
                    case END:
                    case BYTE:
                    case SHORT:
                    case INT:
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                    case BYTE_ARRAY:
                    case STRING:
                    case INT_ARRAY:
                    case LONG_ARRAY: {
                        tag.accept(tagVisitor);
                        break;
                    }
                    case LIST: {
                        ListTag listTag = (ListTag) tag;
                        TagListVisitor visitor = tagVisitor.visitList();
                        stack[pointer++] = new ListContext(visitor, listTag);
                        visitor.visitType(listTag.getElemType());
                        visitor.visitLength(listTag.size());
                        break;
                    }
                    case COMPOUND: {
                        CompoundTag compoundTag = (CompoundTag) tag;
                        stack[pointer++] = new CompoundContext(tagVisitor.visitCompound(), compoundTag);
                        break;
                    }
                }
            } else if (context instanceof ListContext) {
                Iterator<? extends Tag> iterator = ((ListContext) context).current;
                TagListVisitor tagVisitor = ((ListContext) context).tagVisitor;
                if (!iterator.hasNext()) {
                    tagVisitor.visitEnd();
                } else {
                    if (++pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                    }
                    Tag tag = iterator.next();
                    stack[pointer++] = new ValueContext(tagVisitor.visitValue(), tag);
                }
            } else if (context instanceof CompoundContext) {
                Iterator<? extends CompoundTag.Entry<?>> iterator = ((CompoundContext) context).current;
                TagCompoundVisitor tagVisitor = ((CompoundContext) context).tagVisitor;
                if (!iterator.hasNext()) {
                    tagVisitor.visitEnd();
                } else {
                    if (++pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                    }
                    CompoundTag.Entry<?> entry = iterator.next();
                    stack[pointer++] = new ValueContext(tagVisitor.visit(entry.getKey()), entry.getValue());
                }
            }
        }
    }

    private static final class ValueContext {
        private final Tag tag;
        private final TagValueVisitor tagVisitor;

        private ValueContext(TagValueVisitor tagVisitor, Tag tag) {
            this.tag = tag;
            this.tagVisitor = tagVisitor;
        }
    }

    private static final class ListContext {
        private final TagListVisitor tagVisitor;
        private final Iterator<? extends Tag> current;

        private ListContext(TagListVisitor tagVisitor, ListTag tag) {
            this.tagVisitor = tagVisitor;
            this.current = tag.dump().iterator();
        }
    }

    private static final class CompoundContext {
        private final TagCompoundVisitor tagVisitor;
        private final Iterator<? extends CompoundTag.Entry<?>> current;

        private CompoundContext(TagCompoundVisitor tagVisitor, CompoundTag tag) {
            this.tagVisitor = tagVisitor;
            this.current = tag.dump().iterator();
        }
    }
}
