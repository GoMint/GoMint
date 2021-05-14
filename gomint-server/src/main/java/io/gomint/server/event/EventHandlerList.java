/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.event;

import com.google.common.base.Preconditions;
import io.gomint.event.CancellableEvent;
import io.gomint.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This list sorts and triggers all EventHandlerMethods which have been registered for a event.
 *
 * @author BlackyPaw
 * @version 1.0
 */
class EventHandlerList {

    private static final Logger LOGGER = LoggerFactory.getLogger( EventHandlerList.class );

    // If the handler list is dirty we need to sort it by the event handler priorities:
    private boolean dirty;
    private Map<String, EventHandlerMethod> handlers = new HashMap<>();
    private EventHandlerMethod[] sortedHandlerList = new EventHandlerMethod[0];
    private int insertIndex = 0;

    /**
     * Construct a new EventHandlerList
     */
    EventHandlerList() {

    }

    /**
     * Add a new handler. This marks the whole list dirty and it gets sorted when the next event arrives.
     *
     * @param key     The key which should be used to store this handler
     * @param handler The handler which should be added
     */
    void addHandler( String key, EventHandlerMethod handler ) {
        Preconditions.checkArgument( !this.handlers.containsKey( key ), "EventHandler can't be registered twice. Other instance: " + this.handlers.get( key ) );

        this.handlers.put( key, handler );

        LOGGER.debug( "Registering handler {} -> {}", key, handler );

        // Array copy to bigger array
        EventHandlerMethod[] newArray = new EventHandlerMethod[this.insertIndex + 1];
        if ( this.sortedHandlerList.length > 0 ) {
            System.arraycopy( this.sortedHandlerList, 0, newArray, 0, this.sortedHandlerList.length );
        }

        newArray[this.insertIndex++] = handler;
        this.sortedHandlerList = newArray;
        this.dirty = true;
    }

    /**
     * Remove a handler from the list. This does not dirty the list.
     *
     * @param key The key which has been used to store this handler
     */
    void removeHandler( String key ) {
        EventHandlerMethod method = this.handlers.remove( key );
        if ( method != null ) {
            // Search for the handler method
            int removed = 0;
            for ( int i = 0; i < this.sortedHandlerList.length; i++ ) {
                EventHandlerMethod method1 = this.sortedHandlerList[i];
                if ( method.equals( method1 ) ) {
                    this.sortedHandlerList[i] = null;
                    removed++;
                }
            }

            // Merge array to remove null
            if ( removed > 0 ) {
                EventHandlerMethod[] newArr = new EventHandlerMethod[this.sortedHandlerList.length - removed];
                int index = 0;
                for ( EventHandlerMethod eventHandlerMethod : this.sortedHandlerList ) {
                    if ( eventHandlerMethod != null ) {
                        newArr[index++] = eventHandlerMethod;
                    }
                }

                this.sortedHandlerList = newArr;
                this.insertIndex = this.sortedHandlerList.length;

                this.dirty = true;
            }
        }
    }

    /**
     * Iterate over all EventHandler Methods and sort them if needed. This also controls when a Event got cancelled
     * that it does not get fired for Handlers which does not want it.
     *
     * @param event The event which gets passed to all handlers
     */
    void triggerEvent( Event event ) {
        if ( this.dirty ) {
            Arrays.sort( this.sortedHandlerList );
            this.dirty = false;
        }

        LOGGER.debug( "Starting to handle event: {}", event );

        if ( event instanceof CancellableEvent ) {
            CancellableEvent<?> cancellableEvent = (CancellableEvent<?>) event;
            for ( EventHandlerMethod handler : this.sortedHandlerList ) {
                LOGGER.debug( "Checking handler {} with event {}", handler, cancellableEvent );
                if ( cancellableEvent.cancelled() && handler.ignoreCancelled() ) {
                    LOGGER.debug( "Handler wants to ignore cancelled events {}: {}", handler, cancellableEvent );
                    continue;
                }

                handler.invoke( event );
                LOGGER.debug( "Event after handler {}: {}", handler, cancellableEvent );
            }
        } else {
            for ( EventHandlerMethod handler : this.sortedHandlerList ) {
                LOGGER.debug( "Checking handler {} with event {}", handler, event );
                handler.invoke( event );
                LOGGER.debug( "Event after handler {}: {}", handler, event );
            }
        }
    }

}
