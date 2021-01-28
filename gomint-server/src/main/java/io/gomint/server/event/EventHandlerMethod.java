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
import io.gomint.event.interfaces.WorldEvent;
import io.gomint.server.maintenance.ReportUploader;
import io.gomint.server.plugin.PluginClassloader;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author BlackyPaw
 * @version 1.0
 */
class EventHandlerMethod implements Comparable<EventHandlerMethod> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerMethod.class);

    private static final AtomicInteger PROXY_COUNT = new AtomicInteger(0);

    private final EventHandler annotation;
    private EventProxy proxy;
    private final IntSet worlds;

    // For toString reference
    private final EventListener instance;

    /**
     * Construct a new data holder for a EventHandler.
     *
     * @param instance   The instance of the EventHandler which should be used to invoke the EventHandler Method
     * @param method     The method which should be invoked when the event arrives
     * @param annotation The annotation which holds additional information about this EventHandler Method
     * @param worlds     The set of string hashCodes of whitelisted worlds or {@code null} for all worlds
     */
    EventHandlerMethod(final EventListener instance, final Method method, final EventHandler annotation, IntSet worlds) {
        this.annotation = annotation;
        this.instance = instance;
        this.worlds = worlds;

        // Build up proxy
        try {
            if (instance.getClass().getClassLoader() instanceof PluginClassloader) {
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
        if (event instanceof WorldEvent && this.worlds != null && !this.worlds.contains(((WorldEvent) event).world().folder().hashCode())) {
            return;
        }
        try {
            this.proxy.call(event);
        } catch (Throwable cause) {
            LOGGER.warn("Event handler has thrown a exception: ", cause);
            ReportUploader.create().exception(cause).upload();
        }
    }

    /**
     * Returns true when this EventHandler accepts cancelled events
     *
     * @return true when it wants to accept events when cancelled, false if not
     */
    boolean ignoreCancelled() {
        return this.annotation.ignoreCancelled();
    }

    @Override
    public int compareTo(EventHandlerMethod o) {
        return (Byte.compare(this.annotation.priority().value(), o.annotation.priority().value()));
    }

}
