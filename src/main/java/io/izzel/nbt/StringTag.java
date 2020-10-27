package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class StringTag extends Tag {

    private final String value;

    private StringTag(String value) {
        super(TagType.STRING);
        this.value = value;
    }

    public String getString() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitString(this.value);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof StringTag && this.value.equals(((StringTag) o).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    public static StringTag of(String s) {
        return new StringTag(s);
    }

    public static String escape(String s) {
        StringBuilder builder = new StringBuilder(" ");
        char backslash = '\\', singleQuote = '\'', doubleQuote = '"';
        char quotation = 0;
        for (int i = 0; i < s.length(); ++i) {
            char current = s.charAt(i);
            if (current == backslash) {
                builder.append(backslash);
            } else if (current == doubleQuote || current == singleQuote) {
                if (quotation == 0) {
                    quotation = current == doubleQuote ? singleQuote : doubleQuote;
                }
                if (quotation == current) {
                    builder.append(backslash);
                }
            }
            builder.append(current);
        }
        if (quotation == 0) {
            quotation = doubleQuote;
        }
        builder.setCharAt(0, quotation);
        builder.append(quotation);
        return builder.toString();
    }
}
