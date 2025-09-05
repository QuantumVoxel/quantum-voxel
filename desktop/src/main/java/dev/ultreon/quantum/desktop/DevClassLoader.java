package dev.ultreon.quantum.desktop;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.WeakHashMap;

public class DevClassLoader extends URLClassLoader {
    private static WeakHashMap<String, Class<?>> classes = new WeakHashMap<>();

    public DevClassLoader(ClassLoader parent) {
        super("dev", loadClasspath(), null);
    }

    private static URL[] loadClasspath() {
        String property = System.getProperty("java.class.path");
        String[] paths = property.split(System.getProperty("path.separator"));
        Collection<URL> urls = new ArrayList<>();
        for(String path : paths) {
            try {
                urls.add(new File(path).toURI().toURL());
                System.out.println("Added to classpath: " + path);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return urls.toArray(new URL[0]);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.equals(this.getClass().getName())) {
            return this.getClass();
        }

        if(classes.containsKey(name)) {
            return classes.get(name);
        }

        Class<?> aClass = super.loadClass(name, resolve);
        classes.put(name, aClass);
        return aClass;
    }

    public static Set<String> getLoadedClasses() {
        return classes.keySet();
    }
}
