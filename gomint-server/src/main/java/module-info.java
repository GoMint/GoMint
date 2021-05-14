module gomint.server {
    // Java modules
    requires java.desktop;
    requires jdk.unsupported;
    requires java.management;
    requires java.compiler;

    // Gomint modules
    requires gomint.taglib;
    requires gomint.api;
    requires gomint.jraknet;
    requires gomint.crypto;
    requires gomint.leveldb;

    // Logging modules
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires java.scripting;
    requires backtrace.java;
    requires com.google.gson;

    // Netty modules
    requires io.netty.codec;
    requires io.netty.transport;
    requires io.netty.buffer;
    requires io.netty.common;

    // Compile modules
    requires org.objectweb.asm;

    // Automatic modules (libs without module-info)
    requires it.unimi.dsi.fastutil;
    requires jopt.simple;
    requires com.google.common;
    requires org.apache.commons.io;
    requires json.simple;
    requires com.github.oshi;
    requires jsr305;
    requires jline.reader;
    requires jline.terminal;
    requires org.apache.commons.text;
    requires org.slf4j;

    // Export logging stuff for log4j2
    exports net.minecrell.terminalconsole to org.apache.logging.log4j, org.apache.logging.log4j.core;
    exports io.gomint.server.logging to org.apache.logging.log4j, org.apache.logging.log4j.core;

    // Export stuff to spring
    exports io.gomint.server to gomint.test;
    exports io.gomint.server.util to gomint.test;
    exports io.gomint.server.network to gomint.test;

    exports io.gomint.server.network.packet to gomint.test;
    exports io.gomint.server.entity.tileentity to gomint.test;
    exports io.gomint.server.world.block to gomint.test;


    // Open config to gomint api reader
    opens io.gomint.server.config to gomint.api;

    exports io.gomint.server.entity to gomint.test;
    exports io.gomint.server.permission to gomint.test;
    exports io.gomint.server.world to gomint.test;
    exports io.gomint.server.util.collection to gomint.test;
    exports io.gomint.server.util.performance;
    exports io.gomint.server.inventory.item to gomint.test;
    exports io.gomint.server.scheduler to gomint.test;
}
