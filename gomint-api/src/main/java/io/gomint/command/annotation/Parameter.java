/*
 * Copyright (c) 2020 GoMint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.command.annotation;

import io.gomint.command.ParamValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 * @see io.gomint.command.CommandOverload#param(String, ParamValidator, boolean, String) 
 */
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface Parameter {

    /**
     * @see io.gomint.command.CommandOverload#param(String, ParamValidator, boolean, String)
     */
    String name();

    /**
     * @see ParamValidator
     * @see io.gomint.command.CommandOverload#param(String, ParamValidator, boolean, String)
     */
    Class<? extends ParamValidator> validator();

    /**
     * Constructor parameter(s) for the given {@linkplain #validator() validator}, if any
     */
    String[] arguments() default {};

    /**
     * @see io.gomint.command.CommandOverload#param(String, ParamValidator, boolean, String)
     */
    boolean optional() default false;

    /**
     * @see io.gomint.command.CommandOverload#param(String, ParamValidator, boolean, String)
     */
    String postfix() default "";

}
