/*
 *  Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 *  This code is licensed under the BSD license found in the
 *  LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.jwt;

import io.gomint.server.network.EncryptionKeyFactory;
import lombok.Getter;

import java.security.Key;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.util.*;

/**
 * @author BlackyPaw
 * @version 1.0
 */
@Getter
public class MojangChainValidator {

    private List<JwtToken> chain;

    private String username;
    private UUID uuid;
    private String xboxId = "";
    private ECPublicKey clientPublicKey;

    private Map<String, Key> trustedKeys;
    private EncryptionKeyFactory encryptionKeyFactory;

    /**
     * Create a new Chain Validator for Mojang encrypted JWT Payloads
     *
     * @param encryptionKeyFactory Factory which provides encryption utils
     */
    public MojangChainValidator( EncryptionKeyFactory encryptionKeyFactory ) {
        this.encryptionKeyFactory = encryptionKeyFactory;
        this.chain = new ArrayList<>();
    }

    /**
     * Add a token to the chain
     *
     * @param token The raw token data
     */
    public void addToken( JwtToken token ) {
        this.chain.add( token );
    }

    /**
     * Validate if the chain is complete
     *
     * @return true when valid and complete, false when not
     */
    public boolean validate() {
        this.trustedKeys = new HashMap<>();
        this.trustedKeys.put( this.encryptionKeyFactory.getMojangRootKeyBase64(), this.encryptionKeyFactory.getMojangRootKey() );

        List<JwtToken> unverified = new ArrayList<>( this.chain );
        boolean hasExtraData = false;

        try {
            while ( !unverified.isEmpty() ) {
                // Take advantage of the 'x5u' header field Mojang sends us with their chains
                // This will tell us using which key a specific token has been signed and we can thus
                // easily find the token in the chain which comes circularly dependant on its own claim.
                // We hook into that dependency using what seems to be a constant non-changing public key
                // of Mojang:

                String x5u = null;
                JwtToken nextToken = null;

                for ( JwtToken token : unverified ) {
                    x5u = token.getHeader().getProperty( String.class, "x5u" );
                    if ( x5u == null ) {
                        // This token comes unexpectedly - might be a faker:
                        return false;
                    }

                    if ( this.trustedKeys.containsKey( x5u ) ) {
                        nextToken = token;
                        break;
                    }
                }

                if ( nextToken == null ) {
                    // No further tokens which could be verified -> yet there are still tokens in the unverified set:
                    return false;
                }

                try {
                    // We always use ES384 independently of what the client sent us in order to prevent algorithm exchange
                    // attacks as described here: https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/
                    if ( !nextToken.validateSignature( JwtAlgorithm.ES384, this.trustedKeys.get( x5u ) ) ) {
                        // Seems to be a forged token:
                        return false;
                    }
                } catch ( JwtSignatureException e ) {
                    e.printStackTrace();
                    return false;
                }

                unverified.remove( nextToken );

                // This token is valid -> add its public key to the set of trusted keys if it specifies any and has its
                // certificateAuthority flag set:
                Boolean certificateAuthority = nextToken.getClaim( Boolean.class, "certificateAuthority" );
                String identityPublicKeyBase64 = nextToken.getClaim( String.class, "identityPublicKey" );

                if ( ( certificateAuthority != null && !certificateAuthority ) || identityPublicKeyBase64 == null ) {
                    // No public key to trust here:
                    continue;
                }

                // This certificate authority wants us to add its public key:
                PublicKey key = this.encryptionKeyFactory.createPublicKey( identityPublicKeyBase64 );
                if ( key != null ) {
                    this.trustedKeys.put( identityPublicKeyBase64, key );
                }

                // Check, if this token provides us with client details:
                Map<String, Object> extraData = nextToken.getClaim( Map.class, "extraData" );
                if ( extraData != null && !hasExtraData ) {
                    hasExtraData = true;
                    this.clientPublicKey = (ECPublicKey) key;
                    this.loadClientInformation( extraData, false );
                }
            }

            return true;
        } finally {
            if ( !hasExtraData ) {
                this.detectClientInformationUnsafe();
            }
        }
    }

    private void detectClientInformationUnsafe() {
        for ( JwtToken token : this.chain ) {
            String identityPublicKeyBase64 = token.getClaim( String.class, "identityPublicKey" );
            if ( identityPublicKeyBase64 == null ) {
                continue;
            }

            Map<String, Object> extraData = token.getClaim( Map.class, "extraData" );
            if ( extraData != null ) {
                PublicKey key = this.encryptionKeyFactory.createPublicKey( identityPublicKeyBase64 );
                if ( key == null ) {
                    continue;
                }

                this.clientPublicKey = (ECPublicKey) key;
                this.loadClientInformation( extraData, true );
                return;
            }
        }
    }

    private void loadClientInformation( Map<String, Object> extraData, boolean unsafe ) {
        Object usernameRaw = extraData.get( "displayName" );
        Object uuidRaw = extraData.get( "identity" );
        Object xuidRaw = extraData.get( "XUID" );

        if ( usernameRaw != null && usernameRaw instanceof String ) {
            this.username = (String) usernameRaw;
        }

        if ( uuidRaw != null && uuidRaw instanceof String ) {
            try {
                this.uuid = UUID.fromString( (String) uuidRaw );
            } catch ( IllegalArgumentException ignored ) {
                // ._.
            }
        }

        if ( !unsafe && xuidRaw != null && xuidRaw instanceof String ) {
            this.xboxId = (String) xuidRaw;
        }
    }

}
