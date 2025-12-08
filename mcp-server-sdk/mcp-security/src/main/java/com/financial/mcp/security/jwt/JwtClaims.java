package com.financial.mcp.security.jwt;

import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtClaims {
    private String sub;
    private String iss;
    private List<String> roles;
    private String dept;
    private long exp;

    public static JwtClaims fromJWT(SignedJWT jwt) throws ParseException {
        var claims = jwt.getJWTClaimsSet();
        return JwtClaims.builder()
                .sub(claims.getSubject())
                .iss(claims.getIssuer())
                .roles((List<String>) claims.getClaim("roles"))
                .dept((String) claims.getClaim("dept"))
                .exp(claims.getExpirationTime().getTime())
                .build();
    }
}
