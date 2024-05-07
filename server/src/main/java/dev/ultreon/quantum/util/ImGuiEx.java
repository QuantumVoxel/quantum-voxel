package dev.ultreon.quantum.util;

import com.ultreon.libs.commons.v0.util.EnumUtils;
import com.ultreon.libs.commons.v0.vector.*;
import com.ultreon.libs.functions.v0.consumer.*;
import com.ultreon.libs.functions.v0.supplier.FloatSupplier;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.*;

public class ImGuiEx {
    public static void text(String label, Supplier<Object> value) {
        ImGui.text(label);
        ImGui.sameLine();
        Object o;
        try {
            o = value.get();
        } catch (Exception e) {
            o = "~@# " + e.getClass().getName() + " #@~";
        }
        ImGui.text(String.valueOf(o));
    }

    public static void editString(String label, String id, Supplier<String> value, Consumer<String> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImString i = new ImString(value.get(), 256);
            if (ImGui.inputText("##" + id, i, ImGuiInputTextFlags.EnterReturnsTrue)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editByte(String label, String id, byte value, ByteConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImShort i = new ImShort(value);
            if (ImGui.inputScalar("##" + id, ImGuiDataType.U8, i)) {
                setter.accept((byte) i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editShort(String label, String id, short value, ShortConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImShort i = new ImShort(value);
            if (ImGui.inputScalar("##" + id, ImGuiDataType.S16, i)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editInt(String label, String id, IntSupplier value, IntConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImInt i = new ImInt(value.getAsInt());
            if (ImGui.inputInt("##" + id, i)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editLong(String label, String id, LongSupplier value, LongConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImLong i = new ImLong(value.getAsLong());
            if (ImGui.inputScalar("##" + id, ImGuiDataType.S64, i)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editFloat(String label, String id, FloatSupplier value, FloatConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImFloat i = new ImFloat(value.getFloat());
            if (ImGui.inputFloat("##" + id, i, 0, 0, "%.6f")) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editDouble(String label, String id, DoubleSupplier value, DoubleConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImDouble i = new ImDouble(value.getAsDouble());
        if (ImGui.inputDouble("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void editBool(String label, String id, BooleanSupplier value, BooleanConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImBoolean i = new ImBoolean(value.getAsBoolean());
        if (ImGui.checkbox("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void bool(String label, BooleanSupplier value) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImGui.checkbox("##", value.getAsBoolean());
        } catch (Exception e) {
            ImGui.text("~@# " + e.getClass().getName() + " #@~");
        }
    }

    public static void slider(String label, String id, int value, int min, int max, IntConsumer onChange) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            int[] v = new int[]{value};
            if (ImGui.sliderInt("##" + id, v, min, max)) {
                onChange.accept(v[0]);
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void button(String label, String id, Runnable func) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            if (ImGui.button("##" + id, 120, 16)) {
                func.run();
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor3(String color, String s, Supplier<@NotNull Color> getter, Consumer<@NotNull Color> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            Color c = getter.get();
            float[] floats = {c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f};
            if (ImGui.colorEdit3("##" + s, floats)) {
                setter.accept(new Color(floats[0], floats[1], floats[2], 1f));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor4(String color, String s, Supplier<Color> getter, Consumer<Color> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            Color c = getter.get();
            float[] floats = {c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f};
            if (ImGui.colorEdit4("##" + s, floats)) {
                setter.accept(new Color(floats[0], floats[1], floats[2], floats[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static <T extends Enum<T>> void editEnum(String s, String s1, Supplier<T> getter, Consumer<T> setter) {
        ImGui.text(s);
        ImGui.sameLine();
        try {
            T e = getter.get();
            ImInt index = new ImInt(e.ordinal());
            if (ImGui.combo("##" + s1, index, Arrays.stream(e.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new))) {
                setter.accept(EnumUtils.byOrdinal(index.get(), e));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2f(String label, String strId, Supplier<Vec2f> getter, Consumer<Vec2f> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec2f v = getter.get();
            float[] vec = {v.getX(), v.getY()};
            if (ImGui.inputFloat2("##" + strId, vec)) {
                setter.accept(new Vec2f(vec[0], vec[1]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3f(String label, String strId, Supplier<Vec3f> getter, Consumer<Vec3f> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec3f v = getter.get();
            float[] vec = {v.getX(), v.getY(), v.getZ()};
            if (ImGui.inputFloat3("##" + strId, vec)) {
                setter.accept(new Vec3f(vec[0], vec[1], vec[2]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4f(String label, String strId, Supplier<Vec4f> getter, Consumer<Vec4f> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec4f v = getter.get();
            float[] vec = {v.getX(), v.getY(), v.getZ(), v.getW()};
            if (ImGui.inputFloat4("##" + strId, vec)) {
                setter.accept(new Vec4f(vec[0], vec[1], vec[2], vec[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2i(String label, String strId, Supplier<Vec2i> getter, Consumer<Vec2i> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec2i v = getter.get();
            int[] vec = {v.getX(), v.getY()};
            if (ImGui.inputInt2("##" + strId, vec)) {
                setter.accept(new Vec2i(vec[0], vec[1]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3i(String label, String strId, Supplier<Vec3i> getter, Consumer<Vec3i> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec3i v = getter.get();
            int[] vec = {v.getX(), v.getY(), v.getZ()};
            if (ImGui.inputInt3("##" + strId, vec)) {
                setter.accept(new Vec3i(vec[0], vec[1], vec[2]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4i(String label, String strId, Supplier<Vec4i> getter, Consumer<Vec4i> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec4i v = getter.get();
            int[] vec = {v.getX(), v.getY(), v.getZ(), v.getW()};
            if (ImGui.inputInt4("##" + strId, vec)) {
                setter.accept(new Vec4i(vec[0], vec[1], vec[2], vec[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2d(String label, String strId, Supplier<Vec2d> getter, Consumer<Vec2d> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec2d v = getter.get();
            ImDouble x = new ImDouble(v.getX());
            ImDouble y = new ImDouble(v.getY());

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new Vec2d(x.get(), y.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new Vec2d(x.get(), y.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3d(String label, String strId, Supplier<Vec3d> getter, Consumer<Vec3d> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec3d v = getter.get();
            ImDouble x = new ImDouble(v.getX());
            ImDouble y = new ImDouble(v.getY());
            ImDouble z = new ImDouble(v.getZ());

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new Vec3d(x.get(), y.get(), z.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new Vec3d(x.get(), y.get(), z.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[2]", z)) {
                setter.accept(new Vec3d(x.get(), y.get(), z.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4d(String label, String strId, Supplier<Vec4d> getter, Consumer<Vec4d> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec4d v = getter.get();
            ImDouble x = new ImDouble(v.getX());
            ImDouble y = new ImDouble(v.getY());
            ImDouble z = new ImDouble(v.getZ());
            ImDouble w = new ImDouble(v.getW());

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new Vec4d(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new Vec4d(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[2]", z)) {
                setter.accept(new Vec4d(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[3]", w)) {
                setter.accept(new Vec4d(x.get(), y.get(), z.get(), w.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }
}