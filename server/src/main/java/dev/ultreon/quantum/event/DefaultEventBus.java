package dev.ultreon.quantum.event;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of the {@link EventBus} interface.
 * This class provides a standard implementation of an event bus.
 */
public class DefaultEventBus implements EventBus {
    private final Map<Class<?>, List<EventHandlerWrapper<?>>> handlers = new ConcurrentHashMap<>();
    private final Map<Object, Set<EventHandlerWrapper<?>>> handlersByOwner = new ConcurrentHashMap<>();
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "EventBus-Async");
        thread.setDaemon(true);
        return thread;
    });
    
    /**
     * Creates a new default event bus.
     */
    public DefaultEventBus() {
        // Nothing to do here
    }
    
    @Override
    public <T extends Event> boolean post(T event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        // Get the class of the event
        Class<?> eventClass = event.getClass();
        
        // Process the event through all applicable handlers
        processEventClass(event, eventClass);
        
        // Return whether the event was canceled
        return event.isCanceled();
    }
    
    @Override
    public <T extends Event> CompletableFuture<Boolean> postAsync(T event) {
        Objects.requireNonNull(event, "Event cannot be null");
        
        return CompletableFuture.supplyAsync(() -> post(event), asyncExecutor);
    }
    
    @Override
    public void register(Object object) {
        Objects.requireNonNull(object, "Object cannot be null");
        
        // Find all methods annotated with @Subscribe
        for (Method method : findSubscribeMethods(object.getClass())) {
            registerMethod(object, method);
        }
    }
    
    @Override
    public void unregister(Object object) {
        Objects.requireNonNull(object, "Object cannot be null");
        
        // Get all handlers owned by this object
        Set<EventHandlerWrapper<?>> ownedHandlers = handlersByOwner.remove(object);
        if (ownedHandlers == null) {
            return;
        }
        
        // Remove all handlers owned by this object
        for (EventHandlerWrapper<?> wrapper : ownedHandlers) {
            Class<?> eventType = getEventType(wrapper);
            if (eventType != null) {
                List<EventHandlerWrapper<?>> typeHandlers = handlers.get(eventType);
                if (typeHandlers != null) {
                    typeHandlers.remove(wrapper);
                    if (typeHandlers.isEmpty()) {
                        handlers.remove(eventType);
                    }
                }
            }
        }
    }
    
    @Override
    public <T extends Event> void register(Class<T> eventType, EventHandler<T> handler) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");
        
        // Create a wrapper for the handler
        EventHandlerWrapper<T> wrapper = new EventHandlerWrapper<>(handler, null);
        
        // Add the wrapper to the handlers map
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(wrapper);
        
        // Sort the handlers by priority
        List<EventHandlerWrapper<?>> typeHandlers = handlers.get(eventType);
        typeHandlers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }
    
    @Override
    public <T extends Event> void unregister(Class<T> eventType, EventHandler<T> handler) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(handler, "Handler cannot be null");
        
        // Get the handlers for this event type
        List<EventHandlerWrapper<?>> typeHandlers = handlers.get(eventType);
        if (typeHandlers == null) {
            return;
        }
        
        // Remove the handler
        typeHandlers.removeIf(wrapper -> wrapper.getHandler().equals(handler));
        
        // Remove the event type if there are no more handlers
        if (typeHandlers.isEmpty()) {
            handlers.remove(eventType);
        }
    }
    
    @Override
    public boolean hasHandlers(Class<? extends Event> eventType) {
        Objects.requireNonNull(eventType, "Event type cannot be null");
        
        return handlers.containsKey(eventType) && !handlers.get(eventType).isEmpty();
    }
    
    @Override
    public void clear() {
        handlers.clear();
        handlersByOwner.clear();
    }
    
    /**
     * Processes an event through all applicable handlers for a specific event class.
     *
     * @param event the event to process
     * @param eventClass the class of the event
     * @param <T> the type of the event
     */
    @SuppressWarnings("unchecked")
    private <T extends Event> void processEventClass(T event, Class<?> eventClass) {
        // Process handlers for this class
        List<EventHandlerWrapper<?>> typeHandlers = handlers.get(eventClass);
        if (typeHandlers != null) {
            for (EventHandlerWrapper<?> wrapper : typeHandlers) {
                ((EventHandlerWrapper<T>) wrapper).handle(event);
                
                // Stop processing if the event is canceled and we're not ignoring canceled events
                if (event.isCanceled() && !wrapper.isIgnoreCanceled()) {
                    return;
                }
            }
        }
        
        // Process handlers for superclasses and interfaces
        for (Class<?> superType : getSuperTypes(eventClass)) {
            processEventClass(event, superType);
            
            // Stop processing if the event is canceled
            if (event.isCanceled()) {
                return;
            }
        }
    }
    
    /**
     * Gets the superclasses and interfaces of a class.
     *
     * @param clazz the class to get the super types of
     * @return the superclasses and interfaces
     */
    private Set<Class<?>> getSuperTypes(Class<?> clazz) {
        Set<Class<?>> superTypes = new HashSet<>();
        
        // Add the superclass
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            superTypes.add(superclass);
        }
        
        // Add the interfaces
        Collections.addAll(superTypes, clazz.getInterfaces());
        
        return superTypes;
    }
    
    /**
     * Finds all methods in a class that are annotated with {@link Subscribe}.
     *
     * @param clazz the class to search
     * @return the annotated methods
     */
    private List<Method> findSubscribeMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        
        // Search all methods in the class and its superclasses
        for (Method method : clazz.getDeclaredMethods()) {
            // Check if the method is annotated with @Subscribe
            if (method.isAnnotationPresent(Subscribe.class)) {
                // Check if the method has exactly one parameter
                if (method.getParameterCount() != 1) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation but has " + method.getParameterCount() + " parameters. Event handler methods must have exactly 1 parameter.");
                }
                
                // Check if the parameter is a subclass of Event
                Class<?> paramType = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(paramType)) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation but its parameter is not a subclass of Event.");
                }
                
                // Add the method to the list
                methods.add(method);
            }
        }
        
        // Search superclass if it's not Object
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            methods.addAll(findSubscribeMethods(superclass));
        }
        
        return methods;
    }
    
    /**
     * Registers a method as an event handler.
     *
     * @param object the object that owns the method
     * @param method the method to register
     */
    @SuppressWarnings("unchecked")
    private void registerMethod(Object object, Method method) {
        // Get the event type from the method parameter
        Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
        
        // Get the priority and ignoreCanceled values from the annotation
        Subscribe annotation = method.getAnnotation(Subscribe.class);
        int priority = annotation.priority();
        boolean ignoreCanceled = annotation.ignoreCanceled();
        
        // Create a wrapper for the method
        EventHandlerWrapper<?> wrapper = new EventHandlerWrapper<>(object, method, priority, ignoreCanceled);
        
        // Add the wrapper to the handlers map
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(wrapper);
        
        // Sort the handlers by priority
        List<EventHandlerWrapper<?>> eventHandlers = handlers.get(eventType);
        eventHandlers.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        // Add the wrapper to the handlersByOwner map
        handlersByOwner.computeIfAbsent(object, k -> new HashSet<>()).add(wrapper);
    }
    
    /**
     * Gets the event type handled by a wrapper.
     *
     * @param wrapper the wrapper to get the event type from
     * @return the event type, or null if it cannot be determined
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Event> getEventType(EventHandlerWrapper<?> wrapper) {
        // If the wrapper has a method, get the event type from the method parameter
        if (wrapper.getMethod() != null) {
            return (Class<? extends Event>) wrapper.getMethod().getParameterTypes()[0];
        }
        
        // Otherwise, try to determine the event type from the handler's generic type
        EventHandler<?> handler = wrapper.getHandler();
        Class<?> handlerClass = handler.getClass();
        
        // Check if the handler class implements EventHandler directly
        for (Type type : handlerClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                if (paramType.getRawType() == EventHandler.class) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length == 1 && typeArgs[0] instanceof Class) {
                        return (Class<? extends Event>) typeArgs[0];
                    }
                }
            }
        }
        
        // If we can't determine the event type, return null
        return null;
    }
}