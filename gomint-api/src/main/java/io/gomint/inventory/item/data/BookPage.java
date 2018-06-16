/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.inventory.item.data;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface BookPage {

    /**
     * Get the content of this page
     *
     * @return content of this page
     */
    String getContent();

    /**
     * Set the content of this book page
     *
     * @param content of the page
     */
    void setContent( String content );

}
