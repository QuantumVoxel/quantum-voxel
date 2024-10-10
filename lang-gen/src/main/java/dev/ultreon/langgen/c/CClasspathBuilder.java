package dev.ultreon.langgen.c;

import dev.ultreon.langgen.api.Converters;
import dev.ultreon.langgen.api.SimpleClasspathBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CClasspathBuilder extends SimpleClasspathBuilder {
    public CClasspathBuilder() {
        super(".c", CClassBuilder::new, CClassBuilder::new);
    }


    @Override
    protected void writeFile(@NotNull Path output, @NotNull String className, String result) throws IOException {
        String filePath = Converters.convert(className);
        if (filePath == null) {
            filePath = className;
        }
        String packageName = filePath.substring(0, filePath.lastIndexOf('.')).replace(".", "._");
        String classNam = filePath.substring(filePath.lastIndexOf('.') + 1);
        filePath = packageName + '.' + classNam;
        Path path = output.resolve(filePath.replace(".", "/").replace('$', '_') + extension);

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }
}
