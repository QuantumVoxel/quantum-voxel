package dev.ultreon.quantum.di;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A simple dependency injection container.
 * This class provides a way to register and resolve dependencies.
 */
public class DependencyContainer {
    private static final DependencyContainer INSTANCE = new DependencyContainer();
    
    private final Map<Class<?>, Supplier<?>> singletons = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final DependencyInjector injector = new DependencyInjector(this);
    
    /**
     * Gets the singleton instance of the dependency container.
     *
     * @return the dependency container
     */
    public static DependencyContainer getInstance() {
        return INSTANCE;
    }
    
    /**
     * Registers a singleton dependency.
     *
     * @param type the type of the dependency
     * @param supplier the supplier that creates the dependency
     * @param <T> the type of the dependency
     */
    public <T> void registerSingleton(Class<T> type, Supplier<T> supplier) {
        singletons.put(type, supplier);
    }
    
    /**
     * Registers an instance of a dependency.
     *
     * @param type the type of the dependency
     * @param instance the instance of the dependency
     * @param <T> the type of the dependency
     */
    public <T> void registerInstance(Class<T> type, T instance) {
        instances.put(type, instance);
    }
    
    /**
     * Resolves a dependency.
     *
     * @param type the type of the dependency
     * @param <T> the type of the dependency
     * @return the dependency
     * @throws DependencyResolutionException if the dependency cannot be resolved
     */
    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> type) {
        // Check if we have an instance
        if (instances.containsKey(type)) {
            return (T) instances.get(type);
        }
        
        // Check if we have a singleton
        if (singletons.containsKey(type)) {
            T instance = (T) singletons.get(type).get();
            instances.put(type, instance);
            return instance;
        }

        T t = injector.create(type);
        if (t != null) {
            instances.put(type, t);
            return t;
        }
        throw new DependencyResolutionException("No dependency registered for type: " + type.getName());
    }
    
    /**
     * Checks if a dependency is registered.
     *
     * @param type the type of the dependency
     * @return true if the dependency is registered, false otherwise
     */
    public boolean isRegistered(Class<?> type) {
        return instances.containsKey(type) || singletons.containsKey(type);
    }
    
    /**
     * Clears all registered dependencies.
     */
    public void clear() {
        singletons.clear();
        instances.clear();
    }
}