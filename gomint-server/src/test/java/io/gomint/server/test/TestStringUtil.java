/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.test;

import io.gomint.server.util.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author geNAZt
 * @version 1.0
 */
public class TestStringUtil {

    @Test
    public void testRandomOutput() {
        for ( int i = 0; i < 500; i++ ) {
            String str = randomString( 32 );
            byte[] a = str.getBytes( StandardCharsets.UTF_8 );
            byte[] b = StringUtil.getUTF8Bytes( str );

            Assertions.assertArrayEquals( a, b );
        }
    }

    public static String randomString( int length ) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder( length );
        while ( sb.length() < length ) {
            char c = (char) ( rand.nextInt() );
            if ( Character.isDefined( c ) ) {
                sb.append( c );
            }
        }

        return sb.toString();
    }

}
