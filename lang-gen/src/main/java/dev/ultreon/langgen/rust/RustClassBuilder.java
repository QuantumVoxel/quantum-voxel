package dev.ultreon.langgen.rust;

import dev.ultreon.langgen.api.ClassBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

public class RustClassBuilder implements ClassBuilder {
    private static final List<String> ILLEGAL_NAMES = List.of(
            "Signature",
            "TryFromJavaValue",
            "FromJavaValue",
            "TryIntoJavaValue",
            "IntoJavaValue",
            "type",
            "fn",
            "const",
            "let",
            "pub",
            "struct",
            "impl",
            "ref",
            "mod",
            "use"
    );

    private final Class<?> self;
    private final String classname;
    private final String name;

    public RustClassBuilder(Class<?> self) {
        String name;
        this.self = self;

        classname = self.getName();
        name = classname.substring(classname.lastIndexOf('.') + 1).replace('$', '_');

        if (ILLEGAL_NAMES.contains(name)) {
            name = "_" + name;
        }
        this.name = name;
    }

    @Override
    public @Nullable List<String> build(StringBuilder result, Path output) {
        String rustStructName = this.name;

        result.append("""
                use robusta_jni::bridge;
                use robusta_jni::convert::Signature;
                use robusta_jni::convert::TryIntoJavaValue;
                use robusta_jni::convert::IntoJavaValue;
                use robusta_jni::convert::TryFromJavaValue;
                use robusta_jni::convert::FromJavaValue;
                
                #[bridge]
                mod jni {
                    use robusta_jni::convert::Signature;
                    use robusta_jni::convert::TryIntoJavaValue;
                    use robusta_jni::convert::IntoJavaValue;
                    use robusta_jni::convert::TryFromJavaValue;
                    use robusta_jni::convert::FromJavaValue;
                
                """);

        StringBuilder content = new StringBuilder();
        classContent(content);
        result.append(content.toString().indent(4));

        result.append("}\n");

        return List.of();
    }

    private void classContent(StringBuilder result) {
        result.append("#[derive(Signature, IntoJavaValue)]\n");
        String packageName = self.getPackageName();
        String[] split = packageName.split("\\.");
        for (int i = 0; i < split.length; i++) {
            split[i] = transform(split[i]);
        }
        packageName = String.join(".", split);
        result.append("#[no_mangle]\n");
        result.append("#[repr(C)]\n");

        result.append("#[package(").append(packageName).append(")]\n");
        result.append("pub struct ").append(name).append("<'env: 'borrow, 'borrow> {\n");
        result.append("""
                    #[instance]
                    r#__self__: robusta_jni::jni::objects::AutoLocal<'env, 'borrow>,
                    """.indent(4));

        result.append("}\n\n");

        for (Method method : self.getDeclaredMethods()) {
//            result.append("    pub extern \"system\" fn ").append(rustMethodName).append("(\n");
//            result.append("        env: JNIEnv,\n");
//            result.append("        obj: JObject,\n");
//
//            Class<?>[] paramTypes = method.getParameterTypes();
//            for (int i = 0; i < paramTypes.length; i++) {
//                result.append("        arg").append(i).append(": ").append(javaTypeToRustType(paramTypes[i]));
//                if (i < paramTypes.length - 1) {
//                    result.append(",");
//                }
//                result.append("\n");
//            }
//
//            result.append("    ) -> ").append(javaTypeToRustType(method.getReturnType())).append(" {\n");
//            result.append("        // TODO: Implement method\n");
//            result.append("        unimplemented!()\n");
//            result.append("    }\n\n");
        }
    }

    private String transform(String s) {
        if (ILLEGAL_NAMES.contains(s)) {
            return "r#" + s;
        }
        return s;
    }

    @Override
    public @Nullable String convertImport(@NotNull Class<?> clazz, @NotNull Class<?> type, @NotNull String java, @NotNull String python) {
        return null;
    }
}
