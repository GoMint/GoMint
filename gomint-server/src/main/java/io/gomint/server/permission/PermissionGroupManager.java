/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.permission;

import io.gomint.permission.Group;
import io.gomint.permission.GroupManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PermissionGroupManager implements GroupManager {

    private Object2ObjectMap<String, Group> groupMap = null;

    @Override
    public synchronized Group group(String name) {
        // Check if this is the first group we get/create
        if (this.groupMap == null) {
            this.groupMap = new Object2ObjectOpenHashMap<>();

            PermissionGroup group = new PermissionGroup(name);
            this.groupMap.put(name, group);
            return group;
        }

        Group group = this.groupMap.get(name);
        if (group == null) {
            group = new PermissionGroup(name);
            this.groupMap.put(name, group);
        }

        return group;
    }

    @Override
    public synchronized GroupManager remove(Group group) {
        this.groupMap.remove(group.name());
        return this;
    }

}
