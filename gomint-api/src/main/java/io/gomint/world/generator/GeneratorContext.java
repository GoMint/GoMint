package io.gomint.world.generator;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author geNAZt
 * @version 1.0
 */
public class GeneratorContext {

    private Map<String, Object> contextData = new ConcurrentHashMap<>();

    /**
     * Get stored data for this key
     *
     * @param key which should hold data
     * @return data assigned to the key or null
     */
    public <T> T get( String key ) {
        return (T) this.contextData.get( key );
    }

    /**
     * Check if there is data assigned to the given key
     *
     * @param key which should be checked
     * @return true if there is data assigned, false if not
     */
    public boolean contains( String key ) {
        return this.contextData.containsKey( key );
    }

    /**
     * Put a new data to key assoc into the context
     *
     * @param key   of the data
     * @param value of the data assoc
     */
    public void put( String key, Object value ) {
        this.contextData.put( key, value );
    }

    /**
     * Only for serialize purposes
     *
     * @return copy of the data maps
     */
    public Map<String, Object> asMap() {
        return new HashMap<>( this.contextData );
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString( this.contextData );
    }

}
