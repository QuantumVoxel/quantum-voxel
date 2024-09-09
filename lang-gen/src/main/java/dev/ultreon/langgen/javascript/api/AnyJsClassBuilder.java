package dev.ultreon.langgen.javascript.api;

import dev.ultreon.langgen.api.ClassBuilder;
import dev.ultreon.langgen.api.Converters;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public interface AnyJsClassBuilder extends ClassBuilder {
    default String convertImport(Class<?> owner, Class<?> target, String javaName, String targetName) {
        try {
            if (target.getName().equals(owner.getName())) return null;

            String prefix = prefix(owner);

            String name = target.getName();
            String[] split = name.split("\\.");
            String simpleName = split[split.length - 1];
            String converted = Converters.convert(name);
            String importedName = name.replace(".", "$");
            if (converted != null) {
                importedName = converted.replace(".", "$");
            }

            if (targetName.equals(javaName)) {
                int endIndex = targetName.lastIndexOf(".");
                if (endIndex == -1) {
                    return "import " + importedName + " from './" + prefix + "/" + simpleName + ".mjs'; // FIXME: Unchanged";
                }

                String pkg = targetName.substring(0, endIndex).replace(".", "/");
                return "import " + importedName + " from './" + prefix + "/" + pkg + "/" + simpleName + ".mjs'; // FIXME: Unchanged";
            }

            int endIndex = targetName.lastIndexOf(".");
            if (endIndex == -1) {
                return "import " + importedName + " from './" + prefix + "/" + simpleName + ".mjs';";
            }

            String pkg = targetName.substring(0, endIndex).replace(".", "/");
            return "import " + importedName + " from './" + prefix + "/" + pkg + "/" + simpleName + ".mjs';";
        } catch (Exception e) {
            throw new Error("Something went wrong", e);
        }
    }

    static @NotNull String prefix(Class<?> owner) {
        String convert = Converters.convert(owner.getName());

        if (convert == null) convert = owner.getName();
        String prefix = Arrays.stream(convert.split("\\.")).map(v -> "..").collect(Collectors.joining("/"));
        if (prefix.endsWith("/..")) prefix = prefix.substring(0, prefix.length() - 3);
        return prefix;
    }
}
