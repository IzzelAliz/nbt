package io.izzel.nbt;

import io.izzel.nbt.visitor.TagValueVisitor;

public final class StringTag extends Tag<String> {

    private final String value;

    public StringTag(String value) {
        super(TagType.STRING);
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void accept(TagValueVisitor visitor) {
        visitor.visitString(this.value);
    }

    @Override
    public String toString() {
        return escape(this.value);
    }

    public static String escape(String s) {
        StringBuilder builder = new StringBuilder(" ");
        char c0 = 0;

        for (int i = 0; i < s.length(); ++i) {
            char c1 = s.charAt(i);
            if (c1 == '\\') {
                builder.append('\\');
            } else if (c1 == '"' || c1 == '\'') {
                if (c0 == 0) {
                    c0 = (char) (c1 == '"' ? 39 : 34);
                }

                if (c0 == c1) {
                    builder.append('\\');
                }
            }

            builder.append(c1);
        }

        if (c0 == 0) {
            c0 = '"';
        }

        builder.setCharAt(0, c0);
        builder.append(c0);
        return builder.toString();
    }
}
