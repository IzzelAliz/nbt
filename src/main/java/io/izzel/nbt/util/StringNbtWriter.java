package io.izzel.nbt.util;

import io.izzel.nbt.StringTag;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringNbtWriter extends TagValueVisitor implements Flushable, Closeable {

    private final Writer data;
    private final List<IOException> suppressed;

    public StringNbtWriter(Writer writer) throws IOException {
        this(writer, new ArrayList<>(1));
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

    @Override
    public void visitEnd() {
        // we allow string representations of individual end tags
    }

    private void closeParentQuietly() {
        try {
            this.data.close();
        } catch (IOException e) {
            this.suppressed.add(e);
        }
    }

    private void throwException() throws IOException {
        Iterator<IOException> iterator = this.suppressed.iterator();
        if (iterator.hasNext()) {
            IOException exception = iterator.next();
            while (iterator.hasNext()) {
                exception.addSuppressed(iterator.next());
            }
            throw exception;
        }
    }

    private StringNbtWriter(Writer writer, List<IOException> suppressed) {
        super(new ValueWriter(writer, suppressed));
        this.suppressed = suppressed;
        this.data = writer;
    }

    private static final class ValueWriter extends TagValueVisitor {

        private final Writer data;
        private final List<IOException> suppressed;

        private ValueWriter(Writer writer, List<IOException> suppressed) {
            super(null);
            this.data = writer;
            this.suppressed = suppressed;
        }

        @Override
        public void visitEnd() {
            if (this.suppressed.isEmpty()) {
                this.suppressed.add(new IOException("End tags are not allowed in string representations"));
            }
        }

        @Override
        public void visitByte(byte b) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(b + "b");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitShort(short s) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(s + "s");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitInt(int i) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(i + "");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitLong(long l) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(l + "l");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitFloat(float f) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(f + "f");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitDouble(double d) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(d + "d");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitByteArray(ImmutableBytes bytes) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(bytes.toString("[B;", ",", "]"));
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitString(String s) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(StringTag.escape(s));
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public TagListVisitor visitList() {
            return new ListWriter(this.data, this.suppressed);
        }

        @Override
        public TagCompoundVisitor visitCompound() {
            return new CompoundWriter(this.data, this.suppressed);
        }

        @Override
        public void visitIntArray(ImmutableInts ints) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(ints.toString("[I;", ",", "]"));
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }

        @Override
        public void visitLongArray(ImmutableLongs longs) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(longs.toString("[L;", ",", "]"));
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }
    }

    private static final class ListWriter extends TagListVisitor {

        private boolean notFirst;

        private final Writer data;
        private final List<IOException> suppressed;

        public ListWriter(Writer data, List<IOException> suppressed) {
            super(null);
            this.data = data;
            this.suppressed = suppressed;
        }

        @Override
        public TagValueVisitor visitValue() {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(this.notFirst ? "," : "[");
                    this.notFirst = true;
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
            return new ValueWriter(this.data, this.suppressed);
        }

        @Override
        public void visitEnd() {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(this.notFirst ? "]" : "[]");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }
    }

    private static final class CompoundWriter extends TagCompoundVisitor {

        private boolean notFirst;

        private final Writer data;
        private final List<IOException> suppressed;

        public CompoundWriter(Writer data, List<IOException> suppressed) {
            super(null);
            this.data = data;
            this.suppressed = suppressed;
        }

        @Override
        public TagValueVisitor visit(String key) {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(this.notFirst ? "," : "{");
                    this.data.write(key);
                    this.data.write(":");
                    this.notFirst = true;
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
            return new ValueWriter(this.data, this.suppressed);
        }

        @Override
        public void visitEnd() {
            if (this.suppressed.isEmpty()) {
                try {
                    this.data.write(this.notFirst ? "}" : "{}");
                } catch (IOException e) {
                    this.suppressed.add(e);
                }
            }
        }
    }
}
