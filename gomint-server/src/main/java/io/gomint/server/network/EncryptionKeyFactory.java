/*
 *  Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 *  This code is licensed under the BSD license found in the
 *  LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.network;

import io.gomint.server.GoMintServer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author geNAZt
 * @version 1.0
 */
@Getter
public class EncryptionKeyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger( EncryptionKeyFactory.class );

    private final String mojangRootKeyBase64 = "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V";
    private PublicKey mojangRootKey;
    private KeyFactory keyFactory;
    private KeyPair keyPair;

    /**
     * Create a new factory which holds / creates a ECDH key factory and a optional keypair
     *
     * @param server The server for which this factory is
     */
    public EncryptionKeyFactory( GoMintServer server ) {
        // Create the key factory
        try {
            this.keyFactory = KeyFactory.getInstance( "EC" );
        } catch ( NoSuchAlgorithmException e ) {
            e.printStackTrace();
            System.err.println( "Could not find ECDH Key Factory - please ensure that you have installed the latest version of BouncyCastle" );
            System.exit( -1 );
        }

        // Unserialize the Mojang root key
        try {
            this.mojangRootKey = this.keyFactory.generatePublic(
                new X509EncodedKeySpec( Base64.getDecoder().decode( EncryptionKeyFactory.this.mojangRootKeyBase64 ) )
            );
        } catch ( InvalidKeySpecException e ) {
            e.printStackTrace();
            System.err.println( "Could not generated public key for trusted Mojang key; please report this error in the GoMint.io discord for further assistance" );
            System.exit( -1 );

        }

        // If needed (for connection encryption) generate a keypair
        if ( server.getServerConfig().getConnection().isEnableEncryption() && !server.getServerConfig().getListener().isUseTCP() ) {
            // Setup KeyPairGenerator:
            KeyPairGenerator generator;
            try {
                generator = KeyPairGenerator.getInstance( "EC" );
                generator.initialize( 384 );
            } catch ( NoSuchAlgorithmException e ) {
                System.err.println( "It seems you have not installed a recent version of BouncyCastle; please ensure that your version supports EC Key-Pair-Generation using the secp384r1 curve" );
                System.exit( -1 );
                return;
            }

            // Generate the keypair:
            this.keyPair = generator.generateKeyPair();

            LOGGER.info( "Server key: {}", Base64.getEncoder().encodeToString( this.keyPair.getPublic().getEncoded() ) );
        }
    }

    /**
     * Return base 64 representation of the mojang public key
     *
     * @return
     */
    public String getMojangRootKeyBase64() {
        return this.mojangRootKeyBase64;
    }

    public PublicKey createPublicKey( String base64 ) {
        try {
            return this.keyFactory.generatePublic( new X509EncodedKeySpec( Base64.getDecoder().decode( base64 ) ) );
        } catch ( InvalidKeySpecException e ) {
            e.printStackTrace();
            return null;
        }
    }

}
