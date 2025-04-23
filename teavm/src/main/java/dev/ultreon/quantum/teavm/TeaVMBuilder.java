package dev.ultreon.quantum.teavm;

import com.github.xpenatan.gdx.backends.teavm.config.AssetFileHandle;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass;
import java.io.File;
import java.io.IOException;

import org.teavm.tooling.TeaVMSourceFilePolicy;
import org.teavm.tooling.TeaVMTargetType;
import org.teavm.tooling.TeaVMTool;
import org.teavm.tooling.TeaVMToolLog;
import org.teavm.tooling.sources.DirectorySourceFileProvider;
import org.teavm.tooling.sources.JarSourceFileProvider;
import org.teavm.vm.TeaVMOptimizationLevel;

/** Builds the TeaVM/HTML application. */
@SkipClass
public class TeaVMBuilder {
    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new AssetFileHandle("../client/src/main/resources"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();

        // Register any extra classpath assets here:
        // teaBuildConfiguration.additionalAssetsClasspathFiles.add("dev/ultreon/asset.extension");

        // Register any classes or packages that require reflection here:
        // TeaReflectionSupplier.addReflectionClass("dev.ultreon.aero7.reflect");

        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);
        tool.setMainClass(TeaVMLauncher.class.getName());
        // For many (or most) applications, using the highest optimization won't add much to build time.
        // If your builds take too long, and runtime performance doesn't matter, you can change FULL to SIMPLE .
        tool.setOptimizationLevel(TeaVMOptimizationLevel.SIMPLE);
        tool.setTargetType(TeaVMTargetType.JAVASCRIPT);
        tool.setObfuscated(true);
        tool.setShortFileNames(false);
        tool.setSourceMapsFileGenerated(true);
        tool.setDebugInformationGenerated(true);
        tool.setSourceFilePolicy(TeaVMSourceFilePolicy.COPY);
        tool.addSourceFileProvider(new DirectorySourceFileProvider(new File("../client/src/main/java")));
        tool.addSourceFileProvider(new DirectorySourceFileProvider(new File("../server/src/main/java")));
        tool.addSourceFileProvider(new DirectorySourceFileProvider(new File("../teavm/src/main/java")));

        String sourceJars = System.getenv("SOURCE_JARS");
        if (sourceJars != null) {
            String[] split = sourceJars.split(File.pathSeparator);
            System.out.println("Adding " + split.length + " source jars");
            tool.getLog().info("Adding " + split.length + " source jars");
            for (String sourceJar : split) {
                System.out.println("Adding source jar: " + sourceJar);
                tool.getLog().info("Adding source jar: " + sourceJar);
                tool.addSourceFileProvider(new JarSourceFileProvider(new File(sourceJar)));
            }
        } else {
            System.out.println("No source jars found");
            tool.getLog().info("No source jars found");
        }
        TeaBuilder.build(tool);
    }
}
