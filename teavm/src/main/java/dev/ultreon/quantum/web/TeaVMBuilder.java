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
        TeaReflectionSupplier.addReflectionClass("dev.ultreon.quantum");

        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);
        tool.setLog(new ConsoleTeaVMToolLog(true));
        tool.setMainClass(TeaVMLauncher.class.getName());
        tool.setTargetType(TeaVMTargetType.JAVASCRIPT);
        // For many (or most) applications, using the highest optimization won't add much to build time.
        // If your builds take too long, and runtime performance doesn't matter, you can change FULL to SIMPLE .
        tool.setOptimizationLevel(TeaVMOptimizationLevel.FULL);
        TeaBuilder.build(tool);
    }
}
