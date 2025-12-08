package com.financial.mcp.security.jwt;

import java.security.interfaces.RSAPublicKey;

public interface JwksProvider {
    RSAPublicKey getPublicKey(String keyId) throws Exception;
}
