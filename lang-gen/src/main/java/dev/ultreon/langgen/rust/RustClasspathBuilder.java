package dev.ultreon.langgen.rust;

import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.GeneratorException;
import dev.ultreon.langgen.api.SimpleClasspathBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RustClasspathBuilder extends SimpleClasspathBuilder {
    public RustClasspathBuilder() {
        super(".rs", RustClassBuilder::new, RustClassBuilder::new);
    }


    @Override
    protected void writeFile(Path output, String className, String result) throws IOException {
        if (result.isBlank()) {
            throw new GeneratorException("Class " + className + " has no content");
        }

        String filePath = Converters.convert(className);
        if (filePath == null) {
            filePath = className;
        }
        String packageName = filePath.substring(0, filePath.lastIndexOf('.')).replace(".", "._");
        String classNam = filePath.substring(filePath.lastIndexOf('.') + 1);
        if (classNam.equalsIgnoreCase("mod")) {
            classNam = "__Mod"; // Special case scenario
        }
        filePath = packageName + '.' + classNam;
        Path path = output.resolve(filePath.replace(".", "/").replace('$', '_') + extension);

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }
}
