/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the method as event handler method. The method must have one parameter, and it's type must extend
 * {@linkplain Event}.
 * <br><br>
 * When annotated method's class instance is registered as event listener, the given method will be called each time an
 * event of the type of its parameter is fired. To register a class as event listener, it must implement the marker
 * interface {@linkplain EventListener}.
 * <br><br>
 * Event handler methods are additionally required to return {@code void} and not be {@code protected}, {@code private}
 * or {@code static}.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 * @see #priority() 
 * @see #ignoreCancelled() 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

    /**
     * Define the priority of the event handler.
     * <p>
     * Event handlers are called in order of priority:
     * <ol>
     * <li>LOWEST</li>
     * <li>LOW</li>
     * <li>NORMAL (default)</li>
     * <li>HIGH</li>
     * <li>HIGHEST</li>
     * </ol>
     *
     * @return priority order by which event handlers are invoked
     * @see EventPriority
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Set to true if you don't want to get {@linkplain CancellableEvent cancelled Events}
     * <br><br>
     * Defaults to {@code false}.
     *
     * @return true when this listener method wants to ignore events when cancelled, false if it wants to get events
     * regardless of its cancelled state
     */
    boolean ignoreCancelled() default false;

}
