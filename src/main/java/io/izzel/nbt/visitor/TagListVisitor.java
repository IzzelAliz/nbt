package io.izzel.nbt.visitor;

import io.izzel.nbt.TagType;

public class TagListVisitor {

    protected final TagListVisitor visitor;

    public TagListVisitor(TagListVisitor visitor) {
        this.visitor = visitor;
    }

    public void visitType(TagType tagType) {
        if (this.visitor != null) {
            this.visitor.visitType(tagType);
        }
    }

    public void visitLength(int length) {
        if (this.visitor != null) {
            this.visitor.visitLength(length);
        }
    }

    public TagValueVisitor visitValue() {
        if (this.visitor != null) {
            return this.visitor.visitValue();
        }
        return null;
    }
}
