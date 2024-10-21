package dev.ultreon.quantum.desktop.imgui

import com.badlogic.gdx.graphics.Color
import dev.ultreon.libs.commons.v0.util.EnumUtils
import dev.ultreon.libs.functions.v0.consumer.*
import dev.ultreon.libs.functions.v0.consumer.IntConsumer
import dev.ultreon.libs.functions.v0.supplier.FloatSupplier
import dev.ultreon.quantum.util.*
import imgui.ImGui
import imgui.flag.ImGuiDataType
import imgui.flag.ImGuiInputTextFlags
import imgui.type.*
import java.util.function.*
import java.util.function.DoubleConsumer
import java.util.function.LongConsumer

object ImGuiEx {
  fun text(label: String?, value: Supplier<Any>) {
    ImGui.text(label)
    ImGui.sameLine()
    val o = try {
      value.get()
    } catch (e: Exception) {
      "~@# " + e.javaClass.name + " #@~"
    }
    ImGui.text(o.toString())
  }

  fun editString(label: String?, id: String, value: Supplier<String?>, setter: Consumer<String?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val i = ImString(value.get(), 256)
      if (ImGui.inputText("##$id", i, ImGuiInputTextFlags.EnterReturnsTrue)) {
        setter.accept(i.get())
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editByte(label: String?, id: String, value: Byte, setter: ByteConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val i = ImShort(value.toShort())
      if (ImGui.inputScalar("##$id", ImGuiDataType.U8, i)) {
        setter.accept(i.get().toByte())
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editShort(label: String?, id: String, value: Short, setter: ShortConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val i = ImShort(value)
      if (ImGui.inputScalar("##$id", ImGuiDataType.S16, i)) {
        setter.accept(i.get())
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  @JvmStatic
  fun editInt(label: String?, id: String, value: IntSupplier, setter: IntConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val i = ImInt(value.asInt)
      if (ImGui.inputInt("##$id", i)) {
        setter.accept(i.get())
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editLong(label: String?, id: String, value: LongSupplier, setter: LongConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val i = ImLong(value.asLong)
      if (ImGui.inputScalar("##$id", ImGuiDataType.S64, i)) {
        setter.accept(i.get())
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editFloat(label: String?, id: String, value: FloatSupplier, setter: FloatConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val i = ImFloat(value.float)
      if (ImGui.inputFloat("##$id", i, 0f, 0f, "%.6f")) {
        setter.accept(i.get())
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editDouble(label: String?, id: String, value: DoubleSupplier, setter: DoubleConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    val i = ImDouble(value.asDouble)
    if (ImGui.inputDouble("##$id", i)) {
      setter.accept(i.get())
    }
  }

  @JvmStatic
  fun editBool(label: String?, id: String, value: BooleanSupplier, setter: BooleanConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    val i = ImBoolean(value.asBoolean)
    if (ImGui.checkbox("##$id", i)) {
      setter.accept(i.get())
    }
  }

  fun bool(label: String?, value: BooleanSupplier) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      ImGui.checkbox("##", value.asBoolean)
    } catch (e: Exception) {
      ImGui.text("~@# " + e.javaClass.name + " #@~")
    }
  }

  fun slider(label: String?, id: String, value: Int, min: Int, max: Int, onChange: IntConsumer) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = intArrayOf(value)
      if (ImGui.sliderInt("##$id", v, min, max)) {
        onChange.accept(v[0])
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  @JvmStatic
  fun button(label: String?, id: String, func: Runnable) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      if (ImGui.button("##$id", 120f, 16f)) {
        func.run()
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editColor3(color: String?, s: String, getter: Supplier<RgbColor>, setter: Consumer<RgbColor>) {
    ImGui.text(color)
    ImGui.sameLine()
    try {
      val c = getter.get()
      val floats = floatArrayOf(c.red / 255f, c.green / 255f, c.blue / 255f, 1f)
      if (ImGui.colorEdit3("##$s", floats)) {
        setter.accept(RgbColor(floats[0], floats[1], floats[2], 1f))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editColor3Gdx(color: String?, s: String, getter: Supplier<Color>) {
    ImGui.text(color)
    ImGui.sameLine()
    try {
      val c = getter.get()
      val floats = floatArrayOf(c.r, c.g, c.b, 1f)
      if (ImGui.colorEdit3("##$s", floats)) {
        c.r = floats[0]
        c.g = floats[1]
        c.b = floats[2]
        c.a = 1f
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editColor4(color: String?, s: String, getter: Supplier<RgbColor>, setter: Consumer<RgbColor?>) {
    ImGui.text(color)
    ImGui.sameLine()
    try {
      val c = getter.get()
      val floats = floatArrayOf(c.red / 255f, c.green / 255f, c.blue / 255f, c.alpha / 255f)
      if (ImGui.colorEdit4("##$s", floats)) {
        setter.accept(RgbColor(floats[0], floats[1], floats[2], floats[3]))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editColor4Gdx(color: String?, s: String, getter: Supplier<Color>) {
    ImGui.text(color)
    ImGui.sameLine()
    try {
      val c = getter.get()
      val floats = floatArrayOf(c.r, c.g, c.b, c.a)
      if (ImGui.colorEdit4("##$s", floats)) {
        c.r = floats[0]
        c.g = floats[1]
        c.b = floats[2]
        c.a = floats[3]
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun <T : Enum<T>?> editEnum(s: String?, s1: String, getter: Supplier<T>, setter: Consumer<T>) {
    ImGui.text(s)
    ImGui.sameLine()
    try {
      val e = getter.get()
      val index = ImInt(e!!.ordinal)
      if (ImGui.combo(
          "##$s1",
          index,
          e.javaClass.enumConstants.asSequence().map { it.name }.toList().toTypedArray()
      )) {
        setter.accept(EnumUtils.byOrdinal(index.get(), e))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec2f(label: String?, strId: String, getter: Supplier<Vec2f>, setter: Consumer<Vec2f?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val vec = floatArrayOf(v.getX(), v.getY())
      if (ImGui.inputFloat2("##$strId", vec)) {
        setter.accept(Vec2f(vec[0], vec[1]))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec3f(label: String?, strId: String, getter: Supplier<Vec3f>, setter: Consumer<Vec3f?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val vec = floatArrayOf(v.getX(), v.getY(), v.getZ())
      if (ImGui.inputFloat3("##$strId", vec)) {
        setter.accept(Vec3f(vec[0], vec[1], vec[2]))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec4f(label: String?, strId: String, getter: Supplier<Vec4f>, setter: Consumer<Vec4f?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val vec = floatArrayOf(v.getX(), v.getY(), v.getZ(), v.getW())
      if (ImGui.inputFloat4("##$strId", vec)) {
        setter.accept(Vec4f(vec[0], vec[1], vec[2], vec[3]))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec2i(label: String?, strId: String, getter: Supplier<Vec2i>, setter: Consumer<Vec2i?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val vec = intArrayOf(v.getX(), v.getY())
      if (ImGui.inputInt2("##$strId", vec)) {
        setter.accept(Vec2i(vec[0], vec[1]))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec3i(label: String?, strId: String, getter: Supplier<Vec3i>, setter: Consumer<Vec3i?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val vec = intArrayOf(v.intX, v.intY, v.intZ)
      if (ImGui.inputInt3("##$strId", vec)) {
        setter.accept(Vec3i(vec[0], vec[1], vec[2]))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec4i(label: String?, strId: String, getter: Supplier<Vec4i>, setter: Consumer<Vec4i?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val vec = intArrayOf(v.getX(), v.getY(), v.getZ(), v.getW())
      if (ImGui.inputInt4("##$strId", vec)) {
        setter.accept(Vec4i(vec[0], vec[1], vec[2], vec[3]))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec2d(label: String?, strId: String, getter: Supplier<Vec2d>, setter: Consumer<Vec2d?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val x = ImDouble(v.getX())
      val y = ImDouble(v.getY())

      if (ImGui.inputDouble("##$strId[0]", x)) {
        setter.accept(Vec2d(x.get(), y.get()))
      }

      if (ImGui.inputDouble("##$strId[1]", y)) {
        setter.accept(Vec2d(x.get(), y.get()))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec3d(label: String?, strId: String, getter: Supplier<Vec3d>, setter: Consumer<Vec3d?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val x = ImDouble(v.getX())
      val y = ImDouble(v.getY())
      val z = ImDouble(v.getZ())

      if (ImGui.inputDouble("##$strId[0]", x)) {
        setter.accept(Vec3d(x.get(), y.get(), z.get()))
      }

      if (ImGui.inputDouble("##$strId[1]", y)) {
        setter.accept(Vec3d(x.get(), y.get(), z.get()))
      }

      if (ImGui.inputDouble("##$strId[2]", z)) {
        setter.accept(Vec3d(x.get(), y.get(), z.get()))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }

  fun editVec4d(label: String?, strId: String, getter: Supplier<Vec4d>, setter: Consumer<Vec4d?>) {
    ImGui.text(label)
    ImGui.sameLine()
    try {
      val v = getter.get()
      val x = ImDouble(v.getX())
      val y = ImDouble(v.getY())
      val z = ImDouble(v.getZ())
      val w = ImDouble(v.getW())

      if (ImGui.inputDouble("##$strId[0]", x)) {
        setter.accept(Vec4d(x.get(), y.get(), z.get(), w.get()))
      }

      if (ImGui.inputDouble("##$strId[1]", y)) {
        setter.accept(Vec4d(x.get(), y.get(), z.get(), w.get()))
      }

      if (ImGui.inputDouble("##$strId[2]", z)) {
        setter.accept(Vec4d(x.get(), y.get(), z.get(), w.get()))
      }

      if (ImGui.inputDouble("##$strId[3]", w)) {
        setter.accept(Vec4d(x.get(), y.get(), z.get(), w.get()))
      }
    } catch (e: Exception) {
      ImGui.text(e.toString())
    }
  }
}