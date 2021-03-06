/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2013 ForgeRock AS.
 */

package org.forgerock.json.jose.builders;

import org.forgerock.json.jose.jwe.EncryptedJwt;
import org.forgerock.json.jose.jwe.JweHeader;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jwt.JwtClaimsSet;

import java.security.Key;

/**
 * An implementation of a JwtBuilder that can build a JWT and encrypt it, resulting in an EncryptedJwt object.
 *
 * @author Phill Cunnington
 * @since 2.0.0
 */
public class EncryptedJwtBuilder extends AbstractJwtBuilder {

    private final Key publicKey;

    /**
     * Constructs a new EncryptedJwtBuilder that will use the given public key to encrypt the JWT.
     *
     * @param publicKey The public key to encrypt the JWT with.
     */
    public EncryptedJwtBuilder(Key publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Gets the JweHeaderBuilder that this JwtBuilder will use to build the JWE's header parameters.
     *
     * @return The JweHeaderBuilder instance.
     */
    @Override
    public JweHeaderBuilder headers() {
        setJwtHeaderBuilder(new JweHeaderBuilder(this));
        return (JweHeaderBuilder) getHeaderBuilder();
    }

    /**
     * Sets the JwtClaimsSet for this JwtBuilder.
     *
     * @param claimsSet {@inheritDoc}
     * @return This EncryptedJwtBuilder.
     */
    @Override
    public EncryptedJwtBuilder claims(JwtClaimsSet claimsSet) {
        return (EncryptedJwtBuilder) super.claims(claimsSet);
    }

    /**
     * Returns a SignedEncryptedJwtBuilder that will build a signed JWT with this builder's encrypted JWT as its
     * payload.
     *
     * @param privateKey The private key to sign the JWT with.
     * @param jwsAlgorithm The JwsAlgorithm to use when signing the JWT.
     * @return The SignedEncryptedJwtBuilder instance.
     */
    public SignedEncryptedJwtBuilder sign(Key privateKey, JwsAlgorithm jwsAlgorithm) {
        return new SignedEncryptedJwtBuilder(this, privateKey, jwsAlgorithm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EncryptedJwt asJwt() {
        JwtHeaderBuilder headerBuilder = getHeaderBuilder();
        JweHeader header;
        if (headerBuilder == null) {
            header = new JweHeader();
        } else {
            header = (JweHeader) getHeaderBuilder().build();
        }
        JwtClaimsSet claimsSet = getClaimsSet();
        if (claimsSet == null) {
            claimsSet = new JwtClaimsSet();
        }
        return new EncryptedJwt(header, claimsSet, publicKey);
    }

    /**
     * Builds the JWE into a <code>String</code> by calling the <tt>build</tt> method on the JWE object.
     * <p>
     * @see org.forgerock.json.jose.jwe.EncryptedJwt#build()
     *
     * @return The base64url encoded UTF-8 parts of the JWE.
     */
    @Override
    public String build() {
        return asJwt().build();
    }
}
