package com.financial.mcp.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * 개발 환경용 JWT 토큰 생성기
 * 프로덕션에서는 사용하지 마세요!
 */
@Component
@ConditionalOnProperty(name = "mcp.security.dev-mode", havingValue = "true")
@RequiredArgsConstructor
public class DevJwtTokenProvider {
    private final RSAPrivateKey privateKey;

    public DevJwtTokenProvider() throws NoSuchAlgorithmException {
        // 개발용 RSA 키 쌍 생성
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    /**
     * 개발용 JWT 토큰 생성
     */
    public String generateToken(String userId, String dept, String... roles) throws JOSEException {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000); // 24시간

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer("http://localhost:8080")
                .issueTime(now)
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("roles", Arrays.asList(roles))
                .claim("dept", dept)
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claimsSet
        );

        signedJWT.sign(new RSASSASigner(privateKey));
        return signedJWT.serialize();
    }

    /**
     * 기본 테스트 토큰 생성
     */
    public String generateTestToken() throws JOSEException {
        return generateToken("user@company.com", "RISK", "ADMIN", "USER");
    }
}
