package dev.ultreon.xeox;

import dev.ultreon.xeox.api.*;
import dev.ultreon.xeox.impl.EntryPoint;
import dev.ultreon.xeox.impl.IPermissionProvider;

import java.util.*;
import java.util.function.Consumer;

public class XeoxLoaderProvider {
    private static IXeoxLoader instance;

    public static IXeoxLoader get() {
        if (instance == null) {
            throw new IllegalStateException("Not loaded by XeoxLoader! Please use XeoxLoader.create(...) to load the game!");
        }
        return instance;
    }

    public static void setInstance(IXeoxLoader instance) {
        XeoxLoaderProvider.instance = copy(instance);
    }

    private static IXeoxLoader copy(IXeoxLoader instance) {
        return new IXeoxLoader() {
            private final Map<String, IMod> mods = new HashMap<>();

            @Override
            public IMod getMod(String modId) throws SecurityException {
                getMods();

                if (!mods.containsKey(modId)) {
                    return null;
                }

                return mods.get(modId);
            }

            @Override
            public boolean isModLoaded(String modId) {
                return instance.isModLoaded(modId);
            }

            @Override
            public IMod getModByClass(Class<?> clazz) {
                return instance.getModByClass(clazz);
            }

            @Override
            public List<IMod> getMods() {
                for (IMod mod : instance.getMods()) {
                    if (mod == null) continue;
                    IMod newMod = new IMod() {
                        @Override
                        public String modId() {
                            return mod.modId();
                        }

                        @Override
                        public String name() {
                            return mod.name();
                        }

                        @Override
                        public String version() {
                            return mod.version();
                        }

                        @Override
                        public String author() {
                            return mod.author();
                        }

                        @Override
                        public List<String> authors() {
                            return mod.authors();
                        }

                        @Override
                        public String description() {
                            return mod.description();
                        }

                        @Override
                        public String website() {
                            return mod.website();
                        }

                        @Override
                        public String source() {
                            return mod.source();
                        }

                        @Override
                        public String issues() {
                            return mod.issues();
                        }

                        @Override
                        public String license() {
                            return mod.license();
                        }

                        @Override
                        public IFileSystem filesystem() {
                            return mod.filesystem();
                        }

                        @Override
                        public List<String> mixinConfigs() {
                            return mod.mixinConfigs();
                        }
                    };

                    mods.put(mod.modId(), newMod);
                }

                return List.copyOf(mods.values());
            }

            @Override
            public List<String> getModIds() {
                return instance.getModIds();
            }

            @Override
            public void requestPermission(String permission, Runnable runnable) {
                instance.requestPermission(permission, runnable);
            }

            @Override
            public <T> void invokeEntrypoints(String type, Class<T> initClass, Consumer<T> initializer) {
                instance.invokeEntrypoints(type, initClass, initializer);}

            @Override
            public boolean isDevEnvironment() {
                return instance.isDevEnvironment();
            }

            @Override
            public Environment getEnvironment() {
                return instance.getEnvironment();
            }

            @Override
            public IPath getConfigDir() {
                return instance.getConfigDir();
            }

            @Override
            public IPath getGameDir() {
                return instance.getGameDir();
            }

            @Override
            public String getGameVersion() {
                return instance.getGameVersion();
            }

            @Override
            public int choose(String title, String message, String[] options) {
                return instance.choose(title, message, options);
            }
        };
    }
}
