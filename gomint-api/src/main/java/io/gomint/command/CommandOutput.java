/*
 * Copyright (c) 2020 GoMint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.command;

import java.util.Collection;

/**
 * All output from a command should be collected in the provided {@link CommandOutput} instance so the client can
 * process it in the proper manner. Sending chat messages for the command result is NOT recommended and should NEVER be
 * done.
 * <br><br>
 * When you want to asynchroniously finish command execution and then send messages to the executor, you need to call
 * {@linkplain #markAsync()} synchroniously and when your async processing is done, call {@linkplain #markFinished()}
 * asynchroniously.
 *
 * @author geNAZt
 * @author derklaro
 * @author Janmm14
 * @version 1.0
 * @stability 3
 * @see #markFinished()
 */
public interface CommandOutput {

    /**
     * When the execution of a command failed you can execute this. This adds a result message created with the given
     * string pattern provided by {@code message} and formatted using the given {@code params} parameters. If you want
     * to submit a stack trace of an exception you should use {@link #fail(Throwable)}.
     *
     * @param format The string format of the fail result message.
     *               See <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html">Formatter documentation</a> for more details.
     * @param params The parameters for the string format in the correct order
     * @return this command output instance for chaining
     * @see #fail(Throwable)
     */
    CommandOutput fail(String format, Object... params);

    /**
     * When the execution of a command failed you can execute this. This adds a result message created from the given
     * throwable which is formatted in the following way: {@code ExceptionClassName: ExceptionMessage} followed by
     * nothing if the exception has no stack trace elements or the first trace element formatted like:
     * {@code @ TraceClassName:TraceLineNumber}.
     *
     * @param throwable The exception which occurred during the command execution
     * @return this command output instance for chaining
     * @see #fail(String, Object...)
     * @since 1.0.0-RC4
     */
    CommandOutput fail(Throwable throwable);

    /**
     * When the execution of the command resulted in a success, you can append a message here. This adds a result
     * message created with the given string pattern provided by {@code message} and formatted using the given
     * {@code params} parameters
     *
     * @param format The string format of the success result message.
     *               See <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html">Formatter documentation</a> for more details.
     * @param params The parameters for the string format in the correct order
     * @return this command output instance for chaining
     */
    CommandOutput success(String format, Object... params);

    /**
     * Indicates if the current output is successful or not.
     *
     * @return if the current output is successful or not.
     */
    boolean success();

    /**
     * Contains all messages which are sent to the command sender. The returned collection is not modifiable.
     *
     * Use @linkplain #success(String, Object...)}, {@linkplain #fail(String, Object...)} or
     * {@linkplain #fail(Throwable)} to add messages to the output.
     *
     * @return all messages which are sent to the command sender.
     */
    Collection<CommandOutputMessage> messages();

    default String[] remap(Object[] params) {
        String[] stringParams = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            stringParams[i] = String.valueOf(params[i]);
        }

        return stringParams;
    }

    /**
     * When the command will finish asynchroniously, you need to call this method so the CommandOUtput will wait for
     * {@linkplain #markFinished()} call and only then send the CommandOutput to the executor.
     *
     * @return this command output instance for chaining
     * @see #markFinished()
     * @see #isAsync()
     */
    CommandOutput markAsync();

    /**
     * When {@linkplain #markAsync()} has been called on this CommandOutput, you need to call this method to mark the
     * end of the command execution.
     * <br><br>
     * Do not add messages to this CommandOutput after you called this method. They will be ignored.
     *
     * @see #markAsync()
     * @see #isFinished()
     */
    void markFinished();

    /**
     * Checks whether the command execution will finish asynchroniously
     *
     * @return whether the command execution will finish asynchroniously
     * @see #markAsync()
     */
    boolean isAsync();

    /**
     * Checks whether the command execution has finished
     *
     * @return whether the command execution has finished
     * @see #markFinished()
     */
    boolean isFinished();

}
