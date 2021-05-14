/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.event;

import io.gomint.event.Event;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * @author BlackyPaw
 * @author geNAZt
 * @author Janmm14
 * @version 2.0
 */
public class EventManager {

    private static final int DISALLOWED_MODIFIERS = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC;

    // All event handlers that have been registered
    private final Int2ObjectMap<EventHandlerList> eventHandlers = new Int2ObjectOpenHashMap<>();

    /**
     * Triggers the event. It will be dispatched to all interested listeners immediately.
     *
     * @param event The event to be triggered
     */
    public void triggerEvent(Event event) {
        // Assume we already acquired a readLock:
        Class<? extends Event> eventClass = event.getClass();
        int eventHash = eventClass.getName().hashCode();

        EventHandlerList eventHandlerList = this.eventHandlers.get(eventHash);
        if (eventHandlerList == null) {
            return;
        }

        eventHandlerList.triggerEvent(event);
    }

    /**
     * Registers all event handler methods found on the specified listener without a filtering predicate.
     *
     * @param listener The listener to register
     * @param <T>      The generic type of the listener
     */
    public <T extends EventListener> void registerListener(T listener) {
        registerListener(listener, null);
    }

    /**
     * Registers all event handler methods found on the specified listener.
     *
     * @param listener  The listener to register
     * @param <T>       The generic type of the listener
     * @param predicate The predicate to check if methods of the listener should recieve certain events
     */
    public <T extends EventListener> void registerListener(T listener, @Nullable Predicate<Event> predicate) {
        Class<? extends EventListener> listenerClass = listener.getClass();
        for (Method method : listenerClass.getDeclaredMethods()) {
            if (isListenerMethod(method)) {
                method.setAccessible(true);
                this.registerListener0(listener, method, predicate);
            }
        }
    }

    /**
     * Unregister a listener. This listener does not get any more events after this
     *
     * @param listener The listener to register
     * @param <T>      The generic type of the listener
     */
    public <T extends EventListener> void unregisterListener(T listener) {
        Class<? extends EventListener> listenerClass = listener.getClass();
        for (Method method : listenerClass.getDeclaredMethods()) {
            if (isListenerMethod(method)) {
                this.unregisterListener0(listener, method);
            }
        }
    }

    /**
     * Determines if the given method is an event listener method.
     * <br><br>
     * An event listener method must be annotated with {@linkplain EventHandler @EventHandler}, have one parameter
     * with a class extending {@linkplain Event}, must return void and may not be {@code private}, {@code protected} or
     * {@code static}.
     *
     * @param method the method to check
     * @return whether the given method is a listener method
     */
    private boolean isListenerMethod(Method method) {
        return method.isAnnotationPresent(EventHandler.class) &&
            method.getParameterCount() == 1 &&
            Event.class.isAssignableFrom(method.getParameterTypes()[0]) &&
            (method.getModifiers() & DISALLOWED_MODIFIERS) == 0 &&
            method.getReturnType() == Void.TYPE;
    }

    private <T extends EventListener> void registerListener0(T listener, Method listenerMethod, @Nullable Predicate<Event> predicate) {
        int eventHash = listenerMethod.getParameterTypes()[0].getName().hashCode();
        EventHandler annotation = listenerMethod.getAnnotation(EventHandler.class);
        EventHandlerList eventHandlerList = this.eventHandlers.get(eventHash);
        if (eventHandlerList == null) {
            eventHandlerList = new EventHandlerList();
            this.eventHandlers.put(eventHash, eventHandlerList);
        }

        eventHandlerList.addHandler(listener.getClass().getName() + "#" + listenerMethod.getName() + "_" + eventHash + "_" + listener.hashCode(),
            new EventHandlerMethod(listener, listenerMethod, annotation, predicate));
    }

    private <T extends EventListener> void unregisterListener0(T listener, Method listenerMethod) {
        int eventHash = listenerMethod.getParameterTypes()[0].getName().hashCode();
        EventHandlerList eventHandlerList = this.eventHandlers.get(eventHash);
        if (eventHandlerList == null) {
            return;
        }

        eventHandlerList.removeHandler(listener.getClass().getName() + "#" + listenerMethod.getName() + "_" + eventHash + "_" + listener.hashCode());
    }

}
