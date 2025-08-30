//package dev.ultreon.quantum.scripting;
//
//class ScriptClassShutter implements ClassShutter {
//    @Override
//    public boolean visibleToScripts(String fullClassName) {
//        if (fullClassName.startsWith("dev.ultreon.quantum.")) return true;
//        if (fullClassName.startsWith("dev.ultreon.ubo.")) return true;
//        if (fullClassName.startsWith("dev.ultreon.corelibs.")) return true;
//        if (fullClassName.startsWith("java.lang.") && !fullClassName.startsWith("java.lang.reflect.") && !fullClassName.startsWith("java.lang.invoke.")) return true;
//        if (fullClassName.startsWith("java.util.")) return true;
//        if (fullClassName.startsWith("com.badlogicgames.")) return true;
//        if (fullClassName.startsWith("org.lwjgl.")) return true;
//        if (fullClassName.startsWith("org.joml.")) return true;
//        if (fullClassName.startsWith("org.jetbrains.annotations.")) return true;
//        if (fullClassName.startsWith("org.slf4j.")) return true;
//        if (fullClassName.startsWith("org.apache.logging.log4j.")) return true;
//        if (fullClassName.startsWith("org.apache.commons.lang3.")) return true;
//        if (fullClassName.startsWith("org.apache.commons.collections4.")) return true;
//        if (fullClassName.startsWith("org.apache.commons.text.")) return true;
//        if (fullClassName.startsWith("org.apache.commons.math.")) return true;
//        if (fullClassName.startsWith("org.apache.commons.math3.")) return true;
//
//        if (fullClassName.startsWith("org.apache.logging.slf4j.")) return false;
//        if (fullClassName.startsWith("org.apache.commons.io.")) return false;
//        if (fullClassName.startsWith("org.mozilla.")) return false;
//        return fullClassName.startsWith("com.github.tommyettinger.");
//    }
//}
