package io.izzel.nbt;

import io.izzel.nbt.util.StringNbtWriter;
import io.izzel.nbt.util.TagReader;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.IOException;
import java.io.StringWriter;

public abstract class Tag {

    private final TagType type;

    protected Tag(TagType type) {
        this.type = type;
    }

    public final TagType getType() {
        return this.type;
    }

    public void accept(TagValueVisitor visitor) {
        new TagReader(this).accept(visitor);
    }

    @Override
    public String toString() {
        try (StringWriter writer = new StringWriter()) {
            this.accept(new StringNbtWriter(writer));
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
