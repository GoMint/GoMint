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
 * @author geNAZt
 * @version 1.0
 * @stability 3
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
     * <li>NORMAL</li>
     * <li>HIGH</li>
     * <li>HIGHEST</li>
     * </ol>
     *
     * @return priority order by which event handlers are invoked
     */
    EventPriority priority() default EventPriority.NORMAL;

    /**
     * Define to true if you don't want to get cancelled Events
     *
     * @return true when this listener ignores the cancelled state of cancelled events and wants to handle them,
     * false otherwise
     */
    boolean ignoreCancelled() default false;

}
