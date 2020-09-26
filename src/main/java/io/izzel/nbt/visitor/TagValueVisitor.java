package io.izzel.nbt.visitor;

public class TagValueVisitor {

    protected final TagValueVisitor visitor;

    protected TagValueVisitor(TagValueVisitor visitor) {
        this.visitor = visitor;
    }

    public void visitEnd() {
        if (this.visitor != null) {
            this.visitor.visitEnd();
        }
    }

    public void visitByte(byte b) {
        if (this.visitor != null) {
            this.visitor.visitByte(b);
        }
    }

    public void visitShort(short s) {
        if (this.visitor != null) {
            this.visitor.visitShort(s);
        }
    }

    public void visitInt(int i) {
        if (this.visitor != null) {
            this.visitor.visitInt(i);
        }
    }

    public void visitLong(long l) {
        if (this.visitor != null) {
            this.visitor.visitLong(l);
        }
    }

    public void visitFloat(float f) {
        if (this.visitor != null) {
            this.visitor.visitFloat(f);
        }
    }

    public void visitDouble(double d) {
        if (this.visitor != null) {
            this.visitor.visitDouble(d);
        }
    }

    public void visitByteArray(byte[] bytes) {
        if (this.visitor != null) {
            this.visitor.visitByteArray(bytes);
        }
    }

    public void visitString(String s) {
        if (this.visitor != null) {
            this.visitor.visitString(s);
        }
    }

    public TagListVisitor visitList() {
        if (this.visitor != null) {
            return this.visitor.visitList();
        }
        return null;
    }

    public TagCompoundVisitor visitCompound() {
        if (this.visitor != null) {
            return this.visitor.visitCompound();
        }
        return null;
    }

    public TagCompoundVisitor visitNamedCompound(String name) {
        if (this.visitor != null) {
            return this.visitor.visitNamedCompound(name);
        }
        return null;
    }

    public void visitIntArray(int[] ints) {
        if (this.visitor != null) {
            this.visitor.visitIntArray(ints);
        }
    }

    public void visitLongArray(long[] longs) {
        if (this.visitor != null) {
            this.visitor.visitLongArray(longs);
        }
    }
}
