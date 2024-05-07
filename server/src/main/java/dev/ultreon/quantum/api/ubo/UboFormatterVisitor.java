package dev.ultreon.quantum.api.ubo;

import dev.ultreon.ubo.DataTypes;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.ubo.util.DataTypeVisitor;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.Color;

import java.util.BitSet;
import java.util.List;

class UboFormatterVisitor implements DataTypeVisitor<TextObject> {
    private final int ident;

    public UboFormatterVisitor(int ident) {
        this.ident = ident;
    }

    @Override
    public TextObject visit(DataType<?> type) {
        int id = type.id();
        if (id == DataTypes.BYTE) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("b").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.SHORT) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("s").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.INT) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("i").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.LONG) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("l").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.FLOAT) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("f").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.DOUBLE) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("d").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.BIG_INT) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("I").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.BIG_DEC) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("D").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.BOOLEAN) {
            return TextObject.literal(String.valueOf(type.getValue())).setColor(Color.MAGENTA);
        } else if (id == DataTypes.STRING) {
            String replace = String.valueOf(type.getValue())
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\\", "\\\\");
            return TextObject.literal("\"" + replace + "\"").setColor(Color.GREEN);
        } else if (id == DataTypes.CHAR) {
            String replace = String.valueOf(type.getValue())
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\\", "\\\\");
            return TextObject.literal("'" + replace + "'").setColor(Color.GREEN);
        } else if (id == DataTypes.BIT_SET) {
            MutableText result = TextObject.literal("x").setColor(Color.LIGHT_GRAY.brighter().brighter());
            for (int i = 0; i < ((BitSet) type.getValue()).length(); i++) {
                result.append(TextObject.literal(String.valueOf(((BitSet) type.getValue()).get(i) ? 1 : 0)).setColor(Color.CYAN));
            }

            return result;
        } else if (id == DataTypes.UUID) {
            String[] replace = String.valueOf(type.getValue()).split("-");

            MutableText result = TextObject.literal(replace[0]).setColor(Color.YELLOW);
            for (int i = 1; i < replace.length; i++) {
                result.append(TextObject.literal("-").setColor(Color.WHITE));
                result.append(TextObject.literal(replace[i]).setColor(Color.YELLOW));
            }

            return MutableText.literal("<").setColor(Color.LIGHT_GRAY).append(result).append(TextObject.literal(">").setColor(Color.LIGHT_GRAY));
        } else if (id == DataTypes.LIST) {
            MutableText result = TextObject.literal("[").append(TextObject.literal("\n  ").setColor(Color.WHITE)).append(TextObject.literal("  ".repeat(ident))).setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < ((List<?>) type.getValue()).size(); i++) {
                result.append(UboFormatter.format((DataType<?>) ((List<?>) type.getValue()).get(i), ident + 1));
                if (i < ((List<?>) type.getValue()).size() - 1) {
                    result.append(TextObject.literal(",").append(TextObject.literal("\n  ").append(TextObject.literal("  ".repeat(ident)))).setColor(Color.WHITE));
                }
            }
            result.append(TextObject.literal("\n").append(TextObject.literal("  ".repeat(ident))).append(TextObject.literal("]").setColor(Color.LIGHT_GRAY)));
            return result;
        } else if (id == DataTypes.MAP) {
            MutableText result = TextObject.literal("{").append(TextObject.literal("\n  ").append(TextObject.literal("  ".repeat(ident))).setColor(Color.WHITE)).setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < ((MapType) type).getValue().size(); i++) {
                String s = ((String) ((MapType) type).getValue().keySet().toArray()[i])
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                        .replace("\\", "\\\\");

                result.append(TextObject.literal("\"" + s + "\"").setColor(Color.GRAY));
                result.append(TextObject.literal(": ").setColor(Color.WHITE));
                result.append(UboFormatter.format((DataType<?>) ((MapType) type).getValue().values().toArray()[i], ident + 1));
                if (i < ((MapType) type).getValue().size() - 1) {
                    result.append(TextObject.literal(",  ").append(TextObject.literal("\n  ").append(TextObject.literal("  ".repeat(ident))).setColor(Color.WHITE)).setColor(Color.WHITE));
                }
            }
            result.append(TextObject.literal("\n").append(TextObject.literal("  ".repeat(ident))).append(TextObject.literal("}").setColor(Color.LIGHT_GRAY)));
            return result;
        } else if (id == DataTypes.BYTE_ARRAY) {
            MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
            result.append(TextObject.literal("b").setColor(Color.YELLOW));
            result.append(TextObject.literal(";").setColor(Color.WHITE));
            for (int i = 0; i < ((byte[]) type.getValue()).length; i++) {
                result.append(TextObject.literal(String.valueOf(((byte[]) type.getValue())[i])).setColor(Color.CYAN));
                if (i < ((byte[]) type.getValue()).length - 1) {
                    result.append(TextObject.literal(", ").setColor(Color.WHITE));
                }
            }

            result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

            return result;
        } else if (id == DataTypes.SHORT_ARRAY) {
            MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
            result.append(TextObject.literal("s").setColor(Color.YELLOW));
            result.append(TextObject.literal(";").setColor(Color.WHITE));
            for (int i = 0; i < ((short[]) type.getValue()).length; i++) {
                result.append(TextObject.literal(String.valueOf(((short[]) type.getValue())[i])).setColor(Color.CYAN));
                if (i < ((short[]) type.getValue()).length - 1) {
                    result.append(TextObject.literal(", ").setColor(Color.WHITE));
                }
            }

            result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

            return result;
        } else if (id == DataTypes.INT_ARRAY) {
            MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
            result.append(TextObject.literal("i").setColor(Color.YELLOW));
            result.append(TextObject.literal(";").setColor(Color.WHITE));
            for (int i = 0; i < ((int[]) type.getValue()).length; i++) {
                result.append(TextObject.literal(String.valueOf(((int[]) type.getValue())[i])).setColor(Color.CYAN));
                if (i < ((int[]) type.getValue()).length - 1) {
                    result.append(TextObject.literal(", ").setColor(Color.WHITE));
                }
            }

            result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

            return result;
        } else if (id == DataTypes.LONG_ARRAY) {
            MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
            result.append(TextObject.literal("l").setColor(Color.YELLOW));
            result.append(TextObject.literal(";").setColor(Color.WHITE));
            for (int i = 0; i < ((long[]) type.getValue()).length; i++) {
                result.append(TextObject.literal(String.valueOf(((long[]) type.getValue())[i])).setColor(Color.CYAN));
                if (i < ((long[]) type.getValue()).length - 1) {
                    result.append(TextObject.literal(", ").setColor(Color.WHITE));
                }
            }

            result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

            return result;
        } else if (id == DataTypes.FLOAT_ARRAY) {
            MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
            result.append(TextObject.literal("f").setColor(Color.YELLOW));
            result.append(TextObject.literal(";").setColor(Color.WHITE));
            for (int i = 0; i < ((float[]) type.getValue()).length; i++) {
                result.append(TextObject.literal(String.valueOf(((float[]) type.getValue())[i])).setColor(Color.CYAN));
                if (i < ((float[]) type.getValue()).length - 1) {
                    result.append(TextObject.literal(", ").setColor(Color.WHITE));
                }
            }

            result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

            return result;
        } else if (id == DataTypes.DOUBLE_ARRAY) {
            MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
            result.append(TextObject.literal("d").setColor(Color.YELLOW));
            result.append(TextObject.literal(";").setColor(Color.WHITE));
            for (int i = 0; i < ((double[]) type.getValue()).length; i++) {
                result.append(TextObject.literal(String.valueOf(((double[]) type.getValue())[i])).setColor(Color.CYAN));
                if (i < ((double[]) type.getValue()).length - 1) {
                    result.append(TextObject.literal(", ").setColor(Color.WHITE));
                }
            }

            result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

            return result;
        }
        return TextObject.literal(String.valueOf(type.getValue()));
    }
}
