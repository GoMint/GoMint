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
import io.gomint.server.maintenance.ReportUploader;
import io.gomint.server.plugin.PluginClassloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * @author BlackyPaw
 * @version 1.0
 */
class EventHandlerMethod implements Comparable<EventHandlerMethod> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerMethod.class);

    private static final AtomicInteger PROXY_COUNT = new AtomicInteger(0);

    private final EventHandler annotation;
    private EventProxy proxy;
    @Nullable
    private final Predicate<Event> predicate;

    // For toString reference
    private final EventListener instance;

    /**
     * Construct a new data holder for a EventHandler.
     *
     * @param instance   The instance of the EventHandler which should be used to invoke the EventHandler Method
     * @param method     The method which should be invoked when the event arrives
     * @param annotation The annotation which holds additional information about this EventHandler Method
     * @param predicate  The predicate to check if the method should recieve certain events
     */
    EventHandlerMethod(final EventListener instance, final Method method, final EventHandler annotation, @Nullable Predicate<Event> predicate) {
        this.annotation = annotation;
        this.instance = instance;
        this.predicate = predicate;

        // Build up proxy
        try {
            if (instance.getClass().getClassLoader() instanceof PluginClassloader || instance.getClass().getClassLoader() == ClassLoader.getSystemClassLoader()) {
                byte[] data = EventCallerClassCreator.createClass(instance, method, PROXY_COUNT.incrementAndGet());

                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(instance.getClass(), lookup);
                Class<? extends EventProxy> proxyClass = (Class<? extends EventProxy>) privateLookup.defineClass(data);

                this.proxy = proxyClass.getDeclaredConstructor().newInstance();
                this.proxy.getClass().getDeclaredField("obj").set(this.proxy, instance);
            } else {
                throw new IllegalArgumentException("Only plugins are allowed to register event listeners");
            }
        } catch (Exception e) {
            LOGGER.error("Could not construct new proxy for " + method.toString(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventHandlerMethod that = (EventHandlerMethod) o;
        return Objects.equals(this.annotation, that.annotation) &&
            Objects.equals(this.proxy, that.proxy) &&
            Objects.equals(this.instance, that.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.annotation, this.proxy, this.instance);
    }

    @Override
    public String toString() {
        return "EventHandlerMethod{" +
            "instance=" + this.instance +
            '}';
    }

    /**
     * Invoke this Event handler.
     *
     * @param event Event which should be handled in this handler
     */
    public void invoke(Event event) {
        if (this.predicate != null && !this.predicate.test(event)) {
            LOGGER.debug("Handler predicate wants to ignore event {}: {}", this, event);
            return;
        }
        try {
            this.proxy.call(event);
        } catch (Exception cause) {
            LOGGER.warn("Event handler has thrown a exception: ", cause);
            ReportUploader.create().exception(cause).upload();
        }
    }

    /**
     * Returns true when this method ignores cancelled events
     *
     * @return true when this listener method wants to ignore events when cancelled, false if it wants to get events
     * regardless of its cancelled state
     */
    boolean ignoreCancelled() {
        return this.annotation.ignoreCancelled();
    }

    @Override
    public int compareTo(EventHandlerMethod o) {
        return (Byte.compare(this.annotation.priority().value(), o.annotation.priority().value()));
    }

}
