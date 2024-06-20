package dev.ultreon.quantum.web;

import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier;
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass;
import org.teavm.tooling.ConsoleTeaVMToolLog;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.vm.TeaVMOptimizationLevel;

import java.io.File;
import java.io.IOException;

/**
 * Builds the TeaVM/HTML application.
 */
@SkipClass
public class TeaVMBuilder {
    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new File("../client/src/main/resources"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();

        // Register any extra classpath assets here:
        // teaBuildConfiguration.additionalAssetsClasspathFiles.add("dev/ultreon/asset.extension");

        // Register any classes or packages that require reflection here:
//        TeaReflectionSupplier.addReflectionClass("dev.ultreon");
//        TeaReflectionSupplier.addReflectionClass("com.google");
//        TeaReflectionSupplier.addReflectionClass("com.sun");
//        TeaReflectionSupplier.addReflectionClass("java");
//        TeaReflectionSupplier.addReflectionClass("javaw");
//        TeaReflectionSupplier.addReflectionClass("javax");
//        TeaReflectionSupplier.addReflectionClass("org");
//        TeaReflectionSupplier.addReflectionClass("com");
//        TeaReflectionSupplier.addReflectionClass("dev");
//        TeaReflectionSupplier.addReflectionClass("net");
//        TeaReflectionSupplier.addReflectionClass("io");
//        TeaReflectionSupplier.addReflectionClass("zyx");
//        TeaReflectionSupplier.addReflectionClass("xyz");
//        TeaReflectionSupplier.addReflectionClass("vm");
//        TeaReflectionSupplier.addReflectionClass("jdk");
//        TeaReflectionSupplier.addReflectionClass("emu");
//        TeaReflectionSupplier.addReflectionClass("emujava");
//        TeaReflectionSupplier.addReflectionClass("javaemul");
//        TeaReflectionSupplier.addReflectionClass("scala");
//        TeaReflectionSupplier.addReflectionClass("it");
//        TeaReflectionSupplier.addReflectionClass("org.apache");
//        TeaReflectionSupplier.addReflectionClass("com.badlogic");
//        TeaReflectionSupplier.addReflectionClass("com.badlogicgames");
//        TeaReflectionSupplier.addReflectionClass("space.earlygrey");

        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);
        tool.setIncremental(true);
        tool.setLog(new ConsoleTeaVMToolLog(true));
        tool.setMainClass(TeaVMLauncher.class.getName());
        // For many (or most) applications, using the highest optimization won't add much to build time.
        // If your builds take too long, and runtime performance doesn't matter, you can change FULL to SIMPLE .
        tool.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE);
        TeaBuilder.build(tool);
    }
}
