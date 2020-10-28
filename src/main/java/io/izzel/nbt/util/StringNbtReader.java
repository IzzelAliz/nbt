package io.izzel.nbt.util;

import io.izzel.nbt.ByteTag;
import io.izzel.nbt.CompoundTag;
import io.izzel.nbt.DoubleTag;
import io.izzel.nbt.EndTag;
import io.izzel.nbt.FloatTag;
import io.izzel.nbt.IntTag;
import io.izzel.nbt.LongTag;
import io.izzel.nbt.ShortTag;
import io.izzel.nbt.StringTag;
import io.izzel.nbt.Tag;
import io.izzel.nbt.TagType;
import io.izzel.nbt.visitor.TagCompoundVisitor;
import io.izzel.nbt.visitor.TagListVisitor;
import io.izzel.nbt.visitor.TagValueVisitor;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringNbtReader implements Closeable {

    private static final Pattern SPECIAL = Pattern.compile("(true|false)");

    private static final Pattern FLOATING = Pattern.compile("(NaN|[-+]?Infinity|[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:[eE][-+]?[0-9]+)?)");
    private static final Pattern INTEGER = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))");

    private static final Pattern DOUBLE = Pattern.compile("(NaN|[-+]?Infinity|[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?)[dD]");
    private static final Pattern FLOAT = Pattern.compile("(NaN|[-+]?Infinity|[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?)[fF]");
    private static final Pattern LONG = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))[lL]");
    private static final Pattern SHORT = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))[sS]");
    private static final Pattern BYTE = Pattern.compile("([-+]?(?:0|[1-9][0-9]*))[bB]");

    private final Reader data;

    private boolean eof;
    private int pointer;
    private char peek;

    public StringNbtReader(Reader data) throws IOException {
        if (data.markSupported()) {
            this.data = data;
            this.pointer = -1;
            this.peek = this.readNextChar();
        } else {
            throw new IOException("The string nbt reader requires a reader which supports the mark operation");
        }
    }

    public StringNbtReader(String string) throws IOException {
        this(new StringReader(string));
    }

    public void accept(TagValueVisitor visitor) throws IOException {
        this.read(new ValueContext(visitor, TagType.END));
    }

    @Override
    public void close() throws IOException {
        this.data.close();
    }

    public Tag toTag() throws IOException {
        TagWriter writer = new TagWriter();
        this.accept(writer);
        return writer.getTag();
    }

    public CompoundTag toCompoundTag() throws IOException {
        Tag tag = this.toTag();
        if (tag.getType() != TagType.COMPOUND) {
            throw new IOException("Expect " + TagType.COMPOUND.getTagName() + " but got " + tag.getType());
        }
        return (CompoundTag) tag;
    }

    public byte[] toBinaryNbt() throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (NbtWriter nbtWriter = new NbtWriter(stream)) {
                this.accept(nbtWriter);
            }
            return stream.toByteArray();
        }
    }

    public void toBinaryFile(Path file) throws IOException {
        try (OutputStream stream = Files.newOutputStream(file)) {
            try (NbtWriter nbtWriter = new NbtWriter(stream)) {
                this.accept(nbtWriter);
            }
        }
    }

    public byte[] toCompressedBinaryNbt() throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (CompressedNbtWriter nbtWriter = new CompressedNbtWriter(stream)) {
                this.accept(nbtWriter);
            }
            return stream.toByteArray();
        }
    }

    public void toCompressedBinaryFile(Path file) throws IOException {
        try (OutputStream stream = Files.newOutputStream(file)) {
            try (CompressedNbtWriter nbtWriter = new CompressedNbtWriter(stream)) {
                this.accept(nbtWriter);
            }
        }
    }

    private void read(ValueContext initContext) throws IOException {
        Object[] stack = new Object[]{initContext};
        int pointer = 1;
        while (pointer > 0) {
            Object context = stack[--pointer];
            if (context instanceof ValueContext) {
                char c = this.readCharAfterSpaces();
                TagType requiredTagType = ((ValueContext) context).tagType;
                if (c == '{') {
                    if (requiredTagType != TagType.END && requiredTagType != TagType.COMPOUND) {
                        throw this.error(ParseFailureException.Type.MIXED_TAG_TYPES_IN_LIST);
                    }
                    stack[pointer++] = new CompoundContext(((ValueContext) context).tagVisitor.visitCompound());
                    this.readNextChar();
                    continue;
                }
                if (c == '[') {
                    stack[pointer++] = new ArrayStartContext(((ValueContext) context).tagVisitor, requiredTagType);
                    this.readNextChar();
                    continue;
                }
                if (c == '\'' || c == '\"') {
                    if (requiredTagType != TagType.END && requiredTagType != TagType.STRING) {
                        throw this.error(ParseFailureException.Type.MIXED_TAG_TYPES_IN_LIST);
                    }
                    this.readString(((ValueContext) context).tagVisitor);
                    continue;
                }
                Tag tag = this.readNumberOrString(TagType.INT, TagType.DOUBLE, pointer <= 0);
                if (requiredTagType != TagType.END && requiredTagType != tag.getType()) {
                    throw this.error(ParseFailureException.Type.MIXED_TAG_TYPES_IN_LIST);
                }
                tag.accept(((ValueContext) context).tagVisitor);
            } else if (context instanceof ArrayStartContext) {
                TagType requiredTagType = ((ArrayStartContext) context).tagType;
                char c = this.peek;
                if (c == 'B') {
                    if (requiredTagType != TagType.END && requiredTagType != TagType.BYTE_ARRAY) {
                        throw this.error(ParseFailureException.Type.MIXED_TAG_TYPES_IN_LIST);
                    }
                    this.readByteArrayPart(((ArrayStartContext) context).tagVisitor);
                    this.readNextChar();
                    continue;
                }
                if (c == 'I') {
                    if (requiredTagType != TagType.END && requiredTagType != TagType.INT_ARRAY) {
                        throw this.error(ParseFailureException.Type.MIXED_TAG_TYPES_IN_LIST);
                    }
                    this.readIntArrayPart(((ArrayStartContext) context).tagVisitor);
                    this.readNextChar();
                    continue;
                }
                if (c == 'L') {
                    if (requiredTagType != TagType.END && requiredTagType != TagType.LONG_ARRAY) {
                        throw this.error(ParseFailureException.Type.MIXED_TAG_TYPES_IN_LIST);
                    }
                    this.readLongArrayPart(((ArrayStartContext) context).tagVisitor);
                    this.readNextChar();
                    continue;
                }
                if (requiredTagType != TagType.END && requiredTagType != TagType.LIST) {
                    throw this.error(ParseFailureException.Type.MIXED_TAG_TYPES_IN_LIST);
                }
                c = this.readCharAfterSpaces();
                TagListVisitor listVisitor = ((ArrayStartContext) context).tagVisitor.visitList();
                if (c == '{') {
                    listVisitor.visitType(TagType.COMPOUND);
                    stack[pointer++] = new ListContext(listVisitor, TagType.COMPOUND);
                    if (pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                    }
                    stack[pointer++] = new CompoundContext(listVisitor.visitValue().visitCompound());
                    this.readNextChar();
                    continue;
                }
                if (c == '[') {
                    c = this.readNextChar();
                    if (c == 'B') {
                        listVisitor.visitType(TagType.BYTE_ARRAY);
                        this.readByteArrayPart(listVisitor.visitValue());
                        stack[pointer++] = new ListContext(listVisitor, TagType.BYTE_ARRAY);
                        this.readNextChar();
                        continue;
                    }
                    if (c == 'I') {
                        listVisitor.visitType(TagType.INT_ARRAY);
                        this.readIntArrayPart(listVisitor.visitValue());
                        stack[pointer++] = new ListContext(listVisitor, TagType.INT_ARRAY);
                        this.readNextChar();
                        continue;
                    }
                    if (c == 'L') {
                        listVisitor.visitType(TagType.LONG_ARRAY);
                        this.readLongArrayPart(listVisitor.visitValue());
                        stack[pointer++] = new ListContext(listVisitor, TagType.LONG_ARRAY);
                        this.readNextChar();
                        continue;
                    }
                    listVisitor.visitType(TagType.LIST);
                    stack[pointer++] = new ListContext(listVisitor, TagType.LIST);
                    if (pointer >= stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                    }
                    stack[pointer++] = new ArrayStartContext(listVisitor.visitValue(), TagType.END);
                    continue;
                }
                if (c == ']') {
                    listVisitor.visitType(TagType.END);
                    listVisitor.visitLength(0);
                    listVisitor.visitEnd();
                    this.readNextChar();
                    continue;
                }
                if (c == '\'' || c == '\"') {
                    listVisitor.visitType(TagType.STRING);
                    this.readString(listVisitor.visitValue());
                    stack[pointer++] = new ListContext(listVisitor, TagType.STRING);
                    continue;
                }
                Tag tag = this.readNumberOrString(TagType.INT, TagType.DOUBLE, false);
                listVisitor.visitType(tag.getType());
                tag.accept(listVisitor.visitValue());
                stack[pointer++] = new ListContext(listVisitor, tag.getType());
            } else if (context instanceof ListContext) {
                char c = this.readCharAfterSpaces();
                TagListVisitor tagVisitor = ((ListContext) context).tagVisitor;
                int count = ((ListContext) context).collectedTagCount.getAndIncrement();
                if (c == ']') {
                    tagVisitor.visitLength(count);
                    tagVisitor.visitEnd();
                    this.readNextChar();
                    continue;
                }
                if (c != ',') {
                    throw this.error(ParseFailureException.Type.UNRECOGNIZED_VALUE_SEPARATOR);
                }
                if (++pointer >= stack.length) {
                    stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                }
                stack[pointer++] = new ValueContext(tagVisitor.visitValue(), ((ListContext) context).tagType);
                this.readNextChar();
            } else if (context instanceof CompoundContext) {
                char c = this.readCharAfterSpaces();
                TagCompoundVisitor tagVisitor = ((CompoundContext) context).tagVisitor;
                boolean isNotFirst = ((CompoundContext) context).isNotFirst.getAndSet(true);
                if (c == '}') {
                    tagVisitor.visitEnd();
                    this.readNextChar();
                    continue;
                }
                if (isNotFirst) {
                    if (c != ',') {
                        throw this.error(ParseFailureException.Type.UNRECOGNIZED_VALUE_SEPARATOR);
                    }
                    this.readNextChar();
                    this.readCharAfterSpaces();
                }
                TagValueVisitor valueVisitor = this.readKey(tagVisitor);
                c = this.readCharAfterSpaces();
                if (c != ':') {
                    throw this.error(ParseFailureException.Type.UNRECOGNIZED_NAME_VALUE_SEPARATOR);
                }
                if (++pointer >= stack.length) {
                    stack = Arrays.copyOf(stack, stack.length * 2 + 1);
                }
                stack[pointer++] = new ValueContext(valueVisitor, TagType.END);
                this.readNextChar();
            }
        }
        this.data.reset();
    }

    private void readByteArrayPart(TagValueVisitor visitor) throws IOException {
        this.readNextChar();
        this.readNextChar();
        ImmutableBytes.Builder builder = ImmutableBytes.builder();
        while (true) {
            this.readCharAfterSpaces();
            Tag tag = this.readNumberOrString(TagType.BYTE, TagType.END, false);
            if (tag.getType() == TagType.BYTE) {
                builder.add(((ByteTag) tag).getByte());
                char c = this.readCharAfterSpaces();
                if (c == ',') {
                    this.readNextChar();
                    continue;
                }
                if (c == ']') {
                    visitor.visitByteArray(builder.build());
                    return;
                }
                throw this.error(ParseFailureException.Type.UNRECOGNIZED_VALUE_SEPARATOR);
            }
            throw this.error(ParseFailureException.Type.INVALID_NUMBER_TYPE_IN_BYTE_ARRAY);
        }
    }

    private void readIntArrayPart(TagValueVisitor visitor) throws IOException {
        this.readNextChar();
        this.readNextChar();
        ImmutableInts.Builder builder = ImmutableInts.builder();
        while (true) {
            this.readCharAfterSpaces();
            Tag tag = this.readNumberOrString(TagType.INT, TagType.END, false);
            if (tag.getType() == TagType.INT) {
                builder.add(((IntTag) tag).getInt());
                char c = this.readCharAfterSpaces();
                if (c == ',') {
                    this.readNextChar();
                    continue;
                }
                if (c == ']') {
                    visitor.visitIntArray(builder.build());
                    return;
                }
                throw this.error(ParseFailureException.Type.UNRECOGNIZED_VALUE_SEPARATOR);
            }
            throw this.error(ParseFailureException.Type.INVALID_NUMBER_TYPE_IN_INT_ARRAY);
        }
    }

    private void readLongArrayPart(TagValueVisitor visitor) throws IOException {
        this.readNextChar();
        this.readNextChar();
        ImmutableLongs.Builder builder = ImmutableLongs.builder();
        while (true) {
            this.readCharAfterSpaces();
            Tag tag = this.readNumberOrString(TagType.LONG, TagType.END, false);
            if (tag.getType() == TagType.LONG) {
                builder.add(((LongTag) tag).getLong());
                char c = this.readCharAfterSpaces();
                if (c == ',') {
                    this.readNextChar();
                    continue;
                }
                if (c == ']') {
                    visitor.visitLongArray(builder.build());
                    return;
                }
                throw this.error(ParseFailureException.Type.UNRECOGNIZED_VALUE_SEPARATOR);
            }
            throw this.error(ParseFailureException.Type.INVALID_NUMBER_TYPE_IN_LONG_ARRAY);
        }
    }

    private Tag readNumberOrString(TagType integerHint, TagType floatingHint, boolean allowEmpty) throws IOException {
        String s = this.readUnquotedString(allowEmpty);
        try {
            Matcher matcher;
            TagType tagType = TagType.END;
            if ((matcher = SPECIAL.matcher(s)).matches()) {
                return ByteTag.of(Boolean.parseBoolean(matcher.group(1)));
            } else if ((matcher = DOUBLE.matcher(s)).matches()) {
                tagType = TagType.DOUBLE;
            } else if ((matcher = FLOAT.matcher(s)).matches()) {
                tagType = TagType.FLOAT;
            } else if ((matcher = LONG.matcher(s)).matches()) {
                tagType = TagType.LONG;
            } else if ((matcher = SHORT.matcher(s)).matches()) {
                tagType = TagType.SHORT;
            } else if ((matcher = BYTE.matcher(s)).matches()) {
                tagType = TagType.BYTE;
            } else if ((matcher = FLOATING.matcher(s)).matches()) {
                tagType = floatingHint;
            } else if ((matcher = INTEGER.matcher(s)).matches()) {
                tagType = integerHint;
            }
            switch (tagType) {
                case BYTE:
                    return ByteTag.of(Byte.parseByte(matcher.group(1)));
                case SHORT:
                    return ShortTag.of(Short.parseShort(matcher.group(1)));
                case INT:
                    return IntTag.of(Integer.parseInt(matcher.group(1)));
                case LONG:
                    return LongTag.of(Long.parseLong(matcher.group(1)));
                case FLOAT:
                    return FloatTag.of(Float.parseFloat(matcher.group(1)));
                case DOUBLE:
                    return DoubleTag.of(Double.parseDouble(matcher.group(1)));
            }
            throw new NumberFormatException();
        } catch (NumberFormatException e) {
            return s.isEmpty() ? EndTag.of() : StringTag.of(s);
        }
    }

    private String readUnquotedString(boolean allowEmpty) throws IOException {
        StringBuilder sb = new StringBuilder();
        char c = this.peek;
        while (true) {
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
                sb.append(c);
                c = this.readNextChar();
                continue;
            }
            if (c == '_' || c == '-' || c == '.' || c == '+') {
                sb.append(c);
                c = this.readNextChar();
                continue;
            }
            if (sb.length() > 0 || allowEmpty) {
                return sb.toString();
            }
            this.readCharAfterSpaces();
            throw this.error(ParseFailureException.Type.UNRECOGNIZED_VALUE_REPRESENTATION);
        }
    }

    private TagValueVisitor readKey(TagCompoundVisitor visitor) throws IOException {
        char c = this.peek;
        if (c == '\'' || c == '\"') {
            final TagValueVisitor tagVisitor = visitor.visit(this.readStringPart());
            this.readNextChar();
            return tagVisitor;
        }
        return visitor.visit(this.readUnquotedString(false));
    }

    private void readString(TagValueVisitor visitor) throws IOException {
        visitor.visitString(this.readStringPart());
        this.readNextChar();
    }

    private String readStringPart() throws IOException {
        StringBuilder sb = new StringBuilder();
        char quote = this.peek;
        while (true) {
            char c = this.readNextChar();
            if (c == quote) {
                return sb.toString();
            }
            if (c == '\\') {
                c = this.readNextChar();
                if (c != '\\' && c != quote) {
                    throw this.error(ParseFailureException.Type.UNEXPECTED_ESCAPED);
                }
            }
            sb.append(c);
        }
    }

    private char readCharAfterSpaces() throws IOException {
        char c = this.peek;
        while (Character.isWhitespace(c)) {
            c = this.readNextChar();
        }
        return c;
    }

    private char readNextChar() throws IOException {
        if (!this.eof) {
            ++this.pointer;
            this.data.mark(1);
            int i = this.data.read();
            if (i < 0) {
                this.eof = true;
                return this.peek = 0;
            }
            return this.peek = (char) i;
        }
        throw this.error(ParseFailureException.Type.UNEXPECTED_EOF);
    }

    private ParseFailureException error(ParseFailureException.Type type) {
        return new ParseFailureException(this.pointer, type);
    }

    private static final class ValueContext {
        private final TagType tagType;
        private final TagValueVisitor tagVisitor;

        private ValueContext(TagValueVisitor tagVisitor, TagType tagType) {
            this.tagType = tagType;
            this.tagVisitor = tagVisitor;
        }
    }

    private static final class ArrayStartContext {
        private final TagType tagType;
        private final TagValueVisitor tagVisitor;

        private ArrayStartContext(TagValueVisitor tagVisitor, TagType tagType) {
            this.tagType = tagType;
            this.tagVisitor = tagVisitor;
        }
    }

    private static final class ListContext {
        private final TagType tagType;
        private final TagListVisitor tagVisitor;
        private final AtomicInteger collectedTagCount;

        private ListContext(TagListVisitor tagVisitor, TagType tagType) {
            this.tagType = tagType;
            this.tagVisitor = tagVisitor;
            this.collectedTagCount = new AtomicInteger(1);
        }
    }

    private static final class CompoundContext {
        private final TagCompoundVisitor tagVisitor;
        private final AtomicBoolean isNotFirst;

        private CompoundContext(TagCompoundVisitor tagVisitor) {
            this.isNotFirst = new AtomicBoolean();
            this.tagVisitor = tagVisitor;
        }
    }

    public static final class ParseFailureException extends IOException {
        private final int position;
        private final Type type;

        public ParseFailureException(int position, Type type) {
            super("Parse error (at position " + position + "): " + type.message);
            this.position = position;
            this.type = type;
        }

        public int getPosition() {
            return this.position;
        }

        public Type getType() {
            return this.type;
        }

        public enum Type {
            UNEXPECTED_EOF("Unexpected EOF"),
            UNEXPECTED_ESCAPED("Unexpected escape character"),
            UNRECOGNIZED_VALUE_REPRESENTATION("Unrecognized value representation"),
            UNRECOGNIZED_VALUE_SEPARATOR("Expected a value separator (,) but not found"),
            UNRECOGNIZED_NAME_VALUE_SEPARATOR("Expected a name-value separator (:) of compound tag but not found"),
            MIXED_TAG_TYPES_IN_LIST("Mixed tag types in a list tag"),
            INVALID_NUMBER_TYPE_IN_BYTE_ARRAY("Number not allowed occurred in a byte array"),
            INVALID_NUMBER_TYPE_IN_INT_ARRAY("Number not allowed occurred in an integer array"),
            INVALID_NUMBER_TYPE_IN_LONG_ARRAY("Number not allowed occurred in an long integer array");

            private final String message;

            Type(String message) {
                this.message = message;
            }
        }
    }
}
