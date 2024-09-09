package dev.ultreon.langgen.python;

import org.jetbrains.annotations.NotNull;

public class PyFinalClassBuilder extends PyClassBuilder {
    public PyFinalClassBuilder(Class<?> clazz) {
        super(clazz);

        addImport(toJavaImport(clazz));
    }

    public PyFinalClassBuilder(Class<?> clazz1, boolean stub) {
        super(clazz1, stub);
    }

    @Override
    public @NotNull String getClassTemplate() {
        return """
                
                %4$s
                %5$s
                
                @final
                class %1$s(%2$s%6$s):
                
                    @staticmethod
                    def _wrap(java_value: _%1$s) -> '%1$s':
                        ...
                
                    #
                    # DO NOT USE THIS. THIS IS FOR THE JAVA WRAPPER ONLY!
                    #
                    @overload
                    def __init__(self, __dynamic__: _%1$s):
                        \"""
                        WARNING: DO NOT USE THIS. THIS IS FOR THE JAVA WRAPPER ONLY!
                        \"""
                        ...
                
                    def __getattr__(self, name: str):
                        ...
                
                    def __setattr__(self, name: str, value: Any):
                        ...
                
                    def __delattr__(self, name: str):
                        raise AttributeError("Cannot delete attribute '%%s' from %%s" %% (name, self.__wrapper.__class__.__name__))
                """;
    }
}
