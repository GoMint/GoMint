/*
 * Copyright (c) 2020 GoMint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.command;

import java.util.Iterator;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 * @see io.gomint.command.validator.BlockPositionValidator
 * @see io.gomint.command.validator.BooleanValidator#BooleanValidator()
 * @see io.gomint.command.validator.CommandValidator#CommandValidator() 
 * @see io.gomint.command.validator.EnumValidator#EnumValidator(List)
 * @see io.gomint.command.validator.FloatValidator
 * @see io.gomint.command.validator.IntegerValidator
 * @see io.gomint.command.validator.StringValidator#StringValidator(String)
 * @see io.gomint.command.validator.TargetValidator
 * @see io.gomint.command.validator.TextValidator
 */
public abstract class ParamValidator<P> {

    private boolean optional;
    private String postfix;

    /**
     * Validates given input
     *
     * @param input         from the command
     * @param commandSender which submitted the command
     * @return non null object of validation on success (string for example) or null when validation failed
     */
    public abstract Object validate(String input, CommandSender<?> commandSender );

    /**
     * Consume all parts this validator needs as input from the command
     *
     * @param data left from the command which can be consumed
     * @return the concatenated data consumed
     */
    public abstract String consume( Iterator<String> data );

    /**
     * Get the type of this param.
     *
     * @return type of param
     */
    public abstract ParamType type();

    /**
     * Get the values when {@link #hasValues()} is true.
     *
     * @return sorted list of values
     */
    public abstract List<String> values();

    /**
     * Does this validator have values which should be sent with the command. This is
     * mostly used by enum params which define a set of values for selection.
     *
     * @return true when there is data to be sent with this parameter, false when not
     */
    public abstract boolean hasValues();

    /**
     * Is this param optional?
     *
     * @return true when its optional, false when not
     */
    public boolean optional() {
        return this.optional;
    }

    /**
     * Set to optional
     *
     * @param optional true when this parameter is optional, false when not
     */
    public P optional(boolean optional ) {
        this.optional = optional;
        return (P) this;
    }

    /**
     * Get the attached postfix for this param validator
     *
     * @return postfix of this validator
     */
    public String postfix() {
        return this.postfix;
    }

    /**
     * Set the postfix for this param validator. Postfixes are currently only supported
     * on int validators, if set to something else it will be ignored.
     *
     * @param postfix which should be used
     */
    public P postfix(String postfix ) {
        this.postfix = postfix;
        return (P) this;
    }

    /**
     * Get a proper help text for the console output
     *
     * @return help text for the console
     */
    public String helpText() {
        return "NO HELP";
    }

}
