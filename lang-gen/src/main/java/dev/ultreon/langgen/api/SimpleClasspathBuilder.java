package dev.ultreon.langgen.api;

import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleClasspathBuilder extends ClasspathBuilder {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleClasspathBuilder.class);
    private final Logger logger;
    protected final String extension;
    private final Function<Class<?>, ClassBuilder> finalClassBuilder;
    private final Function<Class<?>, ClassBuilder> classBuilder;

    public SimpleClasspathBuilder(String extension, Function<Class<?>, ClassBuilder> finalClassBuilder, Function<Class<?>, ClassBuilder> classBuilder) {
        super(false);
        this.finalClassBuilder = finalClassBuilder;
        this.classBuilder = classBuilder;
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        this.extension = extension;
        this.logger = Logger.getLogger("ClasspathBuilder" + extension);
    }

    @Override
    protected void writeFile(Path output, String className, String result) throws IOException {
        if (!Files.exists(output)) {
            Files.createDirectories(output);
        }
        if (result.isBlank()) {
            throw new GeneratorException("Class " + className + " has no content");
        }

        String filePath = Converters.convert(className);
        if (filePath == null) {
            filePath = className;
        }
        Path path = output.resolve(filePath.replace('.', '/') + extension);

        Files.createDirectories(path.getParent());
        Files.writeString(path, result);
    }

    @Override
    protected @Nullable String visitClass(Path output, Class<?> clazz, StringBuilder sb) {
        StringBuilder builder = new StringBuilder();

        try {
            ClassBuilder chosen = Modifier.isFinal(clazz.getModifiers())
                    ? this.finalClassBuilder.apply(clazz)
                    : this.classBuilder.apply(clazz);

            String className = clazz.getName();
            String filePath = Converters.convert(className);
            if (filePath == null) {
                filePath = className;
            }
            Path path = output.resolve(filePath.replace('.', '/') + extension);

            List<String> imports = chosen.build(builder, path);
            if (imports == null) return null;

            for (String anImport : imports) {
                sb.append(anImport).append('\n');
            }
        } catch (GeneratorException e) {
            throw e;
        } catch (NoClassDefFoundError e) {
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while building class " + clazz.getName(), e);
            return null;
        }

        String trim = builder.toString().trim();
        if (!trim.isEmpty()) {
            sb.append(trim);
        } else {
//            logger.warning("Class " + clazz.getName() + " has no content");
        }

        return sb.toString();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    public String transformName(Class<?> entry) {
        String convert = Converters.convert(entry.getName());
        if (convert != null && !convert.equals(entry.getName())) {
            return convert;
        }

        return entry.getName();
    }
}
