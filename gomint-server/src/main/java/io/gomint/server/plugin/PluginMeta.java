/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.plugin;

import io.gomint.plugin.PluginVersion;
import io.gomint.plugin.StartupPriority;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

/**
 * @author BlackyPaw
 * @version 1.0
 */
public class PluginMeta {

    // Plugin basics
    private final File pluginFile;
    private String name;
    private PluginVersion version;
    private StartupPriority priority;
    private Set<String> depends;
    private Set<String> softDepends;
    private String mainClass;

    // Injection stuff
    private Set<String> injectionCommands;

    // Module stuff
    private String moduleName;
    private boolean hasModuleInfo;
    private Set<String> packages;
    private Set<File> moduleDependencies;
    
    // World stuff
    private PluginWorldConfig pluginWorldConfig;
    @Nullable
    private ConcurrentHashMap.KeySetView<String, Boolean> activeLoadedWorlds;

    public PluginMeta(File pluginFile) {
        this.pluginFile = pluginFile;
    }

    public void addPackage(String packageName) {
        if (this.packages == null) {
            this.packages = new HashSet<>();
        }

        this.packages.add(packageName);
    }

    public String name() {
        return this.name;
    }

    public void name(String name) {
        this.name = name;
    }

    public PluginVersion version() {
        return this.version;
    }

    public void version(PluginVersion version) {
        this.version = version;
    }

    public StartupPriority priority() {
        return this.priority;
    }

    public void priority(StartupPriority priority) {
        this.priority = priority;
    }

    public Set<String> depends() {
        return this.depends;
    }

    public void depends(Set<String> depends) {
        this.depends = depends;
    }

    public Set<String> softDepends() {
        return this.softDepends;
    }

    public void softDepends(Set<String> softDepends) {
        this.softDepends = softDepends;
    }

    public String mainClass() {
        return this.mainClass;
    }

    public void mainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public Set<String> injectionCommands() {
        return this.injectionCommands;
    }

    public void injectionCommands(Set<String> injectionCommands) {
        this.injectionCommands = injectionCommands;
    }

    public String moduleName() {
        return this.moduleName;
    }

    public void moduleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean hasModuleInfo() {
        return this.hasModuleInfo;
    }

    public void hasModuleInfo(boolean hasModuleInfo) {
        this.hasModuleInfo = hasModuleInfo;
    }

    public Set<String> packages() {
        return this.packages;
    }

    public void packages(Set<String> packages) {
        this.packages = packages;
    }

    public Set<File> moduleDependencies() {
        return this.moduleDependencies;
    }

    public void moduleDependencies(Set<File> moduleDependencies) {
        this.moduleDependencies = moduleDependencies;
    }

    public File pluginFile() {
        return this.pluginFile;
    }

    public PluginWorldConfig pluginWorldConfig() {
        return this.pluginWorldConfig;
    }

    public void pluginWorldConfig(PluginWorldConfig pluginWorldConfig) {
        this.pluginWorldConfig = pluginWorldConfig;
    }

    @Nullable
    public ConcurrentHashMap.KeySetView<String, Boolean> activeLoadedWorlds() {
        return this.activeLoadedWorlds;
    }

    public void activeLoadedWorlds(ConcurrentHashMap.KeySetView<String, Boolean> activeWorlds) {
        this.activeLoadedWorlds = activeWorlds;
    }

}
