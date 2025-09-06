package dev.ultreon.quantum.desktop.imgui;

import com.badlogic.gdx.graphics.Color;
import de.damios.guacamole.func.BooleanConsumer;
import de.damios.guacamole.func.FloatConsumer;
import de.damios.guacamole.func.ShortConsumer;
import dev.ultreon.libs.commons.v0.util.EnumUtils;
import dev.ultreon.quantum.util.*;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;
import java.util.function.DoubleConsumer;
import java.util.function.LongConsumer;

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

    public static void editColor3(String color, String s, Supplier<RgbColor> getter, Consumer<RgbColor> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            RgbColor c = getter.get();
            float[] floats = {c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f};
            if (ImGui.colorEdit3("##" + s, floats)) {
                setter.accept(new RgbColor(floats[0], floats[1], floats[2], 1f));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor3Gdx(String color, String s, Supplier<Color> getter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            @NotNull com.badlogic.gdx.graphics.Color c = getter.get();
            float[] floats = {c.r, c.g, c.b, 1f};
            if (ImGui.colorEdit3("##" + s, floats)) {
                c.r = floats[0];
                c.g = floats[1];
                c.b = floats[2];
                c.a = 1f;
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor4(String color, String s, Supplier<RgbColor> getter, Consumer<RgbColor> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            RgbColor c = getter.get();
            float[] floats = {c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f};
            if (ImGui.colorEdit4("##" + s, floats)) {
                setter.accept(new RgbColor(floats[0], floats[1], floats[2], floats[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor4Gdx(String color, String s, Supplier<Color> getter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            @NotNull com.badlogic.gdx.graphics.Color c = getter.get();
            float[] floats = {c.r, c.g, c.b, c.a};
            if (ImGui.colorEdit4("##" + s, floats)) {
                c.r = floats[0];
                c.g = floats[1];
                c.b = floats[2];
                c.a = floats[3];
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
            List<String> collect = new ArrayList<>();
            for (Enum<?> constant : e.getClass().getEnumConstants()) {
                collect.add(constant.name());
            }
            if (ImGui.combo("##" + s1, index, collect.toArray(String[]::new))) {
                setter.accept(EnumUtils.byOrdinal(index.get(), e));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2f(String label, String strId, Supplier<Vec2> getter, Consumer<Vec2> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec2 v = getter.get();
            float[] vec = {v.getX(), v.getY()};
            if (ImGui.inputFloat2("##" + strId, vec)) {
                setter.accept(new Vec2(vec[0], vec[1]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3f(String label, String strId, Supplier<Vec3> getter, Consumer<Vec3> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec3 v = getter.get();
            float[] vec = {v.getX(), v.getY(), v.getZ()};
            if (ImGui.inputFloat3("##" + strId, vec)) {
                setter.accept(new Vec3(vec[0], vec[1], vec[2]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4f(String label, String strId, Supplier<Vec4> getter, Consumer<Vec4> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vec4 v = getter.get();
            float[] vec = {v.getX(), v.getY(), v.getZ(), v.getW()};
            if (ImGui.inputFloat4("##" + strId, vec)) {
                setter.accept(new Vec4(vec[0], vec[1], vec[2], vec[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2i(String label, String strId, Supplier<IVec2> getter, Consumer<IVec2> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            IVec2 v = getter.get();
            int[] vec = {v.getX(), v.getY()};
            if (ImGui.inputInt2("##" + strId, vec)) {
                setter.accept(new IVec2(vec[0], vec[1]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3i(String label, String strId, Supplier<IVec3> getter, Consumer<IVec3> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            IVec3 v = getter.get();
            int[] vec = {v.getIntX(), v.getIntY(), v.getIntZ()};
            if (ImGui.inputInt3("##" + strId, vec)) {
                setter.accept(new IVec3(vec[0], vec[1], vec[2]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4i(String label, String strId, Supplier<IVec4> getter, Consumer<IVec4> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            IVec4 v = getter.get();
            int[] vec = {v.getX(), v.getY(), v.getZ(), v.getW()};
            if (ImGui.inputInt4("##" + strId, vec)) {
                setter.accept(new IVec4(vec[0], vec[1], vec[2], vec[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2d(String label, String strId, Supplier<DVec2> getter, Consumer<DVec2> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            DVec2 v = getter.get();
            ImDouble x = new ImDouble(v.getX());
            ImDouble y = new ImDouble(v.getY());

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new DVec2(x.get(), y.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new DVec2(x.get(), y.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3d(String label, String strId, Supplier<DVec3> getter, Consumer<DVec3> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            DVec3 v = getter.get();
            ImDouble x = new ImDouble(v.getX());
            ImDouble y = new ImDouble(v.getY());
            ImDouble z = new ImDouble(v.getZ());

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new DVec3(x.get(), y.get(), z.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new DVec3(x.get(), y.get(), z.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[2]", z)) {
                setter.accept(new DVec3(x.get(), y.get(), z.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4d(String label, String strId, Supplier<DVec4> getter, Consumer<DVec4> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            DVec4 v = getter.get();
            ImDouble x = new ImDouble(v.getX());
            ImDouble y = new ImDouble(v.getY());
            ImDouble z = new ImDouble(v.getZ());
            ImDouble w = new ImDouble(v.getW());

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new DVec4(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new DVec4(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[2]", z)) {
                setter.accept(new DVec4(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[3]", w)) {
                setter.accept(new DVec4(x.get(), y.get(), z.get(), w.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }
}