package dev.ultreon.quantum.api.neocommand;

import dev.ultreon.libs.functions.v0.misc.ThrowingFunction;
import dev.ultreon.quantum.ubo.DataIo;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public final class Selector<T> {
    private final Type<T> type;
    private final T value;

    public Selector(
            Type<T> type,
            T value
    ) {
        this.type = type;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <R> Selector<R> cast() {
        return new Selector<>((Type<R>) type, (R) value);
    }

    public Type<T> type() {
        return type;
    }

    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Selector) obj;
        return Objects.equals(this.type, that.type) &&
               Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "Selector[" +
               "type=" + type + ", " +
               "value=" + value + ']';
    }


    public static class Type<T> {
        public static final Type<String> TAG = new Type<>('#', Type::readAlphaNumericWithUnderscore, Type::matchAlphaNumericWithUnderscore);

        public static final Type<String> CLASS = new Type<>('.', Type::readAlphaNumericWithUnderscore, Type::matchAlphaNumericWithUnderscore);
        public static final Type<Integer> ID = new Type<>('*', Type::readInteger, Type::matchInteger);
        public static final Type<NamespaceID> KEY = new Type<>('*', Type::readNamespaceID, Type::matchNamespaceID);
        public static final Type<String> NAME = new Type<>('@', Type::readAlphaNumericString, Type::matchAlphaNumericString);
        public static final Type<MapType> DATA = new Type<>('?', Type::readUbo, Type::matchUbo);
        public static final Type<UUID> UUID = new Type<>(':', Type::readUuid, Type::matchUuid);
        public static final Type<String> DISPLAY_NAME = new Type<>('=', Type::readAlphaNumericPlusSpaces, Type::matchAlphaNumericPlusSpaces);
        public static final Type<String> CUSTOM_NAME = new Type<>('+', Type::readAlphaNumericPlusSpaces, Type::matchAlphaNumericPlusSpaces);
        public static final Type<String> VARIABLE = new Type<>('$', Type::readAlphaNumericWithSeps, Type::matchAlphaNumericWithSeps);
        public final char character;

        private final ThrowingFunction<StringReader, T, IOException> reader;
        private final Predicate<StringReader> matcher;

        private Type(char character, ThrowingFunction<StringReader, T, IOException> reader, Predicate<StringReader> matcher) {
            this.character = character;
            this.reader = reader;
            this.matcher = matcher;
        }

        @Override
        public String toString() {
            return String.valueOf(character);
        }

        private static Integer readInteger(StringReader stringReader) throws IOException {
            int read = stringReader.read();
            if (read == -1) return null;
            StringBuilder builder = new StringBuilder();
            while (Character.isDigit((char) read)) {
                builder.append((char) read);
                read = stringReader.read();
            }
            return Integer.parseInt(builder.toString());
        }

        private static boolean matchInteger(StringReader reader) {
            int read;
            try {
                while ((read = reader.read()) != -1) {
                    if (fromChar((char) read) != null) return false;
                    if (!Character.isDigit((char) read)) return false;
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        private static MapType readUbo(StringReader reader) throws IOException {
            int depth = 0;
            StringBuilder data = new StringBuilder();
            char c = (char) reader.read();
            if (c != '{') throw new IOException("Expected '{'");
            while (c != '}' || depth > 0) {
                if (c == '{') depth++;
                if (c == '}') depth--;
                data.append(c);
                c = (char) reader.read();
            }

            return DataIo.fromUso(data.toString());
        }

        private static boolean matchUbo(StringReader reader) {
            try {
                return matchUboUnsafe(reader);
            } catch (IOException e) {
                return false;
            }
        }

        private static boolean matchUboUnsafe(StringReader reader) throws IOException {
            int depth = 0;
            char c = (char) reader.read();
            StringBuilder data = new StringBuilder();
            data.append(c);
            if (c != '{') return false;
            while (c != '}' || depth > 0) {
                if (c == '{') depth++;
                if (c == '}') depth--;
                int read = reader.read();
                if (read == -1) return false;
                c = (char) read;
                data.append(c);
            }

            try {
                return DataIo.<MapType>fromUso(data.toString()) != null;
            } catch (Exception e) {
                return false;
            }
        }

        private static @NotNull UUID readUuid(StringReader reader) throws IOException {
            StringBuilder uuid = new StringBuilder();
            for (int i = 0; i < 32; i++) {
                int read = reader.read();
                char c = (char) read;
                if (i == 8 || i == 12 || i == 16 || i == 20) {
                    if (c == '-') uuid.append('-');
                    else throw new IOException("Expected '-'");
                    continue;
                }
                uuid.append(c);
            }
            return java.util.UUID.fromString(uuid.toString());
        }

        private static boolean matchUuid(StringReader reader) {
            try {
                return matchUuidUnsafe(reader);
            } catch (IOException e) {
                return false;
            }
        }

        @SuppressWarnings("t")
        private static boolean matchUuidUnsafe(StringReader reader) throws IOException {
            int read;
            int index = 0;
            while ((read = reader.read()) != -1) {
                if (fromChar((char) read) != null) return false;
                if (index == 8 || index == 12 || index == 16 || index == 20) {
                    if ((char) read == '-') index++;
                    else return false;
                    continue;
                }

                if (index > 32) return false;
                else if (index == 32) {
                    reader.mark(1);
                    if ((read = reader.read()) == -1 || fromChar((char) read) != null) return false;
                    reader.reset();
                    return true;
                }

                if (!Character.isDigit((char) read)) return false;

                index++;
            }
            return true;
        }

        private static @NotNull String readAlphaNumericWithSeps(StringReader reader) throws IOException {
            return readAllIn("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-".toCharArray(), reader);
        }

        private static boolean matchAlphaNumericWithSeps(StringReader reader) {
            int read;
            try {
                while ((read = reader.read()) != -1) {
                    if (fromChar((char) read) != null) return true;
                    if (!Character.isLetterOrDigit((char) read) && (char) read != '_' && (char) read != '-')
                        return false;
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        private static @NotNull String readAlphaNumericPlusSpaces(StringReader reader) throws IOException {
            return readAllIn("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ -".toCharArray(), reader);
        }

        private static boolean matchAlphaNumericPlusSpaces(StringReader reader) {
            int read;
            try {
                while ((read = reader.read()) != -1) {
                    if (fromChar((char) read) != null) return true;
                    if (!Character.isLetterOrDigit((char) read) && (char) read != '_' && (char) read != '-' && (char) read != ' ')
                        return false;
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        private static @NotNull String readAlphaNumericWithUnderscore(StringReader reader) throws IOException {
            return readAllIn("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".toCharArray(), reader);
        }

        private static boolean matchAlphaNumericWithUnderscore(StringReader reader) {
            int read;
            try {
                while ((read = reader.read()) != -1) {
                    if (fromChar((char) read) != null) return true;
                    if (!Character.isLetterOrDigit((char) read) && (char) read != '_') return false;
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        private static @NotNull String readAlphaNumericString(StringReader reader) throws IOException {
            return readAllIn("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray(), reader);
        }

        private static boolean matchAlphaNumericString(StringReader reader) {
            int read;
            try {
                while ((read = reader.read()) != -1) {
                    if (fromChar((char) read) != null) return true;
                    if (!Character.isLetterOrDigit((char) read)) return false;
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        private static String readAllIn(char[] charArray, StringReader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            int read;
            while ((read = reader.read()) != -1) {
                if (charArray[read] == 0) break;
                builder.append((char) read);
            }
            return builder.toString();
        }

        private static NamespaceID readNamespaceID(StringReader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            int read;
            boolean only = false;
            boolean hasRead = false;
            while ((read = reader.read()) != -1) {
                if (read == ':') break;
                if (fromChar((char) read) != null) {
                    if (!hasRead) throw new IOException("Expected namespace ID domain");
                    only = true;
                    reader.reset();
                    break;
                }
                if (!Character.isLetterOrDigit((char) read)
                    && (char) read != '_'
                    && (char) read != '-') throw new IOException("Expected namespace ID");
                builder.append((char) read);
                hasRead = true;
            }

            String domain = builder.toString();
            if (only) return new NamespaceID(builder.toString());

            builder = new StringBuilder();
            String path = readPath(reader, builder);
            return new NamespaceID(domain, path);
        }

        private static boolean matchNamespaceID(StringReader reader) {
            try {
                int read;

                reader.mark(1);
                while ((read = reader.read()) != -1) {
                    if (read == ':') break;
                    if (fromChar((char) read) != null) {
                        reader.reset();
                        return true;
                    }
                    if (!Character.isLetterOrDigit((char) read)
                        && (char) read != '_'
                        && (char) read != '-') return false;

                    reader.mark(1);
                }

                return matchPath(reader);
            } catch (IOException e) {
                return false;
            }
        }

        private static @NotNull String readPath(StringReader reader, StringBuilder builder) throws IOException {
            int read;
            while (true) {
                if ((read = reader.read()) == -1) break;
                if (fromChar((char) read) != null) break;
                if (!Character.isLetterOrDigit((char) read)
                    && (char) read != '_'
                    && (char) read != '/'
                    && (char) read != '-'
                    && (char) read != '.') throw new IOException("Expected namespace ID");
                builder.append((char) read);
            }
            return builder.toString();
        }

        private static boolean matchPath(StringReader reader) throws IOException {
            int read;

            reader.mark(1);
            while (true) {
                if ((read = reader.read()) == -1) return true;
                if (fromChar((char) read) != null) {
                    reader.reset();
                    return true;
                }
                if (!Character.isLetterOrDigit((char) read)
                    && (char) read != '_'
                    && (char) read != '/'
                    && (char) read != '-'
                    && (char) read != '.') return false;

                reader.mark(1);
            }
        }

        public static Type<?> fromChar(char c) {
            switch (c) {
                case '#':
                    return TAG;
                case '.':
                    return CLASS;
                case '*':
                    return ID;
                case '@':
                    return NAME;
                case '?':
                    return DATA;
                case ':':
                    return UUID;
                case '=':
                    return DISPLAY_NAME;
                case '+':
                    return CUSTOM_NAME;
                case '$':
                    return VARIABLE;
                default:
                    return null;
            }
        }

        public static Selector<?> parse(StringReader reader) throws IOException {
            int read = reader.read();
            if (read == -1) return null;
            char c = (char) read;
            Type<?> type = fromChar(c);
            if (type == null) throw new IllegalArgumentException("Invalid selector type: " + c);
            return gottaLoveGenerics(reader, type);
        }

        private static <T> @NotNull Selector<T> gottaLoveGenerics(StringReader reader, Type<T> type) throws IOException {
            return new Selector<>(type, type.reader.apply(reader));
        }

        public boolean match(StringReader stringReader) {
            return matcher.test(stringReader);
        }
    }
}
