/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.permission;

import io.gomint.permission.Group;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PermissionGroup implements Group {

    private final String name;

    private Object2BooleanMap<String> permissions;
    private final Set<PermissionManager> attachedTo = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Create a new permission group. This needs to be configured via
     *
     * @param name    of the group
     */
    PermissionGroup(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Group permission(String permission, boolean value ) {
        if ( this.permissions == null ) {
            this.permissions = new Object2BooleanOpenHashMap<>();
        }

        this.permissions.put( permission, value );
        markDirty();
        return this;
    }

    @Override
    public Group removePermission( String permission ) {
        if ( this.permissions != null ) {
            this.permissions.remove( permission );
        }

        markDirty();
        return this;
    }

    @Override
    public ObjectSet<Object2BooleanMap.Entry<String>> cursor() {
        if ( this.permissions == null ) {
            return null;
        }

        return this.permissions.object2BooleanEntrySet();
    }

    private synchronized void markDirty() {
        for (PermissionManager attached : this.attachedTo) {
            attached.markDirty(this);
        }
    }

    synchronized void addAttached(PermissionManager attached) {
        this.attachedTo.add(attached);
    }

    synchronized void removeAttached(PermissionManager attached) {
        this.attachedTo.remove(attached);
    }

    /**
     * Get a permission setting of this group
     *
     * @param permission which we need the setting for
     * @return true or false
     */
    public Boolean get( String permission ) {
        return this.permissions.get( permission );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionGroup that = (PermissionGroup) o;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public String toString() {
        return "PermissionGroup{" +
            "name='" + this.name + '\'' +
            ", permissions=" + this.permissions +
            '}';
    }

}
