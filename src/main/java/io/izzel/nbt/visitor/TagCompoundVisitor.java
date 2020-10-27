package io.izzel.nbt.visitor;

public abstract class TagCompoundVisitor {

    protected final TagCompoundVisitor visitor;

    public TagCompoundVisitor(TagCompoundVisitor visitor) {
        this.visitor = visitor;
    }

    public TagValueVisitor visit(String key) {
        if (this.visitor != null) {
            return this.visitor.visit(key);
        }
        return new TagValueVisitor(null) {};
    }

    public void visitEnd() {
        if (this.visitor != null) {
            this.visitor.visitEnd();
        }
    }
}
