/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.util;

/**
 * @author geNAZt
 * @version 1.0
 */
public class StringUtil {

    /**
     * Check if string a starts with b
     *
     * @param a to check
     * @param b to check against
     * @return true when string a starts with b, false when not
     */
    public static boolean startsWith( String a, String b ) {
        // Get length of both strings
        int al = a.length();
        int bl = b.length();

        // Check if a is even long enough to contain b
        if ( al < bl ) {
            return false;
        }

        // We try a sneak peak first
        while ( --bl >= 0 ) {
            if ( a.charAt( bl ) != b.charAt( bl ) ) {
                return false;
            }
        }

        return true;
    }

    public static boolean endsWith(String subject, char c) {
        if (subject.isEmpty()) {
            return false;
        }
        return subject.charAt(subject.length() - 1) == c;
    }

    /**
     * Get byte encoding from UTF8 string
     *
     * @param input string which should be converted to its byte representation
     * @return byte array of string contents
     */
    public static byte[] getUTF8Bytes( String input ) {
        byte[] output = new byte[input.length() * 3];
        int byteCount = 0;

        // Fast forward all ascii chars
        int fastForward = 0;
        for ( int i = Math.min( input.length(), output.length );
              fastForward < i && input.charAt( fastForward ) < 128;
              output[byteCount++] = (byte) input.charAt( fastForward++ ) ) { }

        for ( int i = fastForward; i < input.length(); i++ ) {
            char c = input.charAt( i );

            if ( c < 128 ) {
                output[byteCount++] = (byte) c;
            } else if ( c < 2048 ) {
                output[byteCount++] = (byte) ( 192 | c >> 6 );
                output[byteCount++] = (byte) ( 128 | c & 63 );
            } else if ( Character.isHighSurrogate( c ) ) {
                if ( i + 1 >= input.length() ) {
                    output[byteCount++] = (char) 63;
                } else {
                    char low = input.charAt( i + 1 );
                    if ( Character.isLowSurrogate( low ) ) {
                        int intChar = Character.toCodePoint( c, low );

                        output[byteCount++] = (byte) ( 240 | intChar >> 18 );
                        output[byteCount++] = (byte) ( 128 | intChar >> 12 & 63 );
                        output[byteCount++] = (byte) ( 128 | intChar >> 6 & 63 );
                        output[byteCount++] = (byte) ( 128 | intChar & 63 );
                        ++i;
                    } else {
                        output[byteCount++] = (char) 63;
                    }
                }
            } else if ( Character.isLowSurrogate( c ) ) {
                output[byteCount++] = (char) 63;
            } else {
                output[byteCount++] = (byte) ( 224 | c >> 12 );
                output[byteCount++] = (byte) ( 128 | c >> 6 & 63 );
                output[byteCount++] = (byte) ( 128 | c & 63 );
            }
        }

        if ( byteCount == output.length ) {
            return output;
        } else {
            byte[] copy = new byte[byteCount];
            System.arraycopy( output, 0, copy, 0, byteCount );
            return copy;
        }
    }

}
