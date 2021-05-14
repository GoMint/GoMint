/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.command.cli;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Permission;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
@Name("memorydump")
@Description("Dump offheap memory stats")
@Permission("gomint.cli.command.memorydump")
public class MemoryDumpCommand extends Command {

    @Override
    public void execute(CommandSender<?> commandSender, String alias, Map<String, Object> arguments, CommandOutput output) {
        System.out.println("==========================================");
        System.out.println("BufferPoolMXBean");
        System.out.println("------------------------------------------");
        for (BufferPoolMXBean mbean : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            System.out.printf("%s: count %d, capacity %d, used %d%n",
                mbean.getName(),
                mbean.getCount(),
                mbean.getTotalCapacity(),
                mbean.getMemoryUsed());
        }
    }

}
