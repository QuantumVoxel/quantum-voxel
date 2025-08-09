package dev.ultreon.quantum.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for injecting dependencies into objects.
 */
public class DependencyInjector {
    private final DependencyContainer container;
    
    /**
     * Creates a new dependency injector.
     *
     * @param container the dependency container to use
     */
    public DependencyInjector(DependencyContainer container) {
        this.container = container;
    }
    
    /**
     * Injects dependencies into the given object.
     *
     * @param object the object to inject dependencies into
     * @throws DependencyResolutionException if a dependency cannot be resolved
     */
    public void inject(Object object) {
        Class<?> clazz = object.getClass();
        
        // Inject fields
        injectFields(object, clazz);
        
        // Inject methods
        injectMethods(object, clazz);
    }
    
    /**
     * Creates a new instance of the given class with dependencies injected.
     *
     * @param clazz the class to instantiate
     * @param <T> the type of the class
     * @return a new instance of the class with dependencies injected
     * @throws DependencyResolutionException if a dependency cannot be resolved
     */
    public <T> T create(Class<T> clazz) {
        // Check if we have a constructor annotated with @Inject
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> injectConstructor = null;
        
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (injectConstructor != null) {
                    throw new DependencyResolutionException("Multiple constructors annotated with @Inject in class: " + clazz.getName());
                }
                injectConstructor = constructor;
            }
        }
        
        try {
            T instance;
            
            if (injectConstructor != null) {
                // Create instance using the @Inject constructor
                injectConstructor.setAccessible(true);
                
                // Resolve constructor parameters
                Class<?>[] paramTypes = injectConstructor.getParameterTypes();
                Object[] params = new Object[paramTypes.length];
                
                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = container.resolve(paramTypes[i]);
                }
                
                instance = (T) injectConstructor.newInstance(params);
            } else {
                // Create instance using the default constructor
                Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
                defaultConstructor.setAccessible(true);
                instance = defaultConstructor.newInstance();
            }
            
            // Inject fields and methods
            inject(instance);
            
            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new DependencyResolutionException("Failed to create instance of class: " + clazz.getName(), e);
        }
    }
    
    /**
     * Injects dependencies into the fields of the given object.
     *
     * @param object the object to inject dependencies into
     * @param clazz the class of the object
     */
    private void injectFields(Object object, Class<?> clazz) {
        // Inject fields in the current class
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                
                try {
                    // Resolve the dependency
                    Object dependency = container.resolve(field.getType());
                    
                    // Inject the dependency
                    field.set(object, dependency);
                } catch (IllegalAccessException e) {
                    throw new DependencyResolutionException("Failed to inject field: " + field.getName() + " in class: " + clazz.getName(), e);
                }
            }
        }
        
        // Inject fields in the superclass
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            injectFields(object, superclass);
        }
    }
    
    /**
     * Injects dependencies into the methods of the given object.
     *
     * @param object the object to inject dependencies into
     * @param clazz the class of the object
     */
    private void injectMethods(Object object, Class<?> clazz) {
        // Inject methods in the current class
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(Inject.class)) {
                method.setAccessible(true);
                
                try {
                    // Resolve method parameters
                    Class<?>[] paramTypes = method.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    
                    for (int i = 0; i < paramTypes.length; i++) {
                        params[i] = container.resolve(paramTypes[i]);
                    }
                    
                    // Invoke the method with the resolved parameters
                    method.invoke(object, params);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new DependencyResolutionException("Failed to inject method: " + method.getName() + " in class: " + clazz.getName(), e);
                }
            }
        }
        
        // Inject methods in the superclass
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            injectMethods(object, superclass);
        }
    }
}