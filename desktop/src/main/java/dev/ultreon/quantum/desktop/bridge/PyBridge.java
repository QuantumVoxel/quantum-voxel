package dev.ultreon.quantum.desktop.bridge;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.desktop.bridge.gen.PyBindingGenerator;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;
import java.nio.file.*;

public class PyBridge implements LanguageBridge {
    private Context context;
    private BridgeAPI api;

    @Override
    public void init() {
        this.context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build();

        try {
            Path path = Paths.get("scripts/quantum/bridge/__init__.py");
            if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
            Files.writeString(path, """
                    def init():
                        import typing as tp
                    
                        class EventRegistry:
                            def __init__(self):
                                self.events: tp.Dict[str, tp.Callable[[tp.Any, ...], None]] = {}
                    
                            def register(self, event, func):
                                self.events[event] = func
                    
                            def fire(self, event, *args):
                                if event in self.events:
                                    self.events[event](*args)
                    
                        def init_inner(mapping):
                            print("Initializing Python Bridge...")
                    
                            # java_meta_importer_graal.py
                            import sys
                            import types
                            import importlib.machinery
                            import java
                    
                            JAVA_ROOTS = ("java", "javax", "dev", "io", "com", "net", "com", "it", "de", "sun", "")
                    
                            class JavaModule(types.ModuleType):
                                ""\"Lazy module representing a Java package""\"
                    
                                def __getattr__(self, name):
                                    try:
                                        print(self.__name__)
                                        fullname = f"{self.__name__}.{name}"
                                        if name == "__path__":
                                            return fullname
                                        print(fullname)
                                        return ensure_java(fullname)
                    
                                    except Exception as e:
                                        import traceback
                                        traceback.print_exception(e)
                                        traceback.print_exception(e)
                    
                            def ensure_java(fullname: str):
                                ""\"
                                Resolve fullname to either:
                                  - Java class (via java.type)
                                  - JavaModule (package fallback)
                                ""\"
                                if fullname in sys.modules:
                                    return sys.modules[fullname]
                    
                                longest = ""
                                java_name = None
                                for e in mapping.entrySet():
                                    k = e.getKey()
                                    v = e.getValue()
                                    if fullname.startswith(v + ".") and len(v) > len(longest):
                                        longest = v
                                        java_name = k
                    
                                if java_name is None:
                                    raise ImportError(fullname)
                    
                                try:
                                    import_name = java_name + fullname[len(longest):]
                                    print(f"Importing: {import_name}")
                                    cls = java.type(import_name)  # 1) Try class first
                                    sys.modules[fullname] = cls  # cache class
                                    return cls
                                except Exception:
                                    # 2) Fallback: package
                                    m = JavaModule(fullname)
                                    sys.modules[fullname] = m
                                    return m
                    
                            class JavaMetaFinderLoader:
                                ""\"Meta-path finder & loader for Java packages and classes""\"
                    
                                def find_spec(self, fullname, path=None, target=None):
                                    if fullname.split(".")[0] != "ultreonjv":
                                        return None
                    
                                    print(f"Fullname: {fullname}")
                    
                                    # Try Java class first
                                    try:
                                        import java
                                        java.type(fullname[10:])
                                        # It's a class
                                        return importlib.machinery.ModuleSpec(fullname, self, is_package=False)
                                    except Exception:
                                        # Not a class → assume package
                                        return importlib.machinery.ModuleSpec(fullname, self, is_package=True)
                    
                                def create_module(self, spec):
                                    fullname = spec.name
                                    print(fullname)
                    
                                    longest = ""
                                    java_name = None
                                    for e in mapping.entrySet():
                                        k = e.getKey()
                                        v = e.getValue()
                                        if fullname.startswith(v + ".") and len(v) > len(longest):
                                            longest = v
                                            java_name = k
                    
                                    if java_name is None:
                                        m = JavaModule(fullname)
                                        sys.modules[fullname] = m
                                        return m
                    
                                    try:
                                        import_name = java_name + fullname[:len(longest)]
                                        print(f"Importing: {import_name}")
                                        cls = java.type(import_name)  # 1) Try class first
                                        sys.modules[fullname] = cls  # cache class
                                        return cls
                                    except Exception:
                                        # 2) Fallback: package
                                        m = JavaModule(fullname)
                                        sys.modules[fullname] = m
                                        return m
                    
                                def exec_module(self, module):
                                    # No further execution needed
                                    pass
                    
                            def install_java_meta_importer(prepend=True):
                                ""\"Install the GraalPython Java meta-path importer""\"
                                finder = JavaMetaFinderLoader()
                                if prepend:
                                    sys.meta_path.insert(0, finder)
                                else:
                                    sys.meta_path.append(finder)
                                return finder
                    
                            install_java_meta_importer()
                            print("Initialized!")
                    
                            from ultreonjv.jvm.lang import System
                            System.out.println("LOL")
                    
                        def _init_wrapper(mapping):
                            import traceback
                            try:
                                init_inner(mapping)
                            except Exception as e:
                                traceback.print_exception(e)
                    
                        registry = EventRegistry()
                        return registry.register, registry.fire, _init_wrapper
                    
                    
                    register_event, fire_event, init_bridge = init()
                    del init
                    """);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to create Python bridge init script!", e);
        }

        try {
            @SuppressWarnings("PyPep8Naming") Source py = Source.newBuilder("python", """
                    # noinspection PyUnresolvedReferences
                    def fireEvent(event, *args):
                        import quantum.bridge
                        return quantum.bridge.fire_event(_event, *_event_args)
                    
                    def main(mapping):
                        import sys
                        import os
                    
                        sys.path.append(os.path.join(os.getcwd(), "scripts"))
                    
                        print(f"Current working directory: {os.getcwd()}")
                    
                        print("Quantum Voxel Python Bridge")
                        print(f"Python Module Path: {sys.path}")
                        print(f"Python Modules: {sys.modules}")
                        print(f"Python Version: {sys.version}")
                        print(f"Python Platform: {sys.platform}")
                        print(f"Python Executable: {sys.executable}")
                        print(f"Python Encoding: {sys.getdefaultencoding()}")
                        print(f"Python Platform: {sys.platform}")
                    
                        import quantum.bridge
                        quantum.bridge.init_bridge(mapping)
                    
                        print("Quantum Voxel Python Bridge Initialized!")
                    """, "quantum/bridge/_internal.py").cached(true).build();
            api = this.context.eval(py).as(BridgeAPI.class);
            api.main(PyBindingGenerator.PACKAGE_BINDINGS);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load Python bridge!", e);
        }
    }

    @Override
    public boolean fireEvent(String event, Object... args) {
        try {
            return api.fireEvent(event, args);
        } catch (Throwable e) {
            CommonConstants.LOGGER.error("Failed to fire event '{}'!", event, e);
        }
        return false;
    }

    @Override
    public void execFile(String name) {
        try {
            context.eval(Source.newBuilder("python", """
                    try:
                        import %s.__main__ as m
                    
                        m.main()
                    except Exception as e:
                        import traceback
                        traceback.print_exception(e)
                    """.formatted(name), "<loader at 0x%08x>".formatted(name.hashCode())).cached(false).internal(true).build());
            CommonConstants.LOGGER.info("Executed file '{}'!", name);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to execute file '{}'!", name, e);
        }
    }

    @Override
    public void dispose() {
        if (context != null) {
            context.close();
        }
    }

    public static void main(String[] args) {
        PyBridge bridge = new PyBridge();
        bridge.init();
        bridge.execFile("helloworld");
        bridge.dispose();
    }
}
