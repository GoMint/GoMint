/*
 * Copyright (c) 2020 GoMint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.command;

import io.gomint.command.CommandOutput;
import io.gomint.command.CommandOutputMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class CommandOutputImpl implements CommandOutput {

    private boolean success = true;
    private final Collection<CommandOutputMessage> messages = Collections.synchronizedCollection(new ArrayList<>());
    private final Consumer<CommandOutput> outputConsumer;
    private volatile boolean async = false;
    private volatile boolean done = false;

    public CommandOutputImpl(Consumer<CommandOutput> outputConsumer) {
        this.outputConsumer = outputConsumer;
    }

    @Override
    public CommandOutput fail(String format, Object... params) {
        this.success = false;
        String[] output = this.remap(params);
        this.messages.add(new CommandOutputMessage(false, format, Arrays.asList(output)));
        return this;
    }

    @Override
    public CommandOutput fail(Throwable throwable) {
        this.success = false;
        StackTraceElement[] trace = throwable.getStackTrace();
        String firstLine = trace.length > 0 ? " @ " + trace[0].getClassName() + ":" + trace[0].getLineNumber() : "";
        return fail("%s: %s%s", throwable.getClass().getSimpleName(), throwable.getMessage(), firstLine);
    }

    @Override
    public CommandOutput success(String format, Object... params) {
        String[] output = this.remap(params);
        this.messages.add(new CommandOutputMessage(true, format, Arrays.asList(output)));
        return this;
    }

    @Override
    public boolean success() {
        return this.success;
    }

    @Override
    public Collection<CommandOutputMessage> messages() {
        return Collections.unmodifiableCollection(this.messages);
    }

    @Override
    public CommandOutput markAsync() {
        this.async = true;
        return this;
    }

    @Override
    public synchronized void markFinished() {
        if (!this.done) {
            this.done = true;
            this.outputConsumer.accept(this);
        }
    }

    @Override
    public boolean isAsync() {
        return this.async;
    }

    @Override
    public boolean isFinished() {
        return this.done;
    }

}
