package com.financial.mcp.security.jwt;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator {
    private final JwksProvider jwksProvider;

    public JwtClaims validateToken(String token) throws Exception {
        SignedJWT signedJWT = SignedJWT.parse(token);
        
        String keyId = signedJWT.getHeader().getKeyID();
        RSAPublicKey publicKey = jwksProvider.getPublicKey(keyId);
        
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        if (!signedJWT.verify(verifier)) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        if (signedJWT.getJWTClaimsSet().getExpirationTime().getTime() < System.currentTimeMillis()) {
            throw new IllegalArgumentException("JWT token expired");
        }

        return JwtClaims.fromJWT(signedJWT);
    }
}
