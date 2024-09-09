package dev.ultreon.quantum;

import dev.ultreon.langgen.LangGenConfig;
import dev.ultreon.langgen.javascript.JavascriptGen;
import dev.ultreon.langgen.python.PythonGen;
import kotlin.io.FilesKt;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LangGenMain {
    public static void genBindings() throws IOException {
        Path langGenMeta = Paths.get("LastLangGenMeta.txt");
        ModContainer selfMod = FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow();
        String currentVersion = selfMod.getMetadata().getVersion().getFriendlyString();
        if (Files.notExists(langGenMeta) || !Files.readString(langGenMeta).equals(currentVersion)) {
            Path quantumJs = Paths.get("mods/javascript/@ultreon/quantumjs");
            Path pyQuantum = Paths.get("mods/python/pyquantum");
            FilesKt.deleteRecursively(quantumJs.toFile());
            FilesKt.deleteRecursively(pyQuantum.toFile());

            Files.createDirectories(quantumJs);
            Files.createDirectories(pyQuantum);

            PythonGen pythonGen = new PythonGen();
            pythonGen.write(pyQuantum);

            JavascriptGen javascriptGen = new JavascriptGen();
            javascriptGen.write(quantumJs);

            Files.writeString(langGenMeta, currentVersion);
            LangGenConfig.progressListener.onDone();
        } else {
            LangGenConfig.progressListener.onDone();
        }
    }
}
